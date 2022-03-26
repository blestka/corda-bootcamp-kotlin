package com.ags.cordabootcamp

import net.corda.core.contracts.Contract
import net.corda.core.identity.CordaX500Name
import net.corda.testing.contracts.DummyState
import net.corda.testing.core.DummyCommandData
import net.corda.testing.core.TestIdentity
import net.corda.testing.node.MockServices
import net.corda.testing.node.ledger
import org.junit.Test

class ContractTests {
    val alice = TestIdentity(CordaX500Name("Alice", "", "GB"))
    val bob = TestIdentity(CordaX500Name("Bob", "", "GB"))
    val ledgerServices = MockServices(TestIdentity(CordaX500Name("TestId", "", "GB")))

    private val tokenState = TokenState(alice.party, bob.party, 1)

    @Test
    fun tokenContractImplementsContract() {
        assert(TokenContract() is Contract)
    }

    @Test
    fun validTokenContract() {
        ledgerServices.ledger {
            transaction {
                // valid transaction, will pass
                output(TokenContract.ID, tokenState)
                command(listOf(alice.publicKey, bob.publicKey), TokenContract.Issue())
                verifies()
            }
        }
    }


    @Test
    fun tokenContractRequiresZeroInputsInTheTransaction() {
        ledgerServices.ledger {
            transaction {
                // has an input, will fail
                input(TokenContract.ID, tokenState)
                output(TokenContract.ID, tokenState)
                command(listOf(alice.publicKey, bob.publicKey), TokenContract.Issue())
                fails()
            }
        }

    }

    @Test
    fun tokenContractRequiresOneOutputInTheTransaction() {
        ledgerServices.ledger {
            transaction {
                // Has two outputs, will fail
                output(TokenContract.ID, tokenState)
                output(TokenContract.ID, tokenState)
                command(listOf(alice.publicKey, bob.publicKey), TokenContract.Issue())
                fails()
            }
        }
    }

    @Test
    fun tokenContractRequiresOneCommandInTheTransaction() {
        ledgerServices.ledger {
            transaction {
                // Has two commands, will fail
                output(TokenContract.ID, tokenState)
                command(listOf(alice.publicKey, bob.publicKey), TokenContract.Issue())
                command(listOf(alice.publicKey, bob.publicKey), TokenContract.Issue())
                fails()
            }
        }
    }


    @Test
    fun tokenContractRequiresTheTransactionsOutputToBeATokenState() {
        ledgerServices.ledger {
            transaction {
                // Has wrong output, will fail
                output(TokenContract.ID, DummyState())
                command(listOf(alice.publicKey, bob.publicKey), TokenContract.Issue())
                command(listOf(alice.publicKey, bob.publicKey), TokenContract.Issue())
                fails()
            }
        }
    }


    @Test
    fun tokenContractRequiresTheTransactionsOutputToHaveAPositiveAmount() {
        val zeroTokenState = TokenState(alice.party, bob.party, 0)
        val negativeTokenState = TokenState(alice.party, bob.party, -1)

        ledgerServices.ledger {
            transaction {
                // Has zero amount TokenState, will fail
                output(TokenContract.ID, zeroTokenState)
                command(listOf(alice.publicKey, bob.publicKey), TokenContract.Issue())
                fails()
            }
        }

        ledgerServices.ledger {
            transaction {
                // Has negative amount TokenState, will fail
                output(TokenContract.ID, negativeTokenState)
                command(listOf(alice.publicKey, bob.publicKey), TokenContract.Issue())
                fails()
            }
        }
    }

    @Test
    fun tokenContractRequiresTheTransactionsCommandToBeAnIssueCommand() {
        ledgerServices.ledger {
            transaction {
                // valid transaction, will pass
                output(TokenContract.ID, tokenState)
                command(listOf(alice.publicKey, bob.publicKey), DummyCommandData)
                fails()
            }
        }
    }

    @Test
    fun tokenContractRequiresTheIssuerToBeARequiredSignerInTheTransaction() {
        val tokenStateWhereBobIsIssuer = TokenState(bob.party, alice.party, 1)

        ledgerServices.ledger {
            transaction {
                // Required signer is not an issuer, will fail
                output(TokenContract.ID, tokenState)
                command(listOf(bob.publicKey), TokenContract.Issue())
                fails()
            }
        }

        ledgerServices.ledger {
            transaction {
                // Issuer is also not a required signer, will fail
                output(TokenContract.ID, tokenStateWhereBobIsIssuer)
                command(alice.publicKey, TokenContract.Issue())
                fails()
            }
        }

        ledgerServices.ledger {
            transaction {
                // Issuer is also a required signer, will verify
                output(TokenContract.ID, tokenStateWhereBobIsIssuer)
                command(listOf(alice.publicKey, bob.publicKey), TokenContract.Issue())
                verifies()
            }
        }
    }
}