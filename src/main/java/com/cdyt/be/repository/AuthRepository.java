package com.cdyt.be.repository;

import com.cdyt.be.entity.UserToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuthRepository extends JpaRepository<UserToken, Integer> {

  Optional<UserToken> findByToken(String token);

  @Query("SELECT ut FROM UserToken ut JOIN FETCH ut.user WHERE ut.token = :token")
  Optional<UserToken> findByTokenWithUser(@Param("token") String token);

  void deleteByUserId(Long userId);
}
