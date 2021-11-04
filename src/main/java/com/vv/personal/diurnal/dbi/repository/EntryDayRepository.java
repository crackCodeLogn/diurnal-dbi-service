package com.vv.personal.diurnal.dbi.repository;

import com.vv.personal.diurnal.dbi.model.EntryDayEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

/**
 * @author Vivek
 * @since 27/10/21
 */
@Repository
//public interface EntryDayRepository extends JpaRepository<EntryDayEntity, EntryDayId> {
public interface EntryDayRepository extends JpaRepository<EntryDayEntity, String> {

    List<EntryDayEntity> findByEmailHash(Integer emailHash);

    @Modifying
    @Transactional
    @Query(value = "delete from EntryDayEntity where emailHash = :emailHash")
    long deleteRowsWithEmailHash(@Param("emailHash") Integer emailHash);
}