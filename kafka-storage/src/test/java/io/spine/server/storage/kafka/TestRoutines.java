/*
 * Copyright 2017, TeamDev Ltd. All rights reserved.
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package io.spine.server.storage.kafka;

/**
 * @author Dmytro Dashenkov
 */
public final class TestRoutines {

    private static final Runtime runtime = Runtime.getRuntime();

    private TestRoutines() {
        // Prevent utility class instantiation.
    }

    public static void beforeEach() {
//        try {
//            // TODO:2017-09-11:dmytro.dashenkov: handle Windows too.
//            runtime.exec(
//                    "../kafka/bin/kafka-server-start.sh ../config/kafka-server.properties");
//        } catch (IOException e) {
//            throw new IllegalStateException(e);
//        }
    }

    public static void afterEach() {
//        try {
//            // TODO:2017-09-11:dmytro.dashenkov: handle Windows too.
//            final Process process = runtime.exec("../kafka/bin/kafka-server-stop.sh");
//            process.waitFor();
//            final File storage = new File("/tmp/kafka-logs");
//            Files.walk(storage.toPath(), FOLLOW_LINKS)
//                 .sorted(Comparator.reverseOrder())
//                 .map(Path::toFile)
//                 .forEachOrdered(File::delete);
//        } catch (IOException | InterruptedException e) {
//            throw new IllegalStateException(e);
//        }
    }
}
