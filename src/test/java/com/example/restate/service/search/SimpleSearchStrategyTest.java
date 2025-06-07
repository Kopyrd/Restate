package com.example.restate.service.search;

import com.example.restate.dto.MieszkanieSearchCriteria;
import com.example.restate.dto.PageResponse;
import com.example.restate.entity.Mieszkanie;
import com.example.restate.repository.MieszkanieRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SimpleSearchStrategyTest {

    @Mock
    private MieszkanieRepository mieszkanieRepository;

    @InjectMocks
    private SimpleSearchStrategy simpleSearchStrategy;

    private Mieszkanie mieszkanie1;
    private Mieszkanie mieszkanie2;
    private List<Mieszkanie> mieszkanieList;
    private Page<Mieszkanie> mieszkaniePage;
    private Pageable pageable;
    private MieszkanieSearchCriteria criteria;

    @BeforeEach
    void setUp() {
        // Setup test data
        mieszkanie1 = new Mieszkanie();
        mieszkanie1.setId(1);
        mieszkanie1.setDeveloper("Test Developer");
        mieszkanie1.setInvestment("Test Investment");
        mieszkanie1.setArea(BigDecimal.valueOf(75.5));
        mieszkanie1.setPrice(BigDecimal.valueOf(500000));

        mieszkanie2 = new Mieszkanie();
        mieszkanie2.setId(2);
        mieszkanie2.setDeveloper("Test Developer");
        mieszkanie2.setInvestment("Another Investment");
        mieszkanie2.setArea(BigDecimal.valueOf(100.0));
        mieszkanie2.setPrice(BigDecimal.valueOf(750000));

        mieszkanieList = Arrays.asList(mieszkanie1, mieszkanie2);
        pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));
        mieszkaniePage = new PageImpl<>(mieszkanieList, pageable, mieszkanieList.size());

        criteria = MieszkanieSearchCriteria.builder().build();
    }

    @Test
    void search_WhenDeveloperIsProvided_ShouldSearchByDeveloper() {
        // Given
        criteria = MieszkanieSearchCriteria.builder()
                .developer("Test Developer")
                .build();

        when(mieszkanieRepository.findByDeveloper("Test Developer", pageable)).thenReturn(mieszkaniePage);

        // When
        PageResponse<Mieszkanie> result = simpleSearchStrategy.search(criteria, pageable);

        // Then
        assertEquals(2, result.getContent().size());
        assertEquals(0, result.getPageNumber());
        assertEquals(10, result.getPageSize());
        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        verify(mieszkanieRepository, times(1)).findByDeveloper("Test Developer", pageable);
        verify(mieszkanieRepository, never()).findByInvestment(anyString(), any(Pageable.class));
        verify(mieszkanieRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void search_WhenInvestmentIsProvided_ShouldSearchByInvestment() {
        // Given
        criteria = MieszkanieSearchCriteria.builder()
                .investment("Test Investment")
                .build();

        Page<Mieszkanie> investmentPage = new PageImpl<>(List.of(mieszkanie1), pageable, 1);
        when(mieszkanieRepository.findByInvestment("Test Investment", pageable)).thenReturn(investmentPage);

        // When
        PageResponse<Mieszkanie> result = simpleSearchStrategy.search(criteria, pageable);

        // Then
        assertEquals(1, result.getContent().size());
        assertEquals(0, result.getPageNumber());
        assertEquals(10, result.getPageSize());
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        verify(mieszkanieRepository, never()).findByDeveloper(anyString(), any(Pageable.class));
        verify(mieszkanieRepository, times(1)).findByInvestment("Test Investment", pageable);
        verify(mieszkanieRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void search_WhenNoCriteriaProvided_ShouldReturnAllMieszkania() {
        // Given
        when(mieszkanieRepository.findAll(pageable)).thenReturn(mieszkaniePage);

        // When
        PageResponse<Mieszkanie> result = simpleSearchStrategy.search(criteria, pageable);

        // Then
        assertEquals(2, result.getContent().size());
        assertEquals(0, result.getPageNumber());
        assertEquals(10, result.getPageSize());
        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        verify(mieszkanieRepository, never()).findByDeveloper(anyString(), any(Pageable.class));
        verify(mieszkanieRepository, never()).findByInvestment(anyString(), any(Pageable.class));
        verify(mieszkanieRepository, times(1)).findAll(pageable);
    }

    @Test
    void supports_WhenSearchTypeIsSimple_ShouldReturnTrue() {
        // When
        boolean result = simpleSearchStrategy.supports(SearchStrategy.SearchType.SIMPLE);

        // Then
        assertTrue(result);
    }

    @Test
    void supports_WhenSearchTypeIsNotSimple_ShouldReturnFalse() {
        // When
        boolean result1 = simpleSearchStrategy.supports(SearchStrategy.SearchType.ADVANCED);
        boolean result2 = simpleSearchStrategy.supports(SearchStrategy.SearchType.BY_LOCATION);

        // Then
        assertFalse(result1);
        assertFalse(result2);
    }

    @Test
    void search_WithMixedCriteria_ShouldOnlyUseSimpleCriteria() {
        // Given - criteria with both simple and non-simple fields
        criteria = MieszkanieSearchCriteria.builder()
                .developer("Test Developer") // Simple criteria
                .city("Warsaw") // Location criteria
                .minPrice(BigDecimal.valueOf(300000)) // Advanced criteria
                .build();

        when(mieszkanieRepository.findByDeveloper("Test Developer", pageable)).thenReturn(mieszkaniePage);

        // When
        PageResponse<Mieszkanie> result = simpleSearchStrategy.search(criteria, pageable);

        // Then
        assertEquals(2, result.getContent().size());
        assertEquals(0, result.getPageNumber());
        assertEquals(10, result.getPageSize());
        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getTotalPages());

        // Should only use the developer criteria, ignoring city and price
        verify(mieszkanieRepository, times(1)).findByDeveloper("Test Developer", pageable);
        verify(mieszkanieRepository, never()).findByInvestment(anyString(), any(Pageable.class));
        verify(mieszkanieRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void search_WithBothDeveloperAndInvestment_ShouldPrioritizeDeveloper() {
        // Given - criteria with both developer and investment
        criteria = MieszkanieSearchCriteria.builder()
                .developer("Test Developer")
                .investment("Test Investment")
                .build();

        when(mieszkanieRepository.findByDeveloper("Test Developer", pageable)).thenReturn(mieszkaniePage);

        // When
        PageResponse<Mieszkanie> result = simpleSearchStrategy.search(criteria, pageable);

        // Then
        assertEquals(2, result.getContent().size());

        // Should prioritize developer over investment
        verify(mieszkanieRepository, times(1)).findByDeveloper("Test Developer", pageable);
        verify(mieszkanieRepository, never()).findByInvestment(anyString(), any(Pageable.class));
        verify(mieszkanieRepository, never()).findAll(any(Pageable.class));
    }
}
