package com.example.restate.service.search;

import com.example.restate.dto.MieszkanieSearchCriteria;
import com.example.restate.dto.PageResponse;
import com.example.restate.entity.Mieszkanie;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchContext {

    private final List<SearchStrategy> strategies;

    public PageResponse<Mieszkanie> executeSearch(SearchStrategy.SearchType type,
                                                  MieszkanieSearchCriteria criteria,
                                                  Pageable pageable) {

        SearchStrategy strategy = strategies.stream()
                .filter(s -> s.supports(type))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No strategy found for type: " + type));

        return strategy.search(criteria, pageable);
    }

    // Automatyczny wybór strategii na podstawie kryteriów
    public PageResponse<Mieszkanie> executeAutoSearch(MieszkanieSearchCriteria criteria,
                                                      Pageable pageable) {
        SearchStrategy.SearchType type = determineSearchType(criteria);
        return executeSearch(type, criteria, pageable);
    }

    private SearchStrategy.SearchType determineSearchType(MieszkanieSearchCriteria criteria) {
        boolean hasLocation = criteria.getCity() != null || criteria.getVoivodeship() != null
                || criteria.getDistrict() != null;
        boolean hasSimple = criteria.getDeveloper() != null || criteria.getInvestment() != null;

        boolean hasOther = criteria.getFloor() != null
                || criteria.getStatus() != null || criteria.getMinPrice() != null
                || criteria.getMaxPrice() != null || criteria.getMinArea() != null
                || criteria.getMaxArea() != null;

        if (hasLocation && !hasSimple && !hasOther) {
            return SearchStrategy.SearchType.BY_LOCATION;
        }

        if (hasSimple && !hasLocation && !hasOther) {
            return SearchStrategy.SearchType.SIMPLE;
        }

        return SearchStrategy.SearchType.ADVANCED;
    }
}