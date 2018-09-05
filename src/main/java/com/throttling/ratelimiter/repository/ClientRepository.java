package com.throttling.ratelimiter.repository;

import com.throttling.ratelimiter.pojo.model.ClientData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository extends JpaRepository<ClientData, String> {

}
