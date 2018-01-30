# todo-list

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/39e3e7d9707f4da58c950c3dbf172cfd)](https://www.codacy.com/app/SpineEventEngine/todo-list?utm_source=github.com&utm_medium=referral&utm_content=SpineEventEngine/todo-list&utm_campaign=badger)

ToDo List example application

---

# Guide

## Step 4

In this step we add following components to the system:
 - `Label` aggregate;
 - `Label`-related projections.
 
Also, the `Task` aggregate is now split into several `AggregatePart`s.

Finally, this step includes integration tests for the system.

### `Label` aggregate

`Label` is the second aggregate in the system. A label is a mark, which could be assigned to a task.

The state of the aggregate is defined by the [`TaskLabel`](./model/src/main/proto/todolist/model.proto) message type.

The [`LabelAggregate`](./api-java/src/main/java/io/spine/examples/todolist/c/aggregate/LabelAggregate.java)
is a good example of how to introduce custom default values for some fields of an entity state (in 
particular, the `TaskLabel.color` field).

### New projections

[`DraftTasksViewProjection`](./api-java/src/main/java/io/spine/examples/todolist/q/projection/DraftTasksViewProjection.java) and
[`LabelledTasksViewProjection`](./api-java/src/main/java/io/spine/examples/todolist/q/projection/LabelledTasksViewProjection.java)
are introduced into the system.

The `DraftTasksViewProjection` represents a list of all non-finalized tasks in the system.
As well as the `MyListView` projection, the draft tasks view is singleton.

The `LabelledTasksViewProjection` represents a list of tasks marked with a given label. 
This projection type may have several instances.

Both added projections handle events produced by both `Task` and `Label` aggregates.

### `Task` aggregate reformat

From now, the the `Task` aggregate is split into two parts:
 - [`TaskPart`](./api-java/src/main/java/io/spine/examples/todolist/c/aggregate/TaskPart.java) 
 represents the information about the task itself (e.g. description, due date, etc.);
 - [`TaskLabelsPart`](./api-java/src/main/java/io/spine/examples/todolist/c/aggregate/TaskLabelsPart.java) 
  represents the information about labels assigned to the task.
The parts are joined with the [`TaskAggregateRoot`](./api-java/src/main/java/io/spine/examples/todolist/c/aggregate/TaskAggregateRoot.java).

Each of the parts and the root has a corresponding repository registered in the TodoList bounded
context.

This separation is performed with the purpose of isolating loosely coupled elements of
the aggregate. This may be useful e.g. when a client wants to fetch only a subset of all the data.

It is in general a good practice to split huge aggregates into several parts. Though, be aware of
eventual consistency when integrating the parts.

### Integration tests

The integration tests for the system are located in the [`integration-tests`](./integration-tests) 
module. See the [notes](./integration-tests/README.md) for more details on the tests organization 
and launching.

---

[Step 3](https://github.com/SpineEventEngine/todo-list/tree/step-3) | [Step 5](https://github.com/SpineEventEngine/todo-list/tree/step-5)
