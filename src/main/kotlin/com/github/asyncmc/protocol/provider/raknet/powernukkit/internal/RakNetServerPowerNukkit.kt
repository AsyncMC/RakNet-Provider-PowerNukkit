package com.github.asyncmc.protocol.provider.raknet.powernukkit.internal

import com.github.asyncmc.protocol.provider.raknet.powernukkit.PowerNukkitRakNet
import com.github.asyncmc.protocol.raknet.api.RakNetServer
import com.github.asyncmc.protocol.raknet.api.RakNetServiceException
import com.github.michaelbull.logging.InlineLogger
import com.nukkitx.network.raknet.RakNetServerListener
import com.nukkitx.network.raknet.RakNetServerSession
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.socket.DatagramPacket
import kotlinx.coroutines.*
import kotlinx.coroutines.future.asDeferred
import java.net.InetSocketAddress
import kotlin.coroutines.CoroutineContext
import com.nukkitx.network.raknet.RakNetServer as PNRakNet

/**
 * @author joserobjr
 * @since 2021-01-06
 */
internal class RakNetServerPowerNukkit(
    private val provider: PowerNukkitRakNet,
    socketAddresses: Set<InetSocketAddress>,
    maxConnections: Int,
): RakNetServer, CoroutineScope {
    private val log = InlineLogger()
    private val job = Job()
    override val coroutineContext: CoroutineContext = job + Dispatchers.IO
    
    init {
        require(socketAddresses.isNotEmpty()) {
            "At least one socket address is required"
        }
    }
    
    private val bindings = socketAddresses.mapIndexed { index, socketAddress ->
        PNRakNet(socketAddress, Runtime.getRuntime().availableProcessors()).also { server ->
            server.maxConnections = maxConnections
            server.listener = PowerNukkitRakNetListener(index, server)
        }
    }

    override val guids: LongArray
        get() = LongArray(bindings.size) { bindings[it].guid }
    
    init {
        launch {
            bindings.forEachIndexed { index, server -> 
                bind(index, server)
            }
        }
        
        job.invokeOnCompletion { 
            bindings.forEach {
                try {
                    it.close()
                } catch (e: Exception) {
                    log.error(RakNetServiceException(e)) { "Failed to close the ${provider.name} binding ${it.bindAddress}" }
                }
            }
        }
    }
    
    private fun bind(index: Int, server: PNRakNet) {
        server.bind().asDeferred().invokeOnCompletion {
            if (it != null && it !is CancellationException) {
                log.warn(RakNetServiceException(it)) { "Failed to bind the RakNet address ${server.bindAddress} using the ${provider.name}, retrying in 3 seconds" }
                launch {
                    delay(3_000)
                    bind(index, server)
                }
            }
        }
    }

    override fun close() {
        try {
            job.cancel(CancellationException("The server was closed"))
        } catch (e: Exception) {
            throw RakNetServiceException(e)
        }
    }
    
    private inner class PowerNukkitRakNetListener(val index: Int, val server: PNRakNet) : RakNetServerListener {
        override fun onConnectionRequest(address: InetSocketAddress): Boolean {
            TODO("Not yet implemented")
        }

        override fun onQuery(address: InetSocketAddress): ByteArray? {
            TODO("Not yet implemented")
        }

        override fun onSessionCreation(session: RakNetServerSession) {
            TODO("Not yet implemented")
        }

        override fun onUnhandledDatagram(ctx: ChannelHandlerContext, packet: DatagramPacket) {
            TODO("Not yet implemented")
        }
    }
}
