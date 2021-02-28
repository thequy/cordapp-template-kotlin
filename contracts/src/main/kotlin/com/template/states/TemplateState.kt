package com.template.states

import com.template.contracts.IOUContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party

// *********
// * State *
// *********
//@BelongsToContract(TemplateContract::class)
//data class TemplateState(val data: String, override val participants: List<AbstractParty> = listOf()) : ContractState
@BelongsToContract(IOUContract::class)
class IOUState(val value: Int, val lender: Party, val borrower: Party, val itemType: String) : ContractState {
    override val participants get() = listOf(lender, borrower)
}

