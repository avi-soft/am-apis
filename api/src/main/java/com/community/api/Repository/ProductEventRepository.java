package com.community.api.Repository;

import com.community.api.entity.ProductEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductEventRepository extends JpaRepository<ProductEvent,Long> {
}
