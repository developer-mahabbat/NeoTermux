#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <termios.h>
#include <signal.h>
#include <sys/ioctl.h>
#include <sys/select.h>
#include <errno.h>
#include <pthread.h>
#include <android/log.h>

#define TAG "NeoTermux-Native"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

#define TERMINAL_BUFFER_SIZE 65536
#define SCROLLBACK_LINES 10000

typedef struct {
    int rows;
    int cols;
    int cursor_x;
    int cursor_y;
    int scroll_pos;
    char **buffer;
    char **scrollback;
    int scrollback_count;
    int buffer_size;
    int modified;
    pthread_mutex_t lock;
} TerminalState;

static TerminalState *g_state = NULL;

int terminal_init(int rows, int cols) {
    if (g_state) return -1;
    g_state = (TerminalState *)calloc(1, sizeof(TerminalState));
    if (!g_state) return -1;
    g_state->rows = rows;
    g_state->cols = cols;
    g_state->buffer_size = rows * cols;
    g_state->buffer = (char **)calloc(rows, sizeof(char *));
    for (int i = 0; i < rows; i++) {
        g_state->buffer[i] = (char *)calloc(cols + 1, 1);
    }
    g_state->scrollback = (char **)calloc(SCROLLBACK_LINES, sizeof(char *));
    g_state->scrollback_count = 0;
    pthread_mutex_init(&g_state->lock, NULL);
    LOGI("Terminal initialized: %dx%d", rows, cols);
    return 0;
}

int terminal_write(const char *data, int len) {
    if (!g_state || !data) return -1;
    pthread_mutex_lock(&g_state->lock);
    for (int i = 0; i < len && i < g_state->buffer_size - 1; i++) {
        if (data[i] == '\n') {
            g_state->cursor_y++;
            g_state->cursor_x = 0;
            if (g_state->cursor_y >= g_state->rows) {
                char *old_line = g_state->buffer[0];
                if (g_state->scrollback_count < SCROLLBACK_LINES) {
                    g_state->scrollback[g_state->scrollback_count++] = old_line;
                } else {
                    free(old_line);
                }
                memmove(&g_state->buffer[0], &g_state->buffer[1],
                        (g_state->rows - 1) * sizeof(char *));
                g_state->buffer[g_state->rows - 1] = (char *)calloc(g_state->cols + 1, 1);
                g_state->cursor_y = g_state->rows - 1;
            }
        } else if (data[i] == '\r') {
            g_state->cursor_x = 0;
        } else if (data[i] >= 32 && data[i] < 127) {
            if (g_state->cursor_x < g_state->cols) {
                g_state->buffer[g_state->cursor_y][g_state->cursor_x++] = data[i];
            }
        }
    }
    g_state->modified = 1;
    pthread_mutex_unlock(&g_state->lock);
    return len;
}

int terminal_resize(int rows, int cols) {
    if (!g_state) return -1;
    pthread_mutex_lock(&g_state->lock);
    char **new_buffer = (char **)calloc(rows, sizeof(char *));
    int min_rows = rows < g_state->rows ? rows : g_state->rows;
    int min_cols = cols < g_state->cols ? cols : g_state->cols;
    for (int i = 0; i < rows; i++) {
        new_buffer[i] = (char *)calloc(cols + 1, 1);
        if (i < min_rows) {
            strncpy(new_buffer[i], g_state->buffer[i], min_cols);
        }
    }
    for (int i = 0; i < g_state->rows; i++) {
        free(g_state->buffer[i]);
    }
    free(g_state->buffer);
    g_state->buffer = new_buffer;
    g_state->rows = rows;
    g_state->cols = cols;
    if (g_state->cursor_x >= cols) g_state->cursor_x = cols - 1;
    if (g_state->cursor_y >= rows) g_state->cursor_y = rows - 1;
    g_state->modified = 1;
    pthread_mutex_unlock(&g_state->lock);
    LOGI("Terminal resized: %dx%d", rows, cols);
    return 0;
}

char *terminal_get_buffer(int *out_len) {
    if (!g_state) return NULL;
    pthread_mutex_lock(&g_state->lock);
    int total = 0;
    for (int i = 0; i < g_state->rows; i++) {
        total += strlen(g_state->buffer[i]) + 1;
    }
    char *result = (char *)calloc(total + 1, 1);
    for (int i = 0; i < g_state->rows; i++) {
        strcat(result, g_state->buffer[i]);
        strcat(result, "\n");
    }
    *out_len = total;
    g_state->modified = 0;
    pthread_mutex_unlock(&g_state->lock);
    return result;
}

void terminal_destroy() {
    if (!g_state) return;
    pthread_mutex_lock(&g_state->lock);
    for (int i = 0; i < g_state->rows; i++) {
        if (g_state->buffer[i]) free(g_state->buffer[i]);
    }
    free(g_state->buffer);
    for (int i = 0; i < g_state->scrollback_count; i++) {
        if (g_state->scrollback[i]) free(g_state->scrollback[i]);
    }
    free(g_state->scrollback);
    pthread_mutex_unlock(&g_state->lock);
    pthread_mutex_destroy(&g_state->lock);
    free(g_state);
    g_state = NULL;
    LOGI("Terminal destroyed");
}
