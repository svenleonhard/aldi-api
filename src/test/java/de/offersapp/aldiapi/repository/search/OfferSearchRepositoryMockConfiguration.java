package de.offersapp.aldiapi.repository.search;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;

/**
 * Configure a Mock version of {@link OfferSearchRepository} to test the
 * application without starting Elasticsearch.
 */
@Configuration
public class OfferSearchRepositoryMockConfiguration {

    @MockBean
    private OfferSearchRepository mockOfferSearchRepository;

}
