package com.gdb.auth.service;

import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistService {

    private final Set<String> revokedJtis = ConcurrentHashMap.newKeySet();

    public void revoke(String jti) {
        revokedJtis.add(jti);
    }

    public boolean isRevoked(String jti) {
        return revokedJtis.contains(jti);
    }
}
