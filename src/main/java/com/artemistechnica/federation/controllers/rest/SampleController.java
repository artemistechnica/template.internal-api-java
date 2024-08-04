package com.artemistechnica.federation.controllers.rest;

import com.artemistechnica.federation.datastore.models.SampleModels;
import com.artemistechnica.federation.services.SampleServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/accounts")
public class SampleController {

    private final SampleServices.AccountService accountService;

    @Autowired
    public SampleController(SampleServices.AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public List<SampleModels.Account> getAllAccounts() {
        return accountService.findAllAccounts();
    }

    @GetMapping("/{id}")
    public SampleModels.Account getAccountById(@PathVariable UUID id) {
        return accountService.findAccountById(id);
    }

    @PostMapping
    public SampleModels.Account createAccount(@RequestBody SampleModels.Account account) {
        return accountService.saveAccount(account);
    }

    @DeleteMapping("/{id}")
    public void deleteAccount(@PathVariable UUID id) {
        accountService.deleteAccount(id);
    }
}
