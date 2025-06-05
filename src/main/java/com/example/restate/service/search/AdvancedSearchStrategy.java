package com.example.restate.service.search;

import com.example.restate.dto.MieszkanieSearchCriteria;
import com.example.restate.dto.PageResponse;
import com.example.restate.entity.Mieszkanie;
import com.example.restate.repository.MieszkanieRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Component
@RequiredArgsConstructor
public class AdvancedSearchStrategy implements SearchStrategy {

    private final EntityManager entityManager;

    @Override
    public PageResponse<Mieszkanie> search(MieszkanieSearchCriteria criteria, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Mieszkanie> query = cb.createQuery(Mieszkanie.class);
        Root<Mieszkanie> mieszkanie = query.from(Mieszkanie.class);

        List<Predicate> predicates = buildPredicates(cb, mieszkanie, criteria);
        query.where(predicates.toArray(new Predicate[0]));

        // Count query
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Mieszkanie> countRoot = countQuery.from(Mieszkanie.class);
        countQuery.select(cb.count(countRoot));
        countQuery.where(predicates.toArray(new Predicate[0]));
        Long totalElements = entityManager.createQuery(countQuery).getSingleResult();

        // Apply pagination
        TypedQuery<Mieszkanie> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<Mieszkanie> content = typedQuery.getResultList();
        Page<Mieszkanie> page = new PageImpl<>(content, pageable, totalElements);

        return convertToPageResponse(page);
    }

    @Override
    public boolean supports(SearchType searchType) {
        return SearchType.ADVANCED.equals(searchType);
    }

    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<Mieszkanie> root,
                                            MieszkanieSearchCriteria criteria) {
        List<Predicate> predicates = new ArrayList<>();

        if (criteria.getDeveloper() != null) {
            predicates.add(cb.equal(root.get("developer"), criteria.getDeveloper()));
        }

        if (criteria.getMinPrice() != null && criteria.getMaxPrice() != null) {
            predicates.add(cb.between(root.get("price"),
                    criteria.getMinPrice(), criteria.getMaxPrice()));
        }

        if (criteria.getMinArea() != null && criteria.getMaxArea() != null) {
            predicates.add(cb.between(root.get("area"),
                    criteria.getMinArea(), criteria.getMaxArea()));
        }

        if (criteria.getCity() != null) {
            predicates.add(cb.equal(root.get("city"), criteria.getCity()));
        }

        return predicates;
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
