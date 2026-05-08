package com.adnanumar.task_manager.entity;

import com.adnanumar.task_manager.enums.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role globalRole;  // ADMIN or MEMBER

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Relationships (mappedBy = don't create extra columns here)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<ProjectMember> projectMembers;

    @OneToMany(mappedBy = "assignedTo")
    @JsonIgnore
    private List<Task> assignedTasks;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
