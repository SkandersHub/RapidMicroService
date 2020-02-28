![rms logo](https://github.com/alexskanders/RapidMicroService/blob/master/assets/logo.png "Rapid MicroService")

## Rapid MicroService

[![license badge](https://img.shields.io/github/license/alexskanders/RapidMicroService?logo=apache)](https://github.com/alexskanders/RapidMicroService/blob/master/LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/com.skanders.rms/rms.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.skanders.rms%22%20AND%20a:%22rms%22)
[![javadoc](https://javadoc.io/badge2/com.skanders.rms/rms/javadoc.svg)](https://javadoc.io/doc/com.skanders.rms/rms)

Maven:

~~~xml
<dependency>
    <groupId>com.skanders.rms</groupId>
    <artifactId>rms</artifactId>
    <version>0.8.0</version>
</dependency>
~~~

Gradle:
~~~javascript
implementation 'com.skanders.rms:rms:0.8.0'
~~~

## 

Rapid MicroService (RMS) is a library for quickly creating Microservices utilizing Grizzly, Jersey, Jackson and HikariCP. RMS contains several utilizes to help manage RESTful API calls and ensure proper Connection Pool resource management as well as easily to track Workflow and request Results.

A lot of features are still either not tested or lightly tested as this library is still very early in development. It isn't recommended to be used for any official releases.

Since the library is very early in development expect large scale changes this early on.

## 

- [Creating Services](#Creating-Services)
- [RMSConfig](#RMSConfig)
- [Dependencies](#Dependencies)

## Creating Services

The RapidMicroService is the abstract base that manages the MicroServices initialization. To create your microservice you will need to extend the `RapidMicroService` class and supply it with a `RMSConfig` instance. RMSConfig is built using `RMSProperties` that takes a yaml configuration file with either plain or encrypted values and can be used to enforce required values.

#### Example

##### Creating a basic service

~~~java
public class Service extends RapidMicroService
{
    private RMSConfig config;
    
    public Service(RMSConfig config, String resourcePath)
    {
        super(config, resourcePath);
        this.config = config;
    }
    
    private RMSConfig getServiceConfig { return config; }
}


public class Main
{
    private static Service service;
    
    public static PoolManager getPoolManager() { return service.getPoolManager(); }
    
    public static void main(String[] args)
    {
        RMSConfig config = new RMSConfig("config.yaml");
        String resourcePath = "com.domain.your.resource.path";
        
        service = new Service(config, resourcePath);
        service.start();
    }
}
~~~

## RMSConfig

`RMSConfig` can be extended to allow for more service configurations to be supplied. When supplied with an encryption algorithm + password encrypted values can be declared in the yaml file using the prefix `enc=<value>` (ignoring angle brackets)

#### Example

##### Extending RMSConfig

~~~java
public class IdmConfig extends RMSConfig
{
    private Long   sessionExpireTime;

    private String secretKey;
    private String optional;
    
    private IdmConfig(RMSProperties prop)
    {
        // Must call super to init values needed by RapidMicroService
        super(prop);

        setIdmConfig(prop);
    }

    public static IdmConfig fromRMSProperties(String file, String algorithm, String pass)
    {
        return new IdmConfig(RMSProperties.fromEncrypted(file, algorithm, pass));
    }

    private void setIdmConfig(RMSProperties prop)
    {
        // using the 'dot' notation a yaml value can be selected easily
        // Using the Required 'Req' methods will throw an error if the value is not found
        sessionExpireTime = prop.getReqLong("idm.sessionExpireTime");

        // Encrypted values will automatically be detected by checking the 'enc=' prefix
        secretKey         = prop.getReqStr("idm.secretKey");

        // If an optional value is missing this will simply return null
        optional          = prop.getStr("idm.optional");
    }
}
~~~

~~~yaml
# RapidMicroService configs
uri:
  scheme:   http://
  hostname: 0.0.0.0
  port:     12345
  path:     /rms

ssl:
  type: full

  keyStoreFile:   keyStoreFilePath
  keyStorePass:   enc=KEYSTOREENCRYPTEDVALUE
  trustStoreFile: trustStoreFilePath
  trustStorePass: enc=TRUSTSTOREENCRYPTEDVALUE

db:
  type: url

  url:         jdbc:mysql://127.0.0.1:3306/db
  username:    username
  password:    enc=DATABASEENCRYPTEDVALUE
  maxLifetime: 50000
  maxPoolSize: 10

cors:
  type: standard

  origin: "*"
  methods: "GET, POST, PUT, OPTIONS"
  headers: "origin, content-type, accept, authorization"
  exposeHeaders: "customHeader, anotherHeader"

# User configs
idm:
  sessionExpireTime: 100000
  secretKey: enc=SECRETENCRYPTEDVALUE
~~~


## Dependencies

### Compile
- [Skanders Commons](https://github.com/alexskanders/Skanders-Commons)
- [Jersey Container Grizzly2 Http](https://mvnrepository.com/artifact/org.glassfish.jersey.containers/jersey-container-grizzly2-http)
- [Grizzly WebSockets](https://mvnrepository.com/artifact/org.glassfish.grizzly/grizzly-websockets)
### Runtime
- [Jersey Inject](https://mvnrepository.com/artifact/org.glassfish.jersey.inject/jersey-hk2)
- [Grizzly Servlet Server](https://mvnrepository.com/artifact/org.glassfish.grizzly/grizzly-http-servlet-server)
- [Jaxb API](https://mvnrepository.com/artifact/javax.xml.bind/jaxb-api)
### Provided
- [SLF4J Api](https://mvnrepository.com/artifact/org.slf4j/slf4j-api)
