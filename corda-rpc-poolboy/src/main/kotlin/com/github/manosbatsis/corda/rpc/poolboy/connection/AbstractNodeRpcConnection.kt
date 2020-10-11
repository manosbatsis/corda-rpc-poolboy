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
import com.github.manosbatsis.corda.rpc.poolboy.support.Util.Companion.createCordaRPCClient
import net.corda.client.rpc.CordaRPCClient
import net.corda.client.rpc.CordaRPCClientConfiguration.Companion.DEFAULT
import net.corda.client.rpc.CordaRPCConnection
import net.corda.client.rpc.RPCException
import net.corda.core.messaging.CordaRPCOps
import org.apache.activemq.artemis.api.core.ActiveMQSecurityException
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit
import javax.annotation.PreDestroy


/**
 * Abstract implementation of [NodeRpcConnection],
 * wraps a [CordaRPCClient]
 */
abstract class AbstractNodeRpcConnection(
        private val config: NodeRpcConnectionConfig,
        val rpcClient: CordaRPCClient = createCordaRPCClient(config.nodeParams)

) : NodeRpcConnection {

    companion object {
        private val logger = LoggerFactory
                .getLogger(AbstractNodeRpcConnection::class.java)

    }


    private lateinit var rpcConnection: CordaRPCConnection

    /**
     * Attempt to obtain a [CordaRPCConnection], retry five times with
     * a five second delay in case of an [RPCException]error
     */
    fun createProxy(): CordaRPCOps {
        var created: CordaRPCOps? = null
        var attemptCount = 0
        var attemptInterval = DEFAULT.connectionRetryInterval.toMillis()
        while (created == null) {
            logger.debug("Initializing RPC connection for {}, attempt: {}",
                    config.targetLegalIdentity, attemptCount)
            attemptCount++
            try {
                rpcConnection = rpcClient.start(
                        username = config.nodeParams.username ?: error("A username is required"),
                        password = config.nodeParams.password ?: error("A password is required"),
                        targetLegalIdentity = config.targetLegalIdentity,
                        externalTrace = config.externalTrace,
                        impersonatedActor = config.impersonatedActor,
                        gracefulReconnect = config.gracefulReconnect)
                created = rpcConnection.proxy
            } catch (secEx: ActiveMQSecurityException) {
                // Happens when incorrect credentials provided - no point to retry connecting.
                throw secEx
            } catch (e: Exception) {
                if (attemptCount >= DEFAULT.maxReconnectAttempts) throw e
                logger.warn("Failed initializing RPC connection for {}, impersonatedActor: {}",
                        config.targetLegalIdentity,
                        config.impersonatedActor)
                TimeUnit.MILLISECONDS.sleep(attemptInterval)
                attemptInterval *= DEFAULT.connectionRetryIntervalMultiplier.toLong()
            }
        }
        logger.debug(
                "Initialized RPC connection for {}, impersonatedActor: {}",
                config.targetLegalIdentity,
                config.impersonatedActor)
        return created
    }

    /** Try cleaning up on [PreDestroy] */
    @PreDestroy
    fun onPreDestroy() {
        try{
            if (::rpcConnection.isInitialized) rpcConnection.notifyServerAndClose()
        }
        catch (e: Throwable){
            logger.warn("Error notifying server ${config.nodeParams.address}", e)
        }
    }

    /** Controls ignoring this node when providing node infos */
    override fun skipInfo(): Boolean = config.nodeParams.skipInfo ?: false
}

