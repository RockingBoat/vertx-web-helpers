Under construction

# Vert.X Routing/Web Helpers

## Description

Some helper which can make your coding easier.
All code write on Kotlin

## Install

### Maven

``` xml
<?xml version="1.0" encoding="UTF-8" ?>
<settings xsi:schemaLocation='http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd'
          xmlns='http://maven.apache.org/SETTINGS/1.0.0' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>
    
    <profiles>
        <profile>
            <repositories>
                <repository>
                    <snapshots>
                        <enabled>false</enabled>
                    </snapshots>
                    <id>bintray-rockingboat-maven</id>
                    <name>bintray</name>
                    <url>https://dl.bintray.com/rockingboat/maven</url>
                </repository>
            </repositories>
            <pluginRepositories>
                <pluginRepository>
                    <snapshots>
                        <enabled>false</enabled>
                    </snapshots>
                    <id>bintray-rockingboat-maven</id>
                    <name>bintray-plugins</name>
                    <url>https://dl.bintray.com/rockingboat/maven</url>
                </pluginRepository>
            </pluginRepositories>
            <id>bintray</id>
        </profile>
    </profiles>
    <activeProfiles>
        <activeProfile>bintray</activeProfile>
    </activeProfiles>
</settings>
```

``` xml
<dependency>
  <groupId>rockingboat.vertx.helpers</groupId>
  <artifactId>web</artifactId>
  <version>0.7.2</version>
  <type>pom</type>
</dependency>
```

### Gradle

``` gradle
repositories {
    maven {
        url  "https://dl.bintray.com/rockingboat/maven" 
    }
}
      
```

``` gradle
compile 'rockingboat.vertx.helpers:web:0.7.2'   
```

## Usage

### Annotations

All Annotation get input variable: path (`String`)

- @Controller
- @Get
- @Post
- @Post
- @Put
- @Options
- @Head
- @Delete
- @Trace
- @Connect
- @Patch
- @All

- @Route


### Response helpers

`jsonOutput(Object)` - response with Json Object

`endJSend(Object, Int)` - response with struct

```json
{
  "data": ANY,
  "code": Int
}
``` 

### Other Helper

`RoutingContext.header(String)` - Get header value
`RoutingContext.param(String)` - Get value of CGI param (no array :() 
`RoutingContext.body<T>())` - Get and convert json body to instance of class T


## Example

In Your Verticle class

``` kotlin
...
vertx.createHttpServer(
            HttpServerOptions(
                port = Integer.getInteger("http.port", 8080)
            ))
            .controllers(
                ControllerA::class,
                ControllerB::class,
                ControllerC::class
            )
            .listen { result ->
                if (result.succeeded()) {
                    startFuture?.complete()
                } else {
                    startFuture?.fail(result.cause())
                }
            }
...
````

`ControllerA.kt`

``` kotlin
...
@Controller("/month")
class ControllerMonthStorage {

...
```
