package br.com.seven.training.state

import br.com.seven.training.contracts.ItemContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.serialization.CordaSerializable

@CordaSerializable
@BelongsToContract(ItemContract::class)
class ItemOwnershipState(
        val owner: AbstractParty,
        override val linearId: UniqueIdentifier,
        override val participants: List<AbstractParty>) : LinearState