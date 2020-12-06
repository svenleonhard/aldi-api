package de.offersapp.aldiapi.service.impl;

import de.offersapp.aldiapi.domain.Offer;
import de.offersapp.aldiapi.service.OffersParserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class OffersParserServiceImpl implements OffersParserService {

    private final Logger log = LoggerFactory.getLogger(OffersParserServiceImpl.class);


    @Override
    public List<Offer> fetchOffers() {
        return null;
    }
}
