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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;
import static org.junit.Assert.assertEquals;

/**
 * @author Dmytro Dashenkov
 */
final class TestRoutines {

    private static final File KAFKA_BIN_DIR;

    static {
        try {
            KAFKA_BIN_DIR = new File("../kafka/bin").getCanonicalFile();
            checkNotNull(KAFKA_BIN_DIR);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    // TODO:2017-09-11:dmytro.dashenkov: handle Windows too.
    private static final String LIST_TOPICS_CMD =
            "/kafka-topics.sh --zookeeper localhost:2181 --list";
    private static final String DELETE_TOPIC_CMD_TEMPLATE =
            "/kafka-topics.sh --zookeeper localhost:2181 --delete --topic %s";
    private static final String KAFKA_INTERNAL_TOPIC_PREFIX = "__";

    private TestRoutines() {
        // Prevent utility class instantiation.
    }

    static void beforeEach() {
        // NoOp fo now.
        // When needed to run some routine each time before *each* test case,
        // add the routine to this method.
    }

    static void afterEach() {
        try {
            final File tmpFile = Files.createTempFile("kafka-test", "topics")
                                      .toFile();
            final Process process = new ProcessBuilder(prepareCmd(LIST_TOPICS_CMD))
                    .redirectOutput(tmpFile)
                    .start();
            process.waitFor();
            Files.lines(tmpFile.toPath())
                 .filter(topic -> !topic.startsWith(KAFKA_INTERNAL_TOPIC_PREFIX))
                 .forEach(TestRoutines::deleteTopic);
            tmpFile.delete();
        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void deleteTopic(String topic) {
        final String[] cmd = prepareCmd(format(DELETE_TOPIC_CMD_TEMPLATE, topic));
        try {
            assertEquals(0, Runtime.getRuntime()
                                   .exec(cmd)
                                   .waitFor());
        } catch (InterruptedException | IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String[] prepareCmd(String kafkaScriptCommand) {
        final String cmd = KAFKA_BIN_DIR.getAbsolutePath() + kafkaScriptCommand;
        final String[] result = cmd.split(" ");
        return result;
    }
}
