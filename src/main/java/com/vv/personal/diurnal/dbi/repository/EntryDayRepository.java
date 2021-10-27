package com.vv.personal.diurnal.dbi.repository;

import com.vv.personal.diurnal.dbi.model.EntryDayEntity;
import com.vv.personal.diurnal.dbi.model.EntryDayIdentifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author Vivek
 * @since 27/10/21
 */
@Repository
public interface EntryDayRepository extends JpaRepository<EntryDayEntity, EntryDayIdentifier> {

    @Query(name = "SELECT * from entry_day where hash_email = :emailHash", nativeQuery = true)
    List<EntryDayEntity> retrieveEntryDaysOnEmailHash(@RequestParam("emailHash") Integer emailHash);

    @Query(name = "DELETE from entry_day where hash_email := emailHash", nativeQuery = true)
    void deleteEntryDaysOnEmailHash(@RequestParam("emailHash") Integer emailHash);
}