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

    @Test
    void executeAutoSearch_WithMixedLocationAndSimpleCriteria_ShouldUseAdvancedStrategy() {
        // Given
        MieszkanieSearchCriteria mixedCriteria = MieszkanieSearchCriteria.builder()
                .city("Test City")
                .developer("Test Developer")
                .build();

        when(advancedSearchStrategy.search(any(MieszkanieSearchCriteria.class), any(Pageable.class)))
                .thenReturn(expectedResponse);

        // When
        PageResponse<Mieszkanie> result = searchContext.executeAutoSearch(mixedCriteria, pageable);

        // Then
        assertEquals(expectedResponse, result);
        verify(advancedSearchStrategy).search(mixedCriteria, pageable);
        verify(simpleSearchStrategy, never()).search(any(), any());
        verify(locationSearchStrategy, never()).search(any(), any());
    }

    @Test
    void executeAutoSearch_WithMixedLocationAndAdvancedCriteria_ShouldUseAdvancedStrategy() {
        // Given
        MieszkanieSearchCriteria mixedCriteria = MieszkanieSearchCriteria.builder()
                .city("Test City")
                .minPrice(BigDecimal.valueOf(400000))
                .build();

        when(advancedSearchStrategy.search(any(MieszkanieSearchCriteria.class), any(Pageable.class)))
                .thenReturn(expectedResponse);

        // When
        PageResponse<Mieszkanie> result = searchContext.executeAutoSearch(mixedCriteria, pageable);

        // Then
        assertEquals(expectedResponse, result);
        verify(advancedSearchStrategy).search(mixedCriteria, pageable);
        verify(simpleSearchStrategy, never()).search(any(), any());
        verify(locationSearchStrategy, never()).search(any(), any());
    }

    @Test
    void executeAutoSearch_WithMixedSimpleAndAdvancedCriteria_ShouldUseAdvancedStrategy() {
        // Given
        MieszkanieSearchCriteria mixedCriteria = MieszkanieSearchCriteria.builder()
                .developer("Test Developer")
                .minPrice(BigDecimal.valueOf(400000))
                .build();

        when(advancedSearchStrategy.search(any(MieszkanieSearchCriteria.class), any(Pageable.class)))
                .thenReturn(expectedResponse);

        // When
        PageResponse<Mieszkanie> result = searchContext.executeAutoSearch(mixedCriteria, pageable);

        // Then
        assertEquals(expectedResponse, result);
        verify(advancedSearchStrategy).search(mixedCriteria, pageable);
        verify(simpleSearchStrategy, never()).search(any(), any());
        verify(locationSearchStrategy, never()).search(any(), any());
    }

    @Test
    void executeAutoSearch_WithAllTypesCriteria_ShouldUseAdvancedStrategy() {
        // Given
        MieszkanieSearchCriteria allTypesCriteria = MieszkanieSearchCriteria.builder()
                .developer("Test Developer")
                .city("Test City")
                .minPrice(BigDecimal.valueOf(400000))
                .build();

        when(advancedSearchStrategy.search(any(MieszkanieSearchCriteria.class), any(Pageable.class)))
                .thenReturn(expectedResponse);

        // When
        PageResponse<Mieszkanie> result = searchContext.executeAutoSearch(allTypesCriteria, pageable);

        // Then
        assertEquals(expectedResponse, result);
        verify(advancedSearchStrategy).search(allTypesCriteria, pageable);
        verify(simpleSearchStrategy, never()).search(any(), any());
        verify(locationSearchStrategy, never()).search(any(), any());
    }

    @Test
    void executeAutoSearch_WithEmptyCriteria_ShouldUseAdvancedStrategy() {
        // Given
        MieszkanieSearchCriteria emptyCriteria = MieszkanieSearchCriteria.builder().build();

        when(advancedSearchStrategy.search(any(MieszkanieSearchCriteria.class), any(Pageable.class)))
                .thenReturn(expectedResponse);

        // When
        PageResponse<Mieszkanie> result = searchContext.executeAutoSearch(emptyCriteria, pageable);

        // Then
        assertEquals(expectedResponse, result);
        verify(advancedSearchStrategy).search(emptyCriteria, pageable);
        verify(simpleSearchStrategy, never()).search(any(), any());
        verify(locationSearchStrategy, never()).search(any(), any());
    }

    @Test
    void executeAutoSearch_WithOnlyCityCriteria_ShouldUseLocationStrategy() {
        // Given
        MieszkanieSearchCriteria cityCriteria = MieszkanieSearchCriteria.builder()
                .city("Test City")
                .build();

        when(locationSearchStrategy.search(any(MieszkanieSearchCriteria.class), any(Pageable.class)))
                .thenReturn(expectedResponse);

        // When
        PageResponse<Mieszkanie> result = searchContext.executeAutoSearch(cityCriteria, pageable);

        // Then
        assertEquals(expectedResponse, result);
        verify(locationSearchStrategy).search(cityCriteria, pageable);
        verify(simpleSearchStrategy, never()).search(any(), any());
        verify(advancedSearchStrategy, never()).search(any(), any());
    }

    @Test
    void executeAutoSearch_WithOnlyVoivodeshipCriteria_ShouldUseLocationStrategy() {
        // Given
        MieszkanieSearchCriteria voivodeshipCriteria = MieszkanieSearchCriteria.builder()
                .voivodeship("Test Voivodeship")
                .build();

        when(locationSearchStrategy.search(any(MieszkanieSearchCriteria.class), any(Pageable.class)))
                .thenReturn(expectedResponse);

        // When
        PageResponse<Mieszkanie> result = searchContext.executeAutoSearch(voivodeshipCriteria, pageable);

        // Then
        assertEquals(expectedResponse, result);
        verify(locationSearchStrategy).search(voivodeshipCriteria, pageable);
        verify(simpleSearchStrategy, never()).search(any(), any());
        verify(advancedSearchStrategy, never()).search(any(), any());
    }

    @Test
    void executeAutoSearch_WithOnlyDistrictCriteria_ShouldUseLocationStrategy() {
        // Given
        MieszkanieSearchCriteria districtCriteria = MieszkanieSearchCriteria.builder()
                .district("Test District")
                .build();

        when(locationSearchStrategy.search(any(MieszkanieSearchCriteria.class), any(Pageable.class)))
                .thenReturn(expectedResponse);

        // When
        PageResponse<Mieszkanie> result = searchContext.executeAutoSearch(districtCriteria, pageable);

        // Then
        assertEquals(expectedResponse, result);
        verify(locationSearchStrategy).search(districtCriteria, pageable);
        verify(simpleSearchStrategy, never()).search(any(), any());
        verify(advancedSearchStrategy, never()).search(any(), any());
    }

    @Test
    void executeAutoSearch_WithOnlyDeveloperCriteria_ShouldUseSimpleStrategy() {
        // Given
        MieszkanieSearchCriteria developerCriteria = MieszkanieSearchCriteria.builder()
                .developer("Test Developer")
                .build();

        when(simpleSearchStrategy.search(any(MieszkanieSearchCriteria.class), any(Pageable.class)))
                .thenReturn(expectedResponse);

        // When
        PageResponse<Mieszkanie> result = searchContext.executeAutoSearch(developerCriteria, pageable);

        // Then
        assertEquals(expectedResponse, result);
        verify(simpleSearchStrategy).search(developerCriteria, pageable);
        verify(locationSearchStrategy, never()).search(any(), any());
        verify(advancedSearchStrategy, never()).search(any(), any());
    }

    @Test
    void executeAutoSearch_WithOnlyInvestmentCriteria_ShouldUseSimpleStrategy() {
        // Given
        MieszkanieSearchCriteria investmentCriteria = MieszkanieSearchCriteria.builder()
                .investment("Test Investment")
                .build();

        when(simpleSearchStrategy.search(any(MieszkanieSearchCriteria.class), any(Pageable.class)))
                .thenReturn(expectedResponse);

        // When
        PageResponse<Mieszkanie> result = searchContext.executeAutoSearch(investmentCriteria, pageable);

        // Then
        assertEquals(expectedResponse, result);
        verify(simpleSearchStrategy).search(investmentCriteria, pageable);
        verify(locationSearchStrategy, never()).search(any(), any());
        verify(advancedSearchStrategy, never()).search(any(), any());
    }

    @Test
    void executeAutoSearch_WithOnlyFloorCriteria_ShouldUseAdvancedStrategy() {
        // Given
        MieszkanieSearchCriteria floorCriteria = MieszkanieSearchCriteria.builder()
                .floor(2)
                .build();

        when(advancedSearchStrategy.search(any(MieszkanieSearchCriteria.class), any(Pageable.class)))
                .thenReturn(expectedResponse);

        // When
        PageResponse<Mieszkanie> result = searchContext.executeAutoSearch(floorCriteria, pageable);

        // Then
        assertEquals(expectedResponse, result);
        verify(advancedSearchStrategy).search(floorCriteria, pageable);
        verify(simpleSearchStrategy, never()).search(any(), any());
        verify(locationSearchStrategy, never()).search(any(), any());
    }

    @Test
    void executeAutoSearch_WithOnlyStatusCriteria_ShouldUseAdvancedStrategy() {
        // Given
        MieszkanieSearchCriteria statusCriteria = MieszkanieSearchCriteria.builder()
                .status("AVAILABLE")
                .build();

        when(advancedSearchStrategy.search(any(MieszkanieSearchCriteria.class), any(Pageable.class)))
                .thenReturn(expectedResponse);

        // When
        PageResponse<Mieszkanie> result = searchContext.executeAutoSearch(statusCriteria, pageable);

        // Then
        assertEquals(expectedResponse, result);
        verify(advancedSearchStrategy).search(statusCriteria, pageable);
        verify(simpleSearchStrategy, never()).search(any(), any());
        verify(locationSearchStrategy, never()).search(any(), any());
    }

    @Test
    void executeAutoSearch_WithOnlyMinPriceCriteria_ShouldUseAdvancedStrategy() {
        // Given
        MieszkanieSearchCriteria minPriceCriteria = MieszkanieSearchCriteria.builder()
                .minPrice(BigDecimal.valueOf(400000))
                .build();

        when(advancedSearchStrategy.search(any(MieszkanieSearchCriteria.class), any(Pageable.class)))
                .thenReturn(expectedResponse);

        // When
        PageResponse<Mieszkanie> result = searchContext.executeAutoSearch(minPriceCriteria, pageable);

        // Then
        assertEquals(expectedResponse, result);
        verify(advancedSearchStrategy).search(minPriceCriteria, pageable);
        verify(simpleSearchStrategy, never()).search(any(), any());
        verify(locationSearchStrategy, never()).search(any(), any());
    }

    @Test
    void executeAutoSearch_WithOnlyMaxPriceCriteria_ShouldUseAdvancedStrategy() {
        // Given
        MieszkanieSearchCriteria maxPriceCriteria = MieszkanieSearchCriteria.builder()
                .maxPrice(BigDecimal.valueOf(600000))
                .build();

        when(advancedSearchStrategy.search(any(MieszkanieSearchCriteria.class), any(Pageable.class)))
                .thenReturn(expectedResponse);

        // When
        PageResponse<Mieszkanie> result = searchContext.executeAutoSearch(maxPriceCriteria, pageable);

        // Then
        assertEquals(expectedResponse, result);
        verify(advancedSearchStrategy).search(maxPriceCriteria, pageable);
        verify(simpleSearchStrategy, never()).search(any(), any());
        verify(locationSearchStrategy, never()).search(any(), any());
    }

    @Test
    void executeAutoSearch_WithOnlyMinAreaCriteria_ShouldUseAdvancedStrategy() {
        // Given
        MieszkanieSearchCriteria minAreaCriteria = MieszkanieSearchCriteria.builder()
                .minArea(BigDecimal.valueOf(70))
                .build();

        when(advancedSearchStrategy.search(any(MieszkanieSearchCriteria.class), any(Pageable.class)))
                .thenReturn(expectedResponse);

        // When
        PageResponse<Mieszkanie> result = searchContext.executeAutoSearch(minAreaCriteria, pageable);

        // Then
        assertEquals(expectedResponse, result);
        verify(advancedSearchStrategy).search(minAreaCriteria, pageable);
        verify(simpleSearchStrategy, never()).search(any(), any());
        verify(locationSearchStrategy, never()).search(any(), any());
    }

    @Test
    void executeAutoSearch_WithOnlyMaxAreaCriteria_ShouldUseAdvancedStrategy() {
        // Given
        MieszkanieSearchCriteria maxAreaCriteria = MieszkanieSearchCriteria.builder()
                .maxArea(BigDecimal.valueOf(120))
                .build();

        when(advancedSearchStrategy.search(any(MieszkanieSearchCriteria.class), any(Pageable.class)))
                .thenReturn(expectedResponse);

        // When
        PageResponse<Mieszkanie> result = searchContext.executeAutoSearch(maxAreaCriteria, pageable);

        // Then
        assertEquals(expectedResponse, result);
        verify(advancedSearchStrategy).search(maxAreaCriteria, pageable);
        verify(simpleSearchStrategy, never()).search(any(), any());
        verify(locationSearchStrategy, never()).search(any(), any());
    }
}
