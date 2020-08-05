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
import com.github.manosbatsis.corda.rpc.poolboy.support.Util.Companion.createCordaRPCClient
import net.corda.client.rpc.CordaRPCClient
import net.corda.core.utilities.contextLogger
import org.apache.commons.pool2.BaseKeyedPooledObjectFactory
import org.apache.commons.pool2.PooledObject
import org.apache.commons.pool2.impl.DefaultPooledObject


class CordaRpcClientFactory(
        val nodeParamsService: RpcConfigurationService
): BaseKeyedPooledObjectFactory<PoolKey, CordaRPCClient>() {

    companion object {
        private val logger = contextLogger()
    }

    /**
     * Wrap the provided instance with an implementation of
     * {@link PooledObject}.
     *
     * @param value the instance to wrap
     *
     * @return The provided instance, wrapped by a {@link PooledObject}
     */
    override fun wrap(value: CordaRPCClient): PooledObject<CordaRPCClient> {
        logger.debug("wrap CordaRPCClient: ${value}")
        return DefaultPooledObject(value)
    }

    /**
     * Create an instance that can be served by the pool.
     *
     * @param key the key used when constructing the object
     * @return an instance that can be served by the pool
     *
     * @throws Exception if there is a problem creating a new instance,
     *    this will be propagated to the code requesting an object.
     */
    override fun create(key: PoolKey): CordaRPCClient {
        logger.debug("create for key $key")
        val config = nodeParamsService.getNodeRpcConnectionConfig(key)
        return createCordaRPCClient(config.nodeParams)
    }

    override fun makeObject(key: PoolKey): PooledObject<CordaRPCClient> {
        logger.debug("makeObject for key $key")
        return super.makeObject(key)
    }

    /**
     * Ensures that the instance is safe to be returned by the pool.
     * <p>
     * The default implementation always returns {@code true}.
     * </p>
     *
     * @param key the key used when selecting the object
     * @param p a {@code PooledObject} wrapping the instance to be validated
     * @return always <code>true</code> in the default implementation
     */
    override fun validateObject(key: PoolKey, p: PooledObject<CordaRPCClient>): Boolean {
        val valid = super.validateObject(key, p)
        logger.debug("validateObject, valid: $valid, key: ${key}")
        return valid
    }

    /**
     * Reinitialize an instance to be returned by the pool.
     * <p>
     * The default implementation is a no-op.
     * </p>
     *
     * @param key the key used when selecting the object
     * @param p a {@code PooledObject} wrapping the instance to be activated
     */
    override fun activateObject(key: PoolKey, p: PooledObject<CordaRPCClient>) {
        logger.debug("activateObject $p for key: ${key}")
        super.activateObject(key, p)
    }

    /**
     * Destroy an instance no longer needed by the pool.
     * <p>
     * The default implementation is a no-op.
     * </p>
     *
     * @param key the key used when selecting the instance
     * @param p a {@code PooledObject} wrapping the instance to be destroyed
     */
    override fun destroyObject(key: PoolKey, p: PooledObject<CordaRPCClient>) {
        logger.debug("destroyObject $p for key: ${key}")
        super.destroyObject(key, p)
    }

    /**
     * Uninitialize an instance to be returned to the idle object pool.
     * <p>
     * The default implementation is a no-op.
     * </p>
     *
     * @param key the key used when selecting the object
     * @param p a {@code PooledObject} wrapping the instance to be passivated
     */
    override fun passivateObject(key: PoolKey, p: PooledObject<CordaRPCClient>) {
        logger.debug("passivateObject $p for key: ${key}")
        super.passivateObject(key, p)
    }
}
