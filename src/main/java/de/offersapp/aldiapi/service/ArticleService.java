package de.offersapp.aldiapi.service;

import de.offersapp.aldiapi.domain.Article;

import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link Article}.
 */
public interface ArticleService {

    /**
     * Save a article.
     *
     * @param article the entity to save.
     * @return the persisted entity.
     */
    Article save(Article article);

    /**
     * Get all the articles.
     *
     * @return the list of entities.
     */
    List<Article> findAll();


    /**
     * Get the "id" article.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<Article> findOne(Long id);

    /**
     * Delete the "id" article.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);

    /**
     * Search for the article corresponding to the query.
     *
     * @param query the query of the search.
     * 
     * @return the list of entities.
     */
    List<Article> search(String query);
}
