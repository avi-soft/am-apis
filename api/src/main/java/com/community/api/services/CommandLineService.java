package com.community.api.services;

import com.community.api.services.exception.ExceptionHandlingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.sql.DataSource;
import javax.transaction.Transactional;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.JdbcTemplate;
@Component
    public class CommandLineService implements ApplicationRunner {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ZoneDivisionService zoneDivisionService;

    @Autowired
    private DataSource dataSource;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    ExceptionHandlingService exceptionHandlingService;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        try{
            System.out.println("insertion start");
            String scriptPathForInsertion = "insertion_log.sql";
            String sqlScript = new BufferedReader(
                    new InputStreamReader(new ClassPathResource(scriptPathForInsertion).getInputStream())
            ).lines().collect(Collectors.joining("\n"));
            jdbcTemplate.execute(sqlScript);
            zoneDivisionService.populateZoneDivision();
            System.out.println("insertion end");
            System.out.println("ALTERATION START");
            String scriptPathForAlteration = "alteration_log.sql";
            sqlScript = new BufferedReader(
                    new InputStreamReader(new ClassPathResource(scriptPathForAlteration).getInputStream())
            ).lines().collect(Collectors.joining("\n"));
            jdbcTemplate.execute(sqlScript);
            // Execute the SQL script
            System.out.println("ALTERATION END");
        }catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
        }
    }

}