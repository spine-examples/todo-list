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

package org.spine3.examples.todolist.context;

import org.spine3.examples.todolist.repositories.LabelAggregateRepository;
import org.spine3.examples.todolist.repositories.TaskDefinitionRepository;
import org.spine3.examples.todolist.repositories.TaskLabelsRepository;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Serves as a repository provider for all the application components.
 *
 * @author Illia Shepilov
 */
class TodoListRepositoryProvider {

    @Nullable
    private TaskLabelsRepository taskLabelsRepo;
    @Nullable
    private TaskDefinitionRepository taskDefinitionRepo;
    @Nullable
    private LabelAggregateRepository labelRepository;

    void setTaskLabelsRepository(TaskLabelsRepository taskLabelsRepo) {
        this.taskLabelsRepo = taskLabelsRepo;
    }

    void setTaskDefinitionRepository(TaskDefinitionRepository labelAggregateRepo) {
        this.taskDefinitionRepo = labelAggregateRepo;
    }

    void setLabelRepository(LabelAggregateRepository labelRepository) {
        this.labelRepository = labelRepository;
    }

    /**
     * Returns the wrapped {@link TaskLabelsRepository} in the {@code Optional}.
     *
     * @return the {@code Optional}
     */
    Optional<TaskLabelsRepository> getTaskLabelsRepo() {
        final Optional<TaskLabelsRepository> result = Optional.ofNullable(this.taskLabelsRepo);
        return result;
    }

    /**
     * Returns the wrapped {@link TaskDefinitionRepository} in the {@code Optional}.
     *
     * @return the {@code Optional}
     */
    Optional<TaskDefinitionRepository> getTaskDefinitionRepo() {
        final Optional<TaskDefinitionRepository> result = Optional.ofNullable(this.taskDefinitionRepo);
        return result;
    }

    Optional<LabelAggregateRepository> getLabelRepository() {
        final Optional<LabelAggregateRepository> result = Optional.ofNullable(this.labelRepository);
        return result;
    }
}
