package com.cdyt.be.repository;

import com.cdyt.be.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Integer> {

  // Find all departments that are not deleted
  @Query("SELECT d FROM Department d WHERE d.isDeleted = false OR d.isDeleted IS NULL")
  List<Department> findAllActive();

  List<Department> findByParentId(int parentId);

  // Find departments by level
  List<Department> findByLevel(Integer level);

  // Custom method to find departments using stored procedure if needed
  // @Query(nativeQuery = true, value = "SELECT * FROM usp_department_getlist()")
  // List<Department> findAllFromStoredProcedure();
}
