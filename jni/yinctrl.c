#include <stdio.h>
#include <errno.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <sys/stat.h>
#include <stdlib.h>
#include <string.h>
#include <sys/time.h>
#include <linux/input.h>
#include <sys/epoll.h>
#include <jni.h>

//字符串匹配
int findstr(char *a, char *b) {
	char *p;
	if (p = strstr(a, b))
		return p - a + 1;
	return -1;
}

//初始化epoll
int fd_key;
struct input_event event_key, event_ts;
int ep_id;
struct epoll_event ev;
struct epoll_event evs[512];

jint JNICALL Java_org_yinctrl_dao_YinCtrlDao_startepoll
  (JNIEnv * env, jobject thiz, jstring jstr){
	const char *str = (*env)->GetStringUTFChars(env, jstr, NULL);
	fd_key = open(str, O_RDONLY);
	ep_id = epoll_create(512);
	ev.data.fd = fd_key;
	ev.events = EPOLLIN;
	epoll_ctl(ep_id, EPOLL_CTL_ADD, fd_key, &ev);
	return fd_key;
}

jobject JNICALL Java_org_yinctrl_dao_YinCtrlDao_getReadpower(JNIEnv * env,
		jobject thiz) {
		int num = epoll_wait(ep_id, evs, 512, -1);
		int i;
		for (i = 0; i < num; i++) {
			if (evs[i].events & EPOLLIN) {
				read(fd_key, &event_key, sizeof(struct input_event));
//				printf("key value:type=%d, code=%d, value=%d\n", event_key.type,
//						event_key.code, event_key.value);
				jclass input = (*env)->FindClass(env, "org/yinctrl/pojo/Input");
				jfieldID type = (*env)->GetFieldID(env,input,"type","I");
				jfieldID code = (*env)->GetFieldID(env,input,"code","I");
				jfieldID value = (*env)->GetFieldID(env,input,"value","I");
				jfieldID time_sce = (*env)->GetFieldID(env,input,"time_sce","I");
				(*env)->SetShortField(env,input,type,event_key.type);
				(*env)->SetShortField(env,input,code,event_key.code);
				(*env)->SetShortField(env,input,value,event_key.value);
				(*env)->SetShortField(env,input,time_sce,event_key.time.tv_sec);
				return input;
			}
		}
}
jstring JNICALL Java_org_yinctrl_dao_YinCtrlDao_getdevices(JNIEnv * env,
		jobject thiz) {
	FILE *fp;
	char *path = "/proc/bus/input/devices";
	char str[1024];
	int n = 1, i = 1024;
	char *all = (char *) malloc(i * sizeof(int));
	fp = fopen(path, "r");
	if (fp != NULL) {
		while (!feof(fp)) {
			fgets(str, sizeof(str), fp);
			if (findstr(str, "gpio-keys") != -1) {
				n++;
				all = (char*) realloc(all, (i * n) * sizeof(int));
				all = strcat(all, str);
			}
		}
		fclose(fp);
		return (*env)->NewStringUTF(env, all);
	} else {
		return (*env)->NewStringUTF(env, "NULL");
	}
}
