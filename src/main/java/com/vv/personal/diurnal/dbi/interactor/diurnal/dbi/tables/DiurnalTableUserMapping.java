package com.vv.personal.diurnal.dbi.interactor.diurnal.dbi.tables;

import com.vv.personal.diurnal.artifactory.generated.UserMappingProto;
import com.vv.personal.diurnal.dbi.model.UserMappingEntity;
import com.vv.personal.diurnal.dbi.repository.UserMappingRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.vv.personal.diurnal.dbi.constants.Constants.*;

/**
 * @author Vivek
 * @since 23/02/21
 */
@Slf4j
public class DiurnalTableUserMapping {
    private static final UserMappingEntity EMPTY_USER_MAPPING_ENTITY = new UserMappingEntity();

    private final UserMappingRepository userMappingRepository;

    public DiurnalTableUserMapping(UserMappingRepository userMappingRepository) {
        this.userMappingRepository = userMappingRepository;
    }

    public int pushNewEntity(UserMappingEntity userMappingEntity) {
        try {
            userMappingRepository.save(userMappingEntity);
            log.info("Pushed new UserMapping entity: {}", userMappingEntity);
            return ONE;
        } catch (Exception e) {
            log.error("Failed to push new user mapping with email: {}. ", userMappingEntity.getEmail(), e);
        }
        return 0;
    }

    public int pushNewEntities(List<UserMappingEntity> userMappingEntityList) {
        try {
            return userMappingRepository.saveAll(userMappingEntityList).size();
        } catch (Exception e) {
            log.error("Failed to bulk push {} new user mappings. ", userMappingEntityList.size(), e);
        }
        return NA_INT;
    }

    public int deleteEntity(int emailHash) {
        try {
            userMappingRepository.deleteById(emailHash);
            log.info("Deleted userMapping entity having email-hash {}", emailHash);
            return ONE;
        } catch (Exception e) {
            log.error("Failed to delete user mapping entity with email-hash: {}. ", emailHash, e);
        }
        return 0;
    }

    public boolean checkIfEntityExists(String email) {
        return userMappingRepository.checkIfEmailExists(email) == ONE;
    }

    public int updateUsername(int emailHash, String username) { //updates the user name
        try {
            UserMappingEntity userMappingEntity = retrieveSingleEntity(emailHash).setUser(username);
            userMappingRepository.save(userMappingEntity);
            return ONE;
        } catch (Exception e) {
            log.error("Failed to update name for hash email {} with {}", emailHash, username, e);
        }
        return NA_INT;
    }

    public int updatePremiumUserStatus(int emailHash, boolean isPremiumUser) {
        try {
            UserMappingEntity userMappingEntity = retrieveSingleEntity(emailHash).setPremiumUser(isPremiumUser);
            userMappingRepository.save(userMappingEntity);
            return ONE;
        } catch (Exception e) {
            log.error("Failed to update isPremiumUser for hash email {} with {}", emailHash, isPremiumUser, e);
        }
        return NA_INT;
    }

    public int updateHashCred(int emailHash, String credHash) {
        try {
            UserMappingEntity userMappingEntity = retrieveSingleEntity(emailHash).setCredHash(credHash);
            userMappingRepository.save(userMappingEntity);
            return ONE;
        } catch (Exception e) {
            log.error("Failed to update credhash for hash email {} with {}", emailHash, credHash, e);
        }
        return NA_INT;
    }

    public int updateMobile(int emailHash, long mobile) {
        try {
            UserMappingEntity userMappingEntity = retrieveSingleEntity(emailHash).setMobile(mobile);
            userMappingRepository.save(userMappingEntity);
            return ONE;
        } catch (Exception e) {
            log.error("Failed to update name for mobile email {} with {}", emailHash, mobile, e);
        }
        return NA_INT;
    }

    public int updateCurrency(int emailHash, UserMappingProto.Currency currency) {
        try {
            UserMappingEntity userMappingEntity = retrieveSingleEntity(emailHash).setCurrency(currency.name());
            userMappingRepository.save(userMappingEntity);
            return ONE;
        } catch (Exception e) {
            log.error("Failed to update currency for hash email {} with {}", emailHash, currency, e);
        }
        return NA_INT;
    }

    public int updateLastCloudSaveTimestamp(int emailHash, long lastCloudSaveTimestamp) {
        try {
            UserMappingEntity userMappingEntity = retrieveSingleEntity(emailHash)
                    .setLastCloudSaveTimestamp(Instant.ofEpochMilli(lastCloudSaveTimestamp));
            userMappingRepository.save(userMappingEntity);
            return ONE;
        } catch (Exception e) {
            log.error("Failed to update lastCloudSaveTimestamp for hash email {} with {}", emailHash, lastCloudSaveTimestamp, e);
        }
        return NA_INT;
    }

    public int updateLastSavedTimestamp(int emailHash, long lastSavedTimestamp) {
        try {
            UserMappingEntity userMappingEntity = retrieveSingleEntity(emailHash)
                    .setLastSaveTimestamp(Instant.ofEpochMilli(lastSavedTimestamp));
            userMappingRepository.save(userMappingEntity);
            return ONE;
        } catch (Exception e) {
            log.error("Failed to update lastSavedTimestamp for hash email {} with {}", emailHash, lastSavedTimestamp, e);
        }
        return NA_INT;
    }

