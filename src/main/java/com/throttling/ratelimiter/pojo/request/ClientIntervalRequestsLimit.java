package com.throttling.ratelimiter.pojo.request;


import com.throttling.ratelimiter.enums.TimeUnit;
import lombok.Data;

@Data
public class ClientIntervalRequestsLimit {

  private TimeUnit timeUnit;

  private long maxRequests;
}
