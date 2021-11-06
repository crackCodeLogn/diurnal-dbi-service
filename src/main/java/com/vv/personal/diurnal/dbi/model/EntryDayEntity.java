package com.vv.personal.diurnal.dbi.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * @author Vivek
 * @since 27/10/21
 */
@Entity
@Table(name = "entry_day")
public class EntryDayEntity implements Serializable {
    private static final long serialVersionUID = 3945060484397253781L;

    @Id
    @Column(name = "hel_dt", nullable = false)
    private String emailHashAndDate;

    @Column(name = "hash_email", nullable = false)
    private int emailHash;

    @Column(name = "date", nullable = false)
    private int date;

    @Column(name = "title")
    private String title;

    @Column(name = "entries_as_string")
    private String entriesAsString;

    public EntryDayEntity() {
    }

    public EntryDayEntity(String emailHashAndDate, int emailHash, int date, String title, String entriesAsString) {
        this.emailHashAndDate = emailHashAndDate;
        this.emailHash = emailHash;
        this.date = date;
        this.title = title;
        this.entriesAsString = entriesAsString;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("emailHashAndDate", emailHashAndDate)
                .append("emailHash", emailHash)
                .append("date", date)
                .append("title", title)
                .append("entriesAsString", entriesAsString)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        EntryDayEntity that = (EntryDayEntity) o;

        return new EqualsBuilder().append(getEmailHash(), that.getEmailHash()).append(getDate(), that.getDate()).append(getEmailHashAndDate(), that.getEmailHashAndDate()).append(getTitle(), that.getTitle()).append(getEntriesAsString(), that.getEntriesAsString()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(getEmailHashAndDate()).append(getEmailHash()).append(getDate()).append(getTitle()).append(getEntriesAsString()).toHashCode();
    }

    public String getEmailHashAndDate() {
        return emailHashAndDate;
    }

    public EntryDayEntity setEmailHashAndDate(String emailHashAndDate) {
        this.emailHashAndDate = emailHashAndDate;
        return this;
    }

    public int getEmailHash() {
        return emailHash;
    }

    public EntryDayEntity setEmailHash(int emailHash) {
        this.emailHash = emailHash;
        return this;
    }

    public int getDate() {
        return date;
    }

    public EntryDayEntity setDate(int date) {
        this.date = date;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public EntryDayEntity setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getEntriesAsString() {
        return entriesAsString;
    }

    public EntryDayEntity setEntriesAsString(String entriesAsString) {
        this.entriesAsString = entriesAsString;
        return this;
    }
}