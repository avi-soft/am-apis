package com.community.api.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;

@Service
public class ScheduleService {
    @Autowired
    private EntityManager entityManager;


}
