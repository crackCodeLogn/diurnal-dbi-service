package com.vv.personal.diurnal.dbi.repository;

import com.vv.personal.diurnal.dbi.model.EntryDayEntity;
import com.vv.personal.diurnal.dbi.model.EntryDayId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

/**
 * @author Vivek
 * @since 27/10/21
 */
@Repository
public interface EntryDayRepository extends JpaRepository<EntryDayEntity, EntryDayId> {

    List<EntryDayEntity> findByEntryDayIdEmailHash(Integer emailHash);

    @Modifying
    @Transactional
    int deleteByEntryDayIdEmailHash(Integer emailHash); //didn't have time to find sufficient query
}