package com.vv.personal.diurnal.dbi.model;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.Instant;

/**
 * @author Vivek
 * @since 24/10/21
 */
@Entity
@Table(name = "user_mapping2")
public class UserMappingLookAlikeEntity implements Serializable {
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
    private Instant lastCloudSaveTimestamp;

    @Column(name = "timestamp_save_last")
    private Instant lastSaveTimestamp;

    @Column(name = "timestamp_expiry_payment")
    private Instant paymentExpiryTimestamp;

    @Column(name = "timestamp_creation_account")
    private Instant accountCreationTimestamp;

    @Column(name = "currency")
    private String currency;

    public UserMappingLookAlikeEntity() {
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

    public UserMappingLookAlikeEntity setEmailHash(int emailHash) {
        this.emailHash = emailHash;
        return this;
    }

    public long getMobile() {
        return mobile;
    }

    public UserMappingLookAlikeEntity setMobile(long mobile) {
        this.mobile = mobile;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public UserMappingLookAlikeEntity setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getUser() {
        return user;
    }

    public UserMappingLookAlikeEntity setUser(String user) {
        this.user = user;
        return this;
    }

    public boolean isPremiumUser() {
        return premiumUser;
    }

    public UserMappingLookAlikeEntity setPremiumUser(boolean premiumUser) {
        this.premiumUser = premiumUser;
        return this;
    }

    public String getCredHash() {
        return credHash;
    }

    public UserMappingLookAlikeEntity setCredHash(String credHash) {
        this.credHash = credHash;
        return this;
    }

    public Instant getLastCloudSaveTimestamp() {
        return lastCloudSaveTimestamp;
    }

    public UserMappingLookAlikeEntity setLastCloudSaveTimestamp(Instant lastCloudSaveTimestamp) {
        this.lastCloudSaveTimestamp = lastCloudSaveTimestamp;
        return this;
    }

    public Instant getLastSaveTimestamp() {
        return lastSaveTimestamp;
    }

    public UserMappingLookAlikeEntity setLastSaveTimestamp(Instant lastSaveTimestamp) {
        this.lastSaveTimestamp = lastSaveTimestamp;
        return this;
    }

    public Instant getPaymentExpiryTimestamp() {
        return paymentExpiryTimestamp;
    }

    public UserMappingLookAlikeEntity setPaymentExpiryTimestamp(Instant paymentExpiryTimestamp) {
        this.paymentExpiryTimestamp = paymentExpiryTimestamp;
        return this;
    }

    public Instant getAccountCreationTimestamp() {
        return accountCreationTimestamp;
    }

    public UserMappingLookAlikeEntity setAccountCreationTimestamp(Instant accountCreationTimestamp) {
        this.accountCreationTimestamp = accountCreationTimestamp;
        return this;
    }

    public String getCurrency() {
        return currency;
    }

    public UserMappingLookAlikeEntity setCurrency(String currency) {
        this.currency = currency;
        return this;
    }
}