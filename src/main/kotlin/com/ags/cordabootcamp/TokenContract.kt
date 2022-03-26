package com.ags.cordabootcamp

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.transactions.LedgerTransaction
import java.security.PublicKey

class TokenContract : Contract {
    companion object{
        val ID = "com.ags.cordabootcamp.TokenContract"
    }
    override fun verify(tx: LedgerTransaction) {
        if (tx.commands.size != 1) {
            throw IllegalArgumentException("Transaction must have one command")
        }
        val command = tx.commands[0]
        val requiredSigners = command.signers
        val commandType = command.value

        if (commandType is Issue) {
            // "Shape" constraints
            if (tx.inputStates.size != 0)
                throw IllegalArgumentException("Issue transaction must have no inputs")
            if (tx.outputStates.size != 1)
                throw IllegalArgumentException("Issue transaction must have one output")
            // Content constraints
            val contractState = tx.outputStates[0]
            if (contractState !is TokenState)
                throw IllegalArgumentException("Output must be a TokenState")
            val tokenState: TokenState = contractState
            if (tokenState.amount <= 0)
                throw IllegalArgumentException("Amount must be greater than zero")
            // Required signer constraints
            val issuerKey: PublicKey = tokenState.issuer.owningKey
            if (!requiredSigners.contains(issuerKey))
                throw IllegalArgumentException("Issuer of the token must sign the transaction")
        } else {
            throw IllegalArgumentException("Command type not recognised")
        }
    }
    class Issue : CommandData {

    }

}

