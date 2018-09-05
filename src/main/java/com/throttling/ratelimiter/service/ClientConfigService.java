package com.throttling.ratelimiter.service;

import com.throttling.ratelimiter.pojo.request.ClientConfigRequest;
import com.throttling.ratelimiter.pojo.response.BaseResponse;

public interface ClientConfigService {

  BaseResponse configureClient(ClientConfigRequest clientConfigRequest);
}
