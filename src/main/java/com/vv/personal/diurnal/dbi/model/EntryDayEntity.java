package com.vv.personal.diurnal.dbi.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
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

    @EmbeddedId
    public EntryDayId entryDayId;

    @Column(name = "title")
    private String title;

    @Column(name = "entries_as_string")
    private String entriesAsString;

    public EntryDayEntity() {
    }

    public EntryDayEntity(EntryDayId entryDayId, String title, String entriesAsString) {
        this.entryDayId = entryDayId;
        this.title = title;
        this.entriesAsString = entriesAsString;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("entryDayId", entryDayId)
                .append("title", title)
                .append("entriesAsString", entriesAsString)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntryDayEntity that = (EntryDayEntity) o;
        return new EqualsBuilder().append(entryDayId, that.entryDayId).append(title, that.title).append(entriesAsString, that.entriesAsString).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(entryDayId).append(title).append(entriesAsString).toHashCode();
    }

    public EntryDayId getEntryDayId() {
        return entryDayId;
    }

    public EntryDayEntity setEntryDayId(EntryDayId entryDayId) {
        this.entryDayId = entryDayId;
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