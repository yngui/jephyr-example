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

package org.jvnet.zephyr.example.philosophers;

import org.jvnet.zephyr.activeobject.annotation.ActiveObject;
import org.jvnet.zephyr.activeobject.annotation.Oneway;
import org.jvnet.zephyr.example.philosophers.Fork.Handle;

import static java.util.Objects.requireNonNull;

@ActiveObject
public final class Philosopher {

    private final String name;
    private final Fork leftFork;
    private final Fork rightFork;

    public Philosopher(String name, Fork leftFork, Fork rightFork) {
        this.name = requireNonNull(name);
        this.leftFork = requireNonNull(leftFork);
        this.rightFork = requireNonNull(rightFork);
    }

    @Oneway
    public void start() {
        while (true) {
            System.out.println(name + " starts to think");
            sleep(5000);

            Handle left = leftFork.take();
            if (left == null) {
                continue;
            }

            Handle right = rightFork.take();
            if (right == null) {
                left.put();
                continue;
            }

            System.out.println(name + " starts to eat");
            sleep(5000);

            left.put();
            right.put();
        }
    }

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
        }
    }
}
