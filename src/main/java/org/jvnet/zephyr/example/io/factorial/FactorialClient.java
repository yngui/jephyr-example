/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Igor Konev
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.jvnet.zephyr.example.io.factorial;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.nio.channels.ByteChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import static org.jvnet.zephyr.example.io.factorial.CodecUtils.readBigInteger;
import static org.jvnet.zephyr.example.io.factorial.CodecUtils.writeNumber;

public final class FactorialClient {

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        String host = System.getProperty("host", "127.0.0.1");
        int port = Integer.parseInt(System.getProperty("port", "8322"));
        int count = Integer.parseInt(System.getProperty("count", "1000"));

        try (ByteChannel channel = SocketChannel.open(new InetSocketAddress(host, port))) {
            new Sender(channel, count).start();
            FutureTask<BigInteger> task = new FutureTask<>(new Receiver(channel, count));
            new Thread(task).start();
            System.err.format("Factorial of %,d is: %,d", count, task.get());
        }
    }

    private static final class Receiver implements Callable<BigInteger> {

        private final ReadableByteChannel channel;
        private final int count;

        Receiver(ReadableByteChannel channel, int count) {
            this.channel = channel;
            this.count = count;
        }

        @Override
        public BigInteger call() throws Exception {
            BigInteger bigInteger = null;
            for (int i = 0; i < count; i++) {
                bigInteger = readBigInteger(channel);
            }
            return bigInteger;
        }
    }

    private static final class Sender extends Thread {

        private final WritableByteChannel channel;
        private final int count;

        Sender(WritableByteChannel channel, int count) {
            this.channel = channel;
            this.count = count;
        }

        @Override
        public void run() {
            try {
                int n = 1;
                for (int i = 0; i < count; i++) {
                    writeNumber(channel, n);
                    n++;
                }
            } catch (Throwable e) {
                try {
                    channel.close();
                } catch (Throwable suppressed) {
                    e.addSuppressed(suppressed);
                }
                e.printStackTrace();
            }
        }
    }
}
