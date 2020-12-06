package de.offersapp.aldiapi.service;

import de.offersapp.aldiapi.AldiApiApp;
import de.offersapp.aldiapi.service.impl.OffersParserServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = AldiApiApp.class)
public class OffersParserServiceTest {

    @Autowired
    private OffersParserServiceImpl offersParserService;

    private final Logger log = LoggerFactory.getLogger(OffersParserServiceImpl.class);

    @Test
    public void fetchOffersTest(){
        Assertions.assertNull(offersParserService.fetchOffers());
    }

}
