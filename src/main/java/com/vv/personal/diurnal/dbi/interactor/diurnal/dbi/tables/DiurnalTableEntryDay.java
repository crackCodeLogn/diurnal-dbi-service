package com.vv.personal.diurnal.dbi.interactor.diurnal.dbi.tables;

import com.vv.personal.diurnal.artifactory.generated.EntryDayProto;
import com.vv.personal.diurnal.dbi.model.EntryDayEntity;
import com.vv.personal.diurnal.dbi.model.EntryDayId;
import com.vv.personal.diurnal.dbi.repository.EntryDayRepository;
import com.vv.personal.diurnal.dbi.util.DiurnalUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

import static com.vv.personal.diurnal.dbi.constants.Constants.EMPTY_ENTRY_DAY;

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

    public int pushNewEntity(Integer emailHash, Integer date, String title, String description) {
        if (log.isDebugEnabled()) log.debug("Pushing new EntryDay entity: {} x {} x {}", emailHash, date, title);
        EntryDayEntity entryDayEntity = new EntryDayEntity()
                .setEntryDayId(generateEntryDayIdentifier(emailHash, date))
                .setTitle(title).setEntriesAsString(description);
        try {
            entryDayRepository.save(entryDayEntity);
            log.info("Pushed new EntryDay entity: {}", entryDayEntity);
            return 1;
        } catch (Exception e) {
            log.error("Failed to push new entry-day mapping with identifier: {}. ", entryDayEntity.getEntryDayId(), e);
        }
        return -1;
    }

    private EntryDayId generateEntryDayIdentifier(Integer emailHash, Integer date) {
        return new EntryDayId().setEmailHash(emailHash).setDate(date);
    }

    public int deleteEntity(Integer emailHash, Integer date) {
        EntryDayId entryDayIdentifier = generateEntryDayIdentifier(emailHash, date);
        try {
            entryDayRepository.deleteById(entryDayIdentifier);
            log.info("Deleted EntryDay entity: {}", entryDayIdentifier);
            return 1;
        } catch (Exception e) {
            log.error("Failed to delete entry-day mapping with identifier: {}. ", entryDayIdentifier, e);
        }
        return -1;
    }

    public boolean checkEntity(Integer emailHash, Integer date) {
        EntryDayId entryDayIdentifier = generateEntryDayIdentifier(emailHash, date);
        try {
            return entryDayRepository.existsById(entryDayIdentifier);
        } catch (Exception e) {
            log.error("Failed to check entry-day mapping with identifier: {}. ", entryDayIdentifier, e);
        }
        return false;
    }

    public EntryDayProto.EntryDayList retrieveAll() {
        List<EntryDayEntity> entryDayEntityList = entryDayRepository.findAll();
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
        EntryDayId entryDayIdentifier = generateEntryDayIdentifier(emailHash, date);
        try {
            return entryDayRepository.getById(entryDayIdentifier);
        } catch (Exception e) {
            log.error("Failed to get entry-day mapping with identifier: {}. ", entryDayIdentifier, e);
        }
        return EMPTY_ENTRY_DAY_ENTITY;
    }

    public EntryDayProto.EntryDayList retrieveSome(Integer emailHash) {
        //for now only retrieving some on basis of the hash_email for the backup data retrieval step
        List<EntryDayEntity> entryDaysOnHash = entryDayRepository.findByEntryDayIdEmailHash(emailHash);
        return generateDetails(entryDaysOnHash);
    }

    public EntryDayProto.EntryDay generateDetail(EntryDayEntity entryDayEntity) {
        EntryDayProto.EntryDay.Builder builder = EntryDayProto.EntryDay.newBuilder();
        try {
            builder.setHashEmail(entryDayEntity.getEntryDayId().getEmailHash());
            builder.setDate(entryDayEntity.getEntryDayId().getDate());
            builder.setTitle(DiurnalUtil.refineDbStringForOriginal(entryDayEntity.getTitle()));
            builder.setEntriesAsString(
                    DiurnalUtil.refineDbStringForOriginal(entryDayEntity.getEntriesAsString())); //refinement - for getting quotes back
        } catch (Exception e) {
            log.error("Failed to retrieve entry-day detail from DB. ", e);
        }
        return builder.build();
    }

    protected Queue<String> processDataToCsv(EntryDayProto.EntryDayList dataList) {
        Queue<String> dataLines = new LinkedList<>();
        dataList.getEntryDayList().forEach(entryDay -> dataLines.add(
                StringUtils.joinWith(",",
                        String.valueOf(entryDay.getHashEmail()), entryDay.getDate(), entryDay.getTitle(), entryDay.getEntriesAsString())
        ));
        return dataLines;
    }

    public Integer bulkDeleteEntryDaysOfUser(Integer emailHash) {
        try {
            int deletedRowCount = entryDayRepository.deleteByEntryDayIdEmailHash(emailHash);
            log.info("Deleted all {} entry days on email hash of {}", deletedRowCount, emailHash);
            return deletedRowCount;
        } catch (Exception e) {
            log.error("Failed to delete all entry days on email hash of: {}. ", emailHash, e);
        }
        return -1;
    }
}