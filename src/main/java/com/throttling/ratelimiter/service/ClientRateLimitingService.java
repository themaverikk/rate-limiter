package com.throttling.ratelimiter.service;

import com.throttling.ratelimiter.pojo.request.ClientApiRequest;
import com.throttling.ratelimiter.pojo.response.BaseResponse;

public interface ClientRateLimitingService {

  BaseResponse verifyApiLimit(ClientApiRequest clientApiRequest);
}
