/*
 * Copyright (c) 2003, 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package nsk.jvmti.PopFrame;

import java.io.*;

/**
 * This test checks that the JVMTI function <code>PopFrame()</code>
 * correctly returns the following errors:
 * <li><i>JVMTI_ERROR_NULL_POINTER</i>
 * <li><i>JVMTI_ERROR_INVALID_THREAD</i>
 * <li><i>JVMTI_ERROR_THREAD_NOT_SUSPENDED</i><br>
 * and no JVMTI events will be generated by the function.<br>
 * The test creates an instance of inner class <code>popFrameCls</code>
 * and start it in a separate thread. Then the test tries to pop frame
 * with the following erroneous thread's parameter of the
 * <code>PopFrame()</code>:
 * <li>NULL pointer to the thread
 * <li>an invalid thread
 * <li>the non suspended popFrameCls' thread<br><br>
 * The test was changed due to the bug 4448675.
 */
public class popframe002 {
    static final int PASSED = 0;
    static final int FAILED = 2;
    static final int JCK_STATUS_BASE = 95;

    static boolean DEBUG_MODE = false;
    static volatile boolean popFdone = false;
    static volatile int totRes = PASSED;
    private PrintStream out;
    private popFrameCls popFrameClsThr;
    static Object readi = new Object(); // for notification about readiness
    static Object barrier = new Object(); // for suspending a child thread

    static {
        try {
            System.loadLibrary("popframe002");
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Could not load popframe002 library");
            System.err.println("java.library.path:" +
                System.getProperty("java.library.path"));
            throw e;
        }
    }

    native static int doPopFrame(int t_case, popFrameCls popFrameClsThr);

    public static void main(String[] argv) {
        argv = nsk.share.jvmti.JVMTITest.commonInit(argv);

        System.exit(run(argv, System.out) + JCK_STATUS_BASE);
    }

    public static int run(String argv[], PrintStream out) {
        return new popframe002().runIt(argv, out);
    }

    private int runIt(String argv[], PrintStream out) {
        int retValue = 0;

        this.out = out;
        for (int i = 0; i < argv.length; i++) {
            if (argv[i].equals("-v")) // verbose mode
                DEBUG_MODE = true;
        }

        popFrameClsThr = new popFrameCls();
        synchronized (barrier) { // force a child thread to pause
            synchronized(readi) {
                popFrameClsThr.start(); // start the child thread
// wait until the thread will enter into a necessary method
                try {
                    readi.wait(); // wait for the child readiness
                } catch (Exception e) {
                    out.println("TEST FAILURE: waiting for " +
                        popFrameClsThr.toString() + ": caught " + e);
                    return FAILED;
                }
            }

/* check that if PopFrame() would be invoked with NULL pointer to
   the thread, it will return the error JVMTI_ERROR_NULL_POINTER */
            if (DEBUG_MODE)
                totRes = retValue = doPopFrame(1, popFrameClsThr);
            else
                totRes = retValue = doPopFrame(0, popFrameClsThr);
            if (DEBUG_MODE && retValue == PASSED)
                out.println("Check #1 PASSED:\n" +
                    "\tPopFrame(), being invoked with NULL pointer " +
                    "to the thread,\n" +
                    "\treturned the appropriate error JVMTI_ERROR_NULL_POINTER");

/* check that if the thread, whose top frame is to be popped,
  is invalid, the PopFrame() will return the error
  JVMTI_ERROR_INVALID_THREAD */
            if (DEBUG_MODE)
                retValue = doPopFrame(3, popFrameClsThr);
            else
                retValue = doPopFrame(2, popFrameClsThr);
            if (retValue == FAILED) {
                popFdone = true;
                totRes = FAILED;
            } else
                if (DEBUG_MODE && retValue == PASSED)
                    out.println("Check #3 PASSED:\n" +
                        "\tPopFrame(), being invoked with " +
                        "the invalid thread,\n" +
                        "\treturned the appropriate error " +
                        "JVMTI_ERROR_INVALID_THREAD");

/* check that if the thread, whose top frame is to be popped,
  has not been suspended, the PopFrame() will return the error
  JVMTI_ERROR_THREAD_NOT_SUSPENDED */
            if (DEBUG_MODE)
                retValue = doPopFrame(5, popFrameClsThr);
            else
                retValue = doPopFrame(4, popFrameClsThr);
            if (retValue == FAILED) {
                popFdone = true;
                totRes = FAILED;
            } else
                if (DEBUG_MODE && retValue == PASSED)
                    out.println("Check #5 PASSED:\n" +
                        "\tPopFrame(), being invoked with " +
                        "the non suspended thread,\n" +
                        "\treturned the appropriate error " +
                        "JVMTI_ERROR_THREAD_NOT_SUSPENDED");
        }

        return totRes;
    }

    class popFrameCls extends Thread {
        public void run() {
            boolean compl = true;

            if (popframe002.popFdone) { // popping has been done
                out.println("TEST FAILED: frame with popFrameCls.run() was popped");
                popframe002.totRes = FAILED;
            }
            else {
                synchronized(readi) {
                    readi.notify(); // notify the main thread
                }
            }
            if (DEBUG_MODE)
                out.println("popFrameCls (" + this +
                    "): inside run()");
            try {
// pause here and get the main thread a chance to run
                synchronized (popframe002.barrier) {}
                compl = false;
            } catch (Exception e) {
                out.println("FAILURE: popFrameCls (" + this +
                    "): caught " + e);
                compl = false;
            } finally {
                if (compl) {
                    out.println("TEST FAILED: finally block was executed after PopFrame()");
                    popframe002.totRes = FAILED;
                }
            }
        }
    }
}
