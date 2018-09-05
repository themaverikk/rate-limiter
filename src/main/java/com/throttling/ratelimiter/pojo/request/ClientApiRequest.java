package com.throttling.ratelimiter.pojo.request;

import com.throttling.ratelimiter.enums.ApiMethod;
import lombok.Data;

@Data
public class ClientApiRequest {

  private String clientId;

  private ApiMethod methodName;

  private String apiName;
}
