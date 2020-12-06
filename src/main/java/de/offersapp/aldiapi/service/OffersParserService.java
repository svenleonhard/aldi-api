package de.offersapp.aldiapi.service;

import de.offersapp.aldiapi.domain.Offer;

import java.util.List;

public interface OffersParserService {

    List<Offer> fetchOffers();

}
