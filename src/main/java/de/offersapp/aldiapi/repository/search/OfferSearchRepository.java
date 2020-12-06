package de.offersapp.aldiapi.repository.search;

import de.offersapp.aldiapi.domain.Offer;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;


/**
 * Spring Data Elasticsearch repository for the {@link Offer} entity.
 */
public interface OfferSearchRepository extends ElasticsearchRepository<Offer, Long> {
}
