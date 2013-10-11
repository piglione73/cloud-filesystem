#ifndef _JAVAUTILS_H_
#define _JAVAUTILS_H_

void java_init();
void java_uninit();

JNIEnv *java_attach();
void java_detach();

typedef int java_jfuse_method(JNIEnv *env, jbyteArray jbuf);


java_jfuse_method java_getattr;



#endif


