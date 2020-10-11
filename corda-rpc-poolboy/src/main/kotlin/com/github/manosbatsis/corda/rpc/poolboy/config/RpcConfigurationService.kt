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
package com.github.manosbatsis.corda.rpc.poolboy.config

import com.github.manosbatsis.corda.rpc.poolboy.PoolKey
import com.github.manosbatsis.corda.rpc.poolboy.pool.connection.NodeRpcConnectionConfig
import com.github.manosbatsis.corda.rpc.poolboy.support.Util
import net.corda.client.rpc.GracefulReconnect
import net.corda.core.identity.CordaX500Name
import net.corda.core.serialization.SerializationCustomSerializer
import org.slf4j.LoggerFactory

/**
 * Provides RPC pool and connection information for nodes.
 * Can be "fixed" or fully dynamic, e.g. based on
 * a properties file or database connection respectively.
 *
 */
interface RpcConfigurationService {

    companion object {
        private val logger = LoggerFactory.getLogger(RpcConfigurationService::class.java)
        const val partyNameIsRequired = "NodeParams must include a valid CordaX500Name string representation as value of the partyName property."
        const val rpcConnectionForNodeNotFound = "Could not find an RPC connection config for node name:"
    }

    /**
     * Custom serializer types found in the configured cordapp packages.
     * Override to bypass classpath scanning and improve search discovery performance.
     */
    fun getCustomSerializers(cordapPackages: List<String>): Set<SerializationCustomSerializer<*, *>>? {
        return null
    }

    /**
     * Get the RPC pool configuration
     */
    fun getRpcPoolParams(): PoolParams

    /**
     * Build a [PoolKey] for the given node name.
     * Override to customise [PoolKey.externalTrace]
     * and [PoolKey.impersonatedActor]
     */
    fun buildPoolKey(nodeName: String): PoolKey

    /**
     * Read-only snapshot of all RPC connection configuration (i.e. [NodeParams])
     * by "node name" per your configuration implementation,
     * e.g. application.properties key, database primary key etc.
     *
     * Note: the implementor is solely responsible for any
     * caching while retrieving results
     */
    fun getAllRpcNodeParams(): Map<String, NodeParams>

    /**
     * Get the RPC connection configuration (i.e. [NodeParams])
     * corresponding to the given [nodeName] per your configuration
     * implementation, e.g. application.properties key,
     * database primary key etc.
     *
     * Note: the implementor is solely responsible for any
     * caching while retrieving results
     */
    fun getRpcNodeParams(nodeName: String): NodeParams

    /**
     * Override to change the [GracefulReconnect] implementation to use when
     * [NodeParams.disableGracefulReconnect] is `false`.
     */
    fun getGracefulReconnect(nodeParams: NodeParams): GracefulReconnect {
        return Util.defaultGracefullReconnect(nodeParams)
    }

    /**
     * Get the RPC connection configuration for the given key.
     * Override to customise further.
     */
    fun getNodeRpcConnectionConfig(
            key: PoolKey
    ): NodeRpcConnectionConfig{
        val nodeParams = getRpcNodeParams(key.nodeName)
        val partyName = nodeParams.partyName
                ?.let { CordaX500Name.parse(it)}
                ?: error(partyNameIsRequired)
        val gracefulReconnect = if(nodeParams.disableGracefulReconnect == true) null
            else getGracefulReconnect(nodeParams)
        return NodeRpcConnectionConfig(
                nodeParams = nodeParams,
                externalTrace = key.externalTrace,
                impersonatedActor = key.impersonatedActor,
                targetLegalIdentity = partyName,
                gracefulReconnect = gracefulReconnect)
    }

}