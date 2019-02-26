/*
 * Copyright 2018, TeamDev. All rights reserved.
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

package io.spine.examples.todolist.context;

import com.google.protobuf.Any;
import io.spine.base.EventMessage;
import io.spine.core.Enrichment;
import io.spine.core.Event;
import io.spine.core.Versions;
import io.spine.examples.todolist.LabelId;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.c.enrichments.DetailsEnrichment;
import io.spine.examples.todolist.c.enrichments.LabelsListEnrichment;
import io.spine.examples.todolist.c.enrichments.TaskEnrichment;
import io.spine.examples.todolist.c.events.LabelledTaskRestored;
import io.spine.examples.todolist.c.events.TaskDraftFinalized;
import io.spine.examples.todolist.repository.LabelAggregateRepository;
import io.spine.examples.todolist.repository.TaskLabelsRepository;
import io.spine.examples.todolist.repository.TaskRepository;
import io.spine.server.enrich.Enricher;
import io.spine.server.event.EventFactory;
import io.spine.server.type.EventEnvelope;
import io.spine.type.TypeName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static io.spine.base.Identifier.newUuid;
import static io.spine.protobuf.AnyPacker.unpack;
import static io.spine.testing.server.TestEventFactory.newInstance;
import static io.spine.validate.Validate.isDefault;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("TodoListEnrichments should")
class TodoListEnrichmentsTest {

    private static final EventFactory events = newInstance(TodoListEnrichmentsTest.class);

    private Enricher enricher;

    @BeforeEach
    void setUp() {
        final TaskRepository taskRepo = mock(TaskRepository.class);
        final LabelAggregateRepository labelRepo = mock(LabelAggregateRepository.class);
        final TaskLabelsRepository taskLabelsRepo = mock(TaskLabelsRepository.class);
        when(taskRepo.find(any(TaskId.class))).thenReturn(Optional.empty());
        when(labelRepo.find(any(LabelId.class))).thenReturn(Optional.empty());
        when(taskLabelsRepo.find(any(TaskId.class))).thenReturn(Optional.empty());
        enricher = TodoListEnrichments.newBuilder()
                                      .setTaskRepository(taskRepo)
                                      .setLabelRepository(labelRepo)
                                      .setTaskLabelsRepository(taskLabelsRepo)
                                      .build()
                                      .createEnricher();
    }

    @Test
    @DisplayName("create EventEnricher that defaults absent Task or TaskLabels to default message")
    void enricherDefaultsTest() {
        final TaskDraftFinalized eventMsg = TaskDraftFinalized.newBuilder()
                                                              .setTaskId(randomTaskId())
                                                              .build();
        final EventEnvelope envelope = enricher.enrich(EventEnvelope.of(event(eventMsg)));
        final EventEnvelope enriched = enricher.enrich(envelope);
        final Enrichment enrichment = enriched.context()
                                              .getEnrichment();

        final TypeName labelsEnrName = TypeName.from(LabelsListEnrichment.getDescriptor());
        final Any labelIds = enrichment.getContainer()
                                       .getItemsMap()
                                       .get(labelsEnrName.value());
        final LabelsListEnrichment labelIdsEnr = (LabelsListEnrichment) unpack(labelIds);
        assertTrue(labelIdsEnr.getLabelIdsList()
                              .getIdsList()
                              .isEmpty());

        final TypeName taskTypeName = TypeName.from(TaskEnrichment.getDescriptor());
        final Any task = enrichment.getContainer()
                                   .getItemsMap()
                                   .get(taskTypeName.value());
        final TaskEnrichment taskEnr = (TaskEnrichment) unpack(task);
        assertTrue(isDefault(taskEnr.getTask()));
    }

    @Test
    @DisplayName("create EventEnricher that defaults absent Label to default message")
    void moreEnricherDefaultsTest() {
        final LabelledTaskRestored eventMsg = LabelledTaskRestored.newBuilder()
                                                                  .setLabelId(randomLabelId())
                                                                  .build();
        final EventEnvelope envelope = enricher.enrich(EventEnvelope.of(event(eventMsg)));
        final EventEnvelope enriched = enricher.enrich(envelope);
        final Enrichment enrichment = enriched.context()
                                              .getEnrichment();

        final TypeName enrTypeName = TypeName.from(DetailsEnrichment.getDescriptor());
        final Any packerEnr = enrichment.getContainer()
                                        .getItemsMap()
                                        .get(enrTypeName.value());
        final DetailsEnrichment enr = (DetailsEnrichment) unpack(packerEnr);
        assertTrue(isDefault(enr.getLabelDetails()));
        assertTrue(isDefault(enr.getTaskDetails()));
    }

    private static Event event(EventMessage msg) {
        return events.createEvent(msg, Versions.zero());
    }

    private static TaskId randomTaskId() {
        return TaskId.newBuilder()
                     .setValue(newUuid())
                     .build();
    }

    private static LabelId randomLabelId() {
        return LabelId.newBuilder()
                      .setValue(newUuid())
                      .build();
    }
}
