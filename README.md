# todo-list

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/39e3e7d9707f4da58c950c3dbf172cfd)](https://www.codacy.com/app/SpineEventEngine/todo-list?utm_source=github.com&utm_medium=referral&utm_content=SpineEventEngine/todo-list&utm_campaign=badger)

ToDo List example application

---

# Guide

## Step 2

In this step we create the system client library and the CLI client.

### TodoList Client

The client is represented with a wrapper upon `Command`, `Query`, and `Subscription` gRPC service
stubs. The interface of the client is defined in [`TodoClient`](./client/src/main/java/io/spine/examples/todolist/client/TodoClient.java)
implemented in [`TodoClientImpl`](./client/src/main/java/io/spine/examples/todolist/client/TodoClientImpl.java).

The client also has an extension in form of the [`SubscriptableClient`](./client/src/main/java/io/spine/examples/todolist/client/SubscribingTodoClient.java),
which is capable of subscribing to the entity state updates (using `SubscriptionService`).

### CLI client

The first TodoList client, that works in command line.

The client is capable of creating and viewing `Task`s.

See [`ClientApp`](./client-cli/src/main/java/io/spine/examples/todolist/ClientApp.java) class
for the instructions of how to start the client.

By default, the client connects to the local gRPC server, e.g. `local-in-mem` server.

---

[Step 2](https://github.com/SpineEventEngine/todo-list/tree/step-2) | [Step 4](https://github.com/SpineEventEngine/todo-list/tree/step-4)
