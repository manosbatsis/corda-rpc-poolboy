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
import com.github.manosbatsis.corda.rpc.poolboy.config.RpcClientsMode.DEDICATED
import com.github.manosbatsis.corda.rpc.poolboy.config.RpcClientsMode.POOLED
import com.github.manosbatsis.corda.rpc.poolboy.config.RpcClientsMode.SHARED
import com.github.manosbatsis.corda.rpc.poolboy.config.RpcConfigurationService
import com.github.manosbatsis.corda.rpc.poolboy.connection.NodeRpcConnection
import com.github.manosbatsis.corda.rpc.poolboy.pool.BaseKeyedPool
import com.github.manosbatsis.corda.rpc.poolboy.pool.client.CordaRpcClientPool
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig


class NodeRpcConnectionPool(
        rpcConnectionPoolConfig: GenericKeyedObjectPoolConfig<NodeRpcConnection>,
        nodeParamsService: RpcConfigurationService
) : BaseKeyedPool<PoolKey, NodeRpcConnection>(
        buildConnectionFactory(nodeParamsService), rpcConnectionPoolConfig
){

        companion object{
                fun getPoolConfig(
                        nodeParamsService: RpcConfigurationService
                ): GenericKeyedObjectPoolConfig<NodeRpcConnection> {
                        return initPoolConfig(
                                nodeParamsService.getRpcPoolParams().rpcOpsPool,
                                GenericKeyedObjectPoolConfig<NodeRpcConnection>())
                }
                fun buildConnectionFactory(nodeParamsService: RpcConfigurationService): AbstractRpcConnectionFactory{
                        return when(nodeParamsService.getRpcPoolParams().rpcClientsMode){
                                POOLED -> RpcClientPoolBackedNodeRpcConnectionFactory(
                                        nodeParamsService,
                                        CordaRpcClientPool(nodeParamsService))
                                SHARED -> SharedRpcClientsNodeRpcConnectionFactory(nodeParamsService)
                                DEDICATED -> DedicatedRpcClientsNodeRpcConnectionFactory(nodeParamsService)
                                null -> error("rpcClientPoolMode cannot be null")
                        }
                }
        }

        constructor(nodeParamsService: RpcConfigurationService): this(
                getPoolConfig(nodeParamsService),
                nodeParamsService
        )
}
