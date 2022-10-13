package br.com.seven.training.state

import br.com.seven.training.contracts.ItemContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.CommandAndState
import net.corda.core.contracts.OwnableState
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.AbstractParty
import net.corda.core.serialization.CordaSerializable

@CordaSerializable
@BelongsToContract(ItemContract::class)
data class ItemState(
        val itemHash: SecureHash,
        override val owner: AbstractParty,
        override val participants: List<AbstractParty>) : OwnableState {
    override fun withNewOwner(newOwner: AbstractParty): CommandAndState {
        val itemState = this.copy(owner = newOwner)
        val command = ItemContract.Commands.Transfer()
        return CommandAndState(command, itemState)
    }
}