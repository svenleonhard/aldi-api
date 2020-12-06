package de.offersapp.aldiapi.service.impl;

import de.offersapp.aldiapi.service.OfferService;
import de.offersapp.aldiapi.domain.Offer;
import de.offersapp.aldiapi.repository.OfferRepository;
import de.offersapp.aldiapi.repository.search.OfferSearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * Service Implementation for managing {@link Offer}.
 */
@Service
@Transactional
public class OfferServiceImpl implements OfferService {

    private final Logger log = LoggerFactory.getLogger(OfferServiceImpl.class);

    private final OfferRepository offerRepository;

    private final OfferSearchRepository offerSearchRepository;

    public OfferServiceImpl(OfferRepository offerRepository, OfferSearchRepository offerSearchRepository) {
        this.offerRepository = offerRepository;
        this.offerSearchRepository = offerSearchRepository;
    }

    @Override
    public Offer save(Offer offer) {
        log.debug("Request to save Offer : {}", offer);
        Offer result = offerRepository.save(offer);
        offerSearchRepository.save(result);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Offer> findAll() {
        log.debug("Request to get all Offers");
        return offerRepository.findAll();
    }


    @Override
    @Transactional(readOnly = true)
    public Optional<Offer> findOne(Long id) {
        log.debug("Request to get Offer : {}", id);
        return offerRepository.findById(id);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete Offer : {}", id);
        offerRepository.deleteById(id);
        offerSearchRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Offer> search(String query) {
        log.debug("Request to search Offers for query {}", query);
        return StreamSupport
            .stream(offerSearchRepository.search(queryStringQuery(query)).spliterator(), false)
        .collect(Collectors.toList());
    }
}
