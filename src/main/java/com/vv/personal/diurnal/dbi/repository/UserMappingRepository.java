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

    @Query(value = "SELECT hash_email from user_mapping where email = :email", nativeQuery = true)
    Integer retrieveEmailHash(@Param("email") String email);
    //Integer findEmailHashByEmail(String email); //proper query is above!

    //@Query(value = "SELECT * from user_mapping where hash_email = :emailHash", nativeQuery = true)
    //UserMappingEntity retrieveUserDetail(@Param("emailHash") Integer emailHash);
    UserMappingEntity findByEmailHash(Integer emailHash); //proper query is above!
}