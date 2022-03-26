package com.ags.cordabootcamp

import com.google.common.collect.ImmutableList
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party

@BelongsToContract(TokenContract::class)
class TokenState(val issuer: Party, val owner: Party, val amount: Int): ContractState {
    override val participants: List<AbstractParty>
        get() = ImmutableList.of(issuer, owner)
}