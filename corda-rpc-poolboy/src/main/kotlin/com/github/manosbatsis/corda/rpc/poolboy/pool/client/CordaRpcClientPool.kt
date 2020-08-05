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
package com.github.manosbatsis.corda.rpc.poolboy.pool.client

import com.github.manosbatsis.corda.rpc.poolboy.PoolKey
import com.github.manosbatsis.corda.rpc.poolboy.config.RpcConfigurationService
import com.github.manosbatsis.corda.rpc.poolboy.pool.BaseKeyedPool
import net.corda.client.rpc.CordaRPCClient
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig


class CordaRpcClientPool(
        config: GenericKeyedObjectPoolConfig<CordaRPCClient>,
        nodeParamsService: RpcConfigurationService
) : BaseKeyedPool<PoolKey, CordaRPCClient>(
        CordaRpcClientFactory(nodeParamsService), config
){
    companion object{
        fun getPoolConfig(
                nodeParamsService: RpcConfigurationService
        ): GenericKeyedObjectPoolConfig<CordaRPCClient> {
            return initPoolConfig(
                    nodeParamsService.getRpcPoolParams().rpcClientPool,
                    GenericKeyedObjectPoolConfig<CordaRPCClient>())
        }
    }

    constructor(nodeParamsService: RpcConfigurationService): this(
            getPoolConfig(nodeParamsService),
            nodeParamsService
    )
}