package com.example.restate.service.search;

import com.example.restate.dto.MieszkanieSearchCriteria;
import com.example.restate.dto.PageResponse;
import com.example.restate.entity.Mieszkanie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class SearchContextTest {

    @Mock
    private SimpleSearchStrategy simpleSearchStrategy;

    @Mock
    private AdvancedSearchStrategy advancedSearchStrategy;

    @Mock
    private LocationSearchStrategy locationSearchStrategy;

    private SearchContext searchContext;

    private MieszkanieSearchCriteria criteria;
    private Pageable pageable;
    private PageResponse<Mieszkanie> expectedResponse;
    private Mieszkanie mieszkanie;

    @BeforeEach
    void setUp() {
        // Setup test data
        mieszkanie = new Mieszkanie();
        mieszkanie.setId(1);
        mieszkanie.setDeveloper("Test Developer");
        mieszkanie.setInvestment("Test Investment");
        mieszkanie.setArea(BigDecimal.valueOf(75.5));
        mieszkanie.setPrice(BigDecimal.valueOf(500000));
        mieszkanie.setVoivodeship("Test Voivodeship");
        mieszkanie.setCity("Test City");
        mieszkanie.setDistrict("Test District");
        mieszkanie.setStatus(Mieszkanie.Status.AVAILABLE);

        criteria = MieszkanieSearchCriteria.builder()
                .developer("Test Developer")
                .build();

        pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));

        expectedResponse = PageResponse.<Mieszkanie>builder()
                .content(List.of(mieszkanie))
                .pageNumber(0)
                .pageSize(10)
                .totalElements(1L)
                .totalPages(1)
                .last(true)
                .first(true)
                .build();

        // Configure strategy mocks with lenient approach
        // Simple strategy only supports SIMPLE
        lenient().when(simpleSearchStrategy.supports(SearchStrategy.SearchType.SIMPLE)).thenReturn(true);
        lenient().when(simpleSearchStrategy.supports(SearchStrategy.SearchType.ADVANCED)).thenReturn(false);
        lenient().when(simpleSearchStrategy.supports(SearchStrategy.SearchType.BY_LOCATION)).thenReturn(false);

        // Advanced strategy only supports ADVANCED
        lenient().when(advancedSearchStrategy.supports(SearchStrategy.SearchType.SIMPLE)).thenReturn(false);
        lenient().when(advancedSearchStrategy.supports(SearchStrategy.SearchType.ADVANCED)).thenReturn(true);
        lenient().when(advancedSearchStrategy.supports(SearchStrategy.SearchType.BY_LOCATION)).thenReturn(false);

        // Location strategy only supports BY_LOCATION
        lenient().when(locationSearchStrategy.supports(SearchStrategy.SearchType.SIMPLE)).thenReturn(false);
        lenient().when(locationSearchStrategy.supports(SearchStrategy.SearchType.ADVANCED)).thenReturn(false);
        lenient().when(locationSearchStrategy.supports(SearchStrategy.SearchType.BY_LOCATION)).thenReturn(true);

        // Initialize SearchContext with the list of strategies
        List<SearchStrategy> strategies = Arrays.asList(
            simpleSearchStrategy, 
            advancedSearchStrategy, 
            locationSearchStrategy
        );
        searchContext = new SearchContext(strategies);
    }

    @Test
    void executeSearch_WhenSimpleStrategy_ShouldUseSimpleStrategy() {
        // Given
        when(simpleSearchStrategy.search(any(MieszkanieSearchCriteria.class), any(Pageable.class)))
                .thenReturn(expectedResponse);

        // When
        PageResponse<Mieszkanie> result = searchContext.executeSearch(
                SearchStrategy.SearchType.SIMPLE, criteria, pageable);

        // Then
        assertEquals(expectedResponse, result);
        verify(simpleSearchStrategy).search(criteria, pageable);
        verify(simpleSearchStrategy).supports(SearchStrategy.SearchType.SIMPLE);
        verify(advancedSearchStrategy, never()).search(any(), any());
        verify(locationSearchStrategy, never()).search(any(), any());
    }

    @Test
    void executeSearch_WhenAdvancedStrategy_ShouldUseAdvancedStrategy() {
        // Given
        when(advancedSearchStrategy.search(any(MieszkanieSearchCriteria.class), any(Pageable.class)))
                .thenReturn(expectedResponse);

        // When
        PageResponse<Mieszkanie> result = searchContext.executeSearch(
                SearchStrategy.SearchType.ADVANCED, criteria, pageable);

        // Then
        assertEquals(expectedResponse, result);
        verify(advancedSearchStrategy).search(criteria, pageable);
        verify(advancedSearchStrategy).supports(SearchStrategy.SearchType.ADVANCED);
        verify(simpleSearchStrategy, never()).search(any(), any());
        verify(locationSearchStrategy, never()).search(any(), any());
    }

    @Test
    void executeSearch_WhenLocationStrategy_ShouldUseLocationStrategy() {
        // Given
        when(locationSearchStrategy.search(any(MieszkanieSearchCriteria.class), any(Pageable.class)))
                .thenReturn(expectedResponse);

        // When
        PageResponse<Mieszkanie> result = searchContext.executeSearch(
                SearchStrategy.SearchType.BY_LOCATION, criteria, pageable);

        // Then
        assertEquals(expectedResponse, result);
        verify(locationSearchStrategy).search(criteria, pageable);
        verify(locationSearchStrategy).supports(SearchStrategy.SearchType.BY_LOCATION);
        verify(simpleSearchStrategy, never()).search(any(), any());
        verify(advancedSearchStrategy, never()).search(any(), any());
    }

    @Test
    void executeSearch_WhenNoStrategySupports_ShouldThrowException() {
        // Given
        when(simpleSearchStrategy.supports(any())).thenReturn(false);
        when(advancedSearchStrategy.supports(any())).thenReturn(false);
        when(locationSearchStrategy.supports(any())).thenReturn(false);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            searchContext.executeSearch(SearchStrategy.SearchType.SIMPLE, criteria, pageable)
        );
    }

    @Test
    void executeAutoSearch_WithDeveloperCriteria_ShouldUseSimpleStrategy() {
        // Given
        MieszkanieSearchCriteria simpleCriteria = MieszkanieSearchCriteria.builder()
                .developer("Test Developer")
                .build();

        when(simpleSearchStrategy.search(any(MieszkanieSearchCriteria.class), any(Pageable.class)))
                .thenReturn(expectedResponse);

        // When
        PageResponse<Mieszkanie> result = searchContext.executeAutoSearch(simpleCriteria, pageable);

        // Then
        assertEquals(expectedResponse, result);
        verify(simpleSearchStrategy).search(simpleCriteria, pageable);
        verify(advancedSearchStrategy, never()).search(any(), any());
        verify(locationSearchStrategy, never()).search(any(), any());
    }

    @Test
    void executeAutoSearch_WithLocationCriteria_ShouldUseLocationStrategy() {
        // Given
        MieszkanieSearchCriteria locationCriteria = MieszkanieSearchCriteria.builder()
                .city("Test City")
                .voivodeship("Test Voivodeship")
                .build();

        when(locationSearchStrategy.search(any(MieszkanieSearchCriteria.class), any(Pageable.class)))
                .thenReturn(expectedResponse);

        // When
        PageResponse<Mieszkanie> result = searchContext.executeAutoSearch(locationCriteria, pageable);

        // Then
        assertEquals(expectedResponse, result);
        verify(locationSearchStrategy).search(locationCriteria, pageable);
        verify(simpleSearchStrategy, never()).search(any(), any());
        verify(advancedSearchStrategy, never()).search(any(), any());
    }

    @Test
    void executeAutoSearch_WithAdvancedCriteria_ShouldUseAdvancedStrategy() {
        // Given
        MieszkanieSearchCriteria advancedCriteria = MieszkanieSearchCriteria.builder()
                .minPrice(BigDecimal.valueOf(400000))
                .maxPrice(BigDecimal.valueOf(600000))
                .build();

        when(advancedSearchStrategy.search(any(MieszkanieSearchCriteria.class), any(Pageable.class)))
                .thenReturn(expectedResponse);

        // When
        PageResponse<Mieszkanie> result = searchContext.executeAutoSearch(advancedCriteria, pageable);

        // Then
        assertEquals(expectedResponse, result);
        verify(advancedSearchStrategy).search(advancedCriteria, pageable);
        verify(simpleSearchStrategy, never()).search(any(), any());
        verify(locationSearchStrategy, never()).search(any(), any());
    }
}
