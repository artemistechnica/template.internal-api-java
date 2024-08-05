package com.artemistechnica.federation.services;


import com.artemistechnica.commons.datatypes.Envelope;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class SampleAsyncService {

    private CompletableFuture<Integer> mkIntegerAsync(int num) {
        return CompletableFuture.completedFuture(num);
    }

    private CompletableFuture<String> mkStringAsync() {
        return CompletableFuture.completedFuture("Hello, you have chosen %d!");
    }

    public CompletableFuture<Envelope<String>> doWork(int num) {
        CompletableFuture<Integer> f0   = mkIntegerAsync(num);
        CompletableFuture<String> f1    = mkStringAsync();
        return f0
                .thenComposeAsync(i -> f1.thenApplyAsync(s -> s.formatted(i)))
                .thenApply(Envelope::mkSuccess);
    }
}
