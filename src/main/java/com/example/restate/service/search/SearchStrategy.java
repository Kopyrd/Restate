package com.example.restate.service.search;

import com.example.restate.dto.MieszkanieSearchCriteria;
import com.example.restate.dto.PageResponse;
import com.example.restate.entity.Mieszkanie;
import org.springframework.data.domain.Pageable;


 //Interfejs strategii wyszukiwania

public interface SearchStrategy {
    PageResponse<Mieszkanie> search(MieszkanieSearchCriteria criteria, Pageable pageable);
    boolean supports(SearchType searchType);

    enum SearchType {
        SIMPLE,
        ADVANCED,
        BY_LOCATION
    }
}