#include <jni.h>
#include <string>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>
#include "Protocol.h" // Тот самый файл со структурой
#define MYPORT 8888


int sock = -1;
struct sockaddr_in server_addr;

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_myapplication_MainActivity_initNetwork(JNIEnv* env, jobject /* this */, jstring ip) {
    sock = socket(AF_INET,SOCK_DGRAM,0);
    if (sock < 0) {
        return env->NewStringUTF("");
    }

    const char *nativeString = env->GetStringUTFChars(ip , nullptr);
    std::string tmpstr(nativeString);
    env->ReleaseStringUTFChars(ip, nativeString);
    int pos = tmpstr.rfind('.');
    std::string broadcast_ip = tmpstr.substr(0, pos+1) + "255";

    // Enable broadcast
    unsigned int broadcast = 1;

    if(setsockopt(sock,SOL_SOCKET,SO_BROADCAST,&broadcast,sizeof(broadcast)) < 0){
        close(sock);
        return 0;
    }

    struct sockaddr_in Recv_addr;
    struct sockaddr_in Sender_addr;

    socklen_t len = sizeof(struct sockaddr_in);
    char sendMSG[] ="Broadcast message from SLAVE TAG";
    char recvbuff[50] = "";
    int recvbufflen = 50;

    Recv_addr.sin_family = AF_INET;
    Recv_addr.sin_port = htons(MYPORT);
    //Recv_addr.sin_addr.s_addr  = INADDR_BROADCAST; // this isq equiv to 255.255.255.255
// better use subnet broadcast (for our subnet is 172.30.255.255)
    Recv_addr.sin_addr.s_addr = inet_addr(broadcast_ip.c_str());

    GamepadPacket packet = {0, 0, 0, 0, 0};
    sendto(sock,&packet,sizeof(packet),0,(sockaddr *)&Recv_addr, len);

    while (true) {
        int bytesReceived = recvfrom(sock,recvbuff,sizeof (recvbuff),0,(sockaddr *)&Sender_addr, &len);

        if (bytesReceived > 0) {

            server_addr.sin_family = AF_INET;
            server_addr.sin_port = Sender_addr.sin_port;
            server_addr.sin_addr.s_addr = Sender_addr.sin_addr.s_addr;

            char* sender_ip = inet_ntoa(Sender_addr.sin_addr);
            return env->NewStringUTF(sender_ip);
        }
    }




//
//    return env->NewStringUTF("");
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_myapplication_MainActivity_sendInput(JNIEnv* env, jobject /* this */, jshort lx, jshort ly, jshort rx, jshort ry, jint btns) {
    if (sock != -1) {
        GamepadPacket packet = {lx, ly, rx, ry, (uint16_t)btns};
        sendto(sock, &packet, sizeof(packet), 0, (struct sockaddr*)&server_addr, sizeof(server_addr));
    }
}

