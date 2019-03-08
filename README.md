[![PDD status](http://www.0pdd.com/svg?name=nms403/chatbot-constructor)](http://www.0pdd.com/p?name=nms403/chatbot-constructor)

# START PROJECT

## Install Java 11
[java]

## Install Node.js
[Node.js]

## Install mode modules
1. `cd source_dir`
2. `npm i`

## Setup IDE
### Checkstyle

[Download and install checkstyle plugin]

Add checkstyle:

`Settings` - `Editor` - `Code style` - `Java` - `*Gear*` - `Import Scheme` - `Intellij IDEA code style XML`

### Inspections

Add inspections:
`Settings` - `Editor` - `Inspections` - `Import profile`

### Annotation processing

`Settings` - `Build, Execution, Deployment` - `Compiler` - `Annotation Processors` - `Enable annotation process` - `true`

## Build project
`gradle build`

## Run project
`gradle bootRun`

## Swagger

Set development profile in application.yml: `spring.profiles.active=development`

[Access to swagger]

[java]: https://www.oracle.com/technetwork/java/javase/downloads/jdk11-downloads-5066655.html
[Node.js]: https://nodejs.org/en/download/
[Download and install checkstyle plugin]: https://plugins.jetbrains.com/plugin/1065-checkstyle-idea
[Access to swagger]: http://localhost:8090/swagger-ui.html