package com.arseniolourenco.inventory_service.repository;

import com.arseniolourenco.inventory_service.model.InventoryModel;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<InventoryModel, Long> {
    // Declare the method so it is available to the service
    List<InventoryModel> findBySkuCodeIn(Collection<String> skuCodes);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM InventoryModel i WHERE i.skuCode IN :skuCodes")
    List<InventoryModel> findBySkuCodeInWithLock(@Param("skuCodes") Collection<String> skuCodes);

}
