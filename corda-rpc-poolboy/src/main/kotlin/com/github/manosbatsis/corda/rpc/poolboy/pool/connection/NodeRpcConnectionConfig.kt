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

import com.github.manosbatsis.corda.rpc.poolboy.annotation.AllOpen
import com.github.manosbatsis.corda.rpc.poolboy.annotation.NoArgs
import com.github.manosbatsis.corda.rpc.poolboy.config.NodeParams
import net.corda.client.rpc.GracefulReconnect
import net.corda.core.context.Actor
import net.corda.core.context.Trace
import net.corda.core.identity.CordaX500Name


@NoArgs
@AllOpen
data class NodeRpcConnectionConfig(
        val nodeParams: NodeParams,
        val targetLegalIdentity: CordaX500Name,
        val externalTrace: Trace? = null,
        val impersonatedActor: Actor? = null,
        val gracefulReconnect: GracefulReconnect? = null

) {
    constructor(
            address: String,
            username: String,
            password: String,
            eager: Boolean,
            externalTrace: Trace? = null,
            impersonatedActor: Actor? = null,
            targetLegalIdentity: CordaX500Name,
            gracefulReconnect: GracefulReconnect? = null
    ) : this(
            nodeParams = NodeParams.mergeParams(NodeParams(
                    address = address,
                    username = username,
                    password = password,
                    eager = eager)),
            externalTrace = externalTrace,
            impersonatedActor = impersonatedActor,
            targetLegalIdentity = targetLegalIdentity,
            gracefulReconnect = gracefulReconnect
    )
}

