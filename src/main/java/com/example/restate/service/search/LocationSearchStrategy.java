package com.example.restate.service.search;

import com.example.restate.dto.MieszkanieSearchCriteria;
import com.example.restate.dto.PageResponse;
import com.example.restate.entity.Mieszkanie;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class LocationSearchStrategy implements SearchStrategy {

    private final EntityManager entityManager;

    @Override
    public PageResponse<Mieszkanie> search(MieszkanieSearchCriteria criteria, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Mieszkanie> query = cb.createQuery(Mieszkanie.class);
        Root<Mieszkanie> mieszkanie = query.from(Mieszkanie.class);

        List<Predicate> predicates = buildPredicates(cb, mieszkanie, criteria);
        query.where(predicates.toArray(new Predicate[0]));

        TypedQuery<Mieszkanie> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<Mieszkanie> content = typedQuery.getResultList();
        long totalElements = getTotalCount(cb, criteria);

        Page<Mieszkanie> page = new PageImpl<>(content, pageable, totalElements);
        return convertToPageResponse(page);
    }

    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<Mieszkanie> root, MieszkanieSearchCriteria criteria) {
        List<Predicate> predicates = new ArrayList<>();

        if (criteria.getVoivodeship() != null) {
            predicates.add(cb.equal(root.get("voivodeship"), criteria.getVoivodeship()));
        }

        if (criteria.getCity() != null) {
            predicates.add(cb.equal(root.get("city"), criteria.getCity()));
        }

        if (criteria.getDistrict() != null) {
            predicates.add(cb.equal(root.get("district"), criteria.getDistrict()));
        }

        return predicates;
    }

    private long getTotalCount(CriteriaBuilder cb, MieszkanieSearchCriteria criteria) {
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Mieszkanie> countRoot = countQuery.from(Mieszkanie.class);
        List<Predicate> countPredicates = buildPredicates(cb, countRoot, criteria);
        
        countQuery.select(cb.count(countRoot));
        countQuery.where(countPredicates.toArray(new Predicate[0]));
        return entityManager.createQuery(countQuery).getSingleResult();
    }

    @Override
    public boolean supports(SearchType searchType) {
        return SearchType.BY_LOCATION.equals(searchType);
    }

    private PageResponse<Mieszkanie> convertToPageResponse(Page<Mieszkanie> page) {
        return PageResponse.<Mieszkanie>builder()
                .content(page.getContent())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .first(page.isFirst())
                .build();
    }
}