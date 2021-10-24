package com.vv.personal.diurnal.dbi.engine.transformer.packet;

import com.vv.personal.diurnal.artifactory.generated.EntryDayProto;
import com.vv.personal.diurnal.artifactory.generated.EntryProto;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author Vivek
 * @since 24/04/21
 */
public class EntryDayRepr {
    private final EntryDayProto.EntryDay entryDay;
    private final Queue<EntryProto.Entry> entries = new LinkedList<>();

    public EntryDayRepr(EntryDayProto.EntryDay entryDay) {
        this.entryDay = entryDay;
    }

    public boolean addEntry(EntryProto.Entry entry) {
        return entries.offer(entry);
    }

    public boolean addEntries(EntryProto.EntryList entryList) {
        return entries.addAll(entryList.getEntryList());
    }

    public EntryDayProto.EntryDay getEntryDay() {
        return entryDay;
    }

    public Queue<EntryProto.Entry> getEntries() {
        return entries;
    }
}
