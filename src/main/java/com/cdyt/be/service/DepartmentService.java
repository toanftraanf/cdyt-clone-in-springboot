package com.cdyt.be.service;

import com.cdyt.be.entity.Department;
import com.cdyt.be.repository.DepartmentRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class DepartmentService {

  private final DepartmentRepository departmentRepository;

  public DepartmentService(DepartmentRepository departmentRepository) {
    this.departmentRepository = departmentRepository;
  }

  public List<Department> getAllDepartments() {
    return departmentRepository.findAll();
  }

  public List<Department> getAllActiveDepartments() {
    return departmentRepository.findAllActive();
  }

  public Optional<Department> getDepartmentById(Integer id) {
    return departmentRepository.findById(id);
  }

  public List<Department> getDepartmentsByParentId(Integer parentId) {
    return departmentRepository.findByParentId(parentId);
  }

  public List<Department> getDepartmentsByLevel(Integer level) {
    return departmentRepository.findByLevel(level);
  }

  public Department saveDepartment(Department department) {
    return departmentRepository.save(department);
  }

  public void deleteDepartment(Integer id) {
    departmentRepository.deleteById(id);
  }
}
