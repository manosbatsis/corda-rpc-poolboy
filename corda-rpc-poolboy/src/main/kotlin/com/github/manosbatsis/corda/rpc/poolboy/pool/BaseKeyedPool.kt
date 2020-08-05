package com.github.manosbatsis.corda.rpc.poolboy.pool

import com.github.manosbatsis.corda.rpc.poolboy.config.GenericKeyedObjectPoolConfigData
import org.apache.commons.pool2.BaseKeyedPooledObjectFactory
import org.apache.commons.pool2.impl.GenericKeyedObjectPool
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig

open class BaseKeyedPool<K,T>(
        keyedPooledObjectFactory: BaseKeyedPooledObjectFactory<K, T>,
        config: GenericKeyedObjectPoolConfig<T>
): GenericKeyedObjectPool<K,T>(keyedPooledObjectFactory){
    companion object {
        fun <T> initPoolConfig(
                with: GenericKeyedObjectPoolConfigData?,
                targetConfig: GenericKeyedObjectPoolConfig<T>
        ): GenericKeyedObjectPoolConfig<T> {
            if (with != null) {
                with(with) {
                    targetConfig.minIdlePerKey = minIdlePerKey
                    targetConfig.maxIdlePerKey = maxIdlePerKey
                    targetConfig.maxTotalPerKey = maxTotalPerKey
                    targetConfig.maxTotal = maxTotal
                }
            }
            return targetConfig
        }
    }
}