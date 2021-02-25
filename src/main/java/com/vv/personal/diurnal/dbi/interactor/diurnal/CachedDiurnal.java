package com.vv.personal.diurnal.dbi.interactor.diurnal;

import com.vv.personal.diurnal.dbi.constants.DbConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Vivek
 * @since 02/01/21
 */
public class CachedDiurnal {
    private static final Logger LOGGER = LoggerFactory.getLogger(CachedDiurnal.class);
    public final ConcurrentHashMap<String, Set<Integer>> activeRefEntityIds = new ConcurrentHashMap<>();

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
        LOGGER.warn("Flushed cache for {}", entity);
    }

    public ConcurrentHashMap<String, Set<Integer>> getActiveRefEntityIds() {
        return activeRefEntityIds;
    }

    private Set<Integer> generateEmptySet() {
        return new HashSet<>(0);
    }
}
