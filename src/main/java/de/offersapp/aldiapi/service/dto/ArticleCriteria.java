package de.offersapp.aldiapi.service.dto;

import java.io.Serializable;
import java.util.Objects;
import io.github.jhipster.service.Criteria;
import de.offersapp.aldiapi.domain.enumeration.ArticleCategory;
import io.github.jhipster.service.filter.BooleanFilter;
import io.github.jhipster.service.filter.DoubleFilter;
import io.github.jhipster.service.filter.Filter;
import io.github.jhipster.service.filter.FloatFilter;
import io.github.jhipster.service.filter.IntegerFilter;
import io.github.jhipster.service.filter.LongFilter;
import io.github.jhipster.service.filter.StringFilter;
import io.github.jhipster.service.filter.BigDecimalFilter;

/**
 * Criteria class for the {@link de.offersapp.aldiapi.domain.Article} entity. This class is used
 * in {@link de.offersapp.aldiapi.web.rest.ArticleResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /articles?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class ArticleCriteria implements Serializable, Criteria {
    /**
     * Class for filtering ArticleCategory
     */
    public static class ArticleCategoryFilter extends Filter<ArticleCategory> {

        public ArticleCategoryFilter() {
        }

        public ArticleCategoryFilter(ArticleCategoryFilter filter) {
            super(filter);
        }

        @Override
        public ArticleCategoryFilter copy() {
            return new ArticleCategoryFilter(this);
        }

    }

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter description;

    private StringFilter producer;

    private StringFilter amount;

    private ArticleCategoryFilter category;

    private BigDecimalFilter price;

    public ArticleCriteria() {
    }

    public ArticleCriteria(ArticleCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.description = other.description == null ? null : other.description.copy();
        this.producer = other.producer == null ? null : other.producer.copy();
        this.amount = other.amount == null ? null : other.amount.copy();
        this.category = other.category == null ? null : other.category.copy();
        this.price = other.price == null ? null : other.price.copy();
    }

    @Override
    public ArticleCriteria copy() {
        return new ArticleCriteria(this);
    }

    public LongFilter getId() {
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public StringFilter getDescription() {
        return description;
    }

    public void setDescription(StringFilter description) {
        this.description = description;
    }

    public StringFilter getProducer() {
        return producer;
    }

    public void setProducer(StringFilter producer) {
        this.producer = producer;
    }

    public StringFilter getAmount() {
        return amount;
    }

    public void setAmount(StringFilter amount) {
        this.amount = amount;
    }

    public ArticleCategoryFilter getCategory() {
        return category;
    }

    public void setCategory(ArticleCategoryFilter category) {
        this.category = category;
    }

    public BigDecimalFilter getPrice() {
        return price;
    }

    public void setPrice(BigDecimalFilter price) {
        this.price = price;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ArticleCriteria that = (ArticleCriteria) o;
        return
            Objects.equals(id, that.id) &&
            Objects.equals(description, that.description) &&
            Objects.equals(producer, that.producer) &&
            Objects.equals(amount, that.amount) &&
            Objects.equals(category, that.category) &&
            Objects.equals(price, that.price);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
        id,
        description,
        producer,
        amount,
        category,
        price
        );
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ArticleCriteria{" +
                (id != null ? "id=" + id + ", " : "") +
                (description != null ? "description=" + description + ", " : "") +
                (producer != null ? "producer=" + producer + ", " : "") +
                (amount != null ? "amount=" + amount + ", " : "") +
                (category != null ? "category=" + category + ", " : "") +
                (price != null ? "price=" + price + ", " : "") +
            "}";
    }

}
