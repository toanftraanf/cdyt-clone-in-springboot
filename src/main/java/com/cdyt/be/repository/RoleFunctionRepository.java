package com.cdyt.be.repository;

import com.cdyt.be.entity.Function;
import com.cdyt.be.entity.RoleFunction;
import com.cdyt.be.entity.RoleFunctionId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleFunctionRepository extends JpaRepository<RoleFunction, RoleFunctionId> {

    @Query("SELECT rf.function FROM RoleFunction rf WHERE rf.role.id IN :roleIds")
    List<Function> findFunctionsByRoleIds(@Param("roleIds") List<Integer> roleIds);
} 