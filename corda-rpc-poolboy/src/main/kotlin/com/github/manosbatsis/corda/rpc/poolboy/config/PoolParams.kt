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

import com.github.manosbatsis.corda.rpc.poolboy.annotation.AllOpen
import com.github.manosbatsis.corda.rpc.poolboy.annotation.NoArgs

enum class RpcClientsMode{
    SHARED,
    POOLED,
    DEDICATED
}

/**
 * Provides configuration parameters for RPC pooling
 */
@NoArgs
@AllOpen
data class PoolParams(
        /** Whether to use shared, pooled or dedicated RPC Clients, default is shared */
        var rpcClientsMode: RpcClientsMode? = null,
        /** RPC client pool configuration, ignored if [rpcClientPoolDisable] is `true` */
        var rpcClientPool: GenericKeyedObjectPoolConfigData? = null,
        /** RPC Ops pool configuration */
        var rpcOpsPool: GenericKeyedObjectPoolConfigData? = null
) {

    companion object {
        @JvmStatic
        val DEFAULT = PoolParams(
                rpcClientsMode = RpcClientsMode.SHARED,
                rpcClientPool = GenericKeyedObjectPoolConfigData(),
                rpcOpsPool = GenericKeyedObjectPoolConfigData()
        )

        /** Merge in order of precedence, with NodeParams.DEFAULT being the implicit last option */
        @JvmStatic
        fun mergeParams(partialParams: PoolParams, defaults: PoolParams?): PoolParams {
            // Clone the partial source
            val nodeParams = PoolParams(partialParams)
            // Use defaults as necessary
            with(nodeParams) {
                rpcClientsMode = rpcClientsMode ?: defaults?.rpcClientsMode ?: DEFAULT.rpcClientsMode!!
                rpcClientPool = rpcClientPool ?: defaults?.rpcClientPool ?: DEFAULT.rpcClientPool
                rpcOpsPool = rpcOpsPool ?: defaults?.rpcOpsPool ?: DEFAULT.rpcOpsPool
            }
            return nodeParams
        }
    }

    constructor(
            baseNodeParams: PoolParams
    ): this(
            rpcClientsMode = baseNodeParams.rpcClientsMode,
            rpcClientPool = baseNodeParams.rpcClientPool,
            rpcOpsPool = baseNodeParams.rpcOpsPool
    )

}
