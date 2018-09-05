package com.throttling.ratelimiter.service.impl;

import com.throttling.ratelimiter.constant.Message;
import com.throttling.ratelimiter.converter.ClientConfigRequestConverter;
import com.throttling.ratelimiter.enums.Status;
import com.throttling.ratelimiter.pojo.model.ClientData;
import com.throttling.ratelimiter.pojo.model.ClientRateLimitData;
import com.throttling.ratelimiter.pojo.request.ClientConfigRequest;
import com.throttling.ratelimiter.pojo.response.BaseResponse;
import com.throttling.ratelimiter.repository.ClientRateLimitsRepository;
import com.throttling.ratelimiter.repository.ClientRepository;
import com.throttling.ratelimiter.service.ClientConfigService;
import com.throttling.ratelimiter.service.ClientLockService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class ClientConfigServiceImpl implements ClientConfigService {

  @Autowired
  private ClientLockService clientLockService;

  @Autowired
  private ClientRepository clientRepository;

  @Autowired
  private ClientRateLimitsRepository clientRateLimitsRepository;

  @Autowired
  private ClientConfigRequestConverter clientConfigRequestConverter;

  @Override
  public BaseResponse configureClient(ClientConfigRequest clientConfigRequest) {

    ClientData client = new ClientData(clientConfigRequest.getClientId());
    List<ClientRateLimitData> clientRateLimits = clientConfigRequestConverter
        .convert(clientConfigRequest);
    clientRateLimitsRepository.saveAll(clientRateLimits);
    clientRepository.save(client);
    clientLockService.addClient(client.getClientId());

    return new BaseResponse(Status.SUCCESS, Message.ADD_CLIENT_SUCCESS);
  }

}
