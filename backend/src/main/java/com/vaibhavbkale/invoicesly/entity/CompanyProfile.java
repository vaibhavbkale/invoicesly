package com.vaibhavbkale.invoicesly.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "company_profile")
public class CompanyProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String companyName;
    private String ownerName;
    private String mobile;
    @Column(length = 1000)
    private String address;
    private String gstin;
}
