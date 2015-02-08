#org.brutusin:commons [![Build Status](https://api.travis-ci.org/brutusin/commons.svg?branch=master)](https://travis-ci.org/brutusin/commons) [![Maven Central Latest Version](https://maven-badges.herokuapp.com/maven-central/org.brutusin/commons/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.brutusin/commons/)
Common utilities project. General purpose functionality used by other projects.

## Main subcomponents:

* [FifoTaskExecutor](src/main/java/org/brutusin/commons/concurrent/FifoTaskExecutor.java): Parallelizes the processing of an ordered input collection, keeping the order in the output.
* [JSON SPI](src/main/java/org/brutusin/commons/json/spi): A service provider interface ([SPI](http://en.wikipedia.org/wiki/Service_provider_interface)) that defines all the JSON-related functionality needed by the rest of Brutusin modules, allowing to use different pluggable implementations (service providers) and decoupling client modules from them. See [json-codec-jackson](https://github.com/brutusin/json-codec-jackson), the default JSON service provider.

## Support bugs and requests
https://github.com/brutusin/commons/issues

## Authors

- Ignacio del Valle Alles (<https://github.com/idelvall/>)

Contributions are always welcome and greatly appreciated!

##License
Apache License, Version 2.0
