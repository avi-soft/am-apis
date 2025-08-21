package com.community.api.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RefundService {
    @Value("${razorpay.key.id}")
    private String razorpayId;
    @Value("${razorpay.key.secret}")
    private String razorpaySecret;
    @Value(("${razorpay.webhook.secret}"))
    private String razorpayWebhookSecret;
}
