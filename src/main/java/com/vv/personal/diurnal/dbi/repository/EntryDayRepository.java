package com.vv.personal.diurnal.dbi.repository;

import com.vv.personal.diurnal.dbi.model.EntryDayEntity;
import com.vv.personal.diurnal.dbi.model.EntryDayId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Vivek
 * @since 27/10/21
 */
@Repository
public interface EntryDayRepository extends JpaRepository<EntryDayEntity, EntryDayId> {

    List<EntryDayEntity> findByEntryDayIdEmailHash(Integer emailHash);

    //@Query(name = "DELETE from entry_day where entryDayId.hash_email := emailHash", nativeQuery = true)
    //void deleteEntryDaysOnEmailHash(@RequestParam("emailHash") Integer emailHash);
    List<EntryDayEntity> deleteByEntryDayIdEmailHash(Integer emailHash);
}