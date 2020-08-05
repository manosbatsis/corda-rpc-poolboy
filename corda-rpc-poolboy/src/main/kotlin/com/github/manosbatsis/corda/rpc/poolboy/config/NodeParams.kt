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
import net.corda.client.rpc.CordaRPCClientConfiguration
import net.corda.core.serialization.SerializationCustomSerializer
import java.time.Duration

/**
 * Configuration of a single node from an RPC perspective, i.e.
 * information corresponding to RPC user credentials and an [CordaRPCClientConfiguration].
 * The `DEFAULTS` are taken from [CordaRPCClientConfiguration.DEFAULT] and can be overridden
 * using `corbeans.nodes.default.xx` in your `application.properties`
 */
@NoArgs
@AllOpen
data class NodeParams(

        /** The x500 name to use for the node when testing */
        var partyName: String? = null,
        /** RPC user */
        var username: String? = null,
        /** RPC user password */
        var password: String? = null,
        /** Node RPC address */
        var address: String? = null,
        /** Node administration RPC address */
        var adminAddress: String? = null,
        /**
         * Whether to use an eagerly initialised [NodeRpcConnection] implementation.
         * Default is `false`. Setting to `true` will probably break integration tests so you will need separate
         * application.properties for those.
         */
        var eager: Boolean? = null,
        /** Corresponds to [ClientRpcSslOptions.trustStorePath] */
        var trustStorePath: String? = null,
        /** Corresponds to [ClientRpcSslOptions.trustStorePassword] */
        var trustStorePassword: String? = null,
        /** Corresponds to [ClientRpcSslOptions.trustStoreProvider] */
        var trustStoreProvider: String = "JKS",
        /** Whether to use GracefulReconnect (4.3+) */
        var disableGracefulReconnect: Boolean? = null,
        // Configuration properties for Corda v4.0+
        // ====================================
        /**
         * The maximum retry interval for re-connections. The client will retry connections if the host is lost with ever
         * increasing spacing until the max is reached. The default is 3 minutes.
         */
        var connectionMaxRetryInterval: Duration? = null,
        /*** The base retry interval for reconnection attempts. The default is 5 seconds. */
        var connectionRetryInterval: Duration? = null,
        /** The retry interval multiplier for exponential backoff. The default is 1.5 */
        var connectionRetryIntervalMultiplier: Double? = null,
        /** The cache expiry of a deduplication watermark per client. Default is 1 day. */
        var deduplicationCacheExpiry: Duration? = null,
        /** Maximum size of RPC responses, in bytes. Default is 10mb. */
        var maxFileSize: Int? = null,
        /** Maximum reconnect attempts on failover or disconnection. The default is -1 which means unlimited. */
        var maxReconnectAttempts: Int? = null,
        /**
         * The minimum protocol version required from the server. This is equivalent to the node's platform version number.
         * If this minimum version is not met, an exception will be thrown at startup. If you use features introduced in a
         * later version, you can bump this to match the platform version you need and get an early check that runs
         * before you do anything.
         */
        var minimumServerProtocolVersion: Int? = null,
        /**
         * The number of threads to use for observations for executing Observable.onNext. This only has any effect if
         * observableExecutor is null (which is the default). The default is 4.
         */
        var observationExecutorPoolSize: Int? = null,
        /**
         * The interval of unused observable reaping. Leaked Observables (unused ones) are detected using weak references and
         * are cleaned up in batches in this interval. If set too large it will waste server side resources for this duration.
         * If set too low it wastes client side cycles. The default is to check once per second.
         */
        var reapInterval: Duration? = null,
        /**
         * If set to true the client will track RPC call sites (default is false). If an error occurs subsequently during the RPC
         * or in a returned Observable stream the stack trace of the originating RPC will be shown as well. Note that
         * constructing call stacks is a moderately expensive operation.
         */
        var trackRpcCallSites: Boolean? = null,
        /** Whether to skip this node from actuator */
        var skipInfo: Boolean? = null,

        var customSerializers: Set<SerializationCustomSerializer<*, *>>? = null
) {

    companion object {
        @JvmStatic
        val NODENAME_DEFAULT = "default"

        @JvmStatic
        val NODENAME_CORDFORM = "cordform"

        @JvmStatic
        val DEFAULT = NodeParams(
                eager = false,
                connectionMaxRetryInterval = CordaRPCClientConfiguration.DEFAULT.connectionMaxRetryInterval,
                connectionRetryInterval = CordaRPCClientConfiguration.DEFAULT.connectionRetryInterval,
                connectionRetryIntervalMultiplier = CordaRPCClientConfiguration.DEFAULT.connectionRetryIntervalMultiplier,
                deduplicationCacheExpiry = CordaRPCClientConfiguration.DEFAULT.deduplicationCacheExpiry,
                maxFileSize = CordaRPCClientConfiguration.DEFAULT.maxFileSize,
                disableGracefulReconnect = false,
                maxReconnectAttempts = CordaRPCClientConfiguration.DEFAULT.maxReconnectAttempts,
                minimumServerProtocolVersion = CordaRPCClientConfiguration.DEFAULT.minimumServerProtocolVersion,
                observationExecutorPoolSize = CordaRPCClientConfiguration.DEFAULT.observationExecutorPoolSize,
                reapInterval = CordaRPCClientConfiguration.DEFAULT.reapInterval,
                trackRpcCallSites = CordaRPCClientConfiguration.DEFAULT.trackRpcCallSites,
                skipInfo = false
        )

        /** Merge in order of precedence, with NodeParams.DEFAULT being the implicit last option */
        @JvmStatic
        fun mergeParams(partialParams: NodeParams, defaults: NodeParams?): NodeParams {
            // Verify required properties exist
            requireNotNull(partialParams.partyName) { "Node configuration is missing a partyName property" }
            requireNotNull(partialParams.username) { "Node configuration is missing a username property" }
            requireNotNull(partialParams.password) { "Node configuration is missing a password property" }
            requireNotNull(partialParams.address) { "Node configuration is missing an address property" }
            requireNotNull(partialParams.adminAddress) { "Node configuration is missing an adminAddress property" }
            // Use defaults as necessary
            return with(partialParams) {
                copy(
                        eager = eager ?: defaults?.eager ?: DEFAULT.eager!!,
                        connectionMaxRetryInterval = connectionMaxRetryInterval
                                ?: defaults?.connectionMaxRetryInterval
                                ?: DEFAULT.connectionMaxRetryInterval!!,
                        connectionRetryInterval = connectionRetryInterval
                                ?: defaults?.connectionRetryInterval
                                ?: DEFAULT.connectionRetryInterval!!,
                        connectionRetryIntervalMultiplier = connectionRetryIntervalMultiplier
                                ?: defaults?.connectionRetryIntervalMultiplier
                                ?: DEFAULT.connectionRetryIntervalMultiplier!!,
                        deduplicationCacheExpiry = deduplicationCacheExpiry
                                ?: defaults?.deduplicationCacheExpiry
                                ?: DEFAULT.deduplicationCacheExpiry!!,
                        maxFileSize = maxFileSize
                                ?: defaults?.maxFileSize
                                ?: DEFAULT.maxFileSize!!,
                        disableGracefulReconnect = disableGracefulReconnect
                                ?: defaults?.disableGracefulReconnect
                                ?: DEFAULT.disableGracefulReconnect!!,
                        maxReconnectAttempts = maxReconnectAttempts
                                ?: defaults?.maxReconnectAttempts
                                ?: DEFAULT.maxReconnectAttempts!!,
                        minimumServerProtocolVersion = minimumServerProtocolVersion
                                ?: defaults?.minimumServerProtocolVersion
                                ?: DEFAULT.minimumServerProtocolVersion!!,
                        observationExecutorPoolSize = observationExecutorPoolSize
                                ?: defaults?.observationExecutorPoolSize
                                ?: DEFAULT.observationExecutorPoolSize!!,
                        reapInterval = reapInterval
                                ?: defaults?.reapInterval
                                ?: DEFAULT.reapInterval!!,
                        trackRpcCallSites = trackRpcCallSites
                                ?: defaults?.trackRpcCallSites
                                ?: DEFAULT.trackRpcCallSites!!,
                        skipInfo = skipInfo
                                ?: defaults?.skipInfo
                                ?: DEFAULT.skipInfo!!
                )
            }

        }
    }
/*
    constructor(
            baseNodeParams: NodeParams
    ): this(
            partyName = baseNodeParams.partyName,
            username = baseNodeParams.username,
            password = baseNodeParams.password,
            address = baseNodeParams.address,
            adminAddress = baseNodeParams.adminAddress,
            eager = baseNodeParams.eager,
            trustStorePath = baseNodeParams.trustStorePath,
            trustStorePassword = baseNodeParams.trustStorePassword,
            trustStoreProvider = baseNodeParams.trustStoreProvider,
            disableGracefulReconnect = baseNodeParams.disableGracefulReconnect,
            connectionMaxRetryInterval = baseNodeParams.connectionMaxRetryInterval,
            connectionRetryInterval = baseNodeParams.connectionRetryInterval,
            connectionRetryIntervalMultiplier = baseNodeParams.connectionRetryIntervalMultiplier,
            deduplicationCacheExpiry = baseNodeParams.deduplicationCacheExpiry,
            maxFileSize = baseNodeParams.maxFileSize,
            maxReconnectAttempts = baseNodeParams.maxReconnectAttempts,
            minimumServerProtocolVersion = baseNodeParams.minimumServerProtocolVersion,
            observationExecutorPoolSize = baseNodeParams.observationExecutorPoolSize,
            reapInterval = baseNodeParams.reapInterval,
            trackRpcCallSites = baseNodeParams.trackRpcCallSites,
            skipInfo = baseNodeParams.skipInfo,
            customSerializers = baseNodeParams.customSerializers
    )*/


}
