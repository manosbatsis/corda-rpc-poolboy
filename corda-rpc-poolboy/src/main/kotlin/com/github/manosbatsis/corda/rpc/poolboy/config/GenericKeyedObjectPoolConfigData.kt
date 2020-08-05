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
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig

/** Data class version of [GenericKeyedObjectPoolConfig] */
@NoArgs
@AllOpen
data class GenericKeyedObjectPoolConfigData(
        var minIdlePerKey: Int = GenericKeyedObjectPoolConfig.DEFAULT_MIN_IDLE_PER_KEY,
        var maxIdlePerKey: Int = GenericKeyedObjectPoolConfig.DEFAULT_MAX_IDLE_PER_KEY,
        var maxTotalPerKey: Int = GenericKeyedObjectPoolConfig.DEFAULT_MAX_TOTAL_PER_KEY,
        var maxTotal: Int = GenericKeyedObjectPoolConfig.DEFAULT_MAX_TOTAL
)