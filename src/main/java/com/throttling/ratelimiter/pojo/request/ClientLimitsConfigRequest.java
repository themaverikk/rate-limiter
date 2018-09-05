package com.throttling.ratelimiter.pojo.request;

import com.throttling.ratelimiter.enums.LimitType;
import java.util.List;
import lombok.Data;

@Data
public class ClientLimitsConfigRequest {

  private LimitType limitType;

  private String limitName;

  private List<ClientIntervalRequestsLimit> timeIntervalLimits;
}
