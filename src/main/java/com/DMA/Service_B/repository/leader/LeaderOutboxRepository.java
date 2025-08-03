package com.DMA.Service_B.repository.leader;

import com.DMA.Service_B.entity.Outbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LeaderOutboxRepository extends JpaRepository<Outbox, Long> {
    
    @Query("SELECT o FROM Outbox o WHERE o.id = 1")
    Optional<Outbox> findSummationRecord();
    
    @Modifying
    @Query("UPDATE Outbox o SET o.sum = o.sum + :value WHERE o.id = 1")
    int updateSummationValue(@Param("value") Integer value);
    
    @Modifying
    @Query("UPDATE Outbox o SET o.sum = :newSum WHERE o.id = 1")
    int setSummationValue(@Param("newSum") Integer newSum);
}
