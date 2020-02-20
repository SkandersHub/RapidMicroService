![rms logo](https://github.com/alexskanders/RapidMicroService/blob/master/assets/logo.png "Rapid MicroService")

## Rapid MicroService

![license badge](https://img.shields.io/github/license/alexskanders/RapidMicroService?style=flat-square)
![maven badge](https://img.shields.io/maven-central/v/com.skanders.rms/rms?style=flat-square)

Maven:

~~~xml
<dependency>
    <groupId>com.skanders.rms</groupId>
    <artifactId>rms</artifactId>
    <version>0.5.1</version>
</dependency>
~~~

Gradle:
~~~javascript
implementation 'com.skanders.rms:rms:0.5.1'
~~~

## 

Rapid MicroService (RMS) is a library for quickly creating Microservices utilizing Grizzly, Jersey, Jackson and HikariCP. RMS contains several utilizes to help manage RESTful API calls and ensure proper Connection Pool resource management as well as easily to track Workflow and request Results.

A lot of features are still either not tested or lightly tested as this library is still very early in development. It isn't recommended to be used for any official releases.

Since the library is very early in development expect large scale changes this early on.

## 

- [Creating Services](#Creating-Services)
- [RMSConfig](#RMSConfig)
- [RMSProperties](#RMSProperties)
- [Result](#Result)
- [Resulted](#Resulted)
- [ModelBuilder](#ModelBuilder)
- [PoolManager](#PoolManager)
- [SQLQuery](#SQLQuery)
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

## RMSProperties

`RMSProperties` will take a yaml file and act as a wrapper to allow for easily decrypting of encrypted values using jasypt, and enforcement of required values. 

#### Example

##### Extending RMSProperties

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


## Result

The Result class holds possible result code and messages to keep track of the different outcomes of Micro-Service events. It is used as the primary status indicator of a request being handled in RMS and can help manage work flow with the `Resulted` class and quickly return the result of a request. It is recommended to create a class of prebuilt static Results to reference. The class holds four internal values, however only two (`Integer:code, String:message`) are deserialized by Jackson for external use, the other two (`Status:status, Exception:exception`) (`Status` being `javax.ws.rs.core.Response.Status`) are for internal use to be used however the caller sees fit. 

~~~javascript
Result.declare(Integer code, String message)
~~~
- Main static builder, defaults the internal `Status` code to be `Status.OK`.

~~~javascript
Result.declare(Integer code, String message, Status status)
~~~
- Allows the `Status` to be variable.

~~~javascript
Result.declare(Exception exception)
~~~
- Stores the exception internally for the caller to use as needed. The other three values are set to default exception values (`code:-1, message:"Something went wrong", status:INTERNAL_SERVER_ERROR`).

~~~javascript
Result.exception(String message)
~~~
- Similar to `Result.build(Exception exception)` however the Exception type will always be `RmsException` but with the given message.





## Resulted

The Resulted class helps keep track of the state of endpoint request handing. Resulted implements `AutoCloseable` and has a `close()` function to help ensure resource management. Resulted is considered "Not Valid" if any other result value other than `Result.VALID` is contained in the Resulted. The class can be created using three different types.

### Main static creators

~~~javascript
Resulted.inValue(T value)
~~~
- When the request has been completed successfully a Resulted instance should be created from the value and returned to the caller with `inValue()`. 
- Once a value has been loaded into Resulted it cannot be changed or removed from that Resulted instance.
- This builder will internally set the result to be `Result.VALID` and cause `result.withResult()` to be FALSE ensuring the callers request continue to be handled.

~~~javascript
Resulted.inResult(Result result)
~~~
- When the request has been preemptively stopped for a Service related reason, the reason should be created with `Result.declare(int code, String message)` or `Result.declare(int code, String message, Status Response.status)` and a Resulted instance should be created from the result with `inResult()`.
- This builder will cause `result.withResult()` to be TRUE and will alert the caller to handle the Result accordingly.

~~~javascript
Resulted.inException(Exception exception)
~~~
- When the request has been preemptively stopped due to a Exception being raised, a Resulted instance should be created from the exception with `inException()`. This will automatically create an internal Result value with the default Exception traits `code: -1, message: "Something went wrong", status:Status.INTERNAL_SERVER_ERROR` and will be preloaded with the raised exception for internal use. 
- This builder will cause `result.withResult()` to be TRUE and will alert the caller to handle the Exception accordingly.

### Helper static creator

~~~javascript
Resulted.inResulted(Resulted resulted)
~~~
- This is a helper function that allows the Resulted to be passed as a new Resulted<T> type if there is a Non-Valid Result as well as a contradiction between the Return Resulted<T> T type and the current Resulted<T> Type.

#### Example

##### Resulted example

~~~java
public Resulted<Integer> divideNoRemainder(int a, int b)
{
    Result remainderFound = Result.declare(100, "Numbers produce a remainder");
    try {
        if (a % b != 0)
            return Resulted.inResult(remainderFound);
            
        int answer = a / b;
        
        return Resulted.inValue(answer);
        
    } catch (ArithmeticException e) {
        return Resulted.inException(e);
        
    }
}
~~~






## ModelBuilder

ModelBuilder contains all the utilities for parsing from JSON, YAML and XML strings and files into objects. The return type of all of the builders are Resulted<T>. All exceptions are returned as Result within the Resulted, however `JsonMappingException` and `JsonParseException` are returned as Prebuild Result values:
- `JSON_MAPPING_EXCEPT: (-10, "JSON: Mapping Exception", Status.BAD_REQUEST)`  
- `JSON_PARSE_EXCEPT:   (-11, "JSON: Parse Exception",   Status.BAD_REQUEST)`

This allows the caller to return the result to the user or service calling this service to notify an error on their part.


### Main Static Builders

~~~javascript
ModelBuilder.fromJson(InputStream jsonStream, Class<T> class);
ModelBuilder.fromJson(File jsonFile, Class<T> class);
ModelBuilder.fromJson(String json, Class<T> class);
~~~
 - ModelBuilder can take an InputStream, File, or String and convert it into the given Object type

~~~javascript
ModelBuilder.fromYaml(...)
ModelBuilder.fromXml(...)
~~~
 - The same three builders can be found for Yaml and Xml types as well.

~~~javascript
ModelBuilder.fromJson(String jsonFile, Class<T> class, DeserializationFeature ... feature);
~~~
 - All ModelBuiler Static builders can also take a list of `DeserializationFeature`

### ObjectMapper

~~~javascript
ModelBuilder.getJsonMapper();
ModelBuilder.getYamlMapper();
ModelBuilder.getXmlMapper();
~~~
 - To prevent multiple instances of jackson's ObjectMappers the three internal mappers can be extracted for use.

#### Example

##### fromJson() example

~~~java
class Datamodel
{
    @JsonProperty("DataOne")
    public Integer dataOne;
    @JsonProperty("DataTwo")
    public Integer dataTwo;
  
    public static Resulted<DataModel> createDefaultDataModel()
    {
        String json = "{\"DataOne\":1, \"DataTwo\":2}";

        Resulted<DataModel> resulted = ModelBuilder.fromJson(json, DataModel.class);

        return resulted;
    }
}
~~~





## PoolManager

PoolManager is a wrapper class around HikariDataSource that controls its creation and maintains its connections.  PoolManager is manually created by `RapidMicroService` if `RMSConfig` is given a db.type. The PoolManager will use either the database Driver, or the Database Url depending on the db.type.
- Future features include the ability to use any given Connection Pool by supplying creating implementing a `PoolContainer` class





## SQLQuery

SQLQuery works in conjunction with PoolManager to help easily create database queries while ensuring proper connection management. SQLQuery has two possible ways of executing a query.

### Creating a Query

~~~javascript
SQLQuery.createQuery(query, poolManager);
~~~
- Internally manages Connection and Prepaired statment by supplying in the poolManager and the SQL query.

### Managing Parameters
~~~javascript
sqlQuery.set(java.sql.Types type, Object value);
~~~
- Adding the parameters for the query is done by using the set function. The parameters MUST be added in order of apperance in the query. The proper `PreparedStatement` setter is called by using the given `java.sql.Types` type.

### Executing Queries

~~~javascript
sqlQuery.executeUpdate()
~~~
- This returns a `Resulted<Integer>` object that contains either the number of rows updated, or the Result exception raised if any where.

~~~javascript
sqlQuery.executeQuery()
~~~
- This returns a `Resulted<SQLResultSet>` object that contains either a `SQLResultSet` object, or the Result exception raised if any where.
- The `SQLResultSet` is a manager that contains the `ResultSet` and `PoolConnection` and implements `AutoClosable`, allowing the caller to either use "try with resources" or `.close()` to release the connection after the user has completed their work.

#### Examples

##### createInstance()

~~~java
public SQLQuery createSQLQuery()
{
    PoolManager poolManager = getPoolManager();
    String query = "SELECT userID FROM users WHERE status = ? AND level = ?;";
    int status = 1;
    int level = 1;
    
    // Creating a new Instance requires both a Query and the PoolManager
    SQLQuery sqlQuery = SQLQuery.createQuery(query, poolManager);
        //Uses PreparedStatment to set Value according to the SQL Types
        sqlQuery.set(Types.INTEGER, status);   
        sqlQuery.set(Types.INTEGER, level);

    return sqlQuery;
}
~~~

##### executeUpdate() example

~~~java
public void updateStatusOfUsers(Integer status, Integer level)
{
    String query =
        "UPDATE users " +
        "SET status = ? " +
        "WHERE level = ?;";
        
    SQLQuery sqlQuery = SQLQuery.createQuery(query, Service.getPool());
        sqlQuery.set(Types.INTEGER, status);
        sqlQuery.set(Types.INTEGER, level);
    
    Resulted<Integer> resulted = sqlQuery.executeUpdate()
    
    if (resulted.withResult()) {
        String cause = resulted.result().message();
        System.out.println("Resulted stopped due to Non-Valid result" + cause);
        return;
    }
    
    String value = resulted.value();
    System.out.println("Rows Updated: " + value);
}
~~~

##### executeQuery() example

~~~java
public void getAllUsersOfLevel(Integer level)
{
    String query =
        "SELECT userID " +
        "FROM users " +
        "WHERE level = ?;";
        
    SQLQuery sqlQuery = SQLQuery.createQuery(query, Service.getPool());
        sqlQuery.set(Types.INTEGER, level);
    
    try (Resulted<SQLResult> resulted = sqlQuery.executeQuery())
    {
        if (resulted.withResult()) {
            String cause = resulted.result().message();
            System.out.println("Resulted stopped due to Non-Valid result" + cause);
            return;
            
        }
        
        ResultSet resultSet = resulted.value().getResultSet();
        
        while (resultSet.next()) {
            System.out.println(resultSet.getInt("userID"))
            
        }
        
    } catch (SQLException e) {
        System.out.println("SQLException")
        
    }
}
~~~





## Dependencies
- [Jersey Container Grizzly2 Http](https://mvnrepository.com/artifact/org.glassfish.jersey.containers/jersey-container-grizzly2-http)
- [Jersey Inject](https://mvnrepository.com/artifact/org.glassfish.jersey.inject/jersey-hk2)
- [Jersey Media JSON Jackson](https://mvnrepository.com/artifact/org.glassfish.jersey.media/jersey-media-json-jackson)
- [Grizzly WebSockets](https://mvnrepository.com/artifact/org.glassfish.grizzly/grizzly-websockets)
- [Jackson Dataformat YAML](https://mvnrepository.com/artifact/com.fasterxml.jackson.dataformat/jackson-dataformat-yaml)
- [Jackson Dataformat XML](https://mvnrepository.com/artifact/com.fasterxml.jackson.dataformat/jackson-dataformat-xml)
- [HikariCP](https://mvnrepository.com/artifact/com.zaxxer/HikariCP)
- [Log4j Core](https://mvnrepository.com/artifact/org.apache.logging.log4j/log4j-core)
- [Log4j Api](https://mvnrepository.com/artifact/org.apache.logging.log4j/log4j-api)
- [Javax WS RS api](https://mvnrepository.com/artifact/javax.ws.rs/javax.ws.rs-api)
- [Jaxb API](https://mvnrepository.com/artifact/javax.xml.bind/jaxb-api)
- [Google Guava](https://mvnrepository.com/artifact/com.google.guava/guava)
- [JASYPT](https://mvnrepository.com/artifact/org.jasypt/jasypt)
