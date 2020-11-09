# vatchecker: a basic java library for fetching VAT information from the VIES webservice

[![Maven Central](https://img.shields.io/maven-central/v/ch.digitalfondue.vatchecker/vatchecker.svg)](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22vatchecker%22)
[![Build Status](https://img.shields.io/github/workflow/status/digitalfondue/vatchecker/Java%20CI%20with%20Maven)](https://github.com/digitalfondue/vatchecker/actions?query=workflow%3A%22Java+CI+with+Maven%22)

 
A small java client for calling the European "VAT Information Exchange System" (VIES) webservice for validating the VAT numbers. See http://ec.europa.eu/taxation_customs/vies/ .

## License

vatchecker is licensed under the Apache License Version 2.0.

## Download

maven:

```xml
<dependency>
    <groupId>ch.digitalfondue.vatchecker</groupId>
    <artifactId>vatchecker</artifactId>
    <version>1.4.3</version>
</dependency>
```

gradle:

```
compile 'ch.digitalfondue.vatchecker:vatchecker:1.4.3'
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

For error handling, see the tests, you may distinguish "invalid" and "error" which can have a Fault object:

 - https://github.com/digitalfondue/vatchecker/blob/master/src/test/java/ch/digitalfondue/vatchecker/EUVatCheckerTest.java

You can use your own data fetcher if customization is needed, see:

 - https://github.com/digitalfondue/vatchecker/blob/master/src/main/java/ch/digitalfondue/vatchecker/EUVatChecker.java#L183
 - https://github.com/digitalfondue/vatchecker/blob/master/src/main/java/ch/digitalfondue/vatchecker/EUVatChecker.java#L67
