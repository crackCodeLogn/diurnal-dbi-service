package com.vv.personal.diurnal.dbi.interactor.diurnal.cache;

import com.vv.personal.diurnal.dbi.constants.DbConstants;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Vivek
 * @since 02/01/21
 */
@Slf4j
public class CachedDiurnal {
    public final ConcurrentMap<String, Set<Integer>> activeRefEntityIds = new ConcurrentHashMap<>();

    public CachedDiurnal() {
        activeRefEntityIds.put(DbConstants.TABLE_DIURNAL_USER_MAPPING, generateEmptySet());
        activeRefEntityIds.put(DbConstants.TABLE_DIURNAL_TITLE_MAPPING, generateEmptySet());
        /*activeRefEntityIds.put(TABLE_REF_PROBLEM, generateEmptySet());
        activeRefEntityIds.put(TABLE_REF_MAKE, generateEmptySet());*/
        activeRefEntityIds.put(DbConstants.TABLE_DIURNAL_ENTRY, generateEmptySet());
    }

    public synchronized Boolean addNewIdToEntityCache(String entity, Integer idToAdd) {
        return activeRefEntityIds.get(entity).add(idToAdd);
    }

    public synchronized Boolean isIdPresentInEntityCache(String entity, Integer idToQuery) {
        return activeRefEntityIds.get(entity).contains(idToQuery);
    }

    public synchronized void bulkAddNewIdsToEntityCache(String entity, Collection<Integer> newIdsToAdd) {
        newIdsToAdd.forEach(newId -> addNewIdToEntityCache(entity, newId));
    }

    public synchronized Boolean deleteIdFromEntityCache(String entity, Integer idToDel) {
        return activeRefEntityIds.get(entity).remove(idToDel);
    }

    public synchronized void flushEntityCache(String entity) {
        activeRefEntityIds.get(entity).clear();
        log.warn("Flushed cache for {}", entity);
    }

    private Set<Integer> generateEmptySet() {
        return new HashSet<>(0);
    }
}