package net.clarenceho.jnimap;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.IntStream;

public class JniMap {
    private native HashMap<Integer, String> process(int mapSize, boolean freeTmpLocalRef, boolean freeReturnedLocalRef, boolean isLeaky);

    static {
        System.loadLibrary("jniMap");
    }

    private static final int PARALLELISM = 10;
    private static final int TRIAL_COUNT = 1_000_000;
    private static final int EXPECTED_MAP_SIZE = 100;

    public static void main(String[] args) {
        loopJni("This is the normal run. All cases should succeed", true, false, false);
        loopJni("This is the normal run wihout explicitly free temp variables. GC should clean them up. All cases should succeed", false, false, false);
        loopJni("This is an incorrect run. Freeing the local ref before JNI returns will cause null returned. All cases will fail", true, true, false);
        loopJni("This is an incorrect run. Leave global references hanging. !! This will cause OutOfMemoryError !!", true, false, true);
        promptEnterKey();

    }

    private static void loopJni(String message, boolean freeTmpLocalRef, boolean freeReturnedLocalRef, boolean isLeaky) {
        ForkJoinPool customThreadPool = new ForkJoinPool(PARALLELISM);
        System.out.println("\n" + message);
        promptEnterKey();
        System.out.println("Running...");

        JniMap jniMap = new JniMap();
        try {
          int successCount = customThreadPool.submit(() ->
              IntStream.rangeClosed(1, PARALLELISM)
                  .parallel()
                  .map(i -> IntStream.rangeClosed(1, TRIAL_COUNT / PARALLELISM)
                      .map(j -> {
                          // call JNI function
                          Map<Integer, String> result = jniMap.process(EXPECTED_MAP_SIZE, freeTmpLocalRef, freeReturnedLocalRef, isLeaky);

                          // verify result
                          if (result == null || result.size() != EXPECTED_MAP_SIZE) {
                              return 0;
                          }
                          boolean keysOk = IntStream.range(0, EXPECTED_MAP_SIZE).allMatch(n -> result.containsKey(n));
                          boolean payLoadOk = keysOk && result.values().stream().allMatch(s -> "hello world".equals(s));

                          return payLoadOk? 1 : 0;
                      })
                      .reduce(0, Integer::sum))
                  .reduce(0, Integer::sum)
          ).get();

          System.out.println("Expected: " + ((TRIAL_COUNT / PARALLELISM) * PARALLELISM) + "; Actual: " + successCount);
        } catch(ExecutionException | InterruptedException e) {
            System.err.println(e);
        } finally {
            customThreadPool.shutdown();
        }
    }

    private static void promptEnterKey(){
       System.out.println("Press \"ENTER\" to continue...");
       Scanner scanner = new Scanner(System.in);
       scanner.nextLine();
    }
}
