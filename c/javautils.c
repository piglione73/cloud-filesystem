#include <stdlib.h>
#include <string.h>
#include <jni.h>
#include "javautils.h"


#define PROXYCLASSNAME "com.paviasystem.jfuse.FuseProxy"



static JavaVM *jvm;
static jclass proxyClass;

static jmethodID mid_getattr;


void java_init() {
	JavaVMOption options[2];
	memset(options, 0, sizeof(options));
	options[0].optionString = "-Djava.class.path=/home/raffy/eclipse-workspace/prova/bin";
	//options[1].optionString = "-verbose:jni";

	JavaVMInitArgs vm_args;
	vm_args.version = JNI_VERSION_1_2;
	vm_args.options = options;
	vm_args.nOptions = 1;
	vm_args.ignoreUnrecognized = JNI_TRUE;

	JNIEnv *env;
	jint ret = JNI_CreateJavaVM(&jvm, (void **)&env, &vm_args);
	if(ret != JNI_OK) {
		perror("java_init JNI_CreateJavaVM");
		abort();
	}
	
	//Init classes and methods
	proxyClass = (*env)->FindClass(env, PROXYCLASSNAME);
	mid_getattr = (*env)->GetStaticMethodID(env, proxyClass, "getattr", "([Ljava/lang/String;)V");
}


void java_uninit() {
	(*jvm)->DestroyJavaVM(jvm);
}


JNIEnv *java_attach() {
	//If the current thread is already attached to the JVM, then just return the env
	JNIEnv *env;
	jint ret = (*jvm)->GetEnv(jvm, (void **)&env, JNI_VERSION_1_2);
	if(ret == JNI_OK)
		return env;
	
	//Otherwise, try to attach the current thread
	ret = (*jvm)->AttachCurrentThread(jvm, (void **)&env, NULL);
	if(ret == JNI_OK)
		return env;
		
	//For some reason, we cannot attach to the JVM
	perror("java_attach AttachCurrentThread");
	return NULL;
}


void java_detach() {
	(*jvm)->DetachCurrentThread(jvm);
}


int java_call_proxy(JNIEnv *env, jmethodID mid, jbyteArray jbuf) {
	return (*env)->CallStaticIntMethod(env, proxyClass, mid, jbuf);
}

int java_getattr(JNIEnv *env, jbyteArray jbuf) {
	return java_call_proxy(env, mid_getattr, jbuf);
}

