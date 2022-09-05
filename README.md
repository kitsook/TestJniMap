# A quick test on Java JNI local references and global references

## Quick start
Assuming your machine has `make`, build-essentials (`gcc`, `ld` etc), and `jdk` installed:
```
git clone https://github.com/kitsook/TestJniMap.git
cd TestJniMap
make
```

## What is this?
This is a quick test on using local and global references of JNI. Specially, it is to answer the following questions:

Q: Is it necessary to free local references within JNI?
A: Not necessary. JVM should free up local references after the JNI call returned. However, if the JNI call is creating lots of local references, it is good practice to free up memory within the JNI call.

Q: Can a JNI call returns local references?
A: Yes. JVM will manage and free the memory on the Java side.

Q: Is it OK to free the returned local reference within JNI function before exiting?
A: NO! Doing so will cause the JNI function to return null.

Q: If leaving global references hanging around without freeing them in JNI cause out-of-memory issue?
A: Yes (obviously).

*Tested with OpenJDK 11.0.16*

## What am I looking at when running the code?
The program creates worker threads which will make (a lot of) JNI calls. A good way to visualize what is going on is to also run VisualVM to monitor the CPU and memory usage.

This is what the memory usage looks like when running the code:
file:///home/cho/Desktop/jni oom.png![image](https://user-images.githubusercontent.com/13360325/188356598-abf252ca-589f-47cb-85bb-48e9dc72c60a.png)

(1) Running the JNI calls normally, freeing local references as it goes. Everything works as expected.

(2) Running the JNI calls without explicitly freeing local references. It is still fine. There were reclaimable objects left in the memory. But triggering the GC (from VisualVM) will free up the space.

(3) Running the JNI calls incorrectly by freeing the local references before returning from JNI. Memory usage normal. But the program printout should indicate 0 test case passed. This is because `null` is returned instead of the expected `HashMap`.

(4) !! This will cause OutOfMemoryError !!. The JNI calls explicitly create global references without freeing them, causing out-of-memory issue.
