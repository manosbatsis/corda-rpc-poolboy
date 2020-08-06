/*
 * Corda-RPC-Poolboy: Connection pooling for Corda RPC clients
 * Copyright (C) 2018 Manos Batsis
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package com.github.manosbatsis.corda.rpc.poolboy

import com.github.manosbatsis.corda.rpc.poolboy.config.RpcConfigurationService
import com.github.manosbatsis.corda.rpc.poolboy.connection.NodeRpcConnection
import com.github.manosbatsis.corda.rpc.poolboy.pool.connection.NodeRpcConnectionPool
import net.corda.core.messaging.CordaRPCOps

/** Provides RPC connection pooling */
class PoolBoy(private val rpcConfigurationService: RpcConfigurationService) {


    /** Maintain an RPC client/ops pool */
    private val rpcConnectionPool = NodeRpcConnectionPool(
            nodeParamsService = rpcConfigurationService
    )

    /** Get a pool-able connection wrapper for the given [key] */
    fun forKey(key: PoolKey): PoolBoyConnection =
            PoolBoyPooledConnection(this, key)

    /**
     * Only use if you know what you are doing. Explicitly borrow a
     * [NodeRpcConnection] from the pool.
     */
    fun borrowConnection(key: PoolKey): NodeRpcConnection =
            rpcConnectionPool.borrowObject(key)

    /**
     * Only use if you know what you are doing. Explicitly return a
     * [NodeRpcConnection] from the pool.
     */
    fun returnConnection(key: PoolKey, rpcConnection: NodeRpcConnection) =
            rpcConnectionPool.returnObject(key, rpcConnection)

    /**
     * Run some code with a [NodeRpcConnection] from the pool in-context
     */
    fun <A> withConnection(key: PoolKey, block: (NodeRpcConnection) -> A): A {
        val connection = borrowConnection(key)
        return try {
            block(connection)
        } finally {
            returnConnection(key, connection)
        }
    }
}

/** RPC connection pooling for a specific node */
interface PoolBoyConnection {

    /**
     * Only use if you know what you are doing. Explicitly borrow a
     * [NodeRpcConnection] from the pool using the preconfigured key.
     */
    fun borrowConnection(): NodeRpcConnection

    /**
     * Only use if you know what you are doing. Explicitly return a
     * [NodeRpcConnection] from the pool using the preconfigured key.
     */
    fun returnConnection(rpcConnection: NodeRpcConnection)

    /**
     * Run some code with with a [NodeRpcConnection] from the pool in-context
     * using the preconfigured key
     */
    fun <A> withConnection( block: (NodeRpcConnection) -> A): A {
        val connection = borrowConnection()
        return try {
            block(connection)
        }catch (e: Exception){
            throw e
        }
        finally {
            returnConnection(connection)
        }
    }
}

/** Regular, pooled [PoolBoyConnection] implementation */
class PoolBoyPooledConnection(
        val poolBoy: PoolBoy,
        val key: PoolKey
) : PoolBoyConnection {

    /**
     * Only use if you know what you are doing. Explicitly borrow a
     * [NodeRpcConnection] from the pool using the preconfigured key.
     */
    override fun borrowConnection() = poolBoy.borrowConnection(key)

    /**
     * Only use if you know what you are doing. Explicitly return a
     * [NodeRpcConnection] from the pool using the preconfigured key.
     */
    override fun returnConnection(rpcConnection: NodeRpcConnection) =
            poolBoy.returnConnection(key, rpcConnection)

}

/**
 * Non-pooled [PoolBoyConnection] adapter implementation
 * for legacy apps using a "static" [NodeRpcConnection]
 */
open class PoolBoyNonPooledConnection(
        val connection: NodeRpcConnection
) : PoolBoyConnection {

    /** Reuses the preconfigured [NodeRpcConnection]*/
    override fun borrowConnection() = connection

    /** No-op */
    override fun returnConnection(rpcConnection: NodeRpcConnection) {/* NO-OP */}

}

/**
 * Non-pooled [PoolBoyConnection] adapter implementation
 * for legacy apps using a "static" [CordaRPCOps]
 */
class PoolBoyNonPooledRawRpcConnection(
        rpcOps: CordaRPCOps
) : PoolBoyNonPooledConnection(CordaRPCOpsWrappingConnection(rpcOps)) {

    class CordaRPCOpsWrappingConnection(
            override val proxy: CordaRPCOps,
            val skipInfo: Boolean = true
    ): NodeRpcConnection


}