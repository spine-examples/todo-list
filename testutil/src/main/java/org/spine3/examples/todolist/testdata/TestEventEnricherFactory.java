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

package org.spine3.examples.todolist.testdata;

import org.spine3.examples.todolist.LabelColor;
import org.spine3.examples.todolist.LabelDetails;
import org.spine3.examples.todolist.LabelId;
import org.spine3.examples.todolist.LabelIdsList;
import org.spine3.examples.todolist.TaskDefinition;
import org.spine3.examples.todolist.TaskDetails;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.TaskPriority;
import org.spine3.protobuf.Timestamps;
import org.spine3.server.event.enrich.EventEnricher;

import java.util.function.Function;

import static org.spine3.examples.todolist.testdata.TestEventFactory.LABEL_ID;

/**
 * Provides event enricher for test needs.
 *
 * @author Illia Shepilov
 */
public class TestEventEnricherFactory {

    public static final String LABEL_TITLE = "title";
    private static final String DESCRIPTION = "description";
    private static final TaskDetails TASK_DETAILS = TaskDetails.newBuilder()
                                                               .setDescription(DESCRIPTION)
                                                               .setPriority(TaskPriority.LOW)
                                                               .build();
    private static final TaskDefinition TASK_DEFINITION = TaskDefinition.newBuilder()
                                                                        .setDescription(DESCRIPTION)
                                                                        .setDueDate(Timestamps.getCurrentTime())
                                                                        .setPriority(TaskPriority.NORMAL)
                                                                        .build();
    private static final LabelDetails LABEL_DETAILS = LabelDetails.newBuilder()
                                                                  .setColor(LabelColor.BLUE)
                                                                  .setTitle(LABEL_TITLE)
                                                                  .build();

    private TestEventEnricherFactory() {
    }

    private static final Function<LabelId, LabelDetails> LABEL_ID_TO_LABEL_DETAILS = labelId -> LABEL_DETAILS;

    private static final Function<TaskId, LabelIdsList> TASK_ID_TO_LABEL_IDS_LIST = taskId -> {

        final LabelIdsList result = LabelIdsList.newBuilder()
                                                .addLabelIds(LABEL_ID)
                                                .build();
        return result;
    };

    private static final Function<TaskId, TaskDetails> TASK_ID_TO_TASK_DETAILS = taskId -> TASK_DETAILS;

    private static final Function<TaskId, TaskDefinition> TASK_ID_TO_TASK_DEFINITION = taskId -> TASK_DEFINITION;

    /**
     * Provides a pre-configured {@link EventEnricher} event instance.
     *
     * @return {@link EventEnricher}
     */
    public static EventEnricher eventEnricherInstance() {
        final EventEnricher result = EventEnricher.newBuilder()
                                                  .addFieldEnrichment(LabelId.class,
                                                                      LabelDetails.class,
                                                                      LABEL_ID_TO_LABEL_DETAILS::apply)
                                                  .addFieldEnrichment(TaskId.class,
                                                                      TaskDetails.class,
                                                                      TASK_ID_TO_TASK_DETAILS::apply)
                                                  .addFieldEnrichment(TaskId.class,
                                                                      LabelIdsList.class,
                                                                      TASK_ID_TO_LABEL_IDS_LIST::apply)
                                                  .addFieldEnrichment(TaskId.class,
                                                                      TaskDefinition.class,
                                                                      TASK_ID_TO_TASK_DEFINITION::apply)
                                                  .build();
        return result;
    }
}
