package com.throttling.ratelimiter.pojo.request;


import java.util.List;
import lombok.Data;

@Data
public class ClientConfigRequest {

  private String clientId;

  private List<ClientLimitsConfigRequest> limits;
}
