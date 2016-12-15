package org.spine3.examples.todolist.server;

import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.aggregate.TaskAggregate;
import org.spine3.server.BoundedContext;
import org.spine3.server.aggregate.AggregateRepository;

/**
 * @author Illia Shepilov
 */
public class TaskRepository extends AggregateRepository<TaskId, TaskAggregate> {

    /**
     * Creates a new repository instance.
     *
     * @param boundedContext the bounded context to which this repository belongs
     */
    public TaskRepository(BoundedContext boundedContext) {
        super(boundedContext);
    }
}
