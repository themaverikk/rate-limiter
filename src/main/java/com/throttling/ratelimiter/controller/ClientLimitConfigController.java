package com.throttling.ratelimiter.controller;

import com.throttling.ratelimiter.pojo.request.ClientApiRequest;
import com.throttling.ratelimiter.pojo.request.ClientConfigRequest;
import com.throttling.ratelimiter.pojo.response.BaseResponse;
import com.throttling.ratelimiter.repository.ClientRateLimitsRepository;
import com.throttling.ratelimiter.service.ClientConfigService;
import com.throttling.ratelimiter.service.ClientRateLimitingService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/throttling")
public class ClientLimitConfigController {

  @Autowired
  private ClientRateLimitsRepository clientRateLimitsRepository;

  @Autowired
  private ClientConfigService clientConfigService;

  @Autowired
  private ClientRateLimitingService clientRateLimitingService;

  @PostMapping("/configure-client")
  public BaseResponse configureClient(@RequestBody ClientConfigRequest clientConfigRequest) {
    return clientConfigService.configureClient(clientConfigRequest);
  }

  @PostMapping("/verify-api-limit")
  public BaseResponse verifyApiLimit(@RequestBody ClientApiRequest clientApiRequest) {
    return clientRateLimitingService.verifyApiLimit(clientApiRequest);
  }

  @GetMapping("/configured-limits")
  public List<?> getConfiguredLimits() {
    return clientRateLimitsRepository.findAll();
  }
}
