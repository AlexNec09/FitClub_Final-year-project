package com.fitclub.fitclub.service;

import com.fitclub.fitclub.dao.subscription.SubscriptionRepository;
import com.fitclub.fitclub.model.Entity.Subscription;

import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class SubscriptionService {

    SubscriptionRepository subscriptionRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository) {
        super();
        this.subscriptionRepository = subscriptionRepository;
    }


    public Subscription saveSubscription(Subscription subscription) {
        subscription.setDate(new Date());
        return subscriptionRepository.save(subscription);
    }

    public void deleteSubscription(Long subscriptionId) {
        subscriptionRepository.deleteById(subscriptionId);
    }

    public List<Subscription> findAllSubscriptions() {
        return subscriptionRepository.findAll();
    }
}
