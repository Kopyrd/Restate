package com.example.restate.service.search;

import com.example.restate.dto.MieszkanieSearchCriteria;
import com.example.restate.dto.PageResponse;
import com.example.restate.entity.Mieszkanie;
import com.example.restate.repository.MieszkanieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/**
 * Prosta strategia wyszukiwania - tylko po developerze lub inwestycji
 */
@Component
@RequiredArgsConstructor
public class SimpleSearchStrategy implements SearchStrategy {

    private final MieszkanieRepository repository;

    @Override
    public PageResponse<Mieszkanie> search(MieszkanieSearchCriteria criteria, Pageable pageable) {
        Page<Mieszkanie> page;

        if (criteria.getDeveloper() != null) {
            page = repository.findByDeveloper(criteria.getDeveloper(), pageable);
        } else if (criteria.getInvestment() != null) {
            page = repository.findByInvestment(criteria.getInvestment(), pageable);
        } else {
            page = repository.findAll(pageable);
        }

        return convertToPageResponse(page);
    }

    @Override
    public boolean supports(SearchType searchType) {
        return SearchType.SIMPLE.equals(searchType);
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