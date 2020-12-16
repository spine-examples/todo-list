/*
 * Copyright 2020, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

package io.spine.examples.todolist.client;

import io.spine.client.Subscription;
import io.spine.examples.todolist.tasks.LabelId;
import io.spine.examples.todolist.tasks.Task;
import io.spine.examples.todolist.tasks.TaskId;
import io.spine.examples.todolist.tasks.TaskLabel;
import io.spine.examples.todolist.tasks.TaskLabels;
import io.spine.examples.todolist.tasks.command.AssignLabelToTask;
import io.spine.examples.todolist.tasks.command.CreateBasicLabel;
import io.spine.examples.todolist.tasks.command.CreateBasicTask;
import io.spine.examples.todolist.tasks.command.CreateDraft;
import io.spine.examples.todolist.tasks.command.FinalizeDraft;
import io.spine.examples.todolist.tasks.view.LabelView;
import io.spine.examples.todolist.tasks.view.TaskView;
import io.spine.grpc.MemoizingObserver;
import io.spine.grpc.StreamObservers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;
import static io.spine.examples.todolist.tasks.LabelColor.DEFAULT;
import static io.spine.examples.todolist.tasks.TaskStatus.DRAFT;
import static io.spine.examples.todolist.tasks.TaskStatus.FINALIZED;
import static io.spine.examples.todolist.tasks.TaskStatus.OPEN;
import static java.util.stream.Collectors.toList;

@DisplayName("Todo client should")
class ClientTest extends TodoClientTest {

    private SubscribingTodoClient client;

    @BeforeEach
    @Override
    void setUp() throws InterruptedException {
        super.setUp();
        client = client();
    }

    @Test
    @DisplayName("contain no entities when no commands are posted")
    void noEntities() {
        List<TaskView> taskViews = client.taskViews();
        List<TaskLabel> labels = client.labels();
        List<Task> tasks = client.tasks();

        List<?> entities = Stream.of(taskViews, labels, tasks)
                                 .flatMap(List::stream)
                                 .collect(toList());
        assertThat(entities)
                .isEmpty();
    }

    @Test
    @DisplayName("fallback to a provided label when querying for a non-existing label")
    void defaultLabelWhenNoLabel() {
        TaskLabel expectedLabel = TaskLabel
                .newBuilder()
                .setId(LabelId.generate())
                .setTitle("Chores")
                .vBuild();

        TaskLabel result = client.labelOr(LabelId.generate(), expectedLabel);
        assertThat(result)
                .isEqualTo(expectedLabel);
    }

    @Test
    @DisplayName("return an empty optional when querying for a non-existing label")
    void emptyOptionalWhenNoLabel() {
        Optional<LabelView> view = client.labelView(LabelId.generate());
        assertThat(view)
                .isEmpty();
    }

    @Test
    @DisplayName("obtain an empty `TaskLabels` when querying for labels of task with no labels")
    void taskWithoutLabels() {
        CreateBasicTask createTask = createBasicTask();
        client.postCommand(createTask);

        TaskId taskId = createTask.getId();
        TaskLabels labels = client.labelsOf(taskId);
        List<LabelId> idsList = labels.getLabelIdsList()
                                      .getIdsList();
        assertThat(idsList)
                .isEmpty();
    }

    @Test
    @DisplayName("obtain a list with task labels when querying for labels of task that has labels")
    void taskWithLabels() {
        CreateBasicTask createTask = createBasicTask();
        CreateBasicLabel createLabel = createBasicLabel();
        client.postCommand(createTask);
        client.postCommand(createLabel);

        TaskId taskId = createTask.getId();
        LabelId labelId = createLabel.getLabelId();

        AssignLabelToTask assignLabel = assignLabelToTask(taskId, labelId);
        client.postCommand(assignLabel);

        List<LabelId> labelIds = client.labelsOf(taskId)
                                       .getLabelIdsList()
                                       .getIdsList();
        assertThat(labelIds)
                .containsExactly(labelId);
    }

    @DisplayName("obtain a proper label view when querying by ID")
    @Test
    void obtainLabelView() {
        CreateBasicLabel createLabel = createBasicLabel();
        client.postCommand(createLabel);

        LabelId labelId = createLabel.getLabelId();
        LabelView expected = LabelView
                .newBuilder()
                .setId(labelId)
                .setTitle(createLabel.getLabelTitle())
                .setColor(DEFAULT)
                .vBuild();

        assertThat(client.labelView(labelId))
                .hasValue(expected);
    }

    @DisplayName("post a command and update the `TaskView` entity state ")
    @Test
    void postCommand() {
        CreateDraft createDraft = createDraft();
        TaskId taskId = createDraft.getId();
        client.postCommand(createDraft);

        assertThat(client.taskViews()).containsExactly(freshDraft(taskId));
    }

    @DisplayName("post a command and not deliver it to an entity if the ID did not match")
    @Test
    void postCommandWrongId() {
        CreateDraft createDraft = createDraft();
        client.postCommand(createDraft);

        TaskId taskId = createDraft.getId();

        FinalizeDraft finalizeDraft = finalizeDraft(TaskId.generate());
        client.postCommand(finalizeDraft);

        assertThat(client.taskViews())
                .containsExactly(freshDraft(taskId));
    }

    @DisplayName("change the state of the `TaskView` after a command has been posted")
    @Test
    void testCommandPostAffectsEntity() {
        CreateDraft createDraft = createDraft();
        client.postCommand(createDraft);

        TaskId taskId = createDraft.getId();

        FinalizeDraft finalizeDraft = finalizeDraft(taskId);
        client.postCommand(finalizeDraft);

        TaskView expected = freshDraft(taskId)
                .toBuilder()
                .setStatus(FINALIZED)
                .vBuild();

        assertThat(client.taskViews())
                .containsExactly(expected);
    }

    @DisplayName("subscribe to task views")
    @Test
    void receiveWorkingSubscription() throws InterruptedException {
        MemoizingObserver<TaskView> observer = StreamObservers.memoizingObserver();
        Subscription subscription = client.subscribeToTasks(observer);

        CreateBasicTask createTask = createBasicTask();
        client.postCommand(createTask);
        Thread.sleep(1_000);


        List<TaskView> responses = observer.responses();
        assertThat(responses).hasSize(1);

        TaskView taskView = responses.get(0);
        assertThat(taskView.getId())
                .isEqualTo(createTask.getId());
        assertThat(taskView.getStatus())
                .isEqualTo(OPEN);
        assertThat(taskView.getDescription())
                .isEqualTo(createTask.getDescription());

        client.unSubscribe(subscription);
    }

    /**
     * Obtains a {@code TaskView} that has the state that is expected after {@link CreateDraft}
     * command.
     */
    private static TaskView freshDraft(TaskId taskId) {
        return TaskView
                .newBuilder()
                .setId(taskId)
                .setStatus(DRAFT)
                .vBuild();
    }
}
