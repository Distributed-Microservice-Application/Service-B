package com.DMA.Service_B.repository.replica;

import com.DMA.Service_B.entity.Outbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReplicaOutboxRepository extends JpaRepository<Outbox, Long> {
    
    @Query("SELECT o FROM Outbox o WHERE o.id = 1")
    Optional<Outbox> findSummationRecord();
}
