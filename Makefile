.PHONY: run dirs classes jnimaplib clean

run: dirs classes jnimaplib
	java -Xms512m -Xmx512m -cp build/classes -Djava.library.path=build/libs/jnimap/shared net.clarenceho.jnimap.JniMap

dirs:
	mkdir -p build/classes build/libs/jnimap/shared

classes: src/main/java/net/clarenceho/jnimap/JniMap.java
	javac -d build/classes src/main/java/net/clarenceho/jnimap/JniMap.java

jnimaplib: src/jnimap/c/net_clarenceho_jnimap_JniMap.c src/jnimap/c/net_clarenceho_jnimap_JniMap.h
	gcc -I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux -fPIC -c src/jnimap/c/net_clarenceho_jnimap_JniMap.c -o build/libs/jnimap/shared/net_clarenceho_jnimap_JniMap.o
	gcc -shared -fPIC -o build/libs/jnimap/shared/libjniMap.so build/libs/jnimap/shared/net_clarenceho_jnimap_JniMap.o -lc

clean:
	rm -fR build
