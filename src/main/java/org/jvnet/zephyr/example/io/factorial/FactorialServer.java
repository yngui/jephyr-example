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
import java.nio.channels.ServerSocketChannel;

import static org.jvnet.zephyr.example.io.factorial.CodecUtils.readBigInteger;
import static org.jvnet.zephyr.example.io.factorial.CodecUtils.writeNumber;

public final class FactorialServer {

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(System.getProperty("port", "8322"));

        try (ServerSocketChannel channel = ServerSocketChannel.open()) {
            channel.bind(new InetSocketAddress(port));
            while (true) {
                new Handler(channel.accept()).start();
            }
        }
    }

    private static final class Handler extends Thread {

        private final ByteChannel channel;

        Handler(ByteChannel channel) {
            this.channel = channel;
        }

        @Override
        public void run() {
            try {
                BigInteger lastMultiplier = BigInteger.ONE;
                BigInteger factorial = BigInteger.ONE;
                BigInteger multiplier;

                while ((multiplier = readBigInteger(channel)) != null) {
                    lastMultiplier = multiplier;
                    factorial = factorial.multiply(multiplier);
                    writeNumber(channel, factorial);
                }

                System.err.printf("Factorial of %,d is: %,d%n", lastMultiplier, factorial);
                channel.close();
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
