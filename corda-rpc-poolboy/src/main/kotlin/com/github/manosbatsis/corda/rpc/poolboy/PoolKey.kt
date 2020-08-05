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

import com.github.manosbatsis.corda.rpc.poolboy.annotation.AllOpen
import com.github.manosbatsis.corda.rpc.poolboy.annotation.NoArgs
import net.corda.core.context.Actor
import net.corda.core.context.Trace

@NoArgs
@AllOpen
data class PoolKey(
        /**
         * The string representation of the node legal identity [CordaX500Name]
         * or other "node name" key per your configuration implementation,
         * e.g. application.properties key, database primary key
         * or other business key etc.
         *
         * It is recommended that your [RpcConfigurationService]
         * implementation supports both.
         */
        val nodeName: String,
        /**
         * The external [Trace] to be used by the RPC proxy, if any.
         */
        val externalTrace: Trace? = null,
        /**
         * The impersonated [Actor] (e.g. Corda Account ID)
         * to be used by the RPC proxy, if any.
         */
        val impersonatedActor: Actor? = null

)