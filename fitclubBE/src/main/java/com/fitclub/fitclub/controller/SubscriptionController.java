package com.fitclub.fitclub.controller;

import com.fitclub.fitclub.model.Entity.Subscription;
import com.fitclub.fitclub.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/1.0")
public class SubscriptionController {

    @Autowired
    private SubscriptionService subscriptionService;

    @PostMapping("/subscription")
    public ResponseEntity<?> saveSubscription(@RequestBody Subscription subscription) {
        return new ResponseEntity<>(subscriptionService.saveSubscription(subscription), HttpStatus.CREATED);
    }

    @DeleteMapping("/subscription/{subscriptionId}")
    public ResponseEntity<?> deleteSubscription(@PathVariable Long subscriptionId) {
        subscriptionService.deleteSubscription(subscriptionId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/subscription")
    public ResponseEntity<?> getAllSubscriptions() {
        return ResponseEntity.ok(subscriptionService.findAllSubscriptions());
    }
}
