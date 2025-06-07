package com.example.restate.integration;

import com.example.restate.config.IntegrationTestConfig;
import com.example.restate.dto.MieszkanieSearchCriteria;
import com.example.restate.dto.PageResponse;
import com.example.restate.entity.Mieszkanie;
import com.example.restate.repository.MieszkanieRepository;
import com.example.restate.service.search.SearchContext;
import com.example.restate.service.search.SearchStrategy;
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

/**
 * Integration tests for the search functionality.
 * Tests all search strategies and the SearchContext.
 */
public class SearchStrategyIntegrationTest extends IntegrationTestConfig {

    @Autowired
    private SearchContext searchContext;

    @Autowired
    private MieszkanieRepository mieszkanieRepository;

    private Mieszkanie mieszkanie1;
    private Mieszkanie mieszkanie2;
    private Mieszkanie mieszkanie3;

    @BeforeEach
    void setUp() {
        // Clean up the database
        mieszkanieRepository.deleteAll();

        // Create test data
        mieszkanie1 = new Mieszkanie();
        mieszkanie1.setDeveloper("Developer A");
        mieszkanie1.setInvestment("Investment X");
        mieszkanie1.setNumber("A1");
        mieszkanie1.setArea(BigDecimal.valueOf(75.5));
        mieszkanie1.setPrice(BigDecimal.valueOf(500000));
        mieszkanie1.setVoivodeship("Mazowieckie");
        mieszkanie1.setCity("Warsaw");
        mieszkanie1.setDistrict("Mokotów");
        mieszkanie1.setFloor(2);
        mieszkanie1.setDescription("Test Description 1");
        mieszkanie1.setStatus(Mieszkanie.Status.AVAILABLE);

        mieszkanie2 = new Mieszkanie();
        mieszkanie2.setDeveloper("Developer B");
        mieszkanie2.setInvestment("Investment Y");
        mieszkanie2.setNumber("B2");
        mieszkanie2.setArea(BigDecimal.valueOf(100.0));
        mieszkanie2.setPrice(BigDecimal.valueOf(750000));
        mieszkanie2.setVoivodeship("Mazowieckie");
        mieszkanie2.setCity("Warsaw");
        mieszkanie2.setDistrict("Śródmieście");
        mieszkanie2.setFloor(3);
        mieszkanie2.setDescription("Test Description 2");
        mieszkanie2.setStatus(Mieszkanie.Status.RESERVED);

        mieszkanie3 = new Mieszkanie();
        mieszkanie3.setDeveloper("Developer A");
        mieszkanie3.setInvestment("Investment Z");
        mieszkanie3.setNumber("C3");
        mieszkanie3.setArea(BigDecimal.valueOf(120.0));
        mieszkanie3.setPrice(BigDecimal.valueOf(900000));
        mieszkanie3.setVoivodeship("Małopolskie");
        mieszkanie3.setCity("Kraków");
        mieszkanie3.setDistrict("Stare Miasto");
        mieszkanie3.setFloor(4);
        mieszkanie3.setDescription("Test Description 3");
        mieszkanie3.setStatus(Mieszkanie.Status.SOLD);

        // Save test data
        mieszkanieRepository.save(mieszkanie1);
        mieszkanieRepository.save(mieszkanie2);
        mieszkanieRepository.save(mieszkanie3);
    }

    @AfterEach
    void tearDown() {
        // Clean up the database
        mieszkanieRepository.deleteAll();
    }

    @Test
    void testSimpleSearchStrategy() {
        // Given
        MieszkanieSearchCriteria criteria = MieszkanieSearchCriteria.builder()
                .developer("Developer A")
                .build();
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").ascending());

        // When
        PageResponse<Mieszkanie> result = searchContext.executeSearch(
                SearchStrategy.SearchType.SIMPLE, criteria, pageable);

        // Then
        assertEquals(2, result.getContent().size());
        assertEquals("Developer A", result.getContent().get(0).getDeveloper());
        assertEquals("Developer A", result.getContent().get(1).getDeveloper());
    }

    @Test
    void testLocationSearchStrategy() {
        // Given
        MieszkanieSearchCriteria criteria = MieszkanieSearchCriteria.builder()
                .city("Warsaw")
                .build();
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").ascending());

        // When
        PageResponse<Mieszkanie> result = searchContext.executeSearch(
                SearchStrategy.SearchType.BY_LOCATION, criteria, pageable);

        // Then
        assertEquals(2, result.getContent().size());
        assertEquals("Warsaw", result.getContent().get(0).getCity());
        assertEquals("Warsaw", result.getContent().get(1).getCity());
    }

    @Test
    void testAdvancedSearchStrategy() {
        // Given
        MieszkanieSearchCriteria criteria = MieszkanieSearchCriteria.builder()
                .minPrice(BigDecimal.valueOf(700000))
                .maxPrice(BigDecimal.valueOf(1000000))
                .minArea(BigDecimal.valueOf(90.0))
                .build();
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").ascending());

        // When
        PageResponse<Mieszkanie> result = searchContext.executeSearch(
                SearchStrategy.SearchType.ADVANCED, criteria, pageable);

        // Then
        assertEquals(2, result.getContent().size());
        assertTrue(result.getContent().get(0).getPrice().compareTo(BigDecimal.valueOf(700000)) >= 0);
        assertTrue(result.getContent().get(0).getArea().compareTo(BigDecimal.valueOf(90.0)) >= 0);
    }

    @Test
    void testAutoSearchWithLocationCriteria() {
        // Given
        MieszkanieSearchCriteria criteria = MieszkanieSearchCriteria.builder()
                .city("Kraków")
                .build();
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").ascending());

        // When
        PageResponse<Mieszkanie> result = searchContext.executeAutoSearch(criteria, pageable);

        // Then
        assertEquals(1, result.getContent().size());
        assertEquals("Kraków", result.getContent().get(0).getCity());
    }

    @Test
    void testAutoSearchWithSimpleCriteria() {
        // Given
        MieszkanieSearchCriteria criteria = MieszkanieSearchCriteria.builder()
                .investment("Investment X")
                .build();
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").ascending());

        // When
        PageResponse<Mieszkanie> result = searchContext.executeAutoSearch(criteria, pageable);

        // Then
        assertEquals(1, result.getContent().size());
        assertEquals("Investment X", result.getContent().get(0).getInvestment());
    }

    @Test
    void testAutoSearchWithAdvancedCriteria() {
        // Given
        MieszkanieSearchCriteria criteria = MieszkanieSearchCriteria.builder()
                .developer("Developer A")
                .minPrice(BigDecimal.valueOf(800000))
                .build();
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").ascending());

        // When
        PageResponse<Mieszkanie> result = searchContext.executeAutoSearch(criteria, pageable);

        // Then
        assertEquals(1, result.getContent().size());
        assertEquals("Developer A", result.getContent().get(0).getDeveloper());
        assertTrue(result.getContent().get(0).getPrice().compareTo(BigDecimal.valueOf(800000)) >= 0);
    }

    @Test
    void testSearchWithMultipleLocationCriteria() {
        // Given
        MieszkanieSearchCriteria criteria = MieszkanieSearchCriteria.builder()
                .voivodeship("Mazowieckie")
                .city("Warsaw")
                .district("Mokotów")
                .build();
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").ascending());

        // When
        PageResponse<Mieszkanie> result = searchContext.executeSearch(
                SearchStrategy.SearchType.BY_LOCATION, criteria, pageable);

        // Then
        assertEquals(1, result.getContent().size());
        assertEquals("Mokotów", result.getContent().get(0).getDistrict());
    }
}