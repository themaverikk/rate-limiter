package com.throttling.ratelimiter.pojo.model;

import com.throttling.ratelimiter.enums.LimitType;
import com.throttling.ratelimiter.enums.TimeUnit;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"clientId", "limitType", "limitName",
    "timeUnit"}))
public class ClientRateLimitData {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private int id;

  private String clientId;

  private LimitType limitType;

  private String limitName;

  private TimeUnit timeUnit;

  private long maxPermits;

  private long availablePermits;

  private long lastRequestTimeStamp;

  public ClientRateLimitData(String clientId, LimitType limitType, String limitName,
      TimeUnit timeUnit, long maxPermits, long availablePermits, long lastRequestTimeStamp) {

    this.clientId = clientId;
    this.limitType = limitType;
    this.limitName = limitName;
    this.timeUnit = timeUnit;
    this.maxPermits = maxPermits;
    this.availablePermits = availablePermits;
    this.lastRequestTimeStamp = lastRequestTimeStamp;
  }
}
