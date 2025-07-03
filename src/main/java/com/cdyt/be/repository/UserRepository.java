package com.cdyt.be.repository;

import com.cdyt.be.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByEmail(String email);

  boolean existsByEmail(String email);

  List<User> findByIsActiveTrueAndIsDeletedFalse();

  List<User> findByIsDeletedFalse();

  Optional<User> findByEmailAndIsDeletedFalse(String email);
}
