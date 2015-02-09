#org.brutusin:commons [![Build Status](https://api.travis-ci.org/brutusin/commons.svg?branch=master)](https://travis-ci.org/brutusin/commons) [![Maven Central Latest Version](https://maven-badges.herokuapp.com/maven-central/org.brutusin/commons/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.brutusin/commons/)
Common utilities project. General purpose functionality used by other projects.

##Maven dependency 
```xml
<dependency>
    <groupId>org.brutusin</groupId>
    <artifactId>commons</artifactId>
    <version>${commons.version}</version>
</dependency>
```
Click [here](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.brutusin%22%20a%3A%22commons%22) to see the latest available version released to the Maven Central Repository.

If you are not using maven and need help you can ask [here](https://github.com/brutusin/commons/issues).

## Main subcomponents:

### FifoTaskExecutor

[FifoTaskExecutor](src/main/java/org/brutusin/commons/concurrent/FifoTaskExecutor.java) parallelizes the processing of an ordered input collection, keeping the order in the output.

### JSON SPI

[JSON SPI](src/main/java/org/brutusin/commons/json/spi) is a service provider interface ([SPI](http://en.wikipedia.org/wiki/Service_provider_interface)) that defines all the JSON-related functionality needed by the rest of Brutusin modules, allowing to use different pluggable implementations (service providers) and decoupling client modules from them. 

**Note for implementing providers:**

Add the following dependency to the provider pom:
```xml
<dependency>
     <groupId>org.brutusin</groupId>
     <artifactId>commons</artifactId>
     <type>test-jar</type>
     <version>${commons.version}</version>
     <scope>test</scope>
</dependency>
```
in order to extend the [predefined tests](https://github.com/brutusin/commons/tree/master/src/test/java/org/brutusin/commons/json/spi) and verify they are passed. 

See also:
* [ServiceLoader](http://docs.oracle.com/javase/6/docs/api/java/util/ServiceLoader.html) for more details.
* [json-codec-jackson](https://github.com/brutusin/json-codec-jackson), the default JSON service provider.

## Support bugs and requests
https://github.com/brutusin/commons/issues

## Authors

- Ignacio del Valle Alles (<https://github.com/idelvall/>)

Contributions are always welcome and greatly appreciated!

##License
Apache License, Version 2.0
