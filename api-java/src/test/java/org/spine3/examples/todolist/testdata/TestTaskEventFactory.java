package org.spine3.examples.todolist.testdata;

import com.google.protobuf.Timestamp;
import org.spine3.examples.todolist.DeletedTaskRestored;
import org.spine3.examples.todolist.LabelAssignedToTask;
import org.spine3.examples.todolist.LabelRemovedFromTask;
import org.spine3.examples.todolist.TaskCompleted;
import org.spine3.examples.todolist.TaskCreated;
import org.spine3.examples.todolist.TaskDeleted;
import org.spine3.examples.todolist.TaskDescriptionUpdated;
import org.spine3.examples.todolist.TaskDetails;
import org.spine3.examples.todolist.TaskDraftCreated;
import org.spine3.examples.todolist.TaskDraftFinalized;
import org.spine3.examples.todolist.TaskDueDateUpdated;
import org.spine3.examples.todolist.TaskLabelId;
import org.spine3.examples.todolist.TaskPriority;
import org.spine3.examples.todolist.TaskPriorityUpdated;
import org.spine3.examples.todolist.TaskReopened;
import org.spine3.protobuf.Timestamps;

import static org.spine3.base.Identifiers.newUuid;

/**
 * Provides methods for instantiation task events for test needs.
 *
 * @author Illia Shepilov
 */
public class TestTaskEventFactory {

    public static final String DESCRIPTION = "task description";
    public static final TaskPriority TASK_PRIORITY = TaskPriority.NORMAL;
    public static final Timestamp TASK_DUE_DATE = Timestamps.getCurrentTime();
    public static final TaskLabelId LABEL_ID = TaskLabelId.newBuilder()
                                                          .setValue(newUuid())
                                                          .build();
    private static final Timestamp CREATION_TIME = Timestamps.getCurrentTime();

    public static TaskCreated taskCreatedInstance() {
        return taskCreatedInstance(DESCRIPTION, TASK_PRIORITY);
    }

    public static TaskCreated taskCreatedInstance(String description, TaskPriority priority) {
        return TaskCreated.newBuilder()
                          .setDetails(TaskDetails.newBuilder()
                                                 .setDescription(description)
                                                 .setPriority(priority))
                          .build();
    }

    public static TaskDraftCreated taskDraftCreatedInstance() {
        return taskDraftCreatedInstance(DESCRIPTION, TASK_PRIORITY, CREATION_TIME);
    }

    public static TaskDraftCreated taskDraftCreatedInstance(String description, TaskPriority priority, Timestamp creationTime) {
        return TaskDraftCreated.newBuilder()
                               .setDetails(TaskDetails.newBuilder()
                                                      .setPriority(priority)
                                                      .setDescription(description))
                               .setDraftCreationTime(creationTime)
                               .build();
    }

    public static TaskDescriptionUpdated taskDescriptionUpdatedInstance() {
        return taskDescriptionUpdatedInstance(DESCRIPTION);
    }

    public static TaskDescriptionUpdated taskDescriptionUpdatedInstance(String description) {
        return TaskDescriptionUpdated.newBuilder()
                                     .setNewDescription(description)
                                     .build();
    }

    public static TaskPriorityUpdated taskPriorityUpdatedInstance() {
        return taskPriorityUpdatedInstance(TASK_PRIORITY);
    }

    public static TaskPriorityUpdated taskPriorityUpdatedInstance(TaskPriority priority) {
        return TaskPriorityUpdated.newBuilder()
                                  .setNewPriority(priority)
                                  .build();
    }

    public static TaskDueDateUpdated taskDueDateUpdatedInstance() {
        return taskDueDateUpdatedInstance(TASK_DUE_DATE);
    }

    public static TaskDueDateUpdated taskDueDateUpdatedInstance(Timestamp dueDate) {
        return TaskDueDateUpdated.newBuilder()
                                 .setNewDueDate(dueDate)
                                 .build();
    }

    public static TaskDraftFinalized taskDraftFinalizedInstance() {
        return TaskDraftFinalized.getDefaultInstance();
    }

    public static TaskCompleted taskCompletedInstance() {
        return TaskCompleted.getDefaultInstance();
    }

    public static TaskReopened taskReopenedInstance() {
        return TaskReopened.getDefaultInstance();
    }

    public static TaskDeleted taskDeletedInstance() {
        return TaskDeleted.getDefaultInstance();
    }

    public static DeletedTaskRestored deletedTaskRestoredInstance() {
        return DeletedTaskRestored.getDefaultInstance();
    }

    public static LabelAssignedToTask labelAssignedToTaskInstance() {
        return labelAssignedToTaskInstance(LABEL_ID);
    }

    public static LabelAssignedToTask labelAssignedToTaskInstance(TaskLabelId labelId) {
        return LabelAssignedToTask.newBuilder()
                                  .setLabelId(labelId)
                                  .build();
    }

    public static LabelRemovedFromTask labelRemovedFromTaskInstance() {
        return LabelRemovedFromTask.newBuilder()
                                   .setLabelId(LABEL_ID)
                                   .build();
    }

}
