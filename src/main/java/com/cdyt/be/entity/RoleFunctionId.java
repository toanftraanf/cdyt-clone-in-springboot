package com.cdyt.be.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Composite primary key for {@link RoleFunction}.
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleFunctionId implements Serializable {

    @Column(name = "role_id")
    private Integer roleId;

    @Column(name = "function_id")
    private Integer functionId;
}