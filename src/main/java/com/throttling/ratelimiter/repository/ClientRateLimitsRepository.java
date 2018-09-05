package com.throttling.ratelimiter.repository;

import com.throttling.ratelimiter.pojo.model.ClientRateLimitData;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRateLimitsRepository extends JpaRepository<ClientRateLimitData, Integer> {

  @Query("FROM ClientRateLimitData WHERE (clientId = :clientId) AND (limitType = 2 OR limitType = 1 AND limitName = :methodName OR limitType = 0 AND limitName = :apiName) ORDER BY limitType, timeUnit")
  List<ClientRateLimitData> fetchApplicableApiLimitsInOrder(@Param("clientId") String clientId,
      @Param("methodName") String methodName, @Param("apiName") String apiName);
}
