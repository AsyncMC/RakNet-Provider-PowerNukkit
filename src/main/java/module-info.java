import com.github.asyncmc.protocol.raknet.api.RakNetAPI;

module com.github.asyncmc.protocol.provider.raknet.powernukkit {
    requires kotlin.stdlib;
    requires kotlin.stdlib.jdk7;
    requires kotlin.stdlib.jdk8;
    
    requires raknet;
    requires kotlinx.coroutines.core.jvm;
    requires kotlinx.coroutines.jdk8;
    requires kotlin.inline.logger.jvm;
    requires io.netty.transport;
    
    requires com.github.asyncmc.protocol.raknet.api;
    provides RakNetAPI with com.github.asyncmc.protocol.provider.raknet.powernukkit.PowerNukkitRakNet;
    
    exports com.github.asyncmc.protocol.provider.raknet.powernukkit;
}
