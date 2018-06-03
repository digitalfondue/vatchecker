# vatchecker: a basic java library for fetching VAT information from the EU webservice

[![Maven Central](https://img.shields.io/maven-central/v/ch.digitalfondue.vatchecker/vatchecker.svg)](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22vatchecker%22)
[![Build Status](https://travis-ci.org/digitalfondue/vatchecker.png?branch=master)](https://travis-ci.org/digitalfondue/jfiveparse)
[![Coverage Status](https://coveralls.io/repos/digitalfondue/vatchecker/badge.svg?branch=master)](https://coveralls.io/r/digitalfondue/vatchecker?branch=master)


A small utility for calling the VIES webservice. See http://ec.europa.eu/taxation_customs/vies/ .

## License

vatchecker is licensed under the Apache License Version 2.0.

## Download

maven:

```xml
<dependency>
    <groupId>ch.digitalfondue.vatchecker</groupId>
    <artifactId>vatchecker</artifactId>
    <version>1.0</version>
</dependency>
```

gradle:

```
compile 'ch.digitalfondue.vatchecker:vatchecker:1.0'
```

## Use

As a static method:

```java
EUVatCheckResponse resp = EUVatChecker.doCheck("IT", "00950501007");
Assert.assertEquals(true, resp.isValid());
Assert.assertEquals("BANCA D'ITALIA", resp.getName());
Assert.assertEquals("VIA NAZIONALE 91 \n00184 ROMA RM\n", resp.getAddress());
```

You can create an instance if you prefer:

```java
EUVatChecker euVatChecker = new EUVatChecker();
EUVatCheckResponse resp = euVatChecker.check("IT", "00950501007");
Assert.assertEquals(true, resp.isValid());
Assert.assertEquals("BANCA D'ITALIA", resp.getName());
Assert.assertEquals("VIA NAZIONALE 91 \n00184 ROMA RM\n", resp.getAddress());
```

You can use your own data fetcher if customization is needed, see:

 - https://github.com/digitalfondue/vatchecker/blob/master/src/main/java/ch/digitalfondue/vatchecker/EUVatChecker.java#L183
 - https://github.com/digitalfondue/vatchecker/blob/master/src/main/java/ch/digitalfondue/vatchecker/EUVatChecker.java#L67
