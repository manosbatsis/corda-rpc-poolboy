# Corda RPC PoolBoy [![Maven Central](https://img.shields.io/maven-central/v/com.github.manosbatsis.corda.rpc.poolboy/corda-rpc-poolboy.svg)](https://repo1.maven.org/maven2/com/github/manosbatsis/corda/rpc/poolboy/corda-rpc-poolboy/)

PoolBoy is an RPC connection pool with support for multiple Corda nodes.

See complete documentation at https://manosbatsis.github.io/corda-rpc-poolboy

Note this is an early release. It is highly unstable but provided in 
hope it can be useful. Contributions are welcome.

### Installation

Add PoolBoy to your Cordapp's Gradle dependencies:

```groovy
dependencies{
    // PoolBoy dependency
    compile("com.github.manosbatsis.corda.rpc.poolboy:corda-rpc-poolboy:$poolboy_version")

    // Corda dependencies etc.
    // ...

}    
```

## Configuration

To use PoolBoy you will have to implement a `RpcConfigurationService`.
The service provides RPC pool and connection configuration.
Implementations can be "fixed" or fully dynamic, e.g. based on 
a properties file or database connection etc. respectively.

> A future release will provide a sample, properties-based implementation. 
In the meantime you can find an example in Corbeans' default  
[RpcConfigurationService](https://github.com/manosbatsis/corbeans/blob/master/corbeans-spring-boot-corda/src/main/kotlin/com/github/manosbatsis/corbeans/spring/boot/corda/service/RpcConfigurationService.kt). 

The main configuration items provided by such a service are as follows: 

1. `getRpcPoolParams()`: A `PoolParams` instance, in each turn specifies:
    - `rpcClientsMode`: An `RpcClientsMode` value to control the `CordaRPCClient` creation strategy, one of `SHARED`, `POOLED`, `DEDICATED`. Default is `SHARED`.
    - `rpcClientPool`: The [commons-pool2 keyed configuration](https://commons.apache.org/proper/commons-pool/apidocs/org/apache/commons/pool2/impl/GenericKeyedObjectPoolConfig.html)
     to use if `rpcClientsMode` is set to `POOLED`, equivalent to a `GenericKeyedObjectPoolConfig`.
    - `rpcOpsPool`: The [commons-pool2 keyed configuration](https://commons.apache.org/proper/commons-pool/apidocs/org/apache/commons/pool2/impl/GenericKeyedObjectPoolConfig.html)
     to use for `NodeRpcConnection` instances - those start and wrap a `CordaRPCOps` 
2. `getRpcNodeParams(String)`: Provides the RPC connection configuration (i.e. `NodeParams`) 
corresponding to the given node key per your configuration implementation, 
e.g. X500 name, application.properties key, database primary key etc. 
Note that the implementor is solely responsible for any caching.
3. `getGracefulReconnect(NodeParams)`: Override to change the `GracefulReconnect` implementation to use 
when `NodeParams.disableGracefulReconnect` is `false`.
4. `getCustomSerializers(List<String>)`: Custom serializer types found in the configured cordapp packages.
Override to bypass classpath scanning and improve search discovery performance.
5. `buildPoolKey(String)`: Builds a `PoolKey` for the given node name. 
Override to customise `PoolKey.externalTrace` and `PoolKey.impersonatedActor`, 
i.e. the equivalent parameters passed to `CordaRPCClient.start()`

## Sample Usage

Using a full `PoolBoy`:

```kotlin
// Create the PoolBoy instance
val poolBoy = PoolBoy(myRpcConfigurationService)

// Obtain a pool key for the target node
val poolKey = myRpcConfigurationService.buildPoolKey(nodeName)

// Do something with the CordaRPCOps
// for the given node name 
poolBoy.withConnection(poolKey){
    // e.g. start a flow
   it.proxy.startFlow(MyFlow::class.java, foo, bar, baz)
}
```

Using a `PoolBoyConnection`, i.e. a pooled connection 
handle to a target node directly:

```kotlin
// Create the PoolBoy instance
val poolBoy = PoolBoy(myRpcConfigurationService)

// Obtain a pool key for the target node
val poolKey = myRpcConfigurationService.buildPoolKey(nodeName)

// Get a pool-able connection handle
val pbCconn: PoolBoyConnection = poolBoy.forKey(poolKey)

// Do something with the CordaRPCOps
// for the target node
pbCconn.withConnection {
    // e.g. start a flow
   it.proxy.startFlow(MyFlow::class.java, foo, bar, baz)
}
```

Both `PoolBoy` and `PoolBoyConnection` have `borrowConnection()` 
and `returnConnection()` methods, but it's highly discouraged; you'd 
better know what you're doing if you use them. The `withConnection` 
approach is preferred.