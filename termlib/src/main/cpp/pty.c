#include <jni.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <fcntl.h>
#include <termios.h>
#include <sys/select.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <signal.h>
#include <errno.h>
#include <pthread.h>
#include <android/log.h>
#include <linux/utsname.h>

#define TAG "NeoTermux-PTY"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)
#define PTY_BUFFER_SIZE 65536

typedef struct {
    int master_fd;
    int slave_fd;
    pid_t child_pid;
    int running;
    pthread_t read_thread;
    pthread_mutex_t lock;
    void (*output_callback)(const char *, int);
} PtySession;

static PtySession *g_session = NULL;

static void *pty_read_thread(void *arg) {
    PtySession *session = (PtySession *)arg;
    char buffer[PTY_BUFFER_SIZE];
    fd_set fds;
    struct timeval tv;

    while (session->running) {
        FD_ZERO(&fds);
        FD_SET(session->master_fd, &fds);
        tv.tv_sec = 0;
        tv.tv_usec = 100000;

        int ret = select(session->master_fd + 1, &fds, NULL, NULL, &tv);
        if (ret < 0) {
            if (errno == EINTR) continue;
            LOGE("select failed: %s", strerror(errno));
            break;
        }
        if (ret > 0 && FD_ISSET(session->master_fd, &fds)) {
            int n = read(session->master_fd, buffer, sizeof(buffer) - 1);
            if (n > 0) {
                buffer[n] = '\0';
                if (session->output_callback) {
                    session->output_callback(buffer, n);
                }
            } else if (n == 0) {
                LOGI("PTY read returned 0, child closed");
                break;
            } else {
                if (errno != EIO) {
                    LOGE("read from PTY failed: %s", strerror(errno));
                }
                break;
            }
        }
    }
    session->running = 0;
    return NULL;
}

int pty_open(const char *shell_path, char *const argv[], const char *term_env,
             void (*callback)(const char *, int)) {
    if (g_session) return -1;

    g_session = (PtySession *)calloc(1, sizeof(PtySession));
    if (!g_session) return -1;

    pthread_mutex_init(&g_session->lock, NULL);
    g_session->output_callback = callback;

    int master = posix_openpt(O_RDWR | O_NOCTTY);
    if (master < 0) {
        LOGE("posix_openpt failed: %s", strerror(errno));
        free(g_session);
        g_session = NULL;
        return -1;
    }

    if (grantpt(master) < 0 || unlockpt(master) < 0) {
        LOGE("grantpt/unlockpt failed: %s", strerror(errno));
        close(master);
        free(g_session);
        g_session = NULL;
        return -1;
    }

    char *slave_name = ptsname(master);
    if (!slave_name) {
        LOGE("ptsname failed");
        close(master);
        free(g_session);
        g_session = NULL;
        return -1;
    }

    g_session->master_fd = master;

    pid_t pid = fork();
    if (pid == 0) {
        setsid();
        int slave_fd = open(slave_name, O_RDWR);
        if (slave_fd < 0) {
            LOGE("child: open slave failed: %s", strerror(errno));
            _exit(1);
        }
        dup2(slave_fd, STDIN_FILENO);
        dup2(slave_fd, STDOUT_FILENO);
        dup2(slave_fd, STDERR_FILENO);
        if (slave_fd > STDERR_FILENO) close(slave_fd);
        close(master);
        struct termios tios;
        tcgetattr(STDIN_FILENO, &tios);
        tios.c_lflag |= ECHO;
        tios.c_oflag |= ONLCR | OPOST;
        tcsetattr(STDIN_FILENO, TCSANOW, &tios);
        if (term_env) setenv("TERM", term_env, 1);
        setenv("HOME", "/data/data/com.neotermux.app/files/home", 1);
        setenv("USER", "u0_a299", 1);
        setenv("SHELL", shell_path, 1);
        setenv("PATH", "/data/data/com.neotermux.app/files/usr/bin:/system/bin", 1);
        setenv("LD_PRELOAD", "", 1);
        execvp(shell_path, (char *const *)argv);
        LOGE("execvp failed: %s", strerror(errno));
        _exit(127);
    } else if (pid < 0) {
        LOGE("fork failed: %s", strerror(errno));
        close(master);
        free(g_session);
        g_session = NULL;
        return -1;
    }

    g_session->child_pid = pid;
    g_session->running = 1;
    pthread_create(&g_session->read_thread, NULL, pty_read_thread, g_session);

    LOGI("PTY opened: master_fd=%d, child_pid=%d, shell=%s", master, pid, shell_path);
    return 0;
}

int pty_write(const char *data, int len) {
    if (!g_session || !g_session->running) return -1;
    pthread_mutex_lock(&g_session->lock);
    int written = 0;
    while (written < len) {
        int n = write(g_session->master_fd, data + written, len - written);
        if (n <= 0) {
            if (errno == EINTR) continue;
            LOGE("write to PTY failed: %s", strerror(errno));
            pthread_mutex_unlock(&g_session->lock);
            return -1;
        }
        written += n;
    }
    pthread_mutex_unlock(&g_session->lock);
    return written;
}

int pty_set_window_size(int rows, int cols) {
    if (!g_session) return -1;
    struct winsize ws;
    ws.ws_row = rows;
    ws.ws_col = cols;
    ws.ws_xpixel = 0;
    ws.ws_ypixel = 0;
    if (ioctl(g_session->master_fd, TIOCSWINSZ, &ws) < 0) {
        LOGE("ioctl TIOCSWINSZ failed: %s", strerror(errno));
        return -1;
    }
    return 0;
}

void pty_close() {
    if (!g_session) return;
    g_session->running = 0;
    if (g_session->child_pid > 0) {
        kill(g_session->child_pid, SIGTERM);
        usleep(100000);
        kill(g_session->child_pid, SIGKILL);
        waitpid(g_session->child_pid, NULL, WNOHANG);
    }
    if (g_session->master_fd >= 0) {
        close(g_session->master_fd);
    }
    pthread_join(g_session->read_thread, NULL);
    pthread_mutex_destroy(&g_session->lock);
    free(g_session);
    g_session = NULL;
    LOGI("PTY closed");
}

int pty_is_running() {
    return g_session && g_session->running;
}
