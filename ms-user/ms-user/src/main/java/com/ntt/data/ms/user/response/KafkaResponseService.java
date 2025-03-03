package com.ntt.data.ms.user.response;

import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class KafkaResponseService {
    private final ConcurrentHashMap<String, CompletableFuture<String>> pendingRequests = new ConcurrentHashMap<>();

    public CompletableFuture<String> registerRequest(String messageId) {
        CompletableFuture<String> future = new CompletableFuture<>();
        pendingRequests.put(messageId, future);
        return future;
    }

    public void completeRequest(String messageId, String response) {
        CompletableFuture<String> future = pendingRequests.remove(messageId);
        if (future != null) {
            future.complete(response);
        }
    }
}