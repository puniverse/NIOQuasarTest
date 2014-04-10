package co.paralleluniverse.quasartkb;

import co.paralleluniverse.fibers.*;
import co.paralleluniverse.fibers.io.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.*;
import java.nio.charset.*;
import java.nio.file.*;
import static java.nio.file.StandardOpenOption.*;
import java.util.concurrent.atomic.*;

public class MainIO1 {
    static final int PORT = 1234;
    static final Charset charset = Charset.forName("UTF-8");
    static final AtomicInteger counter = new AtomicInteger(0);
    
    public static void main(String[] args) throws Exception {
        new Fiber(() -> {
            try {
                System.out.println("Starting server");
                FiberServerSocketChannel socket = FiberServerSocketChannel.open().bind(new InetSocketAddress(PORT));
                for (;;) {
                    FiberSocketChannel ch = socket.accept();
                    new Fiber(() -> {
                        try {
                            ByteBuffer buf = ByteBuffer.allocateDirect(1024);
                            int n = ch.read(buf);
                            FiberFileChannel fch = FiberFileChannel.open(Paths.get("/tmp/dummy/", String.valueOf(counter.incrementAndGet())), READ, WRITE, CREATE, TRUNCATE_EXISTING);
                            buf.flip();
                            fch.write(buf);
                            fch.close();

                            String response = "HTTP/1.0 200 OK\r\nDate: Fri, 31 Dec 1999 23:59:59 GMT\r\nContent-Type: text/html\r\nContent-Length: 0\r\n\r\n";
                            n = ch.write(charset.newEncoder().encode(CharBuffer.wrap(response)));
                            ch.close();
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
        Thread.sleep(Long.MAX_VALUE);
    }
}
