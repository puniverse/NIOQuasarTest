package co.paralleluniverse.quasartkb;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncServer {

    public static void main(String[] args) throws IOException {

        ExecutorService executor = Executors.newFixedThreadPool(8);
        AsynchronousChannelGroup asyncChannelGroup = AsynchronousChannelGroup.withThreadPool(executor);
        final AsynchronousServerSocketChannel listener = AsynchronousServerSocketChannel.open(asyncChannelGroup);

        listener.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        listener.bind(new InetSocketAddress(1234));
        System.out.println("started");

        while (true) {
            listener.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
                @Override
                public void completed(AsynchronousSocketChannel connection, Void v) {
                    listener.accept(null, this);            
                    final ByteBuffer buffer = ByteBuffer.allocateDirect(1024);

                    connection.read(buffer, connection, new CompletionHandler<Integer, AsynchronousSocketChannel>() {
                        @Override
                        public void completed(Integer result, final AsynchronousSocketChannel readAttachment) {
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
                                    if (writeAttachment.hasRemaining()) {
                                        readAttachment.write(writeAttachment, writeAttachment, this);
                                    } else {
                                        writeAttachment.clear();
                                        try {
                                            readAttachment.close();
                                        } catch (IOException ex) {
                                        }
                                    }
                                }

                                @Override
                                public void failed(Throwable t, ByteBuffer bbAttachment) {
                                }
                            });

                        }

                        @Override
                        public void failed(Throwable t, AsynchronousSocketChannel scAttachment) {
                        }
                    });
                }

                @Override
                public void failed(Throwable t, Void v) {
                }
            });

        }

    }
}