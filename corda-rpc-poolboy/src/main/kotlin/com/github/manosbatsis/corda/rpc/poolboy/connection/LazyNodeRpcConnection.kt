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
package com.github.manosbatsis.corda.rpc.poolboy.connection

import com.github.manosbatsis.corda.rpc.poolboy.pool.connection.NodeRpcConnectionConfig
import net.corda.client.rpc.CordaRPCClient
import net.corda.core.messaging.CordaRPCOps
import org.slf4j.LoggerFactory

/**
 * Lazily initialised implementation of [NodeRpcConnection],
 * wraps a [CordaRPCClient]
 *
 * @param nodeParams the RPC connection params
 * @property proxy The RPC proxy.
 */
open class LazyNodeRpcConnection(
        config: NodeRpcConnectionConfig
): com.github.manosbatsis.corda.rpc.poolboy.connection.AbstractNodeRpcConnection(config) {

    companion object {
        private val logger = LoggerFactory.getLogger(LazyNodeRpcConnection::class.java)
    }

    /** Provides lazy access to a [CordaRPCOps] RPC proxy */
    override val proxy: CordaRPCOps by lazy {
        createProxy()
    }

}
