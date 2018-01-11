# todo-list

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/39e3e7d9707f4da58c950c3dbf172cfd)](https://www.codacy.com/app/SpineEventEngine/todo-list?utm_source=github.com&utm_medium=referral&utm_content=SpineEventEngine/todo-list&utm_campaign=badger)

ToDo List example application

---

# Guide

## Step 2

In this step we create the first projection in the system.

### Model

The projection `MyListView` represents a list of active (finalized, non-deleted, non-completed) 
tasks.

The projection state is defined in [`projections.proto`](./model/src/main/proto/todolist/q/projections.proto)
file.

The projection has no own commands/events, but uses the events produces by `Task` aggregate instead.

### Entities
 
The [`MyListViewProjection`](./api-java/src/main/java/io/spine/examples/todolist/q/projection/MyListViewProjection.java)
class defines the event handlers, which build the projection state.

Note that projections use [`@Subscribe`](https://spine.io/core-java/javadoc/core/io/spine/core/Subscribe.html)
annotation for the event handler methods instead of [`@Apply`](https://spine.io/core-java/javadoc/server/io/spine/server/aggregate/Apply.html),
which is specific to aggregates only.

### Enrichments

The event enrichments is a mechanism of binding extra data to an event. To create an enrichment,
define the enrichment type in Protobuf and create an instance of [`EventEnricher`](https://spine.io/core-java/javadoc/server/io/spine/server/event/EventEnricher.html)
and pass it to `EventBus` on creation.

In TodoList, we enrich `TaskDraftFinalized` event with the state of the `Task` repository.
The enrichment is defined in [`enrichments.proto`](./model/src/main/proto/todolist/c/enrichments.proto).
[`TodoListEnrichments`](./api-java/src/main/java/io/spine/examples/todolist/context/TodoListEnrichments.java)
creates instances of the `EventEnricher`, which maps the ID of the `Task` which produced the given 
event to the `Task` state.

Later, the enrichment is used by the `MyListViewProjection` when handling `TaskDraftFinalized` 
event.

### Tests

When testing projections, we adhere the same pattern as when testing aggregates — create separate 
tests for each event type and check the effect of a single event on the projection state.

The event enrichments are mostly tested when performing the projection tests, like in the case of
TodoList, but sometimes, it's better to test the functions which create the enrichments.

---

[Step 1](https://github.com/SpineEventEngine/todo-list/tree/step-1) | [Step 3](https://github.com/SpineEventEngine/todo-list/tree/step-3)
