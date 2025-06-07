package com.example.restate.service.search;

import com.example.restate.dto.MieszkanieSearchCriteria;
import com.example.restate.dto.PageResponse;
import com.example.restate.entity.Mieszkanie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdvancedSearchStrategyTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    private CriteriaQuery<Mieszkanie> criteriaQuery;

    @Mock
    private CriteriaQuery<Long> countQuery;

    @Mock
    private Root<Mieszkanie> root;

    @Mock
    private Root<Mieszkanie> countRoot;

    @Mock
    private TypedQuery<Mieszkanie> typedQuery;

    @Mock
    private TypedQuery<Long> countTypedQuery;

    @Captor
    private ArgumentCaptor<Predicate[]> predicatesCaptor;

    @InjectMocks
    private AdvancedSearchStrategy advancedSearchStrategy;

    private Pageable pageable;
    private List<Mieszkanie> mieszkanieList;

    @BeforeEach
    void setUp() {
        // Setup test data
        Mieszkanie mieszkanie1 = new Mieszkanie();
        mieszkanie1.setId(1);
        mieszkanie1.setDeveloper("Test Developer");
        mieszkanie1.setInvestment("Test Investment");
        mieszkanie1.setCity("Warsaw");
        mieszkanie1.setDistrict("Mokotow");
        mieszkanie1.setVoivodeship("Mazowieckie");
        mieszkanie1.setFloor(2);
        mieszkanie1.setArea(BigDecimal.valueOf(75.5));
        mieszkanie1.setPrice(BigDecimal.valueOf(500000));
        mieszkanie1.setStatus(Mieszkanie.Status.AVAILABLE);

        Mieszkanie mieszkanie2 = new Mieszkanie();
        mieszkanie2.setId(2);
        mieszkanie2.setDeveloper("Another Developer");
        mieszkanie2.setInvestment("Another Investment");
        mieszkanie2.setCity("Krakow");
        mieszkanie2.setDistrict("Nowa Huta");
        mieszkanie2.setVoivodeship("Malopolskie");
        mieszkanie2.setFloor(3);
        mieszkanie2.setArea(BigDecimal.valueOf(100.0));
        mieszkanie2.setPrice(BigDecimal.valueOf(750000));
        mieszkanie2.setStatus(Mieszkanie.Status.SOLD);

        mieszkanieList = Arrays.asList(mieszkanie1, mieszkanie2);
        pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));

        // Setup mocks for EntityManager and related objects
        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(Mieszkanie.class)).thenReturn(criteriaQuery);
        when(criteriaBuilder.createQuery(Long.class)).thenReturn(countQuery);
        when(criteriaQuery.from(Mieszkanie.class)).thenReturn(root);
        when(countQuery.from(Mieszkanie.class)).thenReturn(countRoot);
        when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQuery);
        when(entityManager.createQuery(countQuery)).thenReturn(countTypedQuery);
        when(countTypedQuery.getSingleResult()).thenReturn(2L);
        when(typedQuery.getResultList()).thenReturn(mieszkanieList);

        // Setup mock for predicates
        Predicate predicate = mock(Predicate.class);
        when(criteriaBuilder.equal(any(), any())).thenReturn(predicate);
        when(criteriaBuilder.greaterThanOrEqualTo(any(), any(Comparable.class))).thenReturn(predicate);
        when(criteriaBuilder.lessThanOrEqualTo(any(), any(Comparable.class))).thenReturn(predicate);
    }

    @Test
    void search_WithDeveloperCriteria_ShouldCreateCorrectPredicate() {
        // Given
        MieszkanieSearchCriteria criteria = MieszkanieSearchCriteria.builder()
                .developer("Test Developer")
                .build();

        // When
        PageResponse<Mieszkanie> result = advancedSearchStrategy.search(criteria, pageable);

        // Then
        verify(criteriaQuery).where(predicatesCaptor.capture());
        Predicate[] predicates = predicatesCaptor.getValue();
        assertEquals(1, predicates.length);

        verify(root, atLeastOnce()).get("developer");
        verify(criteriaBuilder, atLeastOnce()).equal(any(), eq("Test Developer"));

        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
    }

    @Test
    void search_WithInvestmentCriteria_ShouldCreateCorrectPredicate() {
        // Given
        MieszkanieSearchCriteria criteria = MieszkanieSearchCriteria.builder()
                .investment("Test Investment")
                .build();

        // When
        PageResponse<Mieszkanie> result = advancedSearchStrategy.search(criteria, pageable);

        // Then
        verify(criteriaQuery).where(predicatesCaptor.capture());
        Predicate[] predicates = predicatesCaptor.getValue();
        assertEquals(1, predicates.length);

        verify(root, atLeastOnce()).get("investment");
        verify(criteriaBuilder, atLeastOnce()).equal(any(), eq("Test Investment"));

        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
    }

    @Test
    void search_WithFloorCriteria_ShouldCreateCorrectPredicate() {
        // Given
        MieszkanieSearchCriteria criteria = MieszkanieSearchCriteria.builder()
                .floor(2)
                .build();

        // When
        PageResponse<Mieszkanie> result = advancedSearchStrategy.search(criteria, pageable);

        // Then
        verify(criteriaQuery).where(predicatesCaptor.capture());
        Predicate[] predicates = predicatesCaptor.getValue();
        assertEquals(1, predicates.length);

        verify(root, atLeastOnce()).get("floor");
        verify(criteriaBuilder, atLeastOnce()).equal(any(), eq(2));

        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
    }

    @Test
    void search_WithStatusCriteria_ShouldCreateCorrectPredicate() {
        // Given
        MieszkanieSearchCriteria criteria = MieszkanieSearchCriteria.builder()
                .status("AVAILABLE")
                .build();

        // When
        PageResponse<Mieszkanie> result = advancedSearchStrategy.search(criteria, pageable);

        // Then
        verify(criteriaQuery).where(predicatesCaptor.capture());
        Predicate[] predicates = predicatesCaptor.getValue();
        assertEquals(1, predicates.length);

        verify(root, atLeastOnce()).get("status");
        verify(criteriaBuilder, atLeastOnce()).equal(any(), eq(Mieszkanie.Status.AVAILABLE));

        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
    }

    @Test
    void search_WithInvalidStatusCriteria_ShouldIgnoreStatus() {
        // Given
        MieszkanieSearchCriteria criteria = MieszkanieSearchCriteria.builder()
                .status("INVALID_STATUS")
                .build();

        // When
        PageResponse<Mieszkanie> result = advancedSearchStrategy.search(criteria, pageable);

        // Then
        verify(criteriaQuery).where(predicatesCaptor.capture());
        Predicate[] predicates = predicatesCaptor.getValue();
        assertEquals(0, predicates.length);

        verify(root, never()).get("status");
        verify(criteriaBuilder, never()).equal(any(), eq(Mieszkanie.Status.AVAILABLE));

        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
    }

    @Test
    void search_WithLocationCriteria_ShouldCreateCorrectPredicates() {
        // Given
        MieszkanieSearchCriteria criteria = MieszkanieSearchCriteria.builder()
                .voivodeship("Mazowieckie")
                .city("Warsaw")
                .district("Mokotow")
                .build();

        // When
        PageResponse<Mieszkanie> result = advancedSearchStrategy.search(criteria, pageable);

        // Then
        verify(criteriaQuery).where(predicatesCaptor.capture());
        Predicate[] predicates = predicatesCaptor.getValue();
        assertEquals(3, predicates.length);

        verify(root, atLeastOnce()).get("voivodeship");
        verify(root, atLeastOnce()).get("city");
        verify(root, atLeastOnce()).get("district");
        verify(criteriaBuilder, atLeastOnce()).equal(any(), eq("Mazowieckie"));
        verify(criteriaBuilder, atLeastOnce()).equal(any(), eq("Warsaw"));
        verify(criteriaBuilder, atLeastOnce()).equal(any(), eq("Mokotow"));

        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
    }

    @Test
    void search_WithPriceRangeCriteria_ShouldCreateCorrectPredicates() {
        // Given
        MieszkanieSearchCriteria criteria = MieszkanieSearchCriteria.builder()
                .minPrice(BigDecimal.valueOf(400000))
                .maxPrice(BigDecimal.valueOf(800000))
                .build();

        // When
        PageResponse<Mieszkanie> result = advancedSearchStrategy.search(criteria, pageable);

        // Then
        verify(criteriaQuery).where(predicatesCaptor.capture());
        Predicate[] predicates = predicatesCaptor.getValue();
        assertEquals(2, predicates.length);

        verify(root, atLeast(2)).get("price");
        verify(criteriaBuilder, atLeastOnce()).greaterThanOrEqualTo(any(), eq(BigDecimal.valueOf(400000)));
        verify(criteriaBuilder, atLeastOnce()).lessThanOrEqualTo(any(), eq(BigDecimal.valueOf(800000)));

        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
    }

    @Test
    void search_WithAreaRangeCriteria_ShouldCreateCorrectPredicates() {
        // Given
        MieszkanieSearchCriteria criteria = MieszkanieSearchCriteria.builder()
                .minArea(BigDecimal.valueOf(70))
                .maxArea(BigDecimal.valueOf(120))
                .build();

        // When
        PageResponse<Mieszkanie> result = advancedSearchStrategy.search(criteria, pageable);

        // Then
        verify(criteriaQuery).where(predicatesCaptor.capture());
        Predicate[] predicates = predicatesCaptor.getValue();
        assertEquals(2, predicates.length);

        verify(root, atLeast(2)).get("area");
        verify(criteriaBuilder, atLeastOnce()).greaterThanOrEqualTo(any(), eq(BigDecimal.valueOf(70)));
        verify(criteriaBuilder, atLeastOnce()).lessThanOrEqualTo(any(), eq(BigDecimal.valueOf(120)));

        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
    }

    @Test
    void search_WithSortingCriteria_ShouldApplySorting() {
        // Given
        MieszkanieSearchCriteria criteria = MieszkanieSearchCriteria.builder().build();
        pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "price"));

        // When
        PageResponse<Mieszkanie> result = advancedSearchStrategy.search(criteria, pageable);

        // Then
        verify(root, atLeastOnce()).get("price");
        verify(criteriaBuilder, atLeastOnce()).asc(any());

        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
    }

    @Test
    void search_WithDescendingSortingCriteria_ShouldApplyDescendingSorting() {
        // Given
        MieszkanieSearchCriteria criteria = MieszkanieSearchCriteria.builder().build();
        pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "price"));

        // When
        PageResponse<Mieszkanie> result = advancedSearchStrategy.search(criteria, pageable);

        // Then
        verify(root, atLeastOnce()).get("price");
        verify(criteriaBuilder, atLeastOnce()).desc(any());

        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
    }

    @Test
    void search_WithPagination_ShouldApplyPagination() {
        // Given
        MieszkanieSearchCriteria criteria = MieszkanieSearchCriteria.builder().build();
        pageable = PageRequest.of(1, 5, Sort.by(Sort.Direction.ASC, "id"));

        // Since we're on page 1 and there are only 2 items total (all on page 0),
        // the result list should be empty
        when(typedQuery.getResultList()).thenReturn(List.of());

        // When
        PageResponse<Mieszkanie> result = advancedSearchStrategy.search(criteria, pageable);

        // Then
        verify(typedQuery).setFirstResult(5);
        verify(typedQuery).setMaxResults(5);

        assertEquals(2, result.getTotalElements());
        assertEquals(0, result.getContent().size());
    }

    @Test
    void supports_WhenSearchTypeIsAdvanced_ShouldReturnTrue() {
        // When
        boolean result = advancedSearchStrategy.supports(SearchStrategy.SearchType.ADVANCED);

        // Then
        assertTrue(result);
    }

    @Test
    void supports_WhenSearchTypeIsNotAdvanced_ShouldReturnFalse() {
        // When
        boolean result1 = advancedSearchStrategy.supports(SearchStrategy.SearchType.SIMPLE);
        boolean result2 = advancedSearchStrategy.supports(SearchStrategy.SearchType.BY_LOCATION);

        // Then
        assertFalse(result1);
        assertFalse(result2);
    }
}
