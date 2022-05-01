package com.fitclub.fitclub.dao.subscription;

import com.fitclub.fitclub.model.Entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
}