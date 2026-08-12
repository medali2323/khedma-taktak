package com.khedmataktak.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "profiles")
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true, columnDefinition = "CHAR(36)")
    private User user;

    @Column(nullable = false, unique = true, length = 30)
    private String slug;

    @Column(nullable = false)
    private boolean published = false;

    @Column(nullable = false, length = 50)
    private String theme = "classic";

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "full_name", nullable = false, columnDefinition = "json")
    private Map<String, String> fullName = new HashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private Map<String, String> title = new HashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private Map<String, String> summary = new HashMap<>();

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "contact_phone", length = 50)
    private String contactPhone;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "contact_location", columnDefinition = "json")
    private Map<String, String> contactLocation = new HashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "social_links", columnDefinition = "json")
    private Map<String, String> socialLinks = new HashMap<>();

    @Column(name = "primary_trade", length = 100)
    private String primaryTrade;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "trade_specialties", columnDefinition = "json")
    private List<String> tradeSpecialties = new ArrayList<>();

    @Column(name = "driving_license", length = 20)
    private String drivingLicense;

    @Column(name = "has_own_vehicle", nullable = false)
    private boolean hasOwnVehicle = false;

    @Column(name = "mobility_radius_km")
    private Integer mobilityRadiusKm;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tools_equipment", columnDefinition = "json")
    private List<String> toolsEquipment = new ArrayList<>();

    @Column(name = "student_institution")
    private String studentInstitution;

    @Column(name = "student_year", length = 50)
    private String studentYear;

    @Column(name = "internship_sought")
    private String internshipSought;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public Map<String, String> getFullName() {
        return fullName;
    }

    public void setFullName(Map<String, String> fullName) {
        this.fullName = fullName;
    }

    public Map<String, String> getTitle() {
        return title;
    }

    public void setTitle(Map<String, String> title) {
        this.title = title;
    }

    public Map<String, String> getSummary() {
        return summary;
    }

    public void setSummary(Map<String, String> summary) {
        this.summary = summary;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public Map<String, String> getContactLocation() {
        return contactLocation;
    }

    public void setContactLocation(Map<String, String> contactLocation) {
        this.contactLocation = contactLocation;
    }

    public Map<String, String> getSocialLinks() {
        return socialLinks;
    }

    public void setSocialLinks(Map<String, String> socialLinks) {
        this.socialLinks = socialLinks;
    }

    public String getPrimaryTrade() {
        return primaryTrade;
    }

    public void setPrimaryTrade(String primaryTrade) {
        this.primaryTrade = primaryTrade;
    }

    public List<String> getTradeSpecialties() {
        return tradeSpecialties;
    }

    public void setTradeSpecialties(List<String> tradeSpecialties) {
        this.tradeSpecialties = tradeSpecialties != null ? tradeSpecialties : new ArrayList<>();
    }

    public String getDrivingLicense() {
        return drivingLicense;
    }

    public void setDrivingLicense(String drivingLicense) {
        this.drivingLicense = drivingLicense;
    }

    public boolean isHasOwnVehicle() {
        return hasOwnVehicle;
    }

    public void setHasOwnVehicle(boolean hasOwnVehicle) {
        this.hasOwnVehicle = hasOwnVehicle;
    }

    public Integer getMobilityRadiusKm() {
        return mobilityRadiusKm;
    }

    public void setMobilityRadiusKm(Integer mobilityRadiusKm) {
        this.mobilityRadiusKm = mobilityRadiusKm;
    }

    public List<String> getToolsEquipment() {
        return toolsEquipment;
    }

    public void setToolsEquipment(List<String> toolsEquipment) {
        this.toolsEquipment = toolsEquipment != null ? toolsEquipment : new ArrayList<>();
    }

    public String getStudentInstitution() {
        return studentInstitution;
    }

    public void setStudentInstitution(String studentInstitution) {
        this.studentInstitution = studentInstitution;
    }

    public String getStudentYear() {
        return studentYear;
    }

    public void setStudentYear(String studentYear) {
        this.studentYear = studentYear;
    }

    public String getInternshipSought() {
        return internshipSought;
    }

    public void setInternshipSought(String internshipSought) {
        this.internshipSought = internshipSought;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
