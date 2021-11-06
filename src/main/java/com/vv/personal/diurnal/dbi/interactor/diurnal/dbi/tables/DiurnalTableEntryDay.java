package com.vv.personal.diurnal.dbi.interactor.diurnal.dbi.tables;

import com.vv.personal.diurnal.artifactory.generated.EntryDayProto;
import com.vv.personal.diurnal.dbi.model.EntryDayEntity;
import com.vv.personal.diurnal.dbi.repository.EntryDayRepository;
import com.vv.personal.diurnal.dbi.util.DiurnalUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.vv.personal.diurnal.dbi.constants.Constants.*;

/**
 * @author Vivek
 * @since 06/03/21
 */
@Slf4j
public class DiurnalTableEntryDay {
    private static final EntryDayEntity EMPTY_ENTRY_DAY_ENTITY = new EntryDayEntity();

    private final EntryDayRepository entryDayRepository;

    public DiurnalTableEntryDay(EntryDayRepository entryDayRepository) {
        this.entryDayRepository = entryDayRepository;
    }

    /*private static EntryDayId generateEntryDayIdentifier(Integer emailHash, Integer date) {
        return new EntryDayId().setEmailHash(emailHash).setDate(date);
    }*/
    private static String generateEntryDayIdentifier(Integer emailHash, Integer date) {
        return String.format("%d^%d", emailHash, date);
    }

    public static EntryDayEntity generateEntryDayEntity(Integer emailHash, Integer date, String title, String description) {
        return new EntryDayEntity()
                //.setEntryDayId(generateEntryDayIdentifier(emailHash, date))
                .setEmailHashAndDate(generateEntryDayIdentifier(emailHash, date))
                .setEmailHash(emailHash)
                .setDate(date)
                .setTitle(title)
                .setEntriesAsString(description);
    }

    public int pushNewEntity(Integer emailHash, Integer date, String title, String description) {
        if (log.isDebugEnabled()) log.debug("Pushing new EntryDay entity: {} x {} x {}", emailHash, date, title);
        EntryDayEntity entryDayEntity = new EntryDayEntity()
                //.setEntryDayId(generateEntryDayIdentifier(emailHash, date))
                .setEmailHashAndDate(generateEntryDayIdentifier(emailHash, date))
                .setEmailHash(emailHash)
                .setDate(date)
                .setTitle(title)
                .setEntriesAsString(description);
        try {
            entryDayRepository.save(entryDayEntity);
            if (log.isDebugEnabled()) log.debug("Pushed new EntryDay entity: {}", entryDayEntity);
            return ONE;
        } catch (Exception e) {
            //log.error("Failed to push new entry-day mapping with identifier: {}. ", entryDayEntity.getEntryDayId(), e);
            log.error("Failed to push new entry-day mapping with identifier: {}. ", entryDayEntity, e);
        }
        return NA_INT;
    }

    public int pushNewEntities(List<EntryDayEntity> entryDays) {
        if (log.isDebugEnabled()) log.debug("Pushing {} new EntryDay entity", entryDays.size());
        try {
            int newEntitiesCreated = entryDayRepository.saveAll(entryDays).size();
            /*List<List<EntryDayEntity>> lists = Lists.partition(entryDays, 100);
            AtomicInteger counter = new AtomicInteger(0);
            lists.forEach(list -> {
                entryDayRepository.saveAll(list);
                entryDayRepository.flush();
                counter.addAndGet(list.size());
                log.info("Pushed {}/{}", counter.get(), entryDays.size());
            });
            return counter.get();
            */
            if (log.isDebugEnabled()) log.debug("Pushed {} new EntryDay entities", newEntitiesCreated);
            return newEntitiesCreated;
        } catch (Exception e) {
            log.error("Failed to push {} new entry-day mapping. ", entryDays.size(), e);
        }
        return NA_INT;
    }

    public int deleteEntity(Integer emailHash, Integer date) {
        //EntryDayId entryDayIdentifier = generateEntryDayIdentifier(emailHash, date);
        String entryDayIdentifier = generateEntryDayIdentifier(emailHash, date);
        try {
            entryDayRepository.deleteById(entryDayIdentifier);
            log.info("Deleted EntryDay entity: {}", entryDayIdentifier);
            return ONE;
        } catch (Exception e) {
            log.error("Failed to delete entry-day mapping with identifier: {}. ", entryDayIdentifier, e);
        }
        return NA_INT;
    }

