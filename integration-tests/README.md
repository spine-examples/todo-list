# integration-tests

A module which contains Integration and Performance Tests.

### Storage types

##### In-memory storage

By default Integration and Performance Tests run on top of in-memory storage. 
Therefore they don't require any additional configuration.

### Running tests
Under package `io.spine.test` you can find two packages `integration` and `performance` which 
contain relevant tests.

You are able to run tests with IDE or Gradle:

* To run Integration or Performance Test with IDE, open it in IDE and hit the run button in the 
toolbar or sidebar. It will automatically run as JUnit Test. You can inspect the test results in the 
JUnit view. To change `storage.type`, open test configurations and add command-line parameter 
described in the paragraph above.

* To run Integration or Performance Tests with Gradle, execute `./gradlew integrationTest` or 
`./gradlew performanceTest`.

### Restrictions
Currently, `todo-list` supports only MySQL as database.
