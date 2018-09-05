package com.throttling.ratelimiter.service;

public interface ClientLockService {

  void addClient(String clientId);

  void acquireLock(String clientId);

  void releaseLock(String clientId);
}
