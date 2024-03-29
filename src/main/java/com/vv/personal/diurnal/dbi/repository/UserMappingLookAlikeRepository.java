package com.vv.personal.diurnal.dbi.repository;

import com.vv.personal.diurnal.dbi.model.UserMappingLookAlikeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Vivek
 * @since 24/10/21
 */
@Repository
public interface UserMappingLookAlikeRepository extends JpaRepository<UserMappingLookAlikeEntity, Integer> {
}