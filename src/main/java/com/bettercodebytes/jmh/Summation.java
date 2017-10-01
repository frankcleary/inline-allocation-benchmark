/*
 * Copyright (c) 2014, Oracle America, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of Oracle nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.bettercodebytes.jmh;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class Summation {
    private static final int LOOP_SIZE = 10000000;

    private static class MutableWrapper {
        private long value;

        public MutableWrapper(long value) {
            this.value = value;
        }

        public void add(long other) {
            value += other;
        }
    }

    @Benchmark
    public Long sumBoxed() {
        Long total = 0L;
        for (int i = 0; i < LOOP_SIZE; i++) {
            total += i;
        }
        return total;
    }

    @Benchmark
    public Long sumPrimitive() {
        long total = 0;
        for (int i = 0; i < LOOP_SIZE; i++) {
            total += i;
        }
        return total;
    }

    @Benchmark
    @Fork(jvmArgs = "-XX:-EliminateAllocations")
    public Long sumMutableWrapperOptOff() {
        long total = 0;
        for (int i = 0; i < LOOP_SIZE; i++) {
            MutableWrapper mutableWrapper = new MutableWrapper(total);
            mutableWrapper.add(i);
            total = mutableWrapper.value;
        }
        return total;
    }

    @Benchmark
    public Long sumMutableWrapper() {
        long total = 0;
        for (int i = 0; i < LOOP_SIZE; i++) {
            MutableWrapper mutableWrapper = new MutableWrapper(total);
            mutableWrapper.add(i);
            total = mutableWrapper.value;
        }
        return total;
    }

    private MutableWrapper escapee;
    @Benchmark
    public Long sumEscapedMutableWrapper() {
        long total = 0;
        for (int i = 0; i < LOOP_SIZE; i++) {
            escapee = new MutableWrapper(total);
            escapee.add(i);
            total = escapee.value;
        }
        return total;
    }

    @Benchmark
    public Long sumStream() {
        return LongStream.range(0, LOOP_SIZE).sum();
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(Summation.class.getSimpleName())
                .warmupIterations(5)
                .measurementIterations(10)
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}
