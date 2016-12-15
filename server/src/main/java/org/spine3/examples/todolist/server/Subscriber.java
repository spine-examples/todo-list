package org.spine3.examples.todolist.server;

import org.spine3.examples.todolist.TaskCreated;
import org.spine3.server.event.EventSubscriber;
import org.spine3.server.event.Subscribe;

/**
 * @author Illia Shepilov
 */
public class Subscriber extends EventSubscriber {

    @Subscribe
    public void on(TaskCreated event) {
        System.out.println("Task created");
    }
}
