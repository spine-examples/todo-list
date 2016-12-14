package org.spine3.examples.todolist.projection;

import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.TaskLabelId;
import org.spine3.examples.todolist.view.TaskListView;
import org.spine3.examples.todolist.view.TaskView;

import java.util.List;

/**
 * Class provides helpful methods for processing projections.
 *
 * @author Illia Shepilov
 */
class ProjectionHelper {

    /**
     * Removes {@link TaskView} from list of task view by specified task's id.
     *
     * @param views list of the {@link TaskView}
     * @param id    task's id
     * @return {@link TaskListView} without deleted task view
     */
    /* package */
    static TaskListView removeViewByTaskId(List<TaskView> views, TaskId id) {
        final TaskView taskView = views.stream()
                                       .filter(t -> t.getId()
                                                     .equals(id))
                                       .findFirst()
                                       .orElse(null);
        views.remove(taskView);
        return TaskListView.newBuilder()
                           .addAllItems(views)
                           .build();
    }

    /**
     * Removes {@link TaskView} from list of task view by specified task's label id.
     *
     * @param views list of the {@link TaskView}
     * @param id    task's label id
     * @return {@link TaskListView} without deleted task view
     */
    /*package*/
    static TaskListView removeViewByLabelId(List<TaskView> views, TaskLabelId id) {
        final TaskView taskView = views.stream()
                                       .filter(t -> t.getLabelId()
                                                     .equals(id))
                                       .findFirst()
                                       .orElse(null);
        views.remove(taskView);
        return TaskListView.newBuilder()
                           .addAllItems(views)
                           .build();
    }
}
