/*
 *     AsyncMC - A fully async, non blocking, thread safe and open source Minecraft server implementation
 *     Copyright (C) 2020  José Roberto de Araújo Júnior <joserobjr@gamemods.com.br>
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.github.asyncmc.protocol.raknet.powernukkit

import com.github.asyncmc.protocol.raknet.api.RakNetDisconnectReason
import com.nukkitx.network.raknet.EncapsulatedPacket
import com.nukkitx.network.raknet.RakNetSession
import com.nukkitx.network.raknet.RakNetSessionListener
import com.nukkitx.network.raknet.RakNetState
import com.nukkitx.network.util.DisconnectReason
import io.ktor.utils.io.core.*
import io.netty.buffer.ByteBuf
import java.net.SocketAddress
import com.github.asyncmc.protocol.raknet.api.RakNetSession as IRakNetSession
import com.github.asyncmc.protocol.raknet.api.RakNetSessionListener as IRakNetSessionListener

/**
 * @author joserobjr
 * @since 2021-02-18
 */
class RakNetPowerNukkitSession(
    override val protocolServer: RakNetPowerNukkitServer,
    val powerNukkit: RakNetSession,
    private val closeHandler: (RakNetPowerNukkitSession) -> Unit
): IRakNetSession {
    override val clientSocket: SocketAddress get() = powerNukkit.address
    var listener: IRakNetSessionListener? = null
    init {
        powerNukkit.listener = RakNetPowerNukkitSessionListenerAdapter() 
    }
    
    private inner class RakNetPowerNukkitSessionListenerAdapter: RakNetSessionListener {
        inline val session get() = this@RakNetPowerNukkitSession
        override fun onSessionChangeState(state: RakNetState) {
            try {
                
            } finally {
                if (state == RakNetState.UNCONNECTED) {
                    closeHandler(session)
                }
            }
        }

        override fun onDisconnect(reason: DisconnectReason) {
            try {
                listener?.onDisconnect(RakNetDisconnectReason.valueOf(reason.name))
            } finally {
                closeHandler(session)
            }
        }

        override fun onEncapsulated(packet: EncapsulatedPacket) {
            listener?.onPacketReceived(false) {
                packet.buffer.toByteReadPacket()
            }
        }

        override fun onDirect(buf: ByteBuf) {
            listener?.onPacketReceived(true) {
                buf.toByteReadPacket()
            }
        }
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RakNetPowerNukkitSession

        if (protocolServer != other.protocolServer) return false
        if (powerNukkit != other.powerNukkit) return false

        return true
    }

    override fun hashCode(): Int {
        var result = protocolServer.hashCode()
        result = 31 * result + powerNukkit.hashCode()
        return result
    }


}
