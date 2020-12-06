package de.offersapp.aldiapi.web.rest;

import de.offersapp.aldiapi.AldiApiApp;
import de.offersapp.aldiapi.domain.Offer;
import de.offersapp.aldiapi.domain.Article;
import de.offersapp.aldiapi.repository.OfferRepository;
import de.offersapp.aldiapi.repository.search.OfferSearchRepository;
import de.offersapp.aldiapi.service.OfferService;
import de.offersapp.aldiapi.service.dto.OfferCriteria;
import de.offersapp.aldiapi.service.OfferQueryService;

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
import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@link OfferResource} REST controller.
 */
@SpringBootTest(classes = AldiApiApp.class)
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
public class OfferResourceIT {

    private static final BigDecimal DEFAULT_ADVANTAGE = new BigDecimal(1);
    private static final BigDecimal UPDATED_ADVANTAGE = new BigDecimal(2);
    private static final BigDecimal SMALLER_ADVANTAGE = new BigDecimal(1 - 1);

    private static final String DEFAULT_AMOUNT = "AAAAAAAAAA";
    private static final String UPDATED_AMOUNT = "BBBBBBBBBB";

    private static final LocalDate DEFAULT_START_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_START_DATE = LocalDate.now(ZoneId.systemDefault());
    private static final LocalDate SMALLER_START_DATE = LocalDate.ofEpochDay(-1L);

    private static final LocalDate DEFAULT_END_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_END_DATE = LocalDate.now(ZoneId.systemDefault());
    private static final LocalDate SMALLER_END_DATE = LocalDate.ofEpochDay(-1L);

    @Autowired
    private OfferRepository offerRepository;

    @Autowired
    private OfferService offerService;

    /**
     * This repository is mocked in the de.offersapp.aldiapi.repository.search test package.
     *
     * @see de.offersapp.aldiapi.repository.search.OfferSearchRepositoryMockConfiguration
     */
    @Autowired
    private OfferSearchRepository mockOfferSearchRepository;

    @Autowired
    private OfferQueryService offerQueryService;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restOfferMockMvc;

    private Offer offer;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Offer createEntity(EntityManager em) {
        Offer offer = new Offer()
            .advantage(DEFAULT_ADVANTAGE)
            .amount(DEFAULT_AMOUNT)
            .startDate(DEFAULT_START_DATE)
            .endDate(DEFAULT_END_DATE);
        return offer;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Offer createUpdatedEntity(EntityManager em) {
        Offer offer = new Offer()
            .advantage(UPDATED_ADVANTAGE)
            .amount(UPDATED_AMOUNT)
            .startDate(UPDATED_START_DATE)
            .endDate(UPDATED_END_DATE);
        return offer;
    }

    @BeforeEach
    public void initTest() {
        offer = createEntity(em);
    }

    @Test
    @Transactional
    public void createOffer() throws Exception {
        int databaseSizeBeforeCreate = offerRepository.findAll().size();
        // Create the Offer
        restOfferMockMvc.perform(post("/api/offers")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(offer)))
            .andExpect(status().isCreated());

        // Validate the Offer in the database
        List<Offer> offerList = offerRepository.findAll();
        assertThat(offerList).hasSize(databaseSizeBeforeCreate + 1);
        Offer testOffer = offerList.get(offerList.size() - 1);
        assertThat(testOffer.getAdvantage()).isEqualTo(DEFAULT_ADVANTAGE);
        assertThat(testOffer.getAmount()).isEqualTo(DEFAULT_AMOUNT);
        assertThat(testOffer.getStartDate()).isEqualTo(DEFAULT_START_DATE);
        assertThat(testOffer.getEndDate()).isEqualTo(DEFAULT_END_DATE);

        // Validate the Offer in Elasticsearch
        verify(mockOfferSearchRepository, times(1)).save(testOffer);
    }

    @Test
    @Transactional
    public void createOfferWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = offerRepository.findAll().size();

        // Create the Offer with an existing ID
        offer.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restOfferMockMvc.perform(post("/api/offers")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(offer)))
            .andExpect(status().isBadRequest());

        // Validate the Offer in the database
        List<Offer> offerList = offerRepository.findAll();
        assertThat(offerList).hasSize(databaseSizeBeforeCreate);

        // Validate the Offer in Elasticsearch
        verify(mockOfferSearchRepository, times(0)).save(offer);
    }


    @Test
    @Transactional
    public void checkAdvantageIsRequired() throws Exception {
        int databaseSizeBeforeTest = offerRepository.findAll().size();
        // set the field null
        offer.setAdvantage(null);

        // Create the Offer, which fails.


        restOfferMockMvc.perform(post("/api/offers")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(offer)))
            .andExpect(status().isBadRequest());

        List<Offer> offerList = offerRepository.findAll();
        assertThat(offerList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkAmountIsRequired() throws Exception {
        int databaseSizeBeforeTest = offerRepository.findAll().size();
        // set the field null
        offer.setAmount(null);

        // Create the Offer, which fails.


        restOfferMockMvc.perform(post("/api/offers")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(offer)))
            .andExpect(status().isBadRequest());

        List<Offer> offerList = offerRepository.findAll();
        assertThat(offerList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllOffers() throws Exception {
        // Initialize the database
        offerRepository.saveAndFlush(offer);

        // Get all the offerList
        restOfferMockMvc.perform(get("/api/offers?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(offer.getId().intValue())))
            .andExpect(jsonPath("$.[*].advantage").value(hasItem(DEFAULT_ADVANTAGE.intValue())))
            .andExpect(jsonPath("$.[*].amount").value(hasItem(DEFAULT_AMOUNT)))
            .andExpect(jsonPath("$.[*].startDate").value(hasItem(DEFAULT_START_DATE.toString())))
            .andExpect(jsonPath("$.[*].endDate").value(hasItem(DEFAULT_END_DATE.toString())));
    }
    
    @Test
    @Transactional
    public void getOffer() throws Exception {
        // Initialize the database
        offerRepository.saveAndFlush(offer);

        // Get the offer
        restOfferMockMvc.perform(get("/api/offers/{id}", offer.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(offer.getId().intValue()))
            .andExpect(jsonPath("$.advantage").value(DEFAULT_ADVANTAGE.intValue()))
            .andExpect(jsonPath("$.amount").value(DEFAULT_AMOUNT))
            .andExpect(jsonPath("$.startDate").value(DEFAULT_START_DATE.toString()))
            .andExpect(jsonPath("$.endDate").value(DEFAULT_END_DATE.toString()));
    }


    @Test
    @Transactional
    public void getOffersByIdFiltering() throws Exception {
        // Initialize the database
        offerRepository.saveAndFlush(offer);

        Long id = offer.getId();

        defaultOfferShouldBeFound("id.equals=" + id);
        defaultOfferShouldNotBeFound("id.notEquals=" + id);

        defaultOfferShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultOfferShouldNotBeFound("id.greaterThan=" + id);

        defaultOfferShouldBeFound("id.lessThanOrEqual=" + id);
        defaultOfferShouldNotBeFound("id.lessThan=" + id);
    }


    @Test
    @Transactional
    public void getAllOffersByAdvantageIsEqualToSomething() throws Exception {
        // Initialize the database
        offerRepository.saveAndFlush(offer);

        // Get all the offerList where advantage equals to DEFAULT_ADVANTAGE
        defaultOfferShouldBeFound("advantage.equals=" + DEFAULT_ADVANTAGE);

        // Get all the offerList where advantage equals to UPDATED_ADVANTAGE
        defaultOfferShouldNotBeFound("advantage.equals=" + UPDATED_ADVANTAGE);
    }

    @Test
    @Transactional
    public void getAllOffersByAdvantageIsNotEqualToSomething() throws Exception {
        // Initialize the database
        offerRepository.saveAndFlush(offer);

        // Get all the offerList where advantage not equals to DEFAULT_ADVANTAGE
        defaultOfferShouldNotBeFound("advantage.notEquals=" + DEFAULT_ADVANTAGE);

        // Get all the offerList where advantage not equals to UPDATED_ADVANTAGE
        defaultOfferShouldBeFound("advantage.notEquals=" + UPDATED_ADVANTAGE);
    }

    @Test
    @Transactional
    public void getAllOffersByAdvantageIsInShouldWork() throws Exception {
        // Initialize the database
        offerRepository.saveAndFlush(offer);

        // Get all the offerList where advantage in DEFAULT_ADVANTAGE or UPDATED_ADVANTAGE
        defaultOfferShouldBeFound("advantage.in=" + DEFAULT_ADVANTAGE + "," + UPDATED_ADVANTAGE);

        // Get all the offerList where advantage equals to UPDATED_ADVANTAGE
        defaultOfferShouldNotBeFound("advantage.in=" + UPDATED_ADVANTAGE);
    }

    @Test
    @Transactional
    public void getAllOffersByAdvantageIsNullOrNotNull() throws Exception {
        // Initialize the database
        offerRepository.saveAndFlush(offer);

        // Get all the offerList where advantage is not null
        defaultOfferShouldBeFound("advantage.specified=true");

        // Get all the offerList where advantage is null
        defaultOfferShouldNotBeFound("advantage.specified=false");
    }

    @Test
    @Transactional
    public void getAllOffersByAdvantageIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        offerRepository.saveAndFlush(offer);

        // Get all the offerList where advantage is greater than or equal to DEFAULT_ADVANTAGE
        defaultOfferShouldBeFound("advantage.greaterThanOrEqual=" + DEFAULT_ADVANTAGE);

        // Get all the offerList where advantage is greater than or equal to UPDATED_ADVANTAGE
        defaultOfferShouldNotBeFound("advantage.greaterThanOrEqual=" + UPDATED_ADVANTAGE);
    }

    @Test
    @Transactional
    public void getAllOffersByAdvantageIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        offerRepository.saveAndFlush(offer);

        // Get all the offerList where advantage is less than or equal to DEFAULT_ADVANTAGE
        defaultOfferShouldBeFound("advantage.lessThanOrEqual=" + DEFAULT_ADVANTAGE);

        // Get all the offerList where advantage is less than or equal to SMALLER_ADVANTAGE
        defaultOfferShouldNotBeFound("advantage.lessThanOrEqual=" + SMALLER_ADVANTAGE);
    }

    @Test
    @Transactional
    public void getAllOffersByAdvantageIsLessThanSomething() throws Exception {
        // Initialize the database
        offerRepository.saveAndFlush(offer);

        // Get all the offerList where advantage is less than DEFAULT_ADVANTAGE
        defaultOfferShouldNotBeFound("advantage.lessThan=" + DEFAULT_ADVANTAGE);

        // Get all the offerList where advantage is less than UPDATED_ADVANTAGE
        defaultOfferShouldBeFound("advantage.lessThan=" + UPDATED_ADVANTAGE);
    }

    @Test
    @Transactional
    public void getAllOffersByAdvantageIsGreaterThanSomething() throws Exception {
        // Initialize the database
        offerRepository.saveAndFlush(offer);

        // Get all the offerList where advantage is greater than DEFAULT_ADVANTAGE
        defaultOfferShouldNotBeFound("advantage.greaterThan=" + DEFAULT_ADVANTAGE);

        // Get all the offerList where advantage is greater than SMALLER_ADVANTAGE
        defaultOfferShouldBeFound("advantage.greaterThan=" + SMALLER_ADVANTAGE);
    }


    @Test
    @Transactional
    public void getAllOffersByAmountIsEqualToSomething() throws Exception {
        // Initialize the database
        offerRepository.saveAndFlush(offer);

        // Get all the offerList where amount equals to DEFAULT_AMOUNT
        defaultOfferShouldBeFound("amount.equals=" + DEFAULT_AMOUNT);

        // Get all the offerList where amount equals to UPDATED_AMOUNT
        defaultOfferShouldNotBeFound("amount.equals=" + UPDATED_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllOffersByAmountIsNotEqualToSomething() throws Exception {
        // Initialize the database
        offerRepository.saveAndFlush(offer);

        // Get all the offerList where amount not equals to DEFAULT_AMOUNT
        defaultOfferShouldNotBeFound("amount.notEquals=" + DEFAULT_AMOUNT);

        // Get all the offerList where amount not equals to UPDATED_AMOUNT
        defaultOfferShouldBeFound("amount.notEquals=" + UPDATED_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllOffersByAmountIsInShouldWork() throws Exception {
        // Initialize the database
        offerRepository.saveAndFlush(offer);

        // Get all the offerList where amount in DEFAULT_AMOUNT or UPDATED_AMOUNT
        defaultOfferShouldBeFound("amount.in=" + DEFAULT_AMOUNT + "," + UPDATED_AMOUNT);

        // Get all the offerList where amount equals to UPDATED_AMOUNT
        defaultOfferShouldNotBeFound("amount.in=" + UPDATED_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllOffersByAmountIsNullOrNotNull() throws Exception {
        // Initialize the database
        offerRepository.saveAndFlush(offer);

        // Get all the offerList where amount is not null
        defaultOfferShouldBeFound("amount.specified=true");

        // Get all the offerList where amount is null
        defaultOfferShouldNotBeFound("amount.specified=false");
    }
                @Test
    @Transactional
    public void getAllOffersByAmountContainsSomething() throws Exception {
        // Initialize the database
        offerRepository.saveAndFlush(offer);

        // Get all the offerList where amount contains DEFAULT_AMOUNT
        defaultOfferShouldBeFound("amount.contains=" + DEFAULT_AMOUNT);

        // Get all the offerList where amount contains UPDATED_AMOUNT
        defaultOfferShouldNotBeFound("amount.contains=" + UPDATED_AMOUNT);
    }

    @Test
    @Transactional
    public void getAllOffersByAmountNotContainsSomething() throws Exception {
        // Initialize the database
        offerRepository.saveAndFlush(offer);

        // Get all the offerList where amount does not contain DEFAULT_AMOUNT
        defaultOfferShouldNotBeFound("amount.doesNotContain=" + DEFAULT_AMOUNT);

        // Get all the offerList where amount does not contain UPDATED_AMOUNT
        defaultOfferShouldBeFound("amount.doesNotContain=" + UPDATED_AMOUNT);
    }


    @Test
    @Transactional
    public void getAllOffersByStartDateIsEqualToSomething() throws Exception {
        // Initialize the database
        offerRepository.saveAndFlush(offer);

        // Get all the offerList where startDate equals to DEFAULT_START_DATE
        defaultOfferShouldBeFound("startDate.equals=" + DEFAULT_START_DATE);

        // Get all the offerList where startDate equals to UPDATED_START_DATE
        defaultOfferShouldNotBeFound("startDate.equals=" + UPDATED_START_DATE);
    }

    @Test
    @Transactional
    public void getAllOffersByStartDateIsNotEqualToSomething() throws Exception {
        // Initialize the database
        offerRepository.saveAndFlush(offer);

        // Get all the offerList where startDate not equals to DEFAULT_START_DATE
        defaultOfferShouldNotBeFound("startDate.notEquals=" + DEFAULT_START_DATE);

        // Get all the offerList where startDate not equals to UPDATED_START_DATE
        defaultOfferShouldBeFound("startDate.notEquals=" + UPDATED_START_DATE);
    }

    @Test
    @Transactional
    public void getAllOffersByStartDateIsInShouldWork() throws Exception {
        // Initialize the database
        offerRepository.saveAndFlush(offer);

        // Get all the offerList where startDate in DEFAULT_START_DATE or UPDATED_START_DATE
        defaultOfferShouldBeFound("startDate.in=" + DEFAULT_START_DATE + "," + UPDATED_START_DATE);

        // Get all the offerList where startDate equals to UPDATED_START_DATE
        defaultOfferShouldNotBeFound("startDate.in=" + UPDATED_START_DATE);
    }

    @Test
    @Transactional
    public void getAllOffersByStartDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        offerRepository.saveAndFlush(offer);

        // Get all the offerList where startDate is not null
        defaultOfferShouldBeFound("startDate.specified=true");

        // Get all the offerList where startDate is null
        defaultOfferShouldNotBeFound("startDate.specified=false");
    }

    @Test
    @Transactional
    public void getAllOffersByStartDateIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        offerRepository.saveAndFlush(offer);

        // Get all the offerList where startDate is greater than or equal to DEFAULT_START_DATE
        defaultOfferShouldBeFound("startDate.greaterThanOrEqual=" + DEFAULT_START_DATE);

        // Get all the offerList where startDate is greater than or equal to UPDATED_START_DATE
        defaultOfferShouldNotBeFound("startDate.greaterThanOrEqual=" + UPDATED_START_DATE);
    }

    @Test
    @Transactional
    public void getAllOffersByStartDateIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        offerRepository.saveAndFlush(offer);

        // Get all the offerList where startDate is less than or equal to DEFAULT_START_DATE
        defaultOfferShouldBeFound("startDate.lessThanOrEqual=" + DEFAULT_START_DATE);

        // Get all the offerList where startDate is less than or equal to SMALLER_START_DATE
        defaultOfferShouldNotBeFound("startDate.lessThanOrEqual=" + SMALLER_START_DATE);
    }

    @Test
    @Transactional
    public void getAllOffersByStartDateIsLessThanSomething() throws Exception {
        // Initialize the database
        offerRepository.saveAndFlush(offer);

        // Get all the offerList where startDate is less than DEFAULT_START_DATE
        defaultOfferShouldNotBeFound("startDate.lessThan=" + DEFAULT_START_DATE);

        // Get all the offerList where startDate is less than UPDATED_START_DATE
        defaultOfferShouldBeFound("startDate.lessThan=" + UPDATED_START_DATE);
    }

    @Test
    @Transactional
    public void getAllOffersByStartDateIsGreaterThanSomething() throws Exception {
        // Initialize the database
        offerRepository.saveAndFlush(offer);

        // Get all the offerList where startDate is greater than DEFAULT_START_DATE
        defaultOfferShouldNotBeFound("startDate.greaterThan=" + DEFAULT_START_DATE);

        // Get all the offerList where startDate is greater than SMALLER_START_DATE
        defaultOfferShouldBeFound("startDate.greaterThan=" + SMALLER_START_DATE);
    }


    @Test
    @Transactional
    public void getAllOffersByEndDateIsEqualToSomething() throws Exception {
        // Initialize the database
        offerRepository.saveAndFlush(offer);

        // Get all the offerList where endDate equals to DEFAULT_END_DATE
        defaultOfferShouldBeFound("endDate.equals=" + DEFAULT_END_DATE);

        // Get all the offerList where endDate equals to UPDATED_END_DATE
        defaultOfferShouldNotBeFound("endDate.equals=" + UPDATED_END_DATE);
    }

    @Test
    @Transactional
    public void getAllOffersByEndDateIsNotEqualToSomething() throws Exception {
        // Initialize the database
        offerRepository.saveAndFlush(offer);

        // Get all the offerList where endDate not equals to DEFAULT_END_DATE
        defaultOfferShouldNotBeFound("endDate.notEquals=" + DEFAULT_END_DATE);

        // Get all the offerList where endDate not equals to UPDATED_END_DATE
        defaultOfferShouldBeFound("endDate.notEquals=" + UPDATED_END_DATE);
    }

    @Test
    @Transactional
    public void getAllOffersByEndDateIsInShouldWork() throws Exception {
        // Initialize the database
        offerRepository.saveAndFlush(offer);

        // Get all the offerList where endDate in DEFAULT_END_DATE or UPDATED_END_DATE
        defaultOfferShouldBeFound("endDate.in=" + DEFAULT_END_DATE + "," + UPDATED_END_DATE);

        // Get all the offerList where endDate equals to UPDATED_END_DATE
        defaultOfferShouldNotBeFound("endDate.in=" + UPDATED_END_DATE);
    }

    @Test
    @Transactional
    public void getAllOffersByEndDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        offerRepository.saveAndFlush(offer);

        // Get all the offerList where endDate is not null
        defaultOfferShouldBeFound("endDate.specified=true");

        // Get all the offerList where endDate is null
        defaultOfferShouldNotBeFound("endDate.specified=false");
    }

    @Test
    @Transactional
    public void getAllOffersByEndDateIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        offerRepository.saveAndFlush(offer);

        // Get all the offerList where endDate is greater than or equal to DEFAULT_END_DATE
        defaultOfferShouldBeFound("endDate.greaterThanOrEqual=" + DEFAULT_END_DATE);

        // Get all the offerList where endDate is greater than or equal to UPDATED_END_DATE
        defaultOfferShouldNotBeFound("endDate.greaterThanOrEqual=" + UPDATED_END_DATE);
    }

    @Test
    @Transactional
    public void getAllOffersByEndDateIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        offerRepository.saveAndFlush(offer);

        // Get all the offerList where endDate is less than or equal to DEFAULT_END_DATE
        defaultOfferShouldBeFound("endDate.lessThanOrEqual=" + DEFAULT_END_DATE);

        // Get all the offerList where endDate is less than or equal to SMALLER_END_DATE
        defaultOfferShouldNotBeFound("endDate.lessThanOrEqual=" + SMALLER_END_DATE);
    }

    @Test
    @Transactional
    public void getAllOffersByEndDateIsLessThanSomething() throws Exception {
        // Initialize the database
        offerRepository.saveAndFlush(offer);

        // Get all the offerList where endDate is less than DEFAULT_END_DATE
        defaultOfferShouldNotBeFound("endDate.lessThan=" + DEFAULT_END_DATE);

        // Get all the offerList where endDate is less than UPDATED_END_DATE
        defaultOfferShouldBeFound("endDate.lessThan=" + UPDATED_END_DATE);
    }

    @Test
    @Transactional
    public void getAllOffersByEndDateIsGreaterThanSomething() throws Exception {
        // Initialize the database
        offerRepository.saveAndFlush(offer);

        // Get all the offerList where endDate is greater than DEFAULT_END_DATE
        defaultOfferShouldNotBeFound("endDate.greaterThan=" + DEFAULT_END_DATE);

        // Get all the offerList where endDate is greater than SMALLER_END_DATE
        defaultOfferShouldBeFound("endDate.greaterThan=" + SMALLER_END_DATE);
    }


    @Test
    @Transactional
    public void getAllOffersByArticleIsEqualToSomething() throws Exception {
        // Initialize the database
        offerRepository.saveAndFlush(offer);
        Article article = ArticleResourceIT.createEntity(em);
        em.persist(article);
        em.flush();
        offer.setArticle(article);
        offerRepository.saveAndFlush(offer);
        Long articleId = article.getId();

        // Get all the offerList where article equals to articleId
        defaultOfferShouldBeFound("articleId.equals=" + articleId);

        // Get all the offerList where article equals to articleId + 1
        defaultOfferShouldNotBeFound("articleId.equals=" + (articleId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultOfferShouldBeFound(String filter) throws Exception {
        restOfferMockMvc.perform(get("/api/offers?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(offer.getId().intValue())))
            .andExpect(jsonPath("$.[*].advantage").value(hasItem(DEFAULT_ADVANTAGE.intValue())))
            .andExpect(jsonPath("$.[*].amount").value(hasItem(DEFAULT_AMOUNT)))
            .andExpect(jsonPath("$.[*].startDate").value(hasItem(DEFAULT_START_DATE.toString())))
            .andExpect(jsonPath("$.[*].endDate").value(hasItem(DEFAULT_END_DATE.toString())));

        // Check, that the count call also returns 1
        restOfferMockMvc.perform(get("/api/offers/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultOfferShouldNotBeFound(String filter) throws Exception {
        restOfferMockMvc.perform(get("/api/offers?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restOfferMockMvc.perform(get("/api/offers/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    public void getNonExistingOffer() throws Exception {
        // Get the offer
        restOfferMockMvc.perform(get("/api/offers/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateOffer() throws Exception {
        // Initialize the database
        offerService.save(offer);

        int databaseSizeBeforeUpdate = offerRepository.findAll().size();

        // Update the offer
        Offer updatedOffer = offerRepository.findById(offer.getId()).get();
        // Disconnect from session so that the updates on updatedOffer are not directly saved in db
        em.detach(updatedOffer);
        updatedOffer
            .advantage(UPDATED_ADVANTAGE)
            .amount(UPDATED_AMOUNT)
            .startDate(UPDATED_START_DATE)
            .endDate(UPDATED_END_DATE);

        restOfferMockMvc.perform(put("/api/offers")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedOffer)))
            .andExpect(status().isOk());

        // Validate the Offer in the database
        List<Offer> offerList = offerRepository.findAll();
        assertThat(offerList).hasSize(databaseSizeBeforeUpdate);
        Offer testOffer = offerList.get(offerList.size() - 1);
        assertThat(testOffer.getAdvantage()).isEqualTo(UPDATED_ADVANTAGE);
        assertThat(testOffer.getAmount()).isEqualTo(UPDATED_AMOUNT);
        assertThat(testOffer.getStartDate()).isEqualTo(UPDATED_START_DATE);
        assertThat(testOffer.getEndDate()).isEqualTo(UPDATED_END_DATE);

        // Validate the Offer in Elasticsearch
        verify(mockOfferSearchRepository, times(2)).save(testOffer);
    }

    @Test
    @Transactional
    public void updateNonExistingOffer() throws Exception {
        int databaseSizeBeforeUpdate = offerRepository.findAll().size();

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restOfferMockMvc.perform(put("/api/offers")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(offer)))
            .andExpect(status().isBadRequest());

        // Validate the Offer in the database
        List<Offer> offerList = offerRepository.findAll();
        assertThat(offerList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Offer in Elasticsearch
        verify(mockOfferSearchRepository, times(0)).save(offer);
    }

    @Test
    @Transactional
    public void deleteOffer() throws Exception {
        // Initialize the database
        offerService.save(offer);

        int databaseSizeBeforeDelete = offerRepository.findAll().size();

        // Delete the offer
        restOfferMockMvc.perform(delete("/api/offers/{id}", offer.getId())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Offer> offerList = offerRepository.findAll();
        assertThat(offerList).hasSize(databaseSizeBeforeDelete - 1);

        // Validate the Offer in Elasticsearch
        verify(mockOfferSearchRepository, times(1)).deleteById(offer.getId());
    }

    @Test
    @Transactional
    public void searchOffer() throws Exception {
        // Configure the mock search repository
        // Initialize the database
        offerService.save(offer);
        when(mockOfferSearchRepository.search(queryStringQuery("id:" + offer.getId())))
            .thenReturn(Collections.singletonList(offer));

        // Search the offer
        restOfferMockMvc.perform(get("/api/_search/offers?query=id:" + offer.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(offer.getId().intValue())))
            .andExpect(jsonPath("$.[*].advantage").value(hasItem(DEFAULT_ADVANTAGE.intValue())))
            .andExpect(jsonPath("$.[*].amount").value(hasItem(DEFAULT_AMOUNT)))
            .andExpect(jsonPath("$.[*].startDate").value(hasItem(DEFAULT_START_DATE.toString())))
            .andExpect(jsonPath("$.[*].endDate").value(hasItem(DEFAULT_END_DATE.toString())));
    }
}
