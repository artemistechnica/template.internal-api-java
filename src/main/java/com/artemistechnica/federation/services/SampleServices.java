package com.artemistechnica.federation.services;

import com.artemistechnica.federation.datastore.models.SampleModels;
import com.artemistechnica.federation.datastore.repositories.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

public class SampleServices {

    @Service
    public static class AccountService {

        private final AccountRepository accountRepository;

        @Autowired
        public AccountService(AccountRepository accountRepository) {
            this.accountRepository = accountRepository;
        }

        public List<SampleModels.Account> findAllAccounts() {
            return accountRepository.findAll();
        }

        public SampleModels.Account findAccountById(UUID id) {
            return accountRepository.findById(id).orElse(null);
        }

        public SampleModels.Account saveAccount(SampleModels.Account account) {
            return accountRepository.save(account);
        }

        public void deleteAccount(UUID id) {
            accountRepository.deleteById(id);
        }
    }
}
