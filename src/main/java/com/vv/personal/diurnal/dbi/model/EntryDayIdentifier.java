package com.vv.personal.diurnal.dbi.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * @author Vivek
 * @since 27/10/21
 */
@Embeddable
public class EntryDayIdentifier implements Serializable {
    private static final long serialVersionUID = 3339611967067170028L;

    @Column(name = "hash_email", nullable = false)
    private int emailHash;

    @Column(name = "date", nullable = false)
    private int date;

    public EntryDayIdentifier() {
    }

    public EntryDayIdentifier(int emailHash, int date) {
        this.emailHash = emailHash;
        this.date = date;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("emailHash", emailHash)
                .append("date", date)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntryDayIdentifier that = (EntryDayIdentifier) o;
        return new EqualsBuilder().append(emailHash, that.emailHash).append(date, that.date).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(emailHash).append(date).toHashCode();
    }

    public int getEmailHash() {
        return emailHash;
    }

    public EntryDayIdentifier setEmailHash(int emailHash) {
        this.emailHash = emailHash;
        return this;
    }

    public int getDate() {
        return date;
    }

    public EntryDayIdentifier setDate(int date) {
        this.date = date;
        return this;
    }
}