package co.paralleluniverse.quasartkb;

import co.paralleluniverse.fibers.*;
import co.paralleluniverse.fibers.io.*;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class MainIO2 {
    static final int PORT = 1234;
    static final Charset charset = Charset.forName("UTF-8");
    static final boolean FJ_SCHEDULER = true;
    
    public static void main(String[] args) throws Exception {
        ThreadFactory tfactory = new ThreadFactoryBuilder().setDaemon(true).setNameFormat("nio-%d").build();
        AsynchronousChannelGroup group = AsynchronousChannelGroup.withFixedThreadPool(1, tfactory);
        FiberServerSocketChannel socket = FiberServerSocketChannel.open(group).bind(new InetSocketAddress(PORT));
        int processors = Runtime.getRuntime().availableProcessors();
        
        final FiberScheduler scheduler = FJ_SCHEDULER
                ? new FiberForkJoinScheduler("fj", processors)
                : new FiberExecutorScheduler("tp", Executors.newFixedThreadPool(processors)); // new FiberExecutorScheduler("io", (Executor) group);
        
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

        System.out.println("Press Enter to quit");
        System.in.read();
        System.exit(0);
    }
}