package com.cdyt.be.controller;

import com.cdyt.be.entity.Department;
import com.cdyt.be.service.DepartmentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/department")
@Tag(name = "Department", description = "APIs for managing departments")
public class DepartmentController {

  private final DepartmentService depService;

  public DepartmentController(DepartmentService depService) {
    this.depService = depService;
  }

  @GetMapping("/getAll")
  public List<Department> getAllDepartments() {
    return depService.getAllDepartments();
  }

  @GetMapping("/active")
  public List<Department> getAllActiveDepartments() {
    return depService.getAllActiveDepartments();
  }

  @GetMapping("/{id}")
  public ResponseEntity<Department> getDepartmentById(@PathVariable Integer id) {
    Optional<Department> department = depService.getDepartmentById(id);
    return department.map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/parent/{parentId}")
  public List<Department> getDepartmentsByParentId(@PathVariable Integer parentId) {
    return depService.getDepartmentsByParentId(parentId);
  }

  @GetMapping("/level/{level}")
  public List<Department> getDepartmentsByLevel(@PathVariable Integer level) {
    return depService.getDepartmentsByLevel(level);
  }

  @PostMapping
  public Department createDepartment(@RequestBody Department department) {
    return depService.saveDepartment(department);
  }

  @PutMapping("/{id}")
  public ResponseEntity<Department> updateDepartment(@PathVariable Integer id,
      @RequestBody Department department) {
    if (depService.getDepartmentById(id).isPresent()) {
      department.setId(id);
      return ResponseEntity.ok(depService.saveDepartment(department));
    }
    return ResponseEntity.notFound().build();
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteDepartment(@PathVariable Integer id) {
    if (depService.getDepartmentById(id).isPresent()) {
      depService.deleteDepartment(id);
      return ResponseEntity.noContent().build();
    }
    return ResponseEntity.notFound().build();
  }
}
