package com.securops.modules.posts.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ops_sites")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Site {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    private String city;

    @Column(length = 200)
    private String address;

    @Column(length = 50, unique = true)
    private String nodeCode; // Used for multi-site sync identification

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
