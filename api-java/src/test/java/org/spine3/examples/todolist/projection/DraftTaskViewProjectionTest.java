package org.spine3.examples.todolist.projection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.spine3.examples.todolist.TaskDeleted;
import org.spine3.examples.todolist.TaskDraftCreated;
import org.spine3.examples.todolist.TaskDraftFinalized;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.view.TaskView;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.spine3.base.Identifiers.newUuid;
import static org.spine3.examples.todolist.testdata.TestEventFactory.taskDeletedInstance;
import static org.spine3.examples.todolist.testdata.TestEventFactory.taskDraftCreatedInstance;
import static org.spine3.examples.todolist.testdata.TestEventFactory.taskDraftFinalizedInstance;

/**
 * @author Illia Shepilov
 */
class DraftTaskViewProjectionTest {

    private DraftTaskViewProjection projection;
    private TaskDraftFinalized taskDraftFinalizedEvent;
    private TaskDraftCreated taskDraftCreatedEvent;
    private TaskDeleted taskDeletedEvent;
    private TaskId ID = TaskId.newBuilder()
                              .setValue(newUuid())
                              .build();

    @BeforeEach
    public void setUp() {
        projection = new DraftTaskViewProjection(ID);
        taskDraftFinalizedEvent = taskDraftFinalizedInstance();
        taskDeletedEvent = taskDeletedInstance();
        taskDraftCreatedEvent = taskDraftCreatedInstance();
    }

    @Test
    public void return_state_when_handle_task_draft_finalized_event() {
        int expectedListSize = 0;
        projection.on(taskDraftCreatedEvent);
        projection.on(taskDraftFinalizedEvent);

        final List<TaskView> views = projection.getState()
                                               .getDraftTasks()
                                               .getItemsList();
        assertEquals(expectedListSize, views.size());
    }

    @Test
    public void return_state_when_handle_task_deleted_event() {
        int expectedListSize = 0;
        projection.on(taskDraftCreatedEvent);
        projection.on(taskDeletedEvent);

        final List<TaskView> views = projection.getState()
                                               .getDraftTasks()
                                               .getItemsList();
        assertEquals(expectedListSize, views.size());
    }

    @Test
    public void return_state_when_handle_task_draft_created() {
        int expectedListSize = 1;
        projection.on(taskDraftCreatedEvent);

        final List<TaskView> views = projection.getState()
                                               .getDraftTasks()
                                               .getItemsList();
        assertEquals(expectedListSize, views.size());
    }
}
