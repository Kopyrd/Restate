package com.example.restate.service.search;

import com.example.restate.config.IntegrationTestConfig;
import com.example.restate.dto.MieszkanieSearchCriteria;
import com.example.restate.dto.PageResponse;
import com.example.restate.entity.Mieszkanie;
import com.example.restate.repository.MieszkanieRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SearchStrategyIntegrationTest extends IntegrationTestConfig {

    @Autowired
    private AdvancedSearchStrategy advancedSearchStrategy;

    @Autowired
    private LocationSearchStrategy locationSearchStrategy;

    @Autowired
    private MieszkanieRepository mieszkanieRepository;

    @BeforeEach
    void setUp() {
        // Clear database
        mieszkanieRepository.deleteAll();

        // Add test data
        setupTestMieszkania();
    }

    @AfterEach
    void tearDown() {
        mieszkanieRepository.deleteAll();
    }

    private void setupTestMieszkania() {
        // Mieszkanie 1 - Luxury
        Mieszkanie m1 = new Mieszkanie();
        m1.setDeveloper("LuxDev");
        m1.setInvestment("Golden Heights");
        m1.setNumber("A101");
        m1.setArea(BigDecimal.valueOf(85.5));
        m1.setPrice(BigDecimal.valueOf(750000));
        m1.setVoivodeship("Mazowieckie");
        m1.setCity("Warszawa");
        m1.setDistrict("Śródmieście");
        m1.setFloor(10);
        m1.setDescription("Luxury apartment in the center");
        m1.setStatus(Mieszkanie.Status.AVAILABLE);

        // Mieszkanie 2 - Mid-range
        Mieszkanie m2 = new Mieszkanie();
        m2.setDeveloper("StandardDev");
        m2.setInvestment("City Park");
        m2.setNumber("B205");
        m2.setArea(BigDecimal.valueOf(62.0));
        m2.setPrice(BigDecimal.valueOf(450000));
        m2.setVoivodeship("Mazowieckie");
        m2.setCity("Warszawa");
        m2.setDistrict("Mokotów");
        m2.setFloor(2);
        m2.setDescription("Modern apartment in a green area");
        m2.setStatus(Mieszkanie.Status.AVAILABLE);

        // Mieszkanie 3 - Budget
        Mieszkanie m3 = new Mieszkanie();
        m3.setDeveloper("BudgetDev");
        m3.setInvestment("Affordable Living");
        m3.setNumber("C15");
        m3.setArea(BigDecimal.valueOf(45.0));
        m3.setPrice(BigDecimal.valueOf(280000));
        m3.setVoivodeship("Śląskie");
        m3.setCity("Katowice");
        m3.setDistrict("Centrum");
        m3.setFloor(5);
        m3.setDescription("Compact apartment for young people");
        m3.setStatus(Mieszkanie.Status.RESERVED);

        mieszkanieRepository.saveAll(List.of(m1, m2, m3));
    }

    // Tests for AdvancedSearchStrategy

    @Test
    void advancedSearchStrategy_WithDeveloperCriteria_ShouldReturnFilteredResults() {
        // Given
        MieszkanieSearchCriteria criteria = MieszkanieSearchCriteria.builder()
                .developer("LuxDev")
                .build();
        Pageable pageable = PageRequest.of(0, 10);

        // When
        PageResponse<Mieszkanie> result = advancedSearchStrategy.search(criteria, pageable);

        // Then
        assertEquals(1, result.getTotalElements());
        assertEquals("LuxDev", result.getContent().get(0).getDeveloper());
        assertEquals("Golden Heights", result.getContent().get(0).getInvestment());
    }

    @Test
    void advancedSearchStrategy_WithInvestmentCriteria_ShouldReturnFilteredResults() {
        // Given
        MieszkanieSearchCriteria criteria = MieszkanieSearchCriteria.builder()
                .investment("City Park")
                .build();
        Pageable pageable = PageRequest.of(0, 10);

        // When
        PageResponse<Mieszkanie> result = advancedSearchStrategy.search(criteria, pageable);

        // Then
        assertEquals(1, result.getTotalElements());
        assertEquals("StandardDev", result.getContent().get(0).getDeveloper());
        assertEquals("City Park", result.getContent().get(0).getInvestment());
    }

    @Test
    void advancedSearchStrategy_WithFloorCriteria_ShouldReturnFilteredResults() {
        // Given
        MieszkanieSearchCriteria criteria = MieszkanieSearchCriteria.builder()
                .floor(10)
                .build();
        Pageable pageable = PageRequest.of(0, 10);

        // When
        PageResponse<Mieszkanie> result = advancedSearchStrategy.search(criteria, pageable);

        // Then
        assertEquals(1, result.getTotalElements());
        assertEquals("LuxDev", result.getContent().get(0).getDeveloper());
        assertEquals(10, result.getContent().get(0).getFloor());
    }

    @Test
    void advancedSearchStrategy_WithStatusCriteria_ShouldReturnFilteredResults() {
        // Given
        MieszkanieSearchCriteria criteria = MieszkanieSearchCriteria.builder()
                .status("RESERVED")
                .build();
        Pageable pageable = PageRequest.of(0, 10);

        // When
        PageResponse<Mieszkanie> result = advancedSearchStrategy.search(criteria, pageable);

        // Then
        assertEquals(1, result.getTotalElements());
        assertEquals("BudgetDev", result.getContent().get(0).getDeveloper());
        assertEquals(Mieszkanie.Status.RESERVED, result.getContent().get(0).getStatus());
    }

    @Test
    void advancedSearchStrategy_WithInvalidStatusCriteria_ShouldIgnoreStatus() {
        // Given
        MieszkanieSearchCriteria criteria = MieszkanieSearchCriteria.builder()
                .status("INVALID_STATUS")
                .build();
        Pageable pageable = PageRequest.of(0, 10);

        // When
        PageResponse<Mieszkanie> result = advancedSearchStrategy.search(criteria, pageable);

        // Then
        assertEquals(3, result.getTotalElements()); // Should return all apartments
    }

    @Test
    void advancedSearchStrategy_WithLocationCriteria_ShouldReturnFilteredResults() {
        // Given
        MieszkanieSearchCriteria criteria = MieszkanieSearchCriteria.builder()
                .voivodeship("Mazowieckie")
                .city("Warszawa")
                .district("Śródmieście")
                .build();
        Pageable pageable = PageRequest.of(0, 10);

        // When
        PageResponse<Mieszkanie> result = advancedSearchStrategy.search(criteria, pageable);

        // Then
        assertEquals(1, result.getTotalElements());
        assertEquals("LuxDev", result.getContent().get(0).getDeveloper());
        assertEquals("Warszawa", result.getContent().get(0).getCity());
        assertEquals("Śródmieście", result.getContent().get(0).getDistrict());
    }

    @Test
    void advancedSearchStrategy_WithPriceRangeCriteria_ShouldReturnFilteredResults() {
        // Given
        MieszkanieSearchCriteria criteria = MieszkanieSearchCriteria.builder()
                .minPrice(BigDecimal.valueOf(400000))
                .maxPrice(BigDecimal.valueOf(500000))
                .build();
        Pageable pageable = PageRequest.of(0, 10);

        // When
        PageResponse<Mieszkanie> result = advancedSearchStrategy.search(criteria, pageable);

        // Then
        assertEquals(1, result.getTotalElements());
        assertEquals("StandardDev", result.getContent().get(0).getDeveloper());
        assertTrue(result.getContent().get(0).getPrice().compareTo(BigDecimal.valueOf(400000)) >= 0);
        assertTrue(result.getContent().get(0).getPrice().compareTo(BigDecimal.valueOf(500000)) <= 0);
    }

    @Test
    void advancedSearchStrategy_WithAreaRangeCriteria_ShouldReturnFilteredResults() {
        // Given
        MieszkanieSearchCriteria criteria = MieszkanieSearchCriteria.builder()
                .minArea(BigDecimal.valueOf(80))
                .maxArea(BigDecimal.valueOf(90))
                .build();
        Pageable pageable = PageRequest.of(0, 10);

        // When
        PageResponse<Mieszkanie> result = advancedSearchStrategy.search(criteria, pageable);

        // Then
        assertEquals(1, result.getTotalElements());
        assertEquals("LuxDev", result.getContent().get(0).getDeveloper());
        assertTrue(result.getContent().get(0).getArea().compareTo(BigDecimal.valueOf(80)) >= 0);
        assertTrue(result.getContent().get(0).getArea().compareTo(BigDecimal.valueOf(90)) <= 0);
    }

    @Test
    void advancedSearchStrategy_WithSortingCriteria_ShouldApplySorting() {
        // Given
        MieszkanieSearchCriteria criteria = MieszkanieSearchCriteria.builder().build();
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "price"));

        // When
        PageResponse<Mieszkanie> result = advancedSearchStrategy.search(criteria, pageable);

        // Then
        assertEquals(3, result.getTotalElements());
        // Verify sorting - prices should be in ascending order
        BigDecimal previousPrice = BigDecimal.ZERO;
        for (Mieszkanie m : result.getContent()) {
            assertTrue(m.getPrice().compareTo(previousPrice) >= 0);
            previousPrice = m.getPrice();
        }
    }

    @Test
    void advancedSearchStrategy_WithDescendingSortingCriteria_ShouldApplyDescendingSorting() {
        // Given
        MieszkanieSearchCriteria criteria = MieszkanieSearchCriteria.builder().build();
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "price"));

        // When
        PageResponse<Mieszkanie> result = advancedSearchStrategy.search(criteria, pageable);

        // Then
        assertEquals(3, result.getTotalElements());
        // Verify sorting - prices should be in descending order
        BigDecimal previousPrice = BigDecimal.valueOf(Double.MAX_VALUE);
        for (Mieszkanie m : result.getContent()) {
            assertTrue(m.getPrice().compareTo(previousPrice) <= 0);
            previousPrice = m.getPrice();
        }
    }

    @Test
    void advancedSearchStrategy_WithMultipleCriteria_ShouldReturnFilteredResults() {
        // Given
        MieszkanieSearchCriteria criteria = MieszkanieSearchCriteria.builder()
                .city("Warszawa")
                .minPrice(BigDecimal.valueOf(700000))
                .minArea(BigDecimal.valueOf(80))
                .status("AVAILABLE")
                .build();
        Pageable pageable = PageRequest.of(0, 10);

        // When
        PageResponse<Mieszkanie> result = advancedSearchStrategy.search(criteria, pageable);

        // Then
        assertEquals(1, result.getTotalElements());
        assertEquals("LuxDev", result.getContent().get(0).getDeveloper());
        assertEquals("Warszawa", result.getContent().get(0).getCity());
        assertTrue(result.getContent().get(0).getPrice().compareTo(BigDecimal.valueOf(700000)) >= 0);
        assertTrue(result.getContent().get(0).getArea().compareTo(BigDecimal.valueOf(80)) >= 0);
        assertEquals(Mieszkanie.Status.AVAILABLE, result.getContent().get(0).getStatus());
    }

    @Test
    void advancedSearchStrategy_SupportsMethod_ShouldReturnCorrectValues() {
        // When
        boolean supportsAdvanced = advancedSearchStrategy.supports(SearchStrategy.SearchType.ADVANCED);
        boolean supportsSimple = advancedSearchStrategy.supports(SearchStrategy.SearchType.SIMPLE);
        boolean supportsByLocation = advancedSearchStrategy.supports(SearchStrategy.SearchType.BY_LOCATION);

        // Then
        assertTrue(supportsAdvanced);
        assertFalse(supportsSimple);
        assertFalse(supportsByLocation);
    }

    // Tests for LocationSearchStrategy

    @Test
    void locationSearchStrategy_WithVoivodeshipCriteria_ShouldReturnFilteredResults() {
        // Given
        MieszkanieSearchCriteria criteria = MieszkanieSearchCriteria.builder()
                .voivodeship("Mazowieckie")
                .build();
        Pageable pageable = PageRequest.of(0, 10);

        // When
        PageResponse<Mieszkanie> result = locationSearchStrategy.search(criteria, pageable);

        // Then
        assertEquals(2, result.getTotalElements());
        assertTrue(result.getContent().stream().allMatch(m -> "Mazowieckie".equals(m.getVoivodeship())));
    }

    @Test
    void locationSearchStrategy_WithCityCriteria_ShouldReturnFilteredResults() {
        // Given
        MieszkanieSearchCriteria criteria = MieszkanieSearchCriteria.builder()
                .city("Katowice")
                .build();
        Pageable pageable = PageRequest.of(0, 10);

        // When
        PageResponse<Mieszkanie> result = locationSearchStrategy.search(criteria, pageable);

        // Then
        assertEquals(1, result.getTotalElements());
        assertEquals("BudgetDev", result.getContent().get(0).getDeveloper());
        assertEquals("Katowice", result.getContent().get(0).getCity());
    }

    @Test
    void locationSearchStrategy_WithDistrictCriteria_ShouldReturnFilteredResults() {
        // Given
        MieszkanieSearchCriteria criteria = MieszkanieSearchCriteria.builder()
                .district("Mokotów")
                .build();
        Pageable pageable = PageRequest.of(0, 10);

        // When
        PageResponse<Mieszkanie> result = locationSearchStrategy.search(criteria, pageable);

        // Then
        assertEquals(1, result.getTotalElements());
        assertEquals("StandardDev", result.getContent().get(0).getDeveloper());
        assertEquals("Mokotów", result.getContent().get(0).getDistrict());
    }

    @Test
    void locationSearchStrategy_WithMultipleLocationCriteria_ShouldReturnFilteredResults() {
        // Given
        MieszkanieSearchCriteria criteria = MieszkanieSearchCriteria.builder()
                .voivodeship("Mazowieckie")
                .city("Warszawa")
                .district("Mokotów")
                .build();
        Pageable pageable = PageRequest.of(0, 10);

        // When
        PageResponse<Mieszkanie> result = locationSearchStrategy.search(criteria, pageable);

        // Then
        assertEquals(1, result.getTotalElements());
        assertEquals("StandardDev", result.getContent().get(0).getDeveloper());
        assertEquals("Mazowieckie", result.getContent().get(0).getVoivodeship());
        assertEquals("Warszawa", result.getContent().get(0).getCity());
        assertEquals("Mokotów", result.getContent().get(0).getDistrict());
    }

    @Test
    void locationSearchStrategy_WithNonLocationCriteria_ShouldIgnoreThem() {
        // Given - criteria with both location and non-location fields
        MieszkanieSearchCriteria criteria = MieszkanieSearchCriteria.builder()
                .city("Warszawa")
                .voivodeship("Mazowieckie")
                .developer("LuxDev") // This would normally be handled by SimpleSearchStrategy
                .minPrice(BigDecimal.valueOf(300000)) // This would normally be handled by AdvancedSearchStrategy
                .build();
        Pageable pageable = PageRequest.of(0, 10);

        // When
        PageResponse<Mieszkanie> result = locationSearchStrategy.search(criteria, pageable);

        // Then
        assertEquals(2, result.getTotalElements());
        // Should return all apartments in Warszawa, Mazowieckie, regardless of developer or price
        assertTrue(result.getContent().stream().allMatch(m -> "Warszawa".equals(m.getCity())));
        assertTrue(result.getContent().stream().allMatch(m -> "Mazowieckie".equals(m.getVoivodeship())));
    }

    @Test
    void locationSearchStrategy_SupportsMethod_ShouldReturnCorrectValues() {
        // When
        boolean supportsAdvanced = locationSearchStrategy.supports(SearchStrategy.SearchType.ADVANCED);
        boolean supportsSimple = locationSearchStrategy.supports(SearchStrategy.SearchType.SIMPLE);
        boolean supportsByLocation = locationSearchStrategy.supports(SearchStrategy.SearchType.BY_LOCATION);

        // Then
        assertFalse(supportsAdvanced);
        assertFalse(supportsSimple);
        assertTrue(supportsByLocation);
    }
}