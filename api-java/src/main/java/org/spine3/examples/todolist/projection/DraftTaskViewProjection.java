package org.spine3.examples.todolist.projection;

import org.spine3.examples.todolist.TaskDeleted;
import org.spine3.examples.todolist.TaskDetails;
import org.spine3.examples.todolist.TaskDraftCreated;
import org.spine3.examples.todolist.TaskDraftFinalized;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.view.DraftTasksView;
import org.spine3.examples.todolist.view.TaskListView;
import org.spine3.examples.todolist.view.TaskView;
import org.spine3.server.event.Subscribe;
import org.spine3.server.projection.Projection;

import java.util.List;
import java.util.stream.Collectors;

import static org.spine3.examples.todolist.projection.ProjectionHelper.removeViewByTaskId;

/**
 * A projection state of tasks in a "draft" state.
 * <p>
 * <p> Contains the data about the task draft task view.
 * <p>
 * <p> This view includes all non-deleted tasks, which creation is in progress.
 *
 * @author Illia Shepilov
 */
public class DraftTaskViewProjection extends Projection<TaskId, DraftTasksView> {
    /**
     * Creates a new instance.
     *
     * @param id the ID for the new instance
     * @throws IllegalArgumentException if the ID is not of one of the supported types
     */
    public DraftTaskViewProjection(TaskId id) {
        super(id);
    }

    @Subscribe
    public void on(TaskDraftCreated event) {
        final TaskDetails taskDetails = event.getDetails();
        final TaskView taskView = TaskView.newBuilder()
                                          .setId(event.getId())
                                          .setDescription(taskDetails.getDescription())
                                          .setPriority(taskDetails.getPriority())
                                          .setCompleted(taskDetails.getCompleted())
                                          .build();
        final List<TaskView> views = getState().getDraftTasks()
                                               .getItemsList()
                                               .stream()
                                               .collect(Collectors.toList());
        views.add(taskView);
        final TaskListView taskListView = TaskListView.newBuilder()
                                                      .addAllItems(views)
                                                      .build();
        final DraftTasksView state = getState().newBuilderForType()
                                               .setDraftTasks(taskListView)
                                               .build();
        incrementState(state);
    }

    @Subscribe
    public void on(TaskDraftFinalized event) {
        final List<TaskView> views = getState().getDraftTasks()
                                               .getItemsList()
                                               .stream()
                                               .collect(Collectors.toList());
        final TaskListView taskListView = removeViewByTaskId(views, event.getId());
        final DraftTasksView state = getState().newBuilderForType()
                                               .setDraftTasks(taskListView)
                                               .build();
        incrementState(state);
    }

    @Subscribe
    public void on(TaskDeleted event) {
        final List<TaskView> views = getState().getDraftTasks()
                                               .getItemsList()
                                               .stream()
                                               .collect(Collectors.toList());
        final TaskListView taskListView = removeViewByTaskId(views, event.getId());
        final DraftTasksView state = getState().newBuilderForType()
                                               .setDraftTasks(taskListView)
                                               .build();
        incrementState(state);
    }
}
