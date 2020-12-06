package de.offersapp.aldiapi.service.impl;

import de.offersapp.aldiapi.service.ArticleService;
import de.offersapp.aldiapi.domain.Article;
import de.offersapp.aldiapi.repository.ArticleRepository;
import de.offersapp.aldiapi.repository.search.ArticleSearchRepository;
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
 * Service Implementation for managing {@link Article}.
 */
@Service
@Transactional
public class ArticleServiceImpl implements ArticleService {

    private final Logger log = LoggerFactory.getLogger(ArticleServiceImpl.class);

    private final ArticleRepository articleRepository;

    private final ArticleSearchRepository articleSearchRepository;

    public ArticleServiceImpl(ArticleRepository articleRepository, ArticleSearchRepository articleSearchRepository) {
        this.articleRepository = articleRepository;
        this.articleSearchRepository = articleSearchRepository;
    }

    @Override
    public Article save(Article article) {
        log.debug("Request to save Article : {}", article);
        Article result = articleRepository.save(article);
        articleSearchRepository.save(result);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Article> findAll() {
        log.debug("Request to get all Articles");
        return articleRepository.findAll();
    }


    @Override
    @Transactional(readOnly = true)
    public Optional<Article> findOne(Long id) {
        log.debug("Request to get Article : {}", id);
        return articleRepository.findById(id);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete Article : {}", id);
        articleRepository.deleteById(id);
        articleSearchRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Article> search(String query) {
        log.debug("Request to search Articles for query {}", query);
        return StreamSupport
            .stream(articleSearchRepository.search(queryStringQuery(query)).spliterator(), false)
        .collect(Collectors.toList());
    }
}
