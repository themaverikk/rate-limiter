package com.throttling.ratelimiter.service.impl;

import com.throttling.ratelimiter.constant.Message;
import com.throttling.ratelimiter.enums.Status;
import com.throttling.ratelimiter.pojo.model.ClientRateLimitData;
import com.throttling.ratelimiter.pojo.request.ClientApiRequest;
import com.throttling.ratelimiter.pojo.response.BaseResponse;
import com.throttling.ratelimiter.repository.ClientRateLimitsRepository;
import com.throttling.ratelimiter.repository.ClientRepository;
import com.throttling.ratelimiter.service.ClientLockService;
import com.throttling.ratelimiter.service.ClientRateLimitingService;
import com.throttling.ratelimiter.util.TimeUnitConversionUtil;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class ClientRateLimitingServiceImpl implements ClientRateLimitingService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ClientRateLimitingServiceImpl.class);

  @Autowired
  private ClientLockService clientLockService;

  @Autowired
  private ClientRepository clientRepository;

  @Autowired
  private ClientRateLimitsRepository clientRateLimitsRepository;

  @Override
  public BaseResponse verifyApiLimit(ClientApiRequest clientApiRequest) {

    try {
      // acquire lock to check all limits for a given client atomically
      clientLockService.acquireLock(clientApiRequest.getClientId());
      long currentTimestamp = System.currentTimeMillis();

      List<ClientRateLimitData> applicableApiLimits = clientRateLimitsRepository
          .fetchApplicableApiLimitsInOrder(clientApiRequest.getClientId(),
              String.valueOf(clientApiRequest.getMethodName()), clientApiRequest.getApiName());

      if (CollectionUtils.isEmpty(applicableApiLimits)) {
        LOGGER.info("No applicable API limits found for ClientData-Id: {}... returning",
            clientApiRequest.getClientId());
        return new BaseResponse(Status.SUCCESS, Message.NO_LIMIT_APPLICABLE);
      }

      for (ClientRateLimitData clientApiLimit : applicableApiLimits) {

        long elapsedTimeMillis = currentTimestamp - clientApiLimit.getLastRequestTimeStamp();
        double elapsedTimeUnits = TimeUnitConversionUtil
            .convert(elapsedTimeMillis, clientApiLimit.getTimeUnit());

        long updatedAvailablePermits = (long) Math.min(
            clientApiLimit.getAvailablePermits() + elapsedTimeUnits * clientApiLimit
                .getMaxPermits(), clientApiLimit.getMaxPermits());

        if (updatedAvailablePermits < 1) {
          LOGGER.warn(
              "API rate limit breached for ClientData-Id: {}, availablePermits: {}, maxPermits: {}, limitType: {}, timeUnit: {}",
              clientApiLimit.getClientId(), clientApiLimit.getAvailablePermits(),
              clientApiLimit.getMaxPermits(), clientApiLimit.getLimitType(),
              clientApiLimit.getTimeUnit());

          return new BaseResponse(Status.FAILURE,
              "Limit: " + clientApiLimit.getLimitName() + ", limitType: " + clientApiLimit
                  .getLimitType() + ", timeUnit: " + clientApiLimit.getTimeUnit()
                  + " breached for the client");
        }

        // request went through
        LOGGER.info(
            "API rate limit passed for ClientData-Id: {}, availablePermits: {}, maxPermits: {}, limitType: {}, timeUnit: {}",
            clientApiLimit.getClientId(), clientApiLimit.getAvailablePermits(),
            clientApiLimit.getMaxPermits(), clientApiLimit.getLimitType(),
            clientApiLimit.getTimeUnit());

        // decrease the no. of available permits by 1 for current request
        updatedAvailablePermits--;
        clientApiLimit.setAvailablePermits(updatedAvailablePermits);
        clientApiLimit.setLastRequestTimeStamp(currentTimestamp);
      }

      clientRateLimitsRepository.saveAll(applicableApiLimits);

      return new BaseResponse(Status.SUCCESS, Message.SUCCESS);
    } finally {
      clientLockService.releaseLock(clientApiRequest.getClientId());
    }
  }
}
