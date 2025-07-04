package com.cdyt.be.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Entity representing a system function/permission that can be assigned to
 * roles.
 * Corresponds to table {@code function}.
 *
 * NOTE: The {@code CategoryID} column is intentionally not mapped because
 * the project does not currently manage function categories.
 */
@Entity
@Table(name = "function")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Function {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "function_id")
    private Integer id;

    @Column(name = "api_url", length = 250)
    private String apiUrl;

    @Column(name = "description", length = 250)
    private String description;

    @Column(name = "is_delete")
    private Boolean isDelete = false;

    @Column(name = "button_show", length = 20)
    private String buttonShow;

    @Column(name = "display_order")
    private Integer displayOrder;
}