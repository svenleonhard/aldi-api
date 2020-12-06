package de.offersapp.aldiapi.service;

import java.util.List;

import javax.persistence.criteria.JoinType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.jhipster.service.QueryService;

import de.offersapp.aldiapi.domain.Offer;
import de.offersapp.aldiapi.domain.*; // for static metamodels
import de.offersapp.aldiapi.repository.OfferRepository;
import de.offersapp.aldiapi.repository.search.OfferSearchRepository;
import de.offersapp.aldiapi.service.dto.OfferCriteria;

/**
 * Service for executing complex queries for {@link Offer} entities in the database.
 * The main input is a {@link OfferCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link Offer} or a {@link Page} of {@link Offer} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class OfferQueryService extends QueryService<Offer> {

    private final Logger log = LoggerFactory.getLogger(OfferQueryService.class);

    private final OfferRepository offerRepository;

    private final OfferSearchRepository offerSearchRepository;

    public OfferQueryService(OfferRepository offerRepository, OfferSearchRepository offerSearchRepository) {
        this.offerRepository = offerRepository;
        this.offerSearchRepository = offerSearchRepository;
    }

    /**
     * Return a {@link List} of {@link Offer} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<Offer> findByCriteria(OfferCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<Offer> specification = createSpecification(criteria);
        return offerRepository.findAll(specification);
    }

    /**
     * Return a {@link Page} of {@link Offer} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<Offer> findByCriteria(OfferCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Offer> specification = createSpecification(criteria);
        return offerRepository.findAll(specification, page);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(OfferCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<Offer> specification = createSpecification(criteria);
        return offerRepository.count(specification);
    }

    /**
     * Function to convert {@link OfferCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Offer> createSpecification(OfferCriteria criteria) {
        Specification<Offer> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), Offer_.id));
            }
            if (criteria.getAdvantage() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getAdvantage(), Offer_.advantage));
            }
            if (criteria.getAmount() != null) {
                specification = specification.and(buildStringSpecification(criteria.getAmount(), Offer_.amount));
            }
            if (criteria.getStartDate() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getStartDate(), Offer_.startDate));
            }
            if (criteria.getEndDate() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getEndDate(), Offer_.endDate));
            }
            if (criteria.getArticleId() != null) {
                specification = specification.and(buildSpecification(criteria.getArticleId(),
                    root -> root.join(Offer_.article, JoinType.LEFT).get(Article_.id)));
            }
        }
        return specification;
    }
}
