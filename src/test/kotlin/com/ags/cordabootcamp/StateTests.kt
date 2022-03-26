package com.ags.cordabootcamp

import net.corda.core.contracts.ContractState
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.testing.core.TestIdentity
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StateTests {
    var alice:Party = TestIdentity(CordaX500Name("Alice", "", "GB")).party
    var bob:Party = TestIdentity(CordaX500Name("Bob", "", "GB")).party

    @Test
    fun tokenStateHasIssuerOwnerAndAmountParamsOfCorrectTypeInConstructor() {
        TokenState(alice, bob, 1)
    }

    @Test
    fun tokenStateHasGettersForIssuerOwnerAndAmount() {
        val tokenState = TokenState(alice, bob, 1)
        assertEquals(alice, tokenState.issuer)
        assertEquals(bob, tokenState.owner)
        assertEquals(1, tokenState.amount)
    }

    @Test
    fun tokenStateImplementsContractState() {
        assertTrue(TokenState(alice, bob, 1) is ContractState)
    }

    @Test
    fun tokenStateHasTwoParticipantsTheIssuerAndTheOwner() {
        val tokenState = TokenState(alice, bob, 1)
        assertEquals(2, tokenState.participants.size)
        assertTrue(tokenState.participants.contains(alice))
        assertTrue(tokenState.participants.contains(bob))
    }
}