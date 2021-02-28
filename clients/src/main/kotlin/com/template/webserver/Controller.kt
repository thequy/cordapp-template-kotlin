package com.template.webserver

import net.corda.core.contracts.ContractState
import net.corda.core.messaging.vaultQueryBy
import com.template.states.IOUState
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.StateRef
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.lang.StringBuilder
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Define your API endpoints here.
 */
@RestController
@RequestMapping("/api") // The paths for HTTP requests are relative to this base path.
class Controller(rpc: NodeRPCConnection) {

    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    private val proxy = rpc.proxy

    @GetMapping(value = ["/status"], produces = ["text/plain"])
    private fun status(): String {
        return "OK"
    }

    @GetMapping(value = ["/info"], produces = ["text/plain"])
    private fun info(): String {
        val returnVal = StringBuilder()
        val serverTime = LocalDateTime.ofInstant(proxy.currentNodeTime(), ZoneId.of("UTC")).toString()
        val address = proxy.nodeInfo().addresses.toString()
        val legalId = proxy.nodeInfo().legalIdentities.toString()
        val platformVersion = proxy.nodeInfo().platformVersion.toString()

        returnVal.append("[Server Time]: $serverTime\n")
        returnVal.append("[Address]: $address\n")
        returnVal.append("[Legal Id]: $legalId\n")
        returnVal.append("[Platform Version]: $platformVersion\n")

        return returnVal.toString()
    }

    @GetMapping(value = ["/peers"], produces = ["text/plain"])
    private fun peers(): String {
        return proxy.networkMapSnapshot().flatMap { it.legalIdentities }.toString()
    }

    @GetMapping(value = ["/notaries"], produces = ["text/plain"])
    private fun notaries(): String {
        return proxy.notaryIdentities().toString()
    }

    @GetMapping(value = ["/flows"], produces = ["text/plain"])
    private fun flows(): String {
        return proxy.registeredFlows().toString()
    }

    @GetMapping(value = ["/states"], produces = ["text/plain"])
//    @GetMapping(value = ["/states"], produces = ["text/json"])
    private fun states(): String {
//    private fun states(): ResponseEntity<List<StateAndRef<IOUState>>> {
//        val criteria = QueryCriteria.VaultQueryCriteria(status = Vault.StateStatus.ALL)
//        return proxy.vaultQueryBy<ContractState>(criteria).states.toString()
//        val gson = Gson()
        return proxy.vaultQuery(IOUState::class.java).toString()
//        return ResponseEntity.ok(proxy.vaultQueryBy<IOUState>().states)
//        return ResponseEntity.ok(proxy.vaultQuery(IOUState::class.java).states)
    }

    @GetMapping(value = ["/statesjson"], produces = ["text/json"])
    private fun statesjson(): ResponseEntity<List<StateAndRef<IOUState>>> {
//        val criteria = QueryCriteria.VaultQueryCriteria(status = Vault.StateStatus.ALL)
//        return proxy.vaultQueryBy<ContractState>(criteria).states.toString()
//        val gson = Gson()
//        return proxy.vaultQuery(IOUState::class.java).toString()
//        return ResponseEntity.ok(proxy.vaultQueryBy<IOUState>().states)
        return ResponseEntity.ok(proxy.vaultQuery(IOUState::class.java).states)
    }
}