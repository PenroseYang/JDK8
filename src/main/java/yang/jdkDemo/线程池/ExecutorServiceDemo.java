package yang.jdkDemo.线程池;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author ：yang
 * @date ：2021/5/12 21:33
 */
public class ExecutorServiceDemo {
    public static void main(String[] args) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
    }
}