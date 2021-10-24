package com.vv.personal.diurnal.dbi.model;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 * @author Vivek
 * @since 24/10/21
 */
@Entity
@Table(name = "user_mapping")
public class UserMappingEntity implements Serializable {
    private static final long serialVersionUID = -3114137776402915017L;

    @Id
    @Column(name = "hash_email", nullable = false)
    private int emailHash;

    @Column(name = "mobile", nullable = false)
    private long mobile;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "\"user\"", nullable = false)
    private String user;

    @Column(name = "premium_user")
    private boolean premiumUser;

    @Column(name = "hash_cred")
    private String credHash;

    @Column(name = "timestamp_save_cloud_last")
    private ZonedDateTime lastCloudSaveTimestamp;

    @Column(name = "timestamp_save_last")
    private ZonedDateTime lastSaveTimestamp;

    @Column(name = "timestamp_expiry_payment")
    private ZonedDateTime paymentExpiryTimestamp;

    @Column(name = "timestamp_creation_account")
    private ZonedDateTime accountCreationTimestamp;

    @Column(name = "currency")
    private String currency;

    public UserMappingEntity() {
        // For JPA
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("emailHash", emailHash)
                .append("mobile", mobile)
                .append("email", email)
                .append("user", user)
                .append("premiumUser", premiumUser)
                .append("credHash", credHash)
                .append("lastCloudSaveTimestamp", lastCloudSaveTimestamp)
                .append("lastSaveTimestamp", lastSaveTimestamp)
                .append("paymentExpiryTimestamp", paymentExpiryTimestamp)
                .append("accountCreationTimestamp", accountCreationTimestamp)
                .append("currency", currency)
                .toString();
    }

    public int getEmailHash() {
        return emailHash;
    }

    public UserMappingEntity setEmailHash(int emailHash) {
        this.emailHash = emailHash;
        return this;
    }

    public long getMobile() {
        return mobile;
    }

    public UserMappingEntity setMobile(long mobile) {
        this.mobile = mobile;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public UserMappingEntity setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getUser() {
        return user;
    }

    public UserMappingEntity setUser(String user) {
        this.user = user;
        return this;
    }

    public boolean isPremiumUser() {
        return premiumUser;
    }

    public UserMappingEntity setPremiumUser(boolean premiumUser) {
        this.premiumUser = premiumUser;
        return this;
    }

    public String getCredHash() {
        return credHash;
    }

    public UserMappingEntity setCredHash(String credHash) {
        this.credHash = credHash;
        return this;
    }

    public ZonedDateTime getLastCloudSaveTimestamp() {
        return lastCloudSaveTimestamp;
    }

    public UserMappingEntity setLastCloudSaveTimestamp(ZonedDateTime lastCloudSaveTimestamp) {
        this.lastCloudSaveTimestamp = lastCloudSaveTimestamp;
        return this;
    }

    public ZonedDateTime getLastSaveTimestamp() {
        return lastSaveTimestamp;
    }

    public UserMappingEntity setLastSaveTimestamp(ZonedDateTime lastSaveTimestamp) {
        this.lastSaveTimestamp = lastSaveTimestamp;
        return this;
    }

    public ZonedDateTime getPaymentExpiryTimestamp() {
        return paymentExpiryTimestamp;
    }

    public UserMappingEntity setPaymentExpiryTimestamp(ZonedDateTime paymentExpiryTimestamp) {
        this.paymentExpiryTimestamp = paymentExpiryTimestamp;
        return this;
    }

    public ZonedDateTime getAccountCreationTimestamp() {
        return accountCreationTimestamp;
    }

    public UserMappingEntity setAccountCreationTimestamp(ZonedDateTime accountCreationTimestamp) {
        this.accountCreationTimestamp = accountCreationTimestamp;
        return this;
    }

    public String getCurrency() {
        return currency;
    }

    public UserMappingEntity setCurrency(String currency) {
        this.currency = currency;
        return this;
    }
}