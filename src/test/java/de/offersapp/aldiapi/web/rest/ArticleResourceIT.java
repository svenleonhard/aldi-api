package de.offersapp.aldiapi.web.rest;

import de.offersapp.aldiapi.AldiApiApp;
import de.offersapp.aldiapi.domain.Article;
import de.offersapp.aldiapi.repository.ArticleRepository;
import de.offersapp.aldiapi.repository.search.ArticleSearchRepository;
import de.offersapp.aldiapi.service.ArticleService;
import de.offersapp.aldiapi.service.dto.ArticleCriteria;
import de.offersapp.aldiapi.service.ArticleQueryService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Base64Utils;
import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import de.offersapp.aldiapi.domain.enumeration.ArticleCategory;
/**
 * Integration tests for the {@link ArticleResource} REST controller.
 */
@SpringBootTest(classes = AldiApiApp.class)
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
public class ArticleResourceIT {

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final String DEFAULT_PRODUCER = "AAAAAAAAAA";
    private static final String UPDATED_PRODUCER = "BBBBBBBBBB";

    private static final String DEFAULT_AMOUNT = "AAAAAAAAAA";
    private static final String UPDATED_AMOUNT = "BBBBBBBBBB";

    private static final ArticleCategory DEFAULT_CATEGORY = ArticleCategory.ELECTRONIC;
    private static final ArticleCategory UPDATED_CATEGORY = ArticleCategory.EXTRAORDINARY;

    private static final BigDecimal DEFAULT_PRICE = new BigDecimal(1);
    private static final BigDecimal UPDATED_PRICE = new BigDecimal(2);
    private static final BigDecimal SMALLER_PRICE = new BigDecimal(1 - 1);

    private static final byte[] DEFAULT_PICTURE = TestUtil.createByteArray(1, "0");
    private static final byte[] UPDATED_PICTURE = TestUtil.createByteArray(1, "1");
    private static final String DEFAULT_PICTURE_CONTENT_TYPE = "image/jpg";
    private static final String UPDATED_PICTURE_CONTENT_TYPE = "image/png";

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private ArticleService articleService;

    /**
     * This repository is mocked in the de.offersapp.aldiapi.repository.search test package.
     *
     * @see de.offersapp.aldiapi.repository.search.ArticleSearchRepositoryMockConfiguration
     */
    @Autowired
    private ArticleSearchRepository mockArticleSearchRepository;

    @Autowired
    private ArticleQueryService articleQueryService;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restArticleMockMvc;

    private Article article;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Article createEntity(EntityManager em) {
        Article article = new Article()
            .description(DEFAULT_DESCRIPTION)
            .producer(DEFAULT_PRODUCER)
            .amount(DEFAULT_AMOUNT)
            .category(DEFAULT_CATEGORY)
            .price(DEFAULT_PRICE)
            .picture(DEFAULT_PICTURE)
            .pictureContentType(DEFAULT_PICTURE_CONTENT_TYPE);
        return article;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Article createUpdatedEntity(EntityManager em) {
        Article article = new Article()
            .description(UPDATED_DESCRIPTION)
            .producer(UPDATED_PRODUCER)
            .amount(UPDATED_AMOUNT)
            .category(UPDATED_CATEGORY)
            .price(UPDATED_PRICE)
            .picture(UPDATED_PICTURE)
            .pictureContentType(UPDATED_PICTURE_CONTENT_TYPE);
        return article;
    }

    @BeforeEach
    public void initTest() {
        article = createEntity(em);
    }

    @Test
    @Transactional
    public void createArticle() throws Exception {
        int databaseSizeBeforeCreate = articleRepository.findAll().size();
        // Create the Article
        restArticleMockMvc.perform(post("/api/articles")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(article)))
            .andExpect(status().isCreated());

        // Validate the Article in the database
        List<Article> articleList = articleRepository.findAll();
        assertThat(articleList).hasSize(databaseSizeBeforeCreate + 1);
        Article testArticle = articleList.get(articleList.size() - 1);
        assertThat(testArticle.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testArticle.getProducer()).isEqualTo(DEFAULT_PRODUCER);
        assertThat(testArticle.getAmount()).isEqualTo(DEFAULT_AMOUNT);
        assertThat(testArticle.getCategory()).isEqualTo(DEFAULT_CATEGORY);
        assertThat(testArticle.getPrice()).isEqualTo(DEFAULT_PRICE);
        assertThat(testArticle.getPicture()).isEqualTo(DEFAULT_PICTURE);
        assertThat(testArticle.getPictureContentType()).isEqualTo(DEFAULT_PICTURE_CONTENT_TYPE);

        // Validate the Article in Elasticsearch
        verify(mockArticleSearchRepository, times(1)).save(testArticle);
    }

    @Test
    @Transactional
    public void createArticleWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = articleRepository.findAll().size();

        // Create the Article with an existing ID
        article.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restArticleMockMvc.perform(post("/api/articles")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(article)))
            .andExpect(status().isBadRequest());

        // Validate the Article in the database
        List<Article> articleList = articleRepository.findAll();
        assertThat(articleList).hasSize(databaseSizeBeforeCreate);

        // Validate the Article in Elasticsearch
        verify(mockArticleSearchRepository, times(0)).save(article);
    }


    @Test
    @Transactional
    public void checkDescriptionIsRequired() throws Exception {
        int databaseSizeBeforeTest = articleRepository.findAll().size();
        // set the field null
        article.setDescription(null);

        // Create the Article, which fails.


        restArticleMockMvc.perform(post("/api/articles")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(article)))
            .andExpect(status().isBadRequest());

        List<Article> articleList = articleRepository.findAll();
        assertThat(articleList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkProducerIsRequired() throws Exception {
        int databaseSizeBeforeTest = articleRepository.findAll().size();
        // set the field null
        article.setProducer(null);

        // Create the Article, which fails.


        restArticleMockMvc.perform(post("/api/articles")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(article)))
            .andExpect(status().isBadRequest());

        List<Article> articleList = articleRepository.findAll();
        assertThat(articleList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkAmountIsRequired() throws Exception {
        int databaseSizeBeforeTest = articleRepository.findAll().size();
        // set the field null
        article.setAmount(null);

        // Create the Article, which fails.


        restArticleMockMvc.perform(post("/api/articles")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(article)))
            .andExpect(status().isBadRequest());

        List<Article> articleList = articleRepository.findAll();
        assertThat(articleList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllArticles() throws Exception {
        // Initialize the database
        articleRepository.saveAndFlush(article);

        // Get all the articleList
        restArticleMockMvc.perform(get("/api/articles?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(article.getId().intValue())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].producer").value(hasItem(DEFAULT_PRODUCER)))
            .andExpect(jsonPath("$.[*].amount").value(hasItem(DEFAULT_AMOUNT)))
            .andExpect(jsonPath("$.[*].category").value(hasItem(DEFAULT_CATEGORY.toString())))
            .andExpect(jsonPath("$.[*].price").value(hasItem(DEFAULT_PRICE.intValue())))
            .andExpect(jsonPath("$.[*].pictureContentType").value(hasItem(DEFAULT_PICTURE_CONTENT_TYPE)))
            .andExpect(jsonPath("$.[*].picture").value(hasItem(Base64Utils.encodeToString(DEFAULT_PICTURE))));
    }
    
    @Test
    @Transactional
    public void getArticle() throws Exception {
        // Initialize the database
        articleRepository.saveAndFlush(article);

        // Get the article
        restArticleMockMvc.perform(get("/api/articles/{id}", article.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(article.getId().intValue()))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION))
            .andExpect(jsonPath("$.producer").value(DEFAULT_PRODUCER))
            .andExpect(jsonPath("$.amount").value(DEFAULT_AMOUNT))
            .andExpect(jsonPath("$.category").value(DEFAULT_CATEGORY.toString()))
            .andExpect(jsonPath("$.price").value(DEFAULT_PRICE.intValue()))
            .andExpect(jsonPath("$.pictureContentType").value(DEFAULT_PICTURE_CONTENT_TYPE))
            .andExpect(jsonPath("$.picture").value(Base64Utils.encodeToString(DEFAULT_PICTURE)));
    }


    @Test
    @Transactional
    public void getArticlesByIdFiltering() throws Exception {
        // Initialize the database
        articleRepository.saveAndFlush(article);

        Long id = article.getId();

        defaultArticleShouldBeFound("id.equals=" + id);
        defaultArticleShouldNotBeFound("id.notEquals=" + id);

        defaultArticleShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultArticleShouldNotBeFound("id.greaterThan=" + id);

        defaultArticleShouldBeFound("id.lessThanOrEqual=" + id);
        defaultArticleShouldNotBeFound("id.lessThan=" + id);
    }


    @Test
    @Transactional
    public void getAllArticlesByDescriptionIsEqualToSomething() throws Exception {
        // Initialize the database
        articleRepository.saveAndFlush(article);

        // Get all the articleList where description equals to DEFAULT_DESCRIPTION
        defaultArticleShouldBeFound("description.equals=" + DEFAULT_DESCRIPTION);

        // Get all the articleList where description equals to UPDATED_DESCRIPTION
        defaultArticleShouldNotBeFound("description.equals=" + UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    public void getAllArticlesByDescriptionIsNotEqualToSomething() throws Exception {
        // Initialize the database
        articleRepository.saveAndFlush(article);

        // Get all the articleList where description not equals to DEFAULT_DESCRIPTION
        defaultArticleShouldNotBeFound("description.notEquals=" + DEFAULT_DESCRIPTION);

        // Get all the articleList where description not equals to UPDATED_DESCRIPTION
        defaultArticleShouldBeFound("description.notEquals=" + UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    public void getAllArticlesByDescriptionIsInShouldWork() throws Exception {
        // Initialize the database
        articleRepository.saveAndFlush(article);

        // Get all the articleList where description in DEFAULT_DESCRIPTION or UPDATED_DESCRIPTION
        defaultArticleShouldBeFound("description.in=" + DEFAULT_DESCRIPTION + "," + UPDATED_DESCRIPTION);

        // Get all the articleList where description equals to UPDATED_DESCRIPTION
        defaultArticleShouldNotBeFound("description.in=" + UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    public void getAllArticlesByDescriptionIsNullOrNotNull() throws Exception {
        // Initialize the database
        articleRepository.saveAndFlush(article);

        // Get all the articleList where description is not null
        defaultArticleShouldBeFound("description.specified=true");

        // Get all the articleList where description is null
        defaultArticleShouldNotBeFound("description.specified=false");
    }
                @Test
    @Transactional
    public void getAllArticlesByDescriptionContainsSomething() throws Exception {
        // Initialize the database
        articleRepository.saveAndFlush(article);

        // Get all the articleList where description contains DEFAULT_DESCRIPTION
        defaultArticleShouldBeFound("description.contains=" + DEFAULT_DESCRIPTION);

        // Get all the articleList where description contains UPDATED_DESCRIPTION
        defaultArticleShouldNotBeFound("description.contains=" + UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    public void getAllArticlesByDescriptionNotContainsSomething() throws Exception {
        // Initialize the database
        articleRepository.saveAndFlush(article);

        // Get all the articleList where description does not contain DEFAULT_DESCRIPTION
        defaultArticleShouldNotBeFound("description.doesNotContain=" + DEFAULT_DESCRIPTION);

        // Get all the articleList where description does not contain UPDATED_DESCRIPTION
        defaultArticleShouldBeFound("description.doesNotContain=" + UPDATED_DESCRIPTION);
    }


    @Test
    @Transactional
    public void getAllArticlesByProducerIsEqualToSomething() throws Exception {
        // Initialize the database
        articleRepository.saveAndFlush(article);

        // Get all the articleList where producer equals to DEFAULT_PRODUCER
        defaultArticleShouldBeFound("producer.equals=" + DEFAULT_PRODUCER);

        // Get all the articleList where producer equals to UPDATED_PRODUCER
        defaultArticleShouldNotBeFound("producer.equals=" + UPDATED_PRODUCER);
    }

    @Test
    @Transactional
    public void getAllArticlesByProducerIsNotEqualToSomething() throws Exception {
        // Initialize the database
        articleRepository.saveAndFlush(article);

        // Get all the articleList where producer not equals to DEFAULT_PRODUCER
        defaultArticleShouldNotBeFound("producer.notEquals=" + DEFAULT_PRODUCER);

        // Get all the articleList where producer not equals to UPDATED_PRODUCER
        defaultArticleShouldBeFound("producer.notEquals=" + UPDATED_PRODUCER);
    }

    @Test
    @Transactional
    public void getAllArticlesByProducerIsInShouldWork() throws Exception {
        // Initialize the database
        articleRepository.saveAndFlush(article);

        // Get all the articleList where producer in DEFAULT_PRODUCER or UPDATED_PRODUCER
        defaultArticleShouldBeFound("producer.in=" + DEFAULT_PRODUCER + "," + UPDATED_PRODUCER);

        // Get all the articleList where producer equals to UPDATED_PRODUCER
        defaultArticleShouldNotBeFound("producer.in=" + UPDATED_PRODUCER);
    }

    @Test
    @Transactional
    public void getAllArticlesByProducerIsNullOrNotNull() throws Exception {
        // Initialize the database
        articleRepository.saveAndFlush(article);

        // Get all the articleList where producer is not null
        defaultArticleShouldBeFound("producer.specified=true");

        // Get all the articleList where producer is null
        defaultArticleShouldNotBeFound("producer.specified=false");
    }
                @Test
    @Transactional
    public void getAllArticlesByProducerContainsSomething() throws Exception {
        // Initialize the database
        articleRepository.saveAndFlush(article);

        // Get all the articleList where producer contains DEFAULT_PRODUCER
        defaultArticleShouldBeFound("producer.contains=" + DEFAULT_PRODUCER);

        // Get all the articleList where producer contains UPDATED_PRODUCER
        defaultArticleShouldNotBeFound("producer.contains=" + UPDATED_PRODUCER);
    }

    @Test
    @Transactional
    public void getAllArticlesByProducerNotContainsSomething() throws Exception {
        // Initialize the database
        articleRepository.saveAndFlush(article);

        // Get all the articleList where producer does not contain DEFAULT_PRODUCER
        defaultArticleShouldNotBeFound("producer.doesNotContain=" + DEFAULT_PRODUCER);

        // Get all the articleList where producer does not contain UPDATED_PRODUCER
        defaultArticleShouldBeFound("producer.doesNotContain=" + UPDATED_PRODUCER);
    }


    @Test
    @Transactional
    public void getAllArticlesByAmountIsEqualToSomething() throws Exception {
        // Initialize the database
        articleRepository.saveAndFlush(article);

        // Get all the articleList where amount equals to DEFAULT_AMOUNT
        defaultArticleShouldBeFound("amount.equals=" + DEFAULT_AMOUNT);

        // Get all the articleList where amount equals to UPDATED_AMOUNT
        defaultArticleShouldNotBeFound("amount.equals=" + UPDATED_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllArticlesByAmountIsNotEqualToSomething() throws Exception {
        // Initialize the database
        articleRepository.saveAndFlush(article);

        // Get all the articleList where amount not equals to DEFAULT_AMOUNT
        defaultArticleShouldNotBeFound("amount.notEquals=" + DEFAULT_AMOUNT);

        // Get all the articleList where amount not equals to UPDATED_AMOUNT
        defaultArticleShouldBeFound("amount.notEquals=" + UPDATED_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllArticlesByAmountIsInShouldWork() throws Exception {
        // Initialize the database
        articleRepository.saveAndFlush(article);

        // Get all the articleList where amount in DEFAULT_AMOUNT or UPDATED_AMOUNT
        defaultArticleShouldBeFound("amount.in=" + DEFAULT_AMOUNT + "," + UPDATED_AMOUNT);

        // Get all the articleList where amount equals to UPDATED_AMOUNT
        defaultArticleShouldNotBeFound("amount.in=" + UPDATED_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllArticlesByAmountIsNullOrNotNull() throws Exception {
        // Initialize the database
        articleRepository.saveAndFlush(article);

        // Get all the articleList where amount is not null
        defaultArticleShouldBeFound("amount.specified=true");

        // Get all the articleList where amount is null
        defaultArticleShouldNotBeFound("amount.specified=false");
    }
                @Test
    @Transactional
    public void getAllArticlesByAmountContainsSomething() throws Exception {
        // Initialize the database
        articleRepository.saveAndFlush(article);

        // Get all the articleList where amount contains DEFAULT_AMOUNT
        defaultArticleShouldBeFound("amount.contains=" + DEFAULT_AMOUNT);

        // Get all the articleList where amount contains UPDATED_AMOUNT
        defaultArticleShouldNotBeFound("amount.contains=" + UPDATED_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllArticlesByAmountNotContainsSomething() throws Exception {
        // Initialize the database
        articleRepository.saveAndFlush(article);

        // Get all the articleList where amount does not contain DEFAULT_AMOUNT
        defaultArticleShouldNotBeFound("amount.doesNotContain=" + DEFAULT_AMOUNT);

        // Get all the articleList where amount does not contain UPDATED_AMOUNT
        defaultArticleShouldBeFound("amount.doesNotContain=" + UPDATED_AMOUNT);
    }


    @Test
    @Transactional
    public void getAllArticlesByCategoryIsEqualToSomething() throws Exception {
        // Initialize the database
        articleRepository.saveAndFlush(article);

        // Get all the articleList where category equals to DEFAULT_CATEGORY
        defaultArticleShouldBeFound("category.equals=" + DEFAULT_CATEGORY);

        // Get all the articleList where category equals to UPDATED_CATEGORY
        defaultArticleShouldNotBeFound("category.equals=" + UPDATED_CATEGORY);
    }

    @Test
    @Transactional
    public void getAllArticlesByCategoryIsNotEqualToSomething() throws Exception {
        // Initialize the database
        articleRepository.saveAndFlush(article);

        // Get all the articleList where category not equals to DEFAULT_CATEGORY
        defaultArticleShouldNotBeFound("category.notEquals=" + DEFAULT_CATEGORY);

        // Get all the articleList where category not equals to UPDATED_CATEGORY
        defaultArticleShouldBeFound("category.notEquals=" + UPDATED_CATEGORY);
    }

    @Test
    @Transactional
    public void getAllArticlesByCategoryIsInShouldWork() throws Exception {
        // Initialize the database
        articleRepository.saveAndFlush(article);

        // Get all the articleList where category in DEFAULT_CATEGORY or UPDATED_CATEGORY
        defaultArticleShouldBeFound("category.in=" + DEFAULT_CATEGORY + "," + UPDATED_CATEGORY);

        // Get all the articleList where category equals to UPDATED_CATEGORY
        defaultArticleShouldNotBeFound("category.in=" + UPDATED_CATEGORY);
    }

    @Test
    @Transactional
    public void getAllArticlesByCategoryIsNullOrNotNull() throws Exception {
        // Initialize the database
        articleRepository.saveAndFlush(article);

        // Get all the articleList where category is not null
        defaultArticleShouldBeFound("category.specified=true");

        // Get all the articleList where category is null
        defaultArticleShouldNotBeFound("category.specified=false");
    }

    @Test
    @Transactional
    public void getAllArticlesByPriceIsEqualToSomething() throws Exception {
        // Initialize the database
        articleRepository.saveAndFlush(article);

        // Get all the articleList where price equals to DEFAULT_PRICE
        defaultArticleShouldBeFound("price.equals=" + DEFAULT_PRICE);

        // Get all the articleList where price equals to UPDATED_PRICE
        defaultArticleShouldNotBeFound("price.equals=" + UPDATED_PRICE);
    }

    @Test
    @Transactional
    public void getAllArticlesByPriceIsNotEqualToSomething() throws Exception {
        // Initialize the database
        articleRepository.saveAndFlush(article);

        // Get all the articleList where price not equals to DEFAULT_PRICE
        defaultArticleShouldNotBeFound("price.notEquals=" + DEFAULT_PRICE);

        // Get all the articleList where price not equals to UPDATED_PRICE
        defaultArticleShouldBeFound("price.notEquals=" + UPDATED_PRICE);
    }

    @Test
    @Transactional
    public void getAllArticlesByPriceIsInShouldWork() throws Exception {
        // Initialize the database
        articleRepository.saveAndFlush(article);

        // Get all the articleList where price in DEFAULT_PRICE or UPDATED_PRICE
        defaultArticleShouldBeFound("price.in=" + DEFAULT_PRICE + "," + UPDATED_PRICE);

        // Get all the articleList where price equals to UPDATED_PRICE
        defaultArticleShouldNotBeFound("price.in=" + UPDATED_PRICE);
    }

    @Test
    @Transactional
    public void getAllArticlesByPriceIsNullOrNotNull() throws Exception {
        // Initialize the database
        articleRepository.saveAndFlush(article);

        // Get all the articleList where price is not null
        defaultArticleShouldBeFound("price.specified=true");

        // Get all the articleList where price is null
        defaultArticleShouldNotBeFound("price.specified=false");
    }

    @Test
    @Transactional
    public void getAllArticlesByPriceIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        articleRepository.saveAndFlush(article);

        // Get all the articleList where price is greater than or equal to DEFAULT_PRICE
        defaultArticleShouldBeFound("price.greaterThanOrEqual=" + DEFAULT_PRICE);

        // Get all the articleList where price is greater than or equal to UPDATED_PRICE
        defaultArticleShouldNotBeFound("price.greaterThanOrEqual=" + UPDATED_PRICE);
    }

    @Test
    @Transactional
    public void getAllArticlesByPriceIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        articleRepository.saveAndFlush(article);

        // Get all the articleList where price is less than or equal to DEFAULT_PRICE
        defaultArticleShouldBeFound("price.lessThanOrEqual=" + DEFAULT_PRICE);

        // Get all the articleList where price is less than or equal to SMALLER_PRICE
        defaultArticleShouldNotBeFound("price.lessThanOrEqual=" + SMALLER_PRICE);
    }

    @Test
    @Transactional
    public void getAllArticlesByPriceIsLessThanSomething() throws Exception {
        // Initialize the database
        articleRepository.saveAndFlush(article);

        // Get all the articleList where price is less than DEFAULT_PRICE
        defaultArticleShouldNotBeFound("price.lessThan=" + DEFAULT_PRICE);

        // Get all the articleList where price is less than UPDATED_PRICE
        defaultArticleShouldBeFound("price.lessThan=" + UPDATED_PRICE);
    }

    @Test
    @Transactional
    public void getAllArticlesByPriceIsGreaterThanSomething() throws Exception {
        // Initialize the database
        articleRepository.saveAndFlush(article);

        // Get all the articleList where price is greater than DEFAULT_PRICE
        defaultArticleShouldNotBeFound("price.greaterThan=" + DEFAULT_PRICE);

        // Get all the articleList where price is greater than SMALLER_PRICE
        defaultArticleShouldBeFound("price.greaterThan=" + SMALLER_PRICE);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultArticleShouldBeFound(String filter) throws Exception {
        restArticleMockMvc.perform(get("/api/articles?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(article.getId().intValue())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].producer").value(hasItem(DEFAULT_PRODUCER)))
            .andExpect(jsonPath("$.[*].amount").value(hasItem(DEFAULT_AMOUNT)))
            .andExpect(jsonPath("$.[*].category").value(hasItem(DEFAULT_CATEGORY.toString())))
            .andExpect(jsonPath("$.[*].price").value(hasItem(DEFAULT_PRICE.intValue())))
            .andExpect(jsonPath("$.[*].pictureContentType").value(hasItem(DEFAULT_PICTURE_CONTENT_TYPE)))
            .andExpect(jsonPath("$.[*].picture").value(hasItem(Base64Utils.encodeToString(DEFAULT_PICTURE))));

        // Check, that the count call also returns 1
        restArticleMockMvc.perform(get("/api/articles/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultArticleShouldNotBeFound(String filter) throws Exception {
        restArticleMockMvc.perform(get("/api/articles?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restArticleMockMvc.perform(get("/api/articles/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    public void getNonExistingArticle() throws Exception {
        // Get the article
        restArticleMockMvc.perform(get("/api/articles/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateArticle() throws Exception {
        // Initialize the database
        articleService.save(article);

        int databaseSizeBeforeUpdate = articleRepository.findAll().size();

        // Update the article
        Article updatedArticle = articleRepository.findById(article.getId()).get();
        // Disconnect from session so that the updates on updatedArticle are not directly saved in db
        em.detach(updatedArticle);
        updatedArticle
            .description(UPDATED_DESCRIPTION)
            .producer(UPDATED_PRODUCER)
            .amount(UPDATED_AMOUNT)
            .category(UPDATED_CATEGORY)
            .price(UPDATED_PRICE)
            .picture(UPDATED_PICTURE)
            .pictureContentType(UPDATED_PICTURE_CONTENT_TYPE);

        restArticleMockMvc.perform(put("/api/articles")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedArticle)))
            .andExpect(status().isOk());

        // Validate the Article in the database
        List<Article> articleList = articleRepository.findAll();
        assertThat(articleList).hasSize(databaseSizeBeforeUpdate);
        Article testArticle = articleList.get(articleList.size() - 1);
        assertThat(testArticle.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testArticle.getProducer()).isEqualTo(UPDATED_PRODUCER);
        assertThat(testArticle.getAmount()).isEqualTo(UPDATED_AMOUNT);
        assertThat(testArticle.getCategory()).isEqualTo(UPDATED_CATEGORY);
        assertThat(testArticle.getPrice()).isEqualTo(UPDATED_PRICE);
        assertThat(testArticle.getPicture()).isEqualTo(UPDATED_PICTURE);
        assertThat(testArticle.getPictureContentType()).isEqualTo(UPDATED_PICTURE_CONTENT_TYPE);

        // Validate the Article in Elasticsearch
        verify(mockArticleSearchRepository, times(2)).save(testArticle);
    }

    @Test
    @Transactional
    public void updateNonExistingArticle() throws Exception {
        int databaseSizeBeforeUpdate = articleRepository.findAll().size();

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restArticleMockMvc.perform(put("/api/articles")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(article)))
            .andExpect(status().isBadRequest());

        // Validate the Article in the database
        List<Article> articleList = articleRepository.findAll();
        assertThat(articleList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Article in Elasticsearch
        verify(mockArticleSearchRepository, times(0)).save(article);
    }

    @Test
    @Transactional
    public void deleteArticle() throws Exception {
        // Initialize the database
        articleService.save(article);

        int databaseSizeBeforeDelete = articleRepository.findAll().size();

        // Delete the article
        restArticleMockMvc.perform(delete("/api/articles/{id}", article.getId())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Article> articleList = articleRepository.findAll();
        assertThat(articleList).hasSize(databaseSizeBeforeDelete - 1);

        // Validate the Article in Elasticsearch
        verify(mockArticleSearchRepository, times(1)).deleteById(article.getId());
    }

    @Test
    @Transactional
    public void searchArticle() throws Exception {
        // Configure the mock search repository
        // Initialize the database
        articleService.save(article);
        when(mockArticleSearchRepository.search(queryStringQuery("id:" + article.getId())))
            .thenReturn(Collections.singletonList(article));

        // Search the article
        restArticleMockMvc.perform(get("/api/_search/articles?query=id:" + article.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(article.getId().intValue())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].producer").value(hasItem(DEFAULT_PRODUCER)))
            .andExpect(jsonPath("$.[*].amount").value(hasItem(DEFAULT_AMOUNT)))
            .andExpect(jsonPath("$.[*].category").value(hasItem(DEFAULT_CATEGORY.toString())))
            .andExpect(jsonPath("$.[*].price").value(hasItem(DEFAULT_PRICE.intValue())))
            .andExpect(jsonPath("$.[*].pictureContentType").value(hasItem(DEFAULT_PICTURE_CONTENT_TYPE)))
            .andExpect(jsonPath("$.[*].picture").value(hasItem(Base64Utils.encodeToString(DEFAULT_PICTURE))));
    }
}
