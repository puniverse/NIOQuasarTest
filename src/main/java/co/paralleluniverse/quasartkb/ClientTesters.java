package co.paralleluniverse.quasartkb;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.ws.rs.client.AsyncClientBuilder;
import com.google.common.util.concurrent.RateLimiter;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;

public class ClientTesters {

    public static void main(String[] args) throws InterruptedException {
        final String URL = "http://localhost:"+MainIO2.PORT;
        final int REQ_PER_SEC = 500;
        final int DURATION = 10;
        final int TOTAL = DURATION * REQ_PER_SEC;

        final Client newClient = AsyncClientBuilder.newClient();
        final AtomicInteger ai = new AtomicInteger();
        int WARMUP_PERIOD = DURATION / 3;
        final RateLimiter rl = RateLimiter.create(REQ_PER_SEC, WARMUP_PERIOD, TimeUnit.SECONDS);

        final CountDownLatch cdl = new CountDownLatch(TOTAL);
        System.out.println("starting.. WARMUP="+WARMUP_PERIOD+" DUARTION="+DURATION+" RATE="+REQ_PER_SEC);
        long start = System.nanoTime();        
        for (int i = 0; i < TOTAL; i++) {
            rl.acquire();
            new Fiber<Void>(() -> {
                try {
                    Response resp = newClient.target(URL).request().buildGet().submit().get(5, TimeUnit.SECONDS);
                    if (resp.getStatus() == 200)
                        ai.incrementAndGet();
                } catch (ExecutionException | TimeoutException ex) {
                    System.out.println(ex);
                } finally {
                    cdl.countDown();
                }
            }).start();
        }
        cdl.await();
        long duration = System.nanoTime() - start;        
        System.out.println("finished " + ai + " out of "+TOTAL + " in "+TimeUnit.NANOSECONDS.toMillis(duration));
    }
}