    public boolean checkEntity(Integer emailHash, Integer date) {
        //EntryDayId entryDayIdentifier = generateEntryDayIdentifier(emailHash, date);
        String entryDayIdentifier = generateEntryDayIdentifier(emailHash, date);
        try {
            return entryDayRepository.existsById(entryDayIdentifier);
        } catch (Exception e) {
            log.error("Failed to check entry-day mapping with identifier: {}. ", entryDayIdentifier, e);
        }
        return false;
    }

    public List<EntryDayEntity> retrieveAllEntities() {
        try {
            return entryDayRepository.findAll();
        } catch (Exception e) {
            log.error("Failed to retrieve all entry-days. ", e);
        }
        return new ArrayList<>();
    }

    public EntryDayProto.EntryDayList retrieveAll() {
        List<EntryDayEntity> entryDayEntityList = retrieveAllEntities();
        return generateDetails(entryDayEntityList);
    }

    private EntryDayProto.EntryDayList generateDetails(List<EntryDayEntity> entryDayEntityList) {
        EntryDayProto.EntryDayList.Builder entryDayBuilder = EntryDayProto.EntryDayList.newBuilder();
        log.info("Extracted {} rows from DB on entry-day retrieveAll operation.", entryDayEntityList.size());
        entryDayBuilder.addAllEntryDay(
                entryDayEntityList.stream().map(this::generateDetail).collect(Collectors.toList())
        );
        return entryDayBuilder.build();
    }

    public EntryDayProto.EntryDay retrieveSingle(Integer emailHash, Integer date) {
        try {
            EntryDayEntity entryDayEntity = retrieveSingleEntity(emailHash, date);
            return generateDetail(entryDayEntity);
        } catch (Exception e) {
            log.error("Failed to retrieve single entry day entity from db. ", e);
        }
        return EMPTY_ENTRY_DAY;
    }

    public EntryDayEntity retrieveSingleEntity(Integer emailHash, Integer date) {
        //EntryDayId entryDayIdentifier = generateEntryDayIdentifier(emailHash, date);
        String entryDayIdentifier = generateEntryDayIdentifier(emailHash, date);
        try {
            return entryDayRepository.findById(entryDayIdentifier).orElseThrow();
        } catch (Exception e) {
            log.error("Failed to get entry-day mapping with identifier: {}. ", entryDayIdentifier, e);
        }
        return EMPTY_ENTRY_DAY_ENTITY;
    }

    public EntryDayProto.EntryDayList retrieveSome(Integer emailHash) {
        //for now only retrieving some on basis of the hash_email for the backup data retrieval step
        List<EntryDayEntity> entryDaysOnHash = entryDayRepository.findByEmailHash(emailHash);
        return generateDetails(entryDaysOnHash);
    }

    public EntryDayProto.EntryDay generateDetail(EntryDayEntity entryDayEntity) {
        EntryDayProto.EntryDay.Builder builder = EntryDayProto.EntryDay.newBuilder();
        try {
            //builder.setHashEmail(entryDayEntity.getEntryDayId().getEmailHash());
            //builder.setDate(entryDayEntity.getEntryDayId().getDate());
            builder.setHashEmail(entryDayEntity.getEmailHash());
            builder.setDate(entryDayEntity.getDate());
            builder.setTitle(DiurnalUtil.refineDbStringForOriginal(entryDayEntity.getTitle()));
            builder.setEntriesAsString(
                    DiurnalUtil.refineDbStringForOriginal(entryDayEntity.getEntriesAsString())); //refinement - for getting quotes back
        } catch (Exception e) {
            log.error("Failed to retrieve entry-day detail from DB. ", e);
        }
        return builder.build();
    }

    public String processAllRowsToCsv() {
        StringBuilder dataLines = new StringBuilder();
        retrieveAllEntities().forEach(entryDay ->
                dataLines.append(StringUtils.joinWith(COMMA_STR,
                                //String.valueOf(entryDay.getEntryDayId().getEmailHash()), entryDay.getEntryDayId().getDate(), entryDay.getTitle(), entryDay.getEntriesAsString()))
                                entryDay.getEmailHashAndDate(), String.valueOf(entryDay.getEmailHash()), entryDay.getDate(), entryDay.getTitle(), entryDay.getEntriesAsString()))
                        .append(NEW_LINE)
        );
        return dataLines.toString();
    }

    public Integer bulkDeleteEntryDaysOfUser(Integer emailHash) {
        try {
            long deletedRowCount = entryDayRepository.deleteRowsWithEmailHash(emailHash);
            log.info("Deleted all {} entry days on email hash of {}", deletedRowCount, emailHash);
            return Math.toIntExact(deletedRowCount);
        } catch (Exception e) {
            log.error("Failed to delete all entry days on email hash of: {}. ", emailHash, e);
        }
        return NA_INT;
    }
}