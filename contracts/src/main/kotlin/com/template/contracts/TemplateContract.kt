package com.template.contracts

//import net.corda.core.contracts.CommandData
//import net.corda.core.contracts.Contract
import com.template.states.IOUState
import net.corda.core.contracts.*
import net.corda.core.transactions.LedgerTransaction

// ************
// * Contract *
// ************
class IOUContract : Contract {
    companion object {
        // Used to identify our contract when building a transaction.
        const val ID = "com.template.contracts.IOUContract"
    }

    class Create : CommandData

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    override fun verify(tx: LedgerTransaction) {
        // An IOUState can only be created, not transferred or redeemed.
        // Creating an IOUState requires an issuance transaction with no inputs,
        // a single IOUState output, and a Create command.
        // The IOUState created by the issuance transaction must have a non-negative value,
        // and the lender and borrower must be different entities.
        val command = tx.commands.requireSingleCommand<Create>()

        requireThat {
            // Constraints on the shape of the transaction
            "Không cần dùng dữ liệu đầu vào khi cấp IOU." using (tx.inputs.isEmpty())
            "Nên có một trạng thái đầu ra thuộc loại IOUState." using (tx.outputs.size == 1)
            // IOU-specific constraints
            val output = tx.outputsOfType<IOUState>().single()
            "Giá trị của IOU không được âm." using (output.value > 0)
            "Người vay và người cho vay không được trùng nhau." using (output.lender != output.borrower)
            // Constraints on the signers
            val expectedSigners = listOf(output.borrower.owningKey, output.lender.owningKey)
            "Cần đủ 2 người ký." using (command.signers.toSet().size == 2)
            "Người ký và người mua đều phải ký." using (command.signers.containsAll(expectedSigners))
        }
    }

    // Used to indicate the transaction's intent.
    interface Commands : CommandData {
        class Action : Commands
    }
}