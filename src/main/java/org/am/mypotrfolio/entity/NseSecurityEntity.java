package org.am.mypotrfolio.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "nse_security_data")
public class NseSecurityEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    private String securityId;
    private String securityName;
    private String status;
    private String series;
    private String isin;
    private Double faceValue;
    private String industry;
    private String instrumentType;
    private String sectorName;
    private String industryNewName;
    private String iGroupName;
    private String iSubGroupName;
    
    @Column(name = "created_at")
    private ZonedDateTime createdAt;
    
    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = ZonedDateTime.now();
        updatedAt = createdAt;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = ZonedDateTime.now();
    }
} 