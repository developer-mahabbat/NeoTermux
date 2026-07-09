#include <jni.h>
#include <string.h>
#include <stdlib.h>
#include <android/log.h>

#define TAG "NeoTermux-JNI"

extern int terminal_init(int rows, int cols);
extern int terminal_write(const char *data, int len);
extern int terminal_resize(int rows, int cols);
extern char *terminal_get_buffer(int *out_len);
extern void terminal_destroy();
extern int pty_open(const char *shell_path, char *const env[], const char *term_env, void (*callback)(const char *, int));
extern int pty_write(const char *data, int len);
extern int pty_resize_window_size(int rows, int cols);
extern void pty_close();
extern int pty_is_running();

static JavaVM *g_vm = NULL;
static jobject g_callback_obj = NULL;
static jmethodID g_callback_method = NULL;

static void on_pty_output(const char *data, int len) {
    JNIEnv *env;
    if ((*g_vm)->GetEnv(g_vm, (void **)&env, JNI_VERSION_1_6) != JNI_OK) {
        (*g_vm)->AttachCurrentThread(g_vm, &env, NULL);
    }
    if (g_callback_obj && g_callback_method) {
        jstring jdata = (*env)->NewStringUTF(env, data);
        (*env)->CallVoidMethod(env, g_callback_obj, g_callback_method, jdata, len);
        (*env)->DeleteLocalRef(env, jdata);
    }
}

JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    g_vm = vm;
    return JNI_VERSION_1_6;
}

JNIEXPORT void JNICALL
Java_com_neotermux_termlib_TerminalNative_init(JNIEnv *env, jobject thiz, jint rows, jint cols) {
    terminal_init(rows, cols);
}

JNIEXPORT jint JNICALL
Java_com_neotermux_termlib_TerminalNative_write(JNIEnv *env, jobject thiz, jbyteArray data) {
    jsize len = (*env)->GetArrayLength(env, data);
    jbyte *bytes = (*env)->GetByteArrayElements(env, data, NULL);
    int result = terminal_write((const char *)bytes, len);
    (*env)->ReleaseByteArrayElements(env, data, bytes, JNI_ABORT);
    return result;
}

JNIEXPORT void JNICALL
Java_com_neotermux_termlib_TerminalNative_resize(JNIEnv *env, jobject thiz, jint rows, jint cols) {
    terminal_resize(rows, cols);
}

JNIEXPORT jstring JNICALL
Java_com_neotermux_termlib_TerminalNative_getBuffer(JNIEnv *env, jobject thiz) {
    int len;
    char *buf = terminal_get_buffer(&len);
    if (!buf) return NULL;
    jstring result = (*env)->NewStringUTF(env, buf);
    free(buf);
    return result;
}

JNIEXPORT void JNICALL
Java_com_neotermux_termlib_TerminalNative_destroy(JNIEnv *env, jobject thiz) {
    terminal_destroy();
}

JNIEXPORT jint JNICALL
Java_com_neotermux_termlib_PtyNative_open(JNIEnv *env, jobject thiz,
                                           jstring shellPath, jobject callback) {
    const char *shell = (*env)->GetStringUTFChars(env, shellPath, NULL);
    if (callback) {
        g_callback_obj = (*env)->NewGlobalRef(env, callback);
        jclass cls = (*env)->GetObjectClass(env, callback);
        g_callback_method = (*env)->GetMethodID(env, cls, "onOutput", "(Ljava/lang/String;I)V");
    }
    char *argv[] = {(char *)shell, NULL};
    int result = pty_open(shell, argv, "xterm-256color", on_pty_output);
    (*env)->ReleaseStringUTFChars(env, shellPath, shell);
    return result;
}

JNIEXPORT jint JNICALL
Java_com_neotermux_termlib_PtyNative_write(JNIEnv *env, jobject thiz, jbyteArray data) {
    jsize len = (*env)->GetArrayLength(env, data);
    jbyte *bytes = (*env)->GetByteArrayElements(env, data, NULL);
    int result = pty_write((const char *)bytes, len);
    (*env)->ReleaseByteArrayElements(env, data, bytes, JNI_ABORT);
    return result;
}

JNIEXPORT void JNICALL
Java_com_neotermux_termlib_PtyNative_resizeWindow(JNIEnv *env, jobject thiz,
                                                  jint rows, jint cols) {
    pty_resize_window_size(rows, cols);
}

JNIEXPORT void JNICALL
Java_com_neotermux_termlib_PtyNative_close(JNIEnv *env, jobject thiz) {
    pty_close();
}

JNIEXPORT jboolean JNICALL
Java_com_neotermux_termlib_PtyNative_isRunning(JNIEnv *env, jobject thiz) {
    return pty_is_running() ? JNI_TRUE : JNI_FALSE;
}
