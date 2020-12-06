package de.offersapp.aldiapi.service.impl;

import de.offersapp.aldiapi.domain.Offer;
import de.offersapp.aldiapi.service.OffersParserService;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OffersParserServiceImpl implements OffersParserService {

    private final Logger log = LoggerFactory.getLogger(OffersParserServiceImpl.class);

    private final String BASE_URL = "https://www.aldi-sued.de";
    private final String OFFER_URL = "/de/angebote.html";

    @Override
    public List<Offer> fetchOffers() {

        try {
            fetchOffersPages().forEach(this::extractOffers);
        } catch (IOException e) {
            log.error(e.getMessage());
            return null;
        }

        return null;
    }

    private List<String> fetchOffersPages() throws IOException {

        return Jsoup.connect(BASE_URL + OFFER_URL)
            .get()
            .select("div.wrapper a[href]")
            .stream()
            .map(element -> element.attr("href"))
            .map(url -> StringUtils.substringBefore(url, "#"))
            .distinct()
            .filter(url -> url.startsWith("/"))
            .collect(Collectors.toList());
    }

    private void extractOffers(String relativePath) {

        try {
            Elements trennerElements = Jsoup.connect(BASE_URL + relativePath)
                .get()
                .select("h2.trenner");
            if (!trennerElements.isEmpty()) {
                extractFreshOffers();
            } else {
                extractWeeklyOffers();
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }

    }

    private void extractFreshOffers() {

    }

    private void extractWeeklyOffers() {

    }


}
