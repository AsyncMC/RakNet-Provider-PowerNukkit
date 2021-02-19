package com.github.asyncmc.protocol.provider.raknet.powernukkit

import com.github.asyncmc.protocol.provider.raknet.powernukkit.internal.RakNetServerPowerNukkit
import com.github.asyncmc.protocol.raknet.api.RakNetAPI
import com.github.asyncmc.protocol.raknet.api.RakNetServer
import com.github.asyncmc.protocol.raknet.api.RakNetServiceException
import java.net.InetSocketAddress

/**
 * @author joserobjr
 * @since 2021-01-06
 */
class PowerNukkitRakNet: RakNetAPI {
    override val name: String
        get() = "PowerNukkit RakNet"

    override fun openServer(socketAddresses: Set<InetSocketAddress>): RakNetServer {
        try {
            return RakNetServerPowerNukkit(this, socketAddresses)
        } catch (e: Exception) {
            throw RakNetServiceException(e)
        }
    }
}
