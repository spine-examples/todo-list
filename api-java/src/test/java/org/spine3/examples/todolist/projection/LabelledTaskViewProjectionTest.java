package org.spine3.examples.todolist.projection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.spine3.examples.todolist.DeletedTaskRestored;
import org.spine3.examples.todolist.LabelAssignedToTask;
import org.spine3.examples.todolist.LabelRemovedFromTask;
import org.spine3.examples.todolist.TaskDeleted;
import org.spine3.examples.todolist.TaskLabelId;
import org.spine3.examples.todolist.view.TaskView;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.spine3.base.Identifiers.newUuid;
import static org.spine3.examples.todolist.testdata.TestEventFactory.deletedTaskRestoredInstance;
import static org.spine3.examples.todolist.testdata.TestEventFactory.labelAssignedToTaskInstance;
import static org.spine3.examples.todolist.testdata.TestEventFactory.labelRemovedFromTaskInstance;
import static org.spine3.examples.todolist.testdata.TestEventFactory.taskDeletedInstance;

/**
 * @author Illia Shepilov
 */
class LabelledTaskViewProjectionTest {

    private LabelledTaskViewProjection projection;
    private DeletedTaskRestored deletedTaskRestoredEvent;
    private LabelRemovedFromTask labelRemovedFromTaskEvent;
    private LabelAssignedToTask labelAssignedToTaskEvent;
    private TaskDeleted taskDeletedEvent;
    private TaskLabelId ID = TaskLabelId.newBuilder()
                                        .setValue(newUuid())
                                        .build();

    @BeforeEach
    public void setUp() {
        projection = new LabelledTaskViewProjection(ID);
        deletedTaskRestoredEvent = deletedTaskRestoredInstance();
        labelAssignedToTaskEvent = labelAssignedToTaskInstance();
        labelRemovedFromTaskEvent = labelRemovedFromTaskInstance();
        taskDeletedEvent = taskDeletedInstance();
    }

    @Test
    public void return_state_when_handle_label_removed_from_task_event() {
        int expectedListSize = 0;
        projection.on(labelAssignedToTaskEvent);
        projection.on(labelRemovedFromTaskEvent);

        final List<TaskView> views = projection.getState()
                                               .getLabelledTasks()
                                               .getItemsList();
        assertEquals(expectedListSize, views.size());
    }

    @Test
    public void return_state_when_handle_deleted_task_restored_event() {
        int expectedListSize = 1;
        projection.on(labelAssignedToTaskEvent);
        projection.on(taskDeletedEvent);
        projection.on(deletedTaskRestoredEvent);

        final List<TaskView> views = projection.getState()
                                               .getLabelledTasks()
                                               .getItemsList();
        assertEquals(expectedListSize, views.size());
    }

    @Test
    public void return_state_when_handle_label_assigned_to_task_event() {
        int expectedListSize = 1;
        projection.on(labelAssignedToTaskEvent);

        final List<TaskView> views = projection.getState()
                                               .getLabelledTasks()
                                               .getItemsList();
        assertEquals(expectedListSize, views.size());
    }

    @Test
    public void return_state_when_handle_task_deleted_event() {
        int expectedListSize = 0;
        projection.on(labelAssignedToTaskEvent);
        projection.on(taskDeletedEvent);

        final List<TaskView> views = projection.getState()
                                               .getLabelledTasks()
                                               .getItemsList();
        assertEquals(expectedListSize, views.size());
    }
}
