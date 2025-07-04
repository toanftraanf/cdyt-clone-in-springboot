package com.cdyt.be.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * Join table entity linking {@link Role} and {@link Function} to represent the
 * permissions assigned to a role.
 */
@Entity
@Table(name = "role_function")
@Getter
@Setter
public class RoleFunction {

    @EmbeddedId
    private RoleFunctionId id = new RoleFunctionId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("roleId")
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("functionId")
    @JoinColumn(name = "function_id", nullable = false)
    private Function function;

    @Column(name = "last_update")
    private LocalDateTime lastUpdate;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.lastUpdate = LocalDateTime.now();
    }
}