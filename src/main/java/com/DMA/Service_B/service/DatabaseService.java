package com.DMA.Service_B.service;

import com.DMA.Service_B.entity.Outbox;
import com.DMA.Service_B.repository.leader.LeaderOutboxRepository;
import com.DMA.Service_B.repository.replica.ReplicaOutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class DatabaseService {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseService.class);
    
    @Autowired
    private LeaderOutboxRepository leaderRepository;
    
    @Autowired
    private ReplicaOutboxRepository replicaRepository;
    
    /**
     * Initialize the summation record in both databases if it doesn't exist
     */
    public void initializeSummationRecord() {
        // Initialize Leader DB
        initializeLeaderRecord();
        
        // Initialize Replica DB
        initializeReplicaRecord();
    }
    
    @Transactional("leaderTransactionManager")
    private void initializeLeaderRecord() {
        try {
            Optional<Outbox> leaderRecord = leaderRepository.findSummationRecord();
            if (leaderRecord.isEmpty()) {
                Outbox newRecord = new Outbox(0);
                leaderRepository.save(newRecord);
                leaderRepository.flush(); // Ensure immediate persistence
                logger.info("Initialized summation record in Leader DB with ID=1 and value: 0");
            } else {
                logger.info("Leader DB summation record already exists with ID=1 and value: {}", leaderRecord.get().getSum());
            }
        } catch (Exception e) {
            logger.error("Error initializing Leader DB: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize Leader DB", e);
        }
    }
    
    @Transactional("replicaTransactionManager")
    private void initializeReplicaRecord() {
        try {
            Optional<Outbox> replicaRecord = replicaRepository.findSummationRecord();
            if (replicaRecord.isEmpty()) {
                Outbox newRecord = new Outbox(0);
                replicaRepository.save(newRecord);
                replicaRepository.flush(); // Ensure immediate persistence
                logger.info("Initialized summation record in Replica DB with ID=1 and value: 0");
            } else {
                logger.info("Replica DB summation record already exists with ID=1 and value: {}", replicaRecord.get().getSum());
            }
        } catch (Exception e) {
            logger.error("Error initializing Replica DB: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize Replica DB", e);
        }
    }
    
    /**
     * Update summation value in Leader DB and synchronize to Replica DB
     */
    public void updateSummationValue(Integer valueToAdd) {
        logger.info("Updating summation value by adding: {}", valueToAdd);
        
        // Update Leader DB and get the new sum
        Integer newSum = updateLeaderDatabase(valueToAdd);

        // Asynchronize to Replica DB
        asyncReplicateToReplica(newSum);
    }
    
    @Transactional("leaderTransactionManager")
    private Integer updateLeaderDatabase(Integer valueToAdd) {
        try {
            Optional<Outbox> currentRecord = leaderRepository.findSummationRecord();
            Integer newSum;
            
            if (currentRecord.isEmpty()) {
                // Create new record if it doesn't exist
                Outbox newRecord = new Outbox(valueToAdd);
                leaderRepository.save(newRecord);
                leaderRepository.flush();
                newSum = valueToAdd;
                logger.info("Created new Leader DB record with sum: {}", newSum);
            } else {
                // Update existing record using entity-based approach
                Outbox record = currentRecord.get();
                Integer oldSum = record.getSum();
                newSum = oldSum + valueToAdd;
                record.setSum(newSum);
                leaderRepository.save(record);
                leaderRepository.flush();
                logger.info("Updated Leader DB record from {} to {} (added {})", oldSum, newSum, valueToAdd);
            }
            
            logger.info("Successfully updated Leader DB. New sum: {}", newSum);
            return newSum;
        } catch (Exception e) {
            logger.error("Error updating Leader DB: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update Leader DB", e);
        }
    }
    
    /**
     * Get summation value with fallback logic (read from Replica first, fallback to Leader if unavailable)
     */
    public Integer getSummationValue() {
        try {
            // First try to read from Replica DB
            return getSummationValueFromReplica();
        } catch (Exception e) {
            logger.warn("Failed to read from Replica DB, falling back to Leader DB: {}", e.getMessage());
            try {
                return getSummationValueFromLeader();
            } catch (Exception leaderException) {
                logger.error("Failed to read from both Replica and Leader DBs. Replica error: {}, Leader error: {}", e.getMessage(), leaderException.getMessage());
                throw new RuntimeException("Failed to read summation value from both databases", leaderException);
            }
        }
    }

    /**
     * Get summation value from Replica DB only
     */
    @Transactional(value = "replicaTransactionManager", readOnly = true)
    public Integer getSummationValueFromReplica() {
        try {
            Optional<Outbox> record = replicaRepository.findSummationRecord();
            if (record.isPresent()) {
                Integer value = record.get().getSum();
                logger.info("Retrieved summation value from Replica DB: {}", value);
                return value;
            } else {
                logger.warn("No summation record found in Replica DB");
                throw new RuntimeException("No summation record found in Replica DB");
            }
        } catch (Exception e) {
            logger.error("Error reading from Replica DB: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to read from Replica DB", e);
        }
    }

    /**
     * Get summation value from Leader DB only
     */
    @Transactional(value = "leaderTransactionManager", readOnly = true)
    public Integer getSummationValueFromLeader() {
        try {
            Optional<Outbox> record = leaderRepository.findSummationRecord();
            if (record.isPresent()) {
                Integer value = record.get().getSum();
                logger.info("Retrieved summation value from Leader DB: {}", value);
                return value;
            } else {
                logger.warn("No summation record found in Leader DB, initializing...");
                initializeSummationRecord();
                return 0;
            }
        } catch (Exception e) {
            logger.error("Error reading from Leader DB: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to read from Leader DB", e);
        }
    }
    
    /**
     * Synchronize data from Leader to Replica
     */
    @Transactional("replicaTransactionManager")
    private void asyncReplicateToReplica(Integer newSum) {
        try {
            Optional<Outbox> replicaRecord = replicaRepository.findSummationRecord();
            if (replicaRecord.isPresent()) {
                Outbox record = replicaRecord.get();
                record.setSum(newSum);
                replicaRepository.save(record);
                replicaRepository.flush(); // Ensure immediate persistence
            } else {
                // Create new record if it doesn't exist
                Outbox newRecord = new Outbox(newSum);
                replicaRepository.save(newRecord);
                replicaRepository.flush(); // Ensure immediate persistence
            }
            
            logger.info("Successfully synchronized to Replica DB. New sum: {}", newSum);
        } catch (Exception e) {
            logger.warn("Replica DB update failed — Leader DB is already updated. Reason: {}", e.getMessage(), e);
            // Do not throw an exception here, as the Leader DB is already updated
        }
    }
}
