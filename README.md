# Todo List example application

This repository contains an example project of a Todo List server application with 
multiple client applications.

## Server

The server application consists of a single TodoList Bounded Context, describing a few scenarios
of working with tasks.

### Deployment

Several options of server deployment are showcased.

- Server running at Google App Engine with data stored in Cloud Datastore ([details](https://github.com/spine-examples/todo-list/tree/master/deployment/appengine-web)).
- Server running at Google Compute Engine using Cloud SQL as a storage([details](https://github.com/spine-examples/todo-list/tree/master/deployment/compute-cloud-sql)).
- Standalone server with Cloud SQL as a storage ([details](https://github.com/spine-examples/todo-list/tree/master/deployment/local-cloud-sql)).
- Standalone server using a standalone MySQL database for storage ([see](https://github.com/spine-examples/todo-list/tree/master/deployment/local-my-sql)).

Also, two options aimed for development or local tests:

- Standalone server with in-memory data storage with the subscription support through Cloud Firestore. 
It's handy to test remote clients such as 3rd-party systems or mobile devices. See [this Gradle module](https://github.com/spine-examples/todo-list/tree/master/deployment/local-firebase).
- Standalone server with in-memory data storage and gRPC interface only ([details](https://github.com/spine-examples/todo-list/tree/master/deployment/local-inmem)).  


## Clients

There are three client applications available as examples.

- [Angular-based web application](https://github.com/spine-examples/todo-list/tree/master/client-angular).
- [Vanilla JS web application](https://github.com/spine-examples/todo-list/tree/master/client-web).
- [Console application](https://github.com/spine-examples/todo-list/tree/master/client-cli). 
