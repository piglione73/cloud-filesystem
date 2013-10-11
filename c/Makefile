jfuse : jfuse.o javautils.o
	gcc -g -o jfuse jfuse.o javautils.o `pkg-config fuse --libs` -ljvm -L/usr/lib/jvm/java-7-openjdk-i386/jre/lib/i386/client

jfuse.o : jfuse.c javautils.h
	gcc -g -Wall `pkg-config fuse --cflags` -I/usr/lib/jvm/java-7-openjdk-i386/include -c jfuse.c

javautils.o : javautils.c javautils.h
	gcc -g -Wall `pkg-config fuse --cflags` -I/usr/lib/jvm/java-7-openjdk-i386/include -c javautils.c

clean:
	rm -Rf prova1
	rm -Rf prova2
	rm -f jfuse *.o

test : jfuse
	rm -Rf prova1
	rm -Rf prova2
	mkdir prova1
	mkdir prova2
	./jfuserun

