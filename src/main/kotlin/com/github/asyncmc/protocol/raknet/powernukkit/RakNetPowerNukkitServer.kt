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

import com.google.common.collect.MapMaker
import com.nukkitx.network.raknet.RakNetServer
import com.nukkitx.network.raknet.RakNetServerListener
import com.nukkitx.network.raknet.RakNetServerSession
import io.ktor.utils.io.core.*
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.socket.DatagramPacket
import java.net.InetSocketAddress
import com.github.asyncmc.protocol.raknet.api.RakNetServer as IRakNetServer
import com.github.asyncmc.protocol.raknet.api.RakNetServerListener as IRakNetServerListener

/**
 * @author joserobjr
 * @since 2021-02-18
 */
class RakNetPowerNukkitServer(
    override val api: RakNetPowerNukkit,
    private val server: RakNetServer,
    listener: IRakNetServerListener
) : IRakNetServer {
    constructor(api: RakNetPowerNukkit, socket: InetSocketAddress, listener: IRakNetServerListener) : this(
        api, RakNetServer(
            socket,
            Runtime.getRuntime().availableProcessors()
        ), listener
    )

    private val sessions: MutableMap<RakNetServerSession, RakNetPowerNukkitSession> = MapMaker().weakKeys().makeMap()

    init {
        server.listener = RakNetPowerNukkitServerListenerAdapter(listener)
    }

    private operator fun get(session: RakNetServerSession) = requireNotNull(sessions[session]) {
        "Session $session not found!"
    }
    
    private operator fun minusAssign(session: RakNetPowerNukkitSession) {
        sessions.remove(session.powerNukkit)
    }
    
    private inner class RakNetPowerNukkitServerListenerAdapter(
        val listener: IRakNetServerListener
    ) : RakNetServerListener {
        inline val server get() = this@RakNetPowerNukkitServer
        
        override fun onConnectionRequest(address: InetSocketAddress): Boolean {
            return listener.onConnectionRequest(address)
        }

        override fun onQuery(address: InetSocketAddress): ByteArray? {
            return listener.onQuery(server, address)
        }

        override fun onSessionCreation(session: RakNetServerSession) {
            val facade = RakNetPowerNukkitSession(server, session, server::minusAssign)
            sessions[session] = facade
            return listener.onSessionCreated(server, facade)
        }

        override fun onUnhandledDatagram(ctx: ChannelHandlerContext, packet: DatagramPacket) {
            return listener.onUnknownDatagram(server, null, packet.sender()) {
                packet.content().toByteReadPacket()
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RakNetPowerNukkitServer

        if (api != other.api) return false
        if (server != other.server) return false

        return true
    }

    override fun hashCode(): Int {
        var result = api.hashCode()
        result = 31 * result + server.hashCode()
        return result
    }
}

