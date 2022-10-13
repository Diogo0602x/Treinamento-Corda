package br.com.seven.training.corda

import net.corda.client.rpc.CordaRPCClient
import net.corda.core.crypto.SecureHash
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.utilities.NetworkHostAndPort
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.InputStream
import javax.annotation.PostConstruct

@Component
class NodeRPCConnection {
    @Value("\${config.rpc.host}")
    lateinit var host: String
    @Value("\${config.rpc.port}")
    var port: Int = 0
    @Value("\${config.rpc.username}")
    lateinit var username: String
    @Value("\${config.rpc.password}")
    lateinit var password: String

    lateinit var proxy: CordaRPCOps

    @PostConstruct
    fun initializeConnection() {
        val rpcAddress = NetworkHostAndPort.parse("$host:$port")
        val rpcClient = CordaRPCClient(rpcAddress)
        val rpcConnection = rpcClient.start(username, password)
        proxy = rpcConnection.proxy
    }

    fun saveAttachment(attachment: Attachment): SecureHash {
        return this.proxy.uploadAttachment(attachment.inputStream)
    }

    fun openAttachment(hash: SecureHash): InputStream {
        return proxy.openAttachment(hash)
    }
}