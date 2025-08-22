package com.community.api.services;

import com.community.api.entity.ErrorResponse;
import com.community.api.entity.Refunds;
import com.razorpay.Entity;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.razorpay.RazorpayClient;
import com.razorpay.Refund;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.Date;

@Service
public class RefundService {

    @Value("${razorpay.key.id}")
    private String razorpayId;

    @Value("${razorpay.key.secret}")
    private String razorpaySecret;

    @Value("${razorpay.webhook.secret}")
    private String razorpayWebhookSecret;

    private RazorpayClient razorpayClient;

    @Autowired
    EntityManager entityManager;

    @PostConstruct
    public void initClient() throws RazorpayException {
        // initialize client with your Razorpay API keys
        this.razorpayClient = new RazorpayClient(razorpayId, razorpaySecret);
    }
    @Async
    @Transactional
    public ResponseEntity<?> refundPayment(Long orderId, String paymentId, Double amount) throws RazorpayException {
        try {
            JSONObject refundRequest = new JSONObject();
            refundRequest.put("amount", amount);
//            refundRequest.put("speed", "normal");
            refundRequest.put("speed","optimum");

            Refund refund = razorpayClient.payments.refund(paymentId, refundRequest);
            Refunds refunds = entityManager.find(Refunds.class, orderId);
            if (refunds == null)
                return ResponseService.generateErrorResponse("Cannot process refund", HttpStatus.BAD_REQUEST);
            refunds.setModifiedAt(new Date());
            String status = refund.get("status"); // "processed" / "pending" / "failed"
            refunds.setRefundState(status);
            if (status.equalsIgnoreCase("SUCCESS"))
                refunds.setRefundSuccess(true);
            refunds.setRefundId(refund.get("id"));
            entityManager.merge(refunds);
            return ResponseService.generateSuccessResponse("Refund Processed", refunds, HttpStatus.OK);
        }catch (Exception exception)
        {
            exception.printStackTrace();
            return ResponseService.generateErrorResponse("Error processing refund",HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
