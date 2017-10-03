# stress-test

A module which contains integration and performance tests.

### Storage types

##### In-memory storage

By default integration tests and performance tests, running with in-memory storage and does not 
require additional settings.

##### JDBC storage

To enable JDBC storage you should pass into test parameter `-Dstorage.type=jdbc`.
Configuration for JDBC(username, password, db name) storing in `jdbc-storage.properties`, which you 
can find in resources.

### Supported storage types

* `in-memory`
* `jdbc`

### Running tests

* You can run with IntelliJ IDEA
* You can run with gradle `./gradlew test`