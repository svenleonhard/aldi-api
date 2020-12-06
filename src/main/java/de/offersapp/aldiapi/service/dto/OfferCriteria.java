package de.offersapp.aldiapi.service.dto;

import java.io.Serializable;
import java.util.Objects;
import io.github.jhipster.service.Criteria;
import io.github.jhipster.service.filter.BooleanFilter;
import io.github.jhipster.service.filter.DoubleFilter;
import io.github.jhipster.service.filter.Filter;
import io.github.jhipster.service.filter.FloatFilter;
import io.github.jhipster.service.filter.IntegerFilter;
import io.github.jhipster.service.filter.LongFilter;
import io.github.jhipster.service.filter.StringFilter;
import io.github.jhipster.service.filter.BigDecimalFilter;
import io.github.jhipster.service.filter.LocalDateFilter;

/**
 * Criteria class for the {@link de.offersapp.aldiapi.domain.Offer} entity. This class is used
 * in {@link de.offersapp.aldiapi.web.rest.OfferResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /offers?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class OfferCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private BigDecimalFilter advantage;

    private StringFilter amount;

    private LocalDateFilter startDate;

    private LocalDateFilter endDate;

    private LongFilter articleId;

    public OfferCriteria() {
    }

    public OfferCriteria(OfferCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.advantage = other.advantage == null ? null : other.advantage.copy();
        this.amount = other.amount == null ? null : other.amount.copy();
        this.startDate = other.startDate == null ? null : other.startDate.copy();
        this.endDate = other.endDate == null ? null : other.endDate.copy();
        this.articleId = other.articleId == null ? null : other.articleId.copy();
    }

    @Override
    public OfferCriteria copy() {
        return new OfferCriteria(this);
    }

    public LongFilter getId() {
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public BigDecimalFilter getAdvantage() {
        return advantage;
    }

    public void setAdvantage(BigDecimalFilter advantage) {
        this.advantage = advantage;
    }

    public StringFilter getAmount() {
        return amount;
    }

    public void setAmount(StringFilter amount) {
        this.amount = amount;
    }

    public LocalDateFilter getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateFilter startDate) {
        this.startDate = startDate;
    }

    public LocalDateFilter getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateFilter endDate) {
        this.endDate = endDate;
    }

    public LongFilter getArticleId() {
        return articleId;
    }

    public void setArticleId(LongFilter articleId) {
        this.articleId = articleId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final OfferCriteria that = (OfferCriteria) o;
        return
            Objects.equals(id, that.id) &&
            Objects.equals(advantage, that.advantage) &&
            Objects.equals(amount, that.amount) &&
            Objects.equals(startDate, that.startDate) &&
            Objects.equals(endDate, that.endDate) &&
            Objects.equals(articleId, that.articleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
        id,
        advantage,
        amount,
        startDate,
        endDate,
        articleId
        );
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "OfferCriteria{" +
                (id != null ? "id=" + id + ", " : "") +
                (advantage != null ? "advantage=" + advantage + ", " : "") +
                (amount != null ? "amount=" + amount + ", " : "") +
                (startDate != null ? "startDate=" + startDate + ", " : "") +
                (endDate != null ? "endDate=" + endDate + ", " : "") +
                (articleId != null ? "articleId=" + articleId + ", " : "") +
            "}";
    }

}
