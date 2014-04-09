package co.paralleluniverse.quasartkb;

import co.paralleluniverse.fibers.*;
import co.paralleluniverse.fibers.io.*;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class MainIO2 {
    static final int PORT = 1234;
    static final Charset charset = Charset.forName("UTF-8");

    public static void main(String[] args) throws Exception {
        int processors = Runtime.getRuntime().availableProcessors();
        int nThreads = 8;
        ThreadFactory tfactory = new ThreadFactoryBuilder().setDaemon(true).setNameFormat("nio-%d").build();

        final AsynchronousChannelGroup group;
        final FiberScheduler scheduler;
        switch (System.getProperty("co.paralleluniverse.scheduler")) {
            case "tp":
                group = AsynchronousChannelGroup.withFixedThreadPool(1, tfactory);
                scheduler = new FiberExecutorScheduler("tp", Executors.newCachedThreadPool(tfactory)); // new FiberExecutorScheduler("io", (Executor) group);
                break;
            case "fj":
                group = AsynchronousChannelGroup.withFixedThreadPool(1, tfactory);
                scheduler = new FiberForkJoinScheduler("fj", nThreads);
                break;
            case "io1":
                group = AsynchronousChannelGroup.withFixedThreadPool(nThreads, tfactory);
                scheduler = new FiberExecutorScheduler("tp", (Executor)group);
                break;
            case "io2":
                group = AsynchronousChannelGroup.withCachedThreadPool(Executors.newCachedThreadPool(), 0);
                scheduler = new FiberExecutorScheduler("tp", (Executor)group);
                break;
            case "io3":
                group = AsynchronousChannelGroup.withCachedThreadPool(Executors.newCachedThreadPool(), 1);
                scheduler = new FiberExecutorScheduler("tp", (Executor)group);
                break;
            default:
                throw new AssertionError();
        }

        FiberServerSocketChannel socket = FiberServerSocketChannel.open(group).bind(new InetSocketAddress(PORT));

        new Fiber(scheduler, () -> {
            try {
                System.out.println("Starting server");
                for (;;) {
                    FiberSocketChannel ch = socket.accept();
                    new Fiber(scheduler, () -> {
                        try {
                            ByteBuffer buf = ByteBuffer.allocateDirect(1024);
                            int n = ch.read(buf);
                            String response = "HTTP/1.0 200 OK\r\nDate: Fri, 31 Dec 1999 23:59:59 GMT\r\nContent-Type: text/html\r\nContent-Length: 0\r\n\r\n";
                            n = ch.write(charset.newEncoder().encode(CharBuffer.wrap(response)));
                            ch.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        System.out.println("started");
        Thread.sleep(999999);
    }
}
