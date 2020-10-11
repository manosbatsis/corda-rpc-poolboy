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
package com.github.manosbatsis.corda.rpc.poolboy.support

import com.github.manosbatsis.corda.rpc.poolboy.config.NodeParams
import net.corda.client.rpc.CordaRPCClient
import net.corda.client.rpc.CordaRPCClientConfiguration
import net.corda.client.rpc.GracefulReconnect
import net.corda.core.messaging.ClientRpcSslOptions
import net.corda.core.utilities.NetworkHostAndPort
import net.corda.core.utilities.contextLogger
import java.io.File

interface Util {

    companion object {
        private val logger = contextLogger()

        fun defaultGracefullReconnect(nodeParams: NodeParams) =
                GracefulReconnect(
                        onDisconnect = { onDisconnect() },
                        onReconnect = { onReconnect() },
                        maxAttempts = nodeParams.maxReconnectAttempts ?: 5)

        fun createCordaRPCClient(config: NodeParams) =
                CordaRPCClient(
                        hostAndPort = buildRpcAddress(config),
                        configuration = buildRpcClientConfig(config),
                        sslConfiguration = clientRpcSslOptions(config),
                        classLoader = Util::class.java.classLoader,
                        customSerializers = config.customSerializers)

        fun onReconnect() {
            logger.warn("Default GracefulReconnect: reconnected")
        }

        fun onDisconnect() {
            logger.warn("Default GracefulReconnect: disconnected")
        }

        /** Build a [NetworkHostAndPort] from the configuration host and port */
        fun buildRpcAddress(config: NodeParams): NetworkHostAndPort {
            logger.warn("buildRpcAddress, config: $config")
            val addressParts = config.address!!.split(":")
            val rpcAddress = NetworkHostAndPort(addressParts[0], addressParts[1].toInt())
            return rpcAddress
        }

        /** Build a [CordaRPCClientConfiguration] based on the provided [NodeParams] */
        fun buildRpcClientConfig(config: NodeParams): CordaRPCClientConfiguration {
            var cordaRPCClientConfiguration = CordaRPCClientConfiguration(
                    connectionMaxRetryInterval = config.connectionMaxRetryInterval!!,
                    connectionRetryInterval = config.connectionRetryInterval!!,
                    connectionRetryIntervalMultiplier = config.connectionRetryIntervalMultiplier!!,
                    deduplicationCacheExpiry = config.deduplicationCacheExpiry!!,
                    maxFileSize = config.maxFileSize!!,
                    maxReconnectAttempts = config.maxReconnectAttempts!!,
                    observationExecutorPoolSize = config.observationExecutorPoolSize!!,
                    reapInterval = config.reapInterval!!,
                    trackRpcCallSites = config.trackRpcCallSites!!,
                    minimumServerProtocolVersion = config.minimumServerProtocolVersion!!
            )
            return cordaRPCClientConfiguration
        }

        /**
         * Get the [ClientRpcSslOptions] if properly configured
         * @throws [IllegalArgumentException] if the configuration is incomplete
         * @return the options if properly configured, `null` otherwise
         */
        fun clientRpcSslOptions(config: NodeParams): ClientRpcSslOptions? {
            val trustStoreProps = listOf(
                    config.trustStorePassword,
                    config.trustStorePath)
            // Check for incomplete SSL configuration
            return if (trustStoreProps.any { it == null || it.isBlank() }) {
                // It's probably a human error if only one of [path, password],
                // better throw an error
                if (trustStoreProps.any { it != null }) throw IllegalArgumentException(
                        "Both or none of [trustStorePassword, trustStorePath] " +
                                "should be configured for node ${config.address}")
                // Warn if not using SSL for remote RPC connections
                if (listOf("localhost", "127.0.0.1").none { config.address!!.startsWith(it) })
                    logger.warn("Not using SSL for RPC to remote node ${config.address}")
                null
            }
            // Config looks legit, pass it on
            else ClientRpcSslOptions(
                    File(config.trustStorePath).toPath(),
                    config.trustStorePassword!!,
                    config.trustStoreProvider)

        }
    }

}