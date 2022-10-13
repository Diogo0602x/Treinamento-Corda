package br.com.seven.training.flows.item

import br.com.seven.training.contracts.ItemContract
import br.com.seven.training.state.ItemOwnershipState
import br.com.seven.training.state.ItemState
import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.contracts.requireThat
import net.corda.core.crypto.SecureHash
import net.corda.core.flows.*
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

object ItemCreationFlow {
    @StartableByRPC
    @InitiatingFlow
    class Initiator(
            private val hash: String
    ): FlowLogic<SignedTransaction>() {

        companion object {
            object GENERATING_TRANSACTION : ProgressTracker.Step("Generating transaction based on new IOU.")
            object VERIFYING_TRANSACTION : ProgressTracker.Step("Verifying contract constraints.")
            object SIGNING_TRANSACTION : ProgressTracker.Step("Signing transaction with our private key.")
            object GATHERING_SIGS : ProgressTracker.Step("Gathering the counterparty's signature.") {
                override fun childProgressTracker() = CollectSignaturesFlow.tracker()
            }

            object FINALISING_TRANSACTION : ProgressTracker.Step("Obtaining notary signature and recording transaction.") {
                override fun childProgressTracker() = FinalityFlow.tracker()
            }

            fun tracker() = ProgressTracker(
                    GENERATING_TRANSACTION,
                    VERIFYING_TRANSACTION,
                    SIGNING_TRANSACTION,
                    GATHERING_SIGS,
                    FINALISING_TRANSACTION
            )
        }

        override val progressTracker: ProgressTracker = tracker()

        @Suspendable
        override fun call(): SignedTransaction {
            val notary = serviceHub.networkMapCache.notaryIdentities.single()
            val secureHash = SecureHash.parse(hash)
            val me = serviceHub.myInfo.legalIdentities.single()
            val participants = serviceHub.networkMapCache.allNodes.map { it.legalIdentities.single() }.filter { it != notary }

            progressTracker.currentStep = GENERATING_TRANSACTION

            val state = ItemState(secureHash, me, participants)
            val ownershipState = ItemOwnershipState(me, UniqueIdentifier(externalId = secureHash.toString()), participants)
            val command = Command(ItemContract.Commands.Create(), participants.map { it.owningKey })

            val txBuilder = TransactionBuilder(notary)
                    .addOutputState(state)
                    .addOutputState(ownershipState)
                    .addAttachment(secureHash)
                    .addCommand(command)

            progressTracker.currentStep = VERIFYING_TRANSACTION
            txBuilder.verify(serviceHub)

            progressTracker.currentStep = SIGNING_TRANSACTION
            val partSignedTx = serviceHub.signInitialTransaction(txBuilder)

            progressTracker.currentStep = GATHERING_SIGS
            val sessions = participants.filter { it != me }.map { initiateFlow(it) }
            val signedTx = subFlow(CollectSignaturesFlow(partSignedTx, sessions, GATHERING_SIGS.childProgressTracker()))

            progressTracker.currentStep = FINALISING_TRANSACTION
            return subFlow(FinalityFlow(signedTx, sessions, FINALISING_TRANSACTION.childProgressTracker()))
        }
    }

    @InitiatedBy(Initiator::class)
    class Acceptor(val otherPartySession: FlowSession): FlowLogic<SignedTransaction>() {
        @Suspendable
        override fun call(): SignedTransaction {
            val signTransactionFlow = object  : SignTransactionFlow(otherPartySession) {
                override fun checkTransaction(stx: SignedTransaction) = requireThat {
                    val outputState = stx.tx.outputs.filter { it.data::class == ItemOwnershipState::class }.single().data as ItemOwnershipState

                    val criteria = QueryCriteria.LinearStateQueryCriteria().withExternalId(listOf(outputState.linearId.externalId!!))
                    val responseList = serviceHub.vaultService.queryBy(ItemOwnershipState::class.java, criteria)
                    "O item já está cadastrado com outro dono" using (responseList.states.size == 0)
                }
            }
            val txID = subFlow(signTransactionFlow).id

            return subFlow(ReceiveFinalityFlow(otherPartySession, expectedTxId = txID))
        }
    }
}