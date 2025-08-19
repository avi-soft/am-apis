package com.community.api.endpoint.avisoft.controller.Acknowledgement;

import com.community.api.entity.AckRef;
import com.community.api.entity.ErrorResponse;
import com.community.api.entity.UserAcknowledgement;
import com.community.api.services.ResponseService;
import io.swagger.models.auth.In;
import org.owasp.esapi.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.math.BigInteger;
import java.util.Date;

@RestController
@RequestMapping("/ack")
public class AcknowledgementWebhook {
    @Autowired
    EntityManager entityManager;

    @PostMapping("/populate")
    public void populateAckRef(@RequestParam(required = true)String id, Integer role, Long uid)
    {
        Boolean res=checkRef(uid,role);
        AckRef ackRef=null;
        if(res) {
            ackRef = new AckRef();
            ackRef.setAckRef(id);
            ackRef.setUserId(uid);
            ackRef.setRoleId(role);
            entityManager.persist(ackRef);
        }
        else
        {
            Query query=entityManager.createQuery("SELECT ackRef from AckRef a where a.userId =:uid and a.roleId =:role",AckRef.class);
            query.setParameter("uid",uid);
            query.setParameter("role",role);
            String ref=(String)query.getSingleResult();
            if(ref!=null)
            {
                ackRef = entityManager.find(AckRef.class,ref);
                ackRef.setAckRef(id);
                ackRef.setUserId(uid);
                ackRef.setRoleId(role);
                entityManager.merge(ackRef);
            }
        }

    }
    @PostMapping("/map")
    public ResponseEntity<?> ackCustomer(Integer role, Long uid)
    {
        Boolean res=checkAck(uid);
        UserAcknowledgement ackRef=null;
        if(res) {
            ackRef = new UserAcknowledgement();
            ackRef.setUserId(uid);
            ackRef.setAcknowledgedAt(new Date());
            ackRef.setAcknowledgementVersion("v.1");
            entityManager.persist(ackRef);
            return ResponseService.generateSuccessResponse("User acknowledged",ackRef,HttpStatus.OK);
        }
        else
        {
           return ResponseService.generateErrorResponse("An account associated with this phone number already exists. Please log in to continue.", HttpStatus.BAD_REQUEST);
            }
        }

    public Boolean checkRef(Long uid,Integer role)
    {
        Query query=entityManager.createQuery("SELECT COUNT(a) from AckRef a where a.userId =:uid and a.roleId =:role",AckRef.class);
        query.setParameter("uid",uid);
        query.setParameter("role",role);
        return ((BigInteger) query.getSingleResult()).intValue() != 0;
    }
    public Boolean checkAck(Long uid)
    {
        Query query=entityManager.createQuery("SELECT COUNT(a) from UserAcknowledgement a where a.userId =:uid ",UserAcknowledgement.class);
        query.setParameter("uid",uid);
        return ((BigInteger) query.getSingleResult()).intValue() != 0;
    }
}
