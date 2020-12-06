package de.offersapp.aldiapi.repository.search;

import de.offersapp.aldiapi.domain.Article;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;


/**
 * Spring Data Elasticsearch repository for the {@link Article} entity.
 */
public interface ArticleSearchRepository extends ElasticsearchRepository<Article, Long> {
}