    public int updatePaymentExpiryTimestamp(int emailHash, long paymentExpiryTimestamp) {
        try {
            UserMappingEntity userMappingEntity = retrieveSingleEntity(emailHash)
                    .setPaymentExpiryTimestamp(Instant.ofEpochMilli(paymentExpiryTimestamp));
            userMappingRepository.save(userMappingEntity);
            return ONE;
        } catch (Exception e) {
            log.error("Failed to update paymentExpiryTimestamp for hash email {} with {}", emailHash, paymentExpiryTimestamp, e);
        }
        return NA_INT;
    }

    public Integer retrieveHashEmail(String email) {
        try {
            return userMappingRepository.retrieveEmailHash(email);
        } catch (Exception e) {
            log.error("Failed to retrieve email hash for '{}'. ", email, e);
        }
        return DEFAULT_EMAIL_HASH;
    }

    public UserMappingEntity retrieveSingleEntity(int emailHash) {
        try {
            return userMappingRepository.findById(emailHash)
                    .orElseThrow();
        } catch (Exception e) {
            log.error("Failed to retrieve single entity og email-hash: {}. ", emailHash, e);
        }
        return EMPTY_USER_MAPPING_ENTITY;
    }

    public String retrieveHashCred(int emailHash) {
        try {
            return retrieveSingleEntity(emailHash).getCredHash();
        } catch (Exception e) {
            log.error("Failed to retrieve cred hash from db for {}. ", emailHash, e);
        }
        return EMPTY_STR;
    }

    public Boolean retrievePremiumUserStatus(int emailHash) {
        try {
            return retrieveSingleEntity(emailHash).isPremiumUser();
        } catch (Exception e) {
            log.error("Failed to retrieve isPremiumUser from db for {}. ", emailHash, e);
        }
        return false;
    }

    public List<UserMappingEntity> retrieveAllEntities() {
        try {
            return userMappingRepository.findAll();
        } catch (Exception e) {
            log.error("Failed to retrieve all user-mappings. ", e);
        }
        return new ArrayList<>();
    }

    public UserMappingProto.UserMappingList retrieveAll() {
        List<UserMappingEntity> userMappingEntityList = retrieveAllEntities();
        UserMappingProto.UserMappingList.Builder userMappingsBuilder = UserMappingProto.UserMappingList.newBuilder();
        log.info("Extracted {} rows from DB on retrieveAll operation.", userMappingEntityList.size());
        userMappingsBuilder.addAllUserMapping(
                userMappingEntityList.stream().map(this::generateDetail).collect(Collectors.toList())
        );
        return userMappingsBuilder.build();
    }

    public UserMappingProto.UserMapping retrieveSingle(int emailHash) {
        try {
            UserMappingEntity userMappingEntity = userMappingRepository.findByEmailHash(emailHash);
            return generateDetail(userMappingEntity);
        } catch (Exception e) {
            log.error("Failed to retrieve single entity from db. ", e);
        }
        return EMPTY_USER_MAPPING;
    }

    UserMappingProto.UserMapping generateDetail(UserMappingEntity userMappingEntity) {
        UserMappingProto.UserMapping.Builder builder = UserMappingProto.UserMapping.newBuilder();
        try {
            builder.setMobile(userMappingEntity.getMobile());
            builder.setEmail(userMappingEntity.getEmail());
            builder.setUsername(userMappingEntity.getUser());
            builder.setPremiumUser(userMappingEntity.isPremiumUser());
            builder.setHashCred(userMappingEntity.getCredHash());
            builder.setHashEmail(userMappingEntity.getEmailHash());
            builder.setLastCloudSaveTimestamp(convertInstantToMillis(userMappingEntity.getLastCloudSaveTimestamp()));
            builder.setLastSavedTimestamp(convertInstantToMillis(userMappingEntity.getLastSaveTimestamp()));
            builder.setPaymentExpiryTimestamp(convertInstantToMillis(userMappingEntity.getPaymentExpiryTimestamp()));
            builder.setAccountCreationTimestamp(convertInstantToMillis(userMappingEntity.getAccountCreationTimestamp()));
            builder.setCurrency(UserMappingProto.Currency.valueOf(userMappingEntity.getCurrency()));
        } catch (Exception e) {
            log.error("Failed to retrieve user-mapping detail from DB. ", e);
        }
        return builder.build();
    }

    private long convertInstantToMillis(Instant instant) {
        return instant.toEpochMilli();
    }

    public String processDataToCsv() {
        StringBuilder dataLines = new StringBuilder();
        retrieveAllEntities().forEach(userMapping ->
                dataLines.append(StringUtils.joinWith(COMMA_STR,
                                String.valueOf(userMapping.getMobile()), userMapping.getEmail(), userMapping.getUser(), userMapping.isPremiumUser(), userMapping.getCredHash(), userMapping.getEmailHash(),
                                userMapping.getLastCloudSaveTimestamp(), userMapping.getLastSaveTimestamp(), userMapping.getPaymentExpiryTimestamp(), userMapping.getAccountCreationTimestamp(), userMapping.getCurrency()))
                        .append(NEW_LINE)
        );
        return dataLines.toString();
    }
}