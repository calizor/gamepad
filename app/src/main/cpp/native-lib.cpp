#include <jni.h>
#include <string>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include "Protocol.h" // Тот самый файл со структурой

int client_socket = -1;
struct sockaddr_in server_addr;

extern "C" JNIEXPORT void JNICALL
Java_com_example_myapplication_MainActivity_initNetwork(JNIEnv* env, jobject /* this */, jstring ip, jint port) {
    const char* ip_str = env->GetStringUTFChars(ip, nullptr);

    client_socket = socket(AF_INET, SOCK_DGRAM, 0);
    server_addr.sin_family = AF_INET;
    server_addr.sin_port = htons(port);
    inet_pton(AF_INET, ip_str, &server_addr.sin_addr);

    env->ReleaseStringUTFChars(ip, ip_str);
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_myapplication_MainActivity_sendInput(JNIEnv* env, jobject /* this */, jshort lx, jshort ly, jshort rx, jshort ry, jint btns) {
    if (client_socket != -1) {
        GamepadPacket packet = {lx, ly, rx, ry, (uint16_t)btns};
        sendto(client_socket, &packet, sizeof(packet), 0, (struct sockaddr*)&server_addr, sizeof(server_addr));
    }
}
