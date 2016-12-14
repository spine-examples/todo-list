package org.spine3.examples.todolist.projection;

import org.spine3.examples.todolist.DeletedTaskRestored;
import org.spine3.examples.todolist.LabelAssignedToTask;
import org.spine3.examples.todolist.LabelColor;
import org.spine3.examples.todolist.LabelRemovedFromTask;
import org.spine3.examples.todolist.TaskDeleted;
import org.spine3.examples.todolist.TaskLabelId;
import org.spine3.examples.todolist.view.LabelledTasksView;
import org.spine3.examples.todolist.view.TaskListView;
import org.spine3.examples.todolist.view.TaskView;
import org.spine3.server.event.Subscribe;
import org.spine3.server.projection.Projection;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.spine3.examples.todolist.projection.ProjectionHelper.removeViewByLabelId;
import static org.spine3.examples.todolist.projection.ProjectionHelper.removeViewByTaskId;

/**
 * A projection state of the created tasks marked with a certain label.
 * <p>
 * <p> Contains the data about the task view.
 * <p>
 * <p> This view includes all tasks per label that are neither in a draft state nor deleted.
 *
 * @author Illia Shepilov
 */
public class LabeledTaskViewProjection extends Projection<TaskLabelId, LabelledTasksView> {

    /**
     * Creates a new instance.
     *
     * @param id the ID for the new instance
     * @throws IllegalArgumentException if the ID is not of one of the supported types
     */
    public LabeledTaskViewProjection(TaskLabelId id) {
        super(id);
    }

    @Subscribe
    public void on(LabelAssignedToTask event) {
        final TaskView taskView = TaskView.newBuilder()
                                          .setId(event.getId())
                                          .setLabelId(event.getLabelId())
                                          .build();
        final LabelledTasksView state = addLabel(taskView);
        incrementState(state);
    }

    @Subscribe
    public void on(DeletedTaskRestored event) {
        final TaskView taskView = TaskView.newBuilder()
                                          .setId(event.getId())
                                          .build();
        final LabelledTasksView state = addLabel(taskView);
        incrementState(state);
    }

    @Subscribe
    public void on(LabelRemovedFromTask event) {
        final List<TaskView> views = getState().getLabelledTasks()
                                               .getItemsList()
                                               .stream()
                                               .collect(Collectors.toList());
        final TaskListView taskListView = removeViewByLabelId(views, event.getLabelId());
        final LabelledTasksView state = getState().newBuilderForType()
                                                  .setLabelledTasks(taskListView)
                                                  .build();
        incrementState(state);
    }

    @Subscribe
    public void on(TaskDeleted event) {
        final List<TaskView> views = getState().getLabelledTasks()
                                               .getItemsList()
                                               .stream()
                                               .collect(Collectors.toList());
        final TaskListView taskListView = removeViewByTaskId(views, event.getId());
        final LabelledTasksView state = getState().newBuilderForType()
                                                  .setLabelledTasks(taskListView)
                                                  .build();
        incrementState(state);
    }

    private LabelledTasksView addLabel(TaskView taskView) {
        final List<TaskView> views = getState().getLabelledTasks()
                                               .getItemsList()
                                               .stream()
                                               .collect(Collectors.toList());
        views.add(taskView);
        final TaskListView taskListView = TaskListView.newBuilder()
                                                      .addAllItems(views)
                                                      .build();
        return getState().newBuilderForType()
                         .setLabelledTasks(taskListView)
                         .build();
    }

    private LabelColor transformToLabelColorFromHex(String hexColor) {
        final Color color = Color.decode(hexColor);
        final int blue = color.getBlue();
        final int green = color.getGreen();
        final int red = color.getRed();

        if (red == green && green == blue) {
            return LabelColor.GRAY;
        }

        int maxValue = Collections.max(Arrays.asList(red, blue, green));

        if (maxValue == blue) {
            return LabelColor.BLUE;
        }

        if (maxValue == green) {
            return LabelColor.GREEN;
        }

        return LabelColor.RED;
    }
}
