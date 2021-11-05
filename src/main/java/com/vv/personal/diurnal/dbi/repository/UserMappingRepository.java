package com.vv.personal.diurnal.dbi.repository;

import com.vv.personal.diurnal.dbi.model.UserMappingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * @author Vivek
 * @since 24/10/21
 */
@Repository
public interface UserMappingRepository extends JpaRepository<UserMappingEntity, Integer> {

    @Query(value = "select emailHash from UserMappingEntity where email = :email")
    Integer retrieveEmailHash(@Param("email") String email);

    @Query(value = "select count(emailHash) from UserMappingEntity where email = :email")
    Long checkIfEmailExists(@Param("email") String email);

    //@Query(value = "SELECT * from user_mapping where hash_email = :emailHash", nativeQuery = true)
    //UserMappingEntity retrieveUserDetail(@Param("emailHash") Integer emailHash);
    UserMappingEntity findByEmailHash(Integer emailHash); //proper query is above!
}