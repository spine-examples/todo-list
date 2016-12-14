package org.spine3.examples.todolist.projection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.spine3.examples.todolist.TaskCreated;
import org.spine3.examples.todolist.TaskDeleted;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.view.TaskView;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.spine3.base.Identifiers.newUuid;
import static org.spine3.examples.todolist.testdata.TestEventFactory.taskCreatedInstance;
import static org.spine3.examples.todolist.testdata.TestEventFactory.taskDeletedInstance;

/**
 * @author Illia Shepilov
 */
class MyListViewProjectionTest {

    private MyListViewProjection projection;
    private TaskCreated taskCreatedEvent;
    private TaskDeleted taskDeletedEvent;
    private TaskId ID = TaskId.newBuilder()
                              .setValue(newUuid())
                              .build();

    @BeforeEach
    void setUp() {
        projection = new MyListViewProjection(ID);
        taskCreatedEvent = taskCreatedInstance();
        taskDeletedEvent = taskDeletedInstance();
    }

    @Test
    public void return_state_when_handle_task_created_event() {
        int expectedSize = 1;
        projection.on(taskCreatedEvent);

        final List<TaskView> views = projection.getState()
                                               .getMyList()
                                               .getItemsList();
        assertEquals(expectedSize, views.size());
    }

    @Test
    public void return_state_when_handle_task_created_and_deleted_event() {
        int expectedListSize = 1;
        projection.on(taskCreatedEvent);
        projection.on(taskCreatedEvent);
        projection.on(taskDeletedEvent);

        List<TaskView> views = projection.getState()
                                         .getMyList()
                                         .getItemsList();
        assertEquals(expectedListSize, views.size());

        projection.on(taskDeletedEvent);
        expectedListSize = 0;
        views = projection.getState()
                          .getMyList()
                          .getItemsList();

        assertEquals(expectedListSize, views.size());
    }
}
