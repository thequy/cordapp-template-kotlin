package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.flows.*
import net.corda.core.contracts.Command
import net.corda.core.identity.Party
import net.corda.core.transactions.TransactionBuilder
import com.template.states.IOUState
import com.template.contracts.IOUContract
import net.corda.core.contracts.requireThat
import net.corda.core.flows.FlowSession
import net.corda.core.flows.FinalityFlow
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker

// *********
// * Flows *
// *********
const val ITEM_TYPE = "BOND"

@InitiatingFlow
@StartableByRPC
class IOUFlow(val iouValue: Int, val otherParty: Party) : FlowLogic<Unit>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call() {
        // Retrieve the notary identity from the network map
        val notary = serviceHub.networkMapCache.notaryIdentities[0]
        // Create the transaction components
        val outputState = IOUState(iouValue, ourIdentity, otherParty, ITEM_TYPE)
        val command= Command(IOUContract.Create(), listOf(ourIdentity.owningKey, otherParty.owningKey))
        // Create a transaction builder and add the components
        val txBuilder = TransactionBuilder(notary = notary)
            .addOutputState(outputState, IOUContract.ID)
            .addCommand(command)
        // Verifying the transaction
        txBuilder.verify(serviceHub)
        // Sign the transaction
        val signedTx = serviceHub.signInitialTransaction(txBuilder)
        // Creating a session with the other party
        val otherPartySession = initiateFlow(otherParty)
        // Obtaining the counterparty's signature
        val fullySignedTx = subFlow(CollectSignaturesFlow(signedTx, listOf(otherPartySession), CollectSignaturesFlow.tracker()))
        // Finalise the transaction and then send it to the counterparty
        subFlow(FinalityFlow(fullySignedTx, otherPartySession))
    }
}

@InitiatedBy(IOUFlow::class)
class IOUFlowResponder(val otherPartySession: FlowSession) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        // The transaction involves an IOUState - this ensures that IOUContract will be run to verify the transaction.
        // The IOU’s value is less than some amount (100 in this case)
        val signTransactionFlow = object : SignTransactionFlow(otherPartySession) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                val output = stx.tx.outputs.single().data
                "Đây phải là giao dịch IOU" using (output is IOUState)
                val iou = output as IOUState
                "Giá trị của IOU không được > 100" using (iou.value < 100)
            }
        }

        val expectedTxId = subFlow(signTransactionFlow).id
        subFlow(ReceiveFinalityFlow(otherPartySession, expectedTxId))
    }
}
