package com.throttling.ratelimiter.converter;


import com.throttling.ratelimiter.pojo.model.ClientRateLimitData;
import com.throttling.ratelimiter.pojo.request.ClientConfigRequest;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ClientConfigRequestConverter {

  public List<ClientRateLimitData> convert(ClientConfigRequest clientConfigRequest) {
    long currentTimestamp = System.currentTimeMillis();

    List<ClientRateLimitData> clientRateLimits = new ArrayList<>();

    clientConfigRequest.getLimits().forEach(limit -> limit.getTimeIntervalLimits().forEach(
        clientIntervalRequestsLimit -> clientRateLimits.add(
            new ClientRateLimitData(clientConfigRequest.getClientId(), limit.getLimitType(),
                limit.getLimitName(), clientIntervalRequestsLimit.getTimeUnit(),
                clientIntervalRequestsLimit.getMaxRequests(),
                clientIntervalRequestsLimit.getMaxRequests(), currentTimestamp))));

    return clientRateLimits;
  }
}
