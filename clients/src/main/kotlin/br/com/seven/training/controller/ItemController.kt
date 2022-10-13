package br.com.seven.training.controller

import br.com.seven.training.corda.NodeRPCConnection
import br.com.seven.training.flows.item.ItemCreationFlow
import net.corda.core.messaging.startTrackedFlow
import net.corda.core.transactions.SignedTransaction
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("item")
class ItemController {

    @Autowired
    private lateinit var nodeRPCConnection: NodeRPCConnection

    @PostMapping(consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun createItem(@RequestParam("hash", required = true) hash: String ): SignedTransaction {
        return this.nodeRPCConnection.proxy.startTrackedFlowDynamic(ItemCreationFlow.Initiator::class.java, hash).returnValue.get()
    }
}