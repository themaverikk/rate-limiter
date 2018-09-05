package com.throttling.ratelimiter.pojo.response;

import com.throttling.ratelimiter.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseResponse {

  private Status status;
  private String message;
}
