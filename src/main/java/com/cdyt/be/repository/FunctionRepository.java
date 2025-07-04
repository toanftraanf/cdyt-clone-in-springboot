package com.cdyt.be.repository;

import com.cdyt.be.entity.Function;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FunctionRepository extends JpaRepository<Function, Integer> {

    Optional<Function> findFirstByApiUrlStartingWith(String apiUrl);
}