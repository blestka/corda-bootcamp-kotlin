package com.ags.cordabootcamp

import co.paralleluniverse.fibers.Suspendable
import com.google.common.collect.ImmutableList
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import java.security.PublicKey

@InitiatingFlow
@StartableByRPC
class TokenIssueFlowInitiator(val owner: Party, val amount: Int): FlowLogic<SignedTransaction>() {
    override val progressTracker:ProgressTracker = ProgressTracker()

    @Suspendable
    override fun call(): SignedTransaction {
        // We choose our transaction's notary (the notary prevents double-spends).
        // We get a reference to our own identity.
        val notary = serviceHub.networkMapCache.notaryIdentities[0]

        /* ============================================================================
         *         TODO 1 - Create our TokenState to represent on-ledger tokens!
         * ===========================================================================*/
        // We create our new TokenState.
        val tokenState = TokenState(ourIdentity, owner, amount)

        /* ============================================================================
         *      TODO 3 - Build our token issuance transaction to update the ledger!
         * ===========================================================================*/
        // We build our transaction.
        val requiredSigners : List<PublicKey> = ImmutableList.of(ourIdentity.owningKey, owner.owningKey)
        val transactionBuilder = TransactionBuilder(notary = notary)
                                    .addOutputState(tokenState, "com.ags.cordabootcamp.TokenContract")
                                    .addCommand(TokenContract.Issue(), requiredSigners)

        /* ============================================================================
         *          TODO 2 - Write our TokenContract to control token issuance!
         * ===========================================================================*/
        // We check our transaction is valid based on its contracts.
        transactionBuilder.verify(serviceHub)

        // We sign the transaction with our private key, making it immutable
        val signedTransaction: SignedTransaction = serviceHub.signInitialTransaction(transactionBuilder)

        val session: FlowSession = initiateFlow(owner)

        // The counter-party signs the transaction
        val fullySignedTransaction: SignedTransaction = subFlow(CollectSignaturesFlow(signedTransaction, listOf(session)))

        // We get the transaction notarised and recorded automatically by the platform
        return subFlow(FinalityFlow(fullySignedTransaction, listOf(session)))
    }
}

@InitiatedBy(TokenIssueFlowInitiator::class)
class Responder(val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        val signTransactionFlow = object : SignTransactionFlow(counterpartySession) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                //Addition checks
            }
        }
        val txId = subFlow(signTransactionFlow).id
        return subFlow(ReceiveFinalityFlow(counterpartySession, expectedTxId = txId))
    }
}
