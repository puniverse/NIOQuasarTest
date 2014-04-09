package co.paralleluniverse.quasartkb;

import co.paralleluniverse.common.util.SameThreadExecutor;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class AsyncServer {

    static void dump(String op) {
//        System.err.println("T: " + Thread.currentThread().getName() + " Op: " + op);
//        Thread.dumpStack();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        String config;
        // config = System.getProperty("co.paralleluniverse.asyncChannelGroup");
        config = "cached";
        int nThreads = 8;
        ThreadFactory tfactory = new ThreadFactoryBuilder().setDaemon(true).setNameFormat("nio-%d").build();
        ThreadFactory tfactory2 = new ThreadFactoryBuilder().setDaemon(true).setNameFormat("nio-wt-%d").build();
        AsynchronousChannelGroup asyncChannelGroup;
        Executor exec;
        switch (config) {
            case "tp":
                asyncChannelGroup = AsynchronousChannelGroup.withThreadPool(Executors.newFixedThreadPool(nThreads, tfactory));
                exec = SameThreadExecutor.getExecutor();
                break;
            case "fixed":
                asyncChannelGroup = AsynchronousChannelGroup.withFixedThreadPool(nThreads, tfactory);
                exec = SameThreadExecutor.getExecutor();
                break;
            case "cached":
                asyncChannelGroup = AsynchronousChannelGroup.withCachedThreadPool(Executors.newCachedThreadPool(tfactory2), 0);
                exec = SameThreadExecutor.getExecutor();
                break;
            case "executor":
                asyncChannelGroup = AsynchronousChannelGroup.withFixedThreadPool(1, tfactory);
                exec = Executors.newCachedThreadPool(tfactory2);
                break;
            default:
                throw new AssertionError();

        }
        final AsynchronousServerSocketChannel listener = AsynchronousServerSocketChannel.open(asyncChannelGroup);

        listener.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        listener.bind(new InetSocketAddress(1234));

        while (true) {
            listener.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
                @Override
                public void completed(AsynchronousSocketChannel connection, Void v) {
                    dump("accept");
                    exec.execute(() -> {
                        listener.accept(null, this);
                        final ByteBuffer buffer = ByteBuffer.allocateDirect(1024);

                        connection.read(buffer, connection, new CompletionHandler<Integer, AsynchronousSocketChannel>() {
                            @Override
                            public void completed(Integer result, final AsynchronousSocketChannel readAttachment) {
                                dump("read");
                                exec.execute(() -> {
                                    if (result == -1) {
                                        try {
                                            readAttachment.close();
                                        } catch (IOException e) {
                                        }
                                    }

                                    String response = "HTTP/1.0 200 OK\r\n"
                                            + "Date: Fri, 31 Dec 1999 23:59:59 GMT\r\n"
                                            + "Content-Type: text/html\r\n"
                                            + "Content-Length: 0\r\n\r\n";

                                    buffer.clear();
                                    final ByteBuffer responseBuffer = ByteBuffer.wrap(response.getBytes());

                                    readAttachment.write(responseBuffer, responseBuffer, new CompletionHandler<Integer, ByteBuffer>() {
                                        @Override
                                        public void completed(Integer result, ByteBuffer writeAttachment) {
                                            dump("write");
                                            exec.execute(() -> {
                                                if (writeAttachment.hasRemaining()) {
                                                    readAttachment.write(writeAttachment, writeAttachment, this);
                                                } else {
                                                    writeAttachment.clear();
                                                    try {
                                                        readAttachment.close();
                                                    } catch (IOException ex) {
                                                    }
                                                }
                                            });
                                        }

                                        @Override
                                        public void failed(Throwable t, ByteBuffer bbAttachment) {
                                        }
                                    });
                                });

                            }

                            @Override
                            public void failed(Throwable t, AsynchronousSocketChannel scAttachment) {
                            }
                        });
                    });
                }

                @Override
                public void failed(Throwable t, Void v) {
                }
            });

            System.out.println("started");
            Thread.sleep(999999);
        }

    }
}
