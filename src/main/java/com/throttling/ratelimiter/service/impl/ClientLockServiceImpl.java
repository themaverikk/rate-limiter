package com.throttling.ratelimiter.service.impl;


import com.throttling.ratelimiter.pojo.model.ClientData;
import com.throttling.ratelimiter.repository.ClientRepository;
import com.throttling.ratelimiter.service.ClientLockService;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClientLockServiceImpl implements ClientLockService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ClientLockServiceImpl.class);

  private volatile Map<String, ReentrantLock> clientLocks;

  @Autowired
  private ClientRepository clientRepository;


  @Override
  public synchronized void addClient(String clientId) {
    Map<String, ReentrantLock> updatedClientsMap = new HashMap<>(clientLocks);
    updatedClientsMap.put(clientId, new ReentrantLock());
    clientLocks = Collections.unmodifiableMap(updatedClientsMap);
  }

  @Override
  public void acquireLock(String clientId) {
    ReentrantLock clientLock = getAndValidateClientLock(clientId);

    clientLock.lock();
  }

  @Override
  public void releaseLock(String clientId) {
    ReentrantLock clientLock = getAndValidateClientLock(clientId);

    clientLock.unlock();
  }

  @PostConstruct
  public void init() {
    List<ClientData> configuredClients = clientRepository.findAll();

    clientLocks = Collections.unmodifiableMap(configuredClients.stream()
        .collect(Collectors.toMap(ClientData::getClientId, client -> new ReentrantLock())));
  }

  private ReentrantLock getAndValidateClientLock(final String clientId) {
    ReentrantLock clientLock = clientLocks.get(clientId);

    if (clientLock == null) {
      LOGGER.error("Lock doesn't exist for ClientData-Id: {}... Invalid client?", clientId);
      throw new IllegalArgumentException("Invalid client-id");
    }
    return clientLock;
  }
}
