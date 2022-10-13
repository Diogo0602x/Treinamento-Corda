package br.com.seven.training.contracts

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.transactions.LedgerTransaction

class ItemContract: Contract {
    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<Commands>()

        when(command.value) {
            is Commands.Create -> {}
            is Commands.Transfer -> {}
        }
    }

    interface Commands: CommandData {
        class Create: Commands
        class Transfer: Commands
    }
}