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
package com.github.manosbatsis.corda.rpc.poolboy.pool.connection

import com.github.manosbatsis.corda.rpc.poolboy.PoolKey
import com.github.manosbatsis.corda.rpc.poolboy.config.RpcConfigurationService
import com.github.manosbatsis.corda.rpc.poolboy.connection.EagerNodeRpcConnection
import com.github.manosbatsis.corda.rpc.poolboy.connection.LazyNodeRpcConnection
import com.github.manosbatsis.corda.rpc.poolboy.connection.NodeRpcConnection
import com.github.manosbatsis.corda.rpc.poolboy.pool.client.CordaRpcClientPool
import com.github.manosbatsis.corda.rpc.poolboy.pool.connection.SharedRpcClientsNodeRpcConnectionFactory.ClientKey
import com.github.manosbatsis.corda.rpc.poolboy.support.Util
import net.corda.client.rpc.CordaRPCClient
import net.corda.core.utilities.contextLogger
import org.apache.commons.pool2.BaseKeyedPooledObjectFactory
import org.apache.commons.pool2.PooledObject
import org.apache.commons.pool2.impl.DefaultPooledObject

abstract class AbstractRpcConnectionFactory :
        BaseKeyedPooledObjectFactory<PoolKey, NodeRpcConnection>() {

    companion object {
        private val logger = contextLogger()
    }

    override fun wrap(value: NodeRpcConnection): PooledObject<NodeRpcConnection> {
        logger.debug("wrap NodeRpcConnection: ${value}")
        return DefaultPooledObject(value)
    }

    override fun destroyObject(key: PoolKey, p: PooledObject<NodeRpcConnection>) {

        logger.debug("destroyObject ${p} for key: ${key}")
        p.`object`.proxy.shutdown()
    }
}


/**
 * A [NodeRpcConnection] pool factory that uses a new [CordaRPCClient]
 * fopr each connection.
 */
class DedicatedRpcClientsNodeRpcConnectionFactory(
        val nodeParamsService: RpcConfigurationService
) : AbstractRpcConnectionFactory() {
    override fun create(key: PoolKey): NodeRpcConnection {
        val config = nodeParamsService.getNodeRpcConnectionConfig(key)
        val rpcClient =
            Util.createCordaRPCClient(config.nodeParams)
        return if (config.nodeParams?.eager == true) EagerNodeRpcConnection(config)
        else LazyNodeRpcConnection(config)
    }
}

/**
 * A [NodeRpcConnection] pool factory using [CordaRPCClient]
 * shared based on a [ClientKey], i.e. by address and RPC user name.
 */
class SharedRpcClientsNodeRpcConnectionFactory(
        val nodeParamsService: RpcConfigurationService
) : AbstractRpcConnectionFactory() {

    data class ClientKey(
            val address: String,
            val username: String
    )

    companion object {
        private val logger = contextLogger()
        private val clients: MutableMap<ClientKey, CordaRPCClient> =
                mutableMapOf()

    }

    override fun create(key: PoolKey): NodeRpcConnection {
        logger.debug("create for key: ${key}")
        val config = nodeParamsService.getNodeRpcConnectionConfig(key)
        val clientKey = ClientKey(
                address = config.nodeParams.address
                        ?: error("An address is required"),
                username = config.nodeParams.username
                        ?: error("A username is required"))
        // Get and/or create a CordaRPCClient
        // shared by address and RPC user name
        val rpcClient = clients.getOrPut(clientKey){
            Util.createCordaRPCClient(config.nodeParams)
        }
        return if (config.nodeParams.eager == true) EagerNodeRpcConnection(config)
        else LazyNodeRpcConnection(config)
    }
}

/** Pool factory for [NodeRpcConnection]s backed by a nested [CordaRPCClient] pool */
class RpcClientPoolBackedNodeRpcConnectionFactory(
        val nodeParamsService: RpcConfigurationService,
        /** Parent pool for reusing [CordaRPCClient] */
        val rpcClientPool: CordaRpcClientPool
) : AbstractRpcConnectionFactory() {

    companion object {
        private val logger = contextLogger()
    }

    override fun create(key: PoolKey): NodeRpcConnection {
        logger.debug("create for key: ${key}")
        // Borrow the CordaRpcClient instance
        val rpcClient = rpcClientPool.borrowObject(key)
        val rpcConn: NodeRpcConnection
        try {
            val config = nodeParamsService.getNodeRpcConnectionConfig(key)
            // Create our RPC proxy wrapper
            rpcConn = if (config.nodeParams.eager == true) EagerNodeRpcConnection(config)
            else LazyNodeRpcConnection(config)
        }catch (e: Exception){
            throw e
        }
        finally {
            // Return the client to the pool
            rpcClientPool.returnObject(key, rpcClient)
        }
        return rpcConn
    }

    override fun makeObject(key: PoolKey): PooledObject<NodeRpcConnection> {
        logger.debug("makeObject for key : ${key}")
        return super.makeObject(key)
    }

    override fun validateObject(key: PoolKey, p: PooledObject<NodeRpcConnection>): Boolean {
        val valid = super.validateObject(key, p)
        logger.debug("validateObject, valid: $valid, key : ${key}")
        return valid
    }

    override fun activateObject(key: PoolKey, p: PooledObject<NodeRpcConnection>) {
        logger.debug("activateObject $p for key : ${key}")
        super.activateObject(key, p)
    }

    override fun destroyObject(key: PoolKey, p: PooledObject<NodeRpcConnection>) {
        logger.debug("destroyObject $p for key : ${key}")
        super.destroyObject(key, p)
    }

    override fun passivateObject(key: PoolKey, p: PooledObject<NodeRpcConnection>) {
        logger.debug("passivateObject $p for key : ${key}")
        super.passivateObject(key, p)
    }
}