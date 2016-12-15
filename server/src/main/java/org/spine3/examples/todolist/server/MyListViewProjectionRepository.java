package org.spine3.examples.todolist.server;

import org.spine3.examples.todolist.TaskListId;
import org.spine3.examples.todolist.projection.MyListViewProjection;
import org.spine3.examples.todolist.view.MyListView;
import org.spine3.server.BoundedContext;
import org.spine3.server.projection.ProjectionRepository;

/**
 * @author Illia Shepilov
 */
public class MyListViewProjectionRepository extends ProjectionRepository<TaskListId, MyListViewProjection, MyListView> {

    protected MyListViewProjectionRepository(BoundedContext boundedContext) {
        super(boundedContext);
    }
}
