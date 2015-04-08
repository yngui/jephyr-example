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

import java.io.EOFException;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

final class CodecUtils {

    private static final byte MAGIC_NUMBER = (byte) 'F';

    private CodecUtils() {
    }

    static BigInteger readBigInteger(ReadableByteChannel channel) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1);
        do {
            if (channel.read(buffer) < 0) {
                return null;
            }
        } while (buffer.hasRemaining());

        buffer.flip();
        byte magicNumber = buffer.get();
        if (magicNumber != MAGIC_NUMBER) {
            throw new IOException("Invalid magic number: " + magicNumber);
        }

        buffer = ByteBuffer.allocate(4);
        readFully(channel, buffer);
        buffer.flip();
        int length = buffer.getInt();

        buffer = ByteBuffer.allocate(length);
        readFully(channel, buffer);
        buffer.flip();
        return new BigInteger(buffer.array());
    }

    private static void readFully(ReadableByteChannel channel, ByteBuffer buffer) throws IOException {
        while (buffer.hasRemaining()) {
            if (channel.read(buffer) < 0) {
                throw new EOFException();
            }
        }
    }

    static void writeNumber(WritableByteChannel channel, Number number) throws IOException {
        BigInteger bigInteger;
        if (number instanceof BigInteger) {
            bigInteger = (BigInteger) number;
        } else {
            bigInteger = new BigInteger(String.valueOf(number));
        }

        byte[] bytes = bigInteger.toByteArray();
        int length = bytes.length;

        ByteBuffer buffer = ByteBuffer.allocate(5 + length);
        buffer.put(MAGIC_NUMBER);
        buffer.putInt(length);
        buffer.put(bytes);
        buffer.flip();

        do {
            channel.write(buffer);
        } while (buffer.hasRemaining());
    }
}
