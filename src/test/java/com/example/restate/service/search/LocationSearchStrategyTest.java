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
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LocationSearchStrategyTest {

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
    private LocationSearchStrategy locationSearchStrategy;

    private Pageable pageable;
    private List<Mieszkanie> mieszkanieList;

    @BeforeEach
    void setUp() {
        // Setup test data
        Mieszkanie mieszkanie1 = new Mieszkanie();
        mieszkanie1.setId(1);
        mieszkanie1.setCity("Warsaw");
        mieszkanie1.setDistrict("Mokotow");
        mieszkanie1.setVoivodeship("Mazowieckie");
        mieszkanie1.setArea(BigDecimal.valueOf(75.5));
        mieszkanie1.setPrice(BigDecimal.valueOf(500000));

        Mieszkanie mieszkanie2 = new Mieszkanie();
        mieszkanie2.setId(2);
        mieszkanie2.setCity("Krakow");
        mieszkanie2.setDistrict("Nowa Huta");
        mieszkanie2.setVoivodeship("Malopolskie");
        mieszkanie2.setArea(BigDecimal.valueOf(100.0));
        mieszkanie2.setPrice(BigDecimal.valueOf(750000));

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
    }

    @Test
    void search_WithVoivodeshipCriteria_ShouldCreateCorrectPredicate() {
        // Given
        MieszkanieSearchCriteria criteria = MieszkanieSearchCriteria.builder()
                .voivodeship("Mazowieckie")
                .build();

        // When
        PageResponse<Mieszkanie> result = locationSearchStrategy.search(criteria, pageable);

        // Then
        verify(criteriaQuery).where(predicatesCaptor.capture());
        Predicate[] predicates = predicatesCaptor.getValue();
        assertEquals(1, predicates.length);

        verify(root, atLeastOnce()).get("voivodeship");
        verify(criteriaBuilder, atLeastOnce()).equal(any(), eq("Mazowieckie"));

        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
    }

    @Test
    void search_WithCityCriteria_ShouldCreateCorrectPredicate() {
        // Given
        MieszkanieSearchCriteria criteria = MieszkanieSearchCriteria.builder()
                .city("Warsaw")
                .build();

        // When
        PageResponse<Mieszkanie> result = locationSearchStrategy.search(criteria, pageable);

        // Then
        verify(criteriaQuery).where(predicatesCaptor.capture());
        Predicate[] predicates = predicatesCaptor.getValue();
        assertEquals(1, predicates.length);

        verify(root, atLeastOnce()).get("city");
        verify(criteriaBuilder, atLeastOnce()).equal(any(), eq("Warsaw"));

        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
    }

    @Test
    void search_WithDistrictCriteria_ShouldCreateCorrectPredicate() {
        // Given
        MieszkanieSearchCriteria criteria = MieszkanieSearchCriteria.builder()
                .district("Mokotow")
                .build();

        // When
        PageResponse<Mieszkanie> result = locationSearchStrategy.search(criteria, pageable);

        // Then
        verify(criteriaQuery).where(predicatesCaptor.capture());
        Predicate[] predicates = predicatesCaptor.getValue();
        assertEquals(1, predicates.length);

        verify(root, atLeastOnce()).get("district");
        verify(criteriaBuilder, atLeastOnce()).equal(any(), eq("Mokotow"));

        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
    }

    @Test
    void search_WithMultipleLocationCriteria_ShouldCreateCorrectPredicates() {
        // Given
        MieszkanieSearchCriteria criteria = MieszkanieSearchCriteria.builder()
                .voivodeship("Mazowieckie")
                .city("Warsaw")
                .district("Mokotow")
                .build();

        // When
        PageResponse<Mieszkanie> result = locationSearchStrategy.search(criteria, pageable);

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
    void search_WithNoCriteria_ShouldReturnEmptyPredicates() {
        // Given
        MieszkanieSearchCriteria criteria = MieszkanieSearchCriteria.builder().build();

        // When
        PageResponse<Mieszkanie> result = locationSearchStrategy.search(criteria, pageable);

        // Then
        verify(criteriaQuery).where(predicatesCaptor.capture());
        Predicate[] predicates = predicatesCaptor.getValue();
        assertEquals(0, predicates.length);

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
        PageResponse<Mieszkanie> result = locationSearchStrategy.search(criteria, pageable);

        // Then
        verify(typedQuery).setFirstResult(5);
        verify(typedQuery).setMaxResults(5);

        assertEquals(2, result.getTotalElements());
        assertEquals(0, result.getContent().size());
    }

    @Test
    void supports_WhenSearchTypeIsByLocation_ShouldReturnTrue() {
        // When
        boolean result = locationSearchStrategy.supports(SearchStrategy.SearchType.BY_LOCATION);

        // Then
        assertTrue(result);
    }

    @Test
    void supports_WhenSearchTypeIsNotByLocation_ShouldReturnFalse() {
        // When
        boolean result1 = locationSearchStrategy.supports(SearchStrategy.SearchType.SIMPLE);
        boolean result2 = locationSearchStrategy.supports(SearchStrategy.SearchType.ADVANCED);

        // Then
        assertFalse(result1);
        assertFalse(result2);
    }

    @Test
    void search_WithMixedCriteria_ShouldCreateCorrectPredicates() {
        // Given - criteria with both location and non-location fields
        MieszkanieSearchCriteria criteria = MieszkanieSearchCriteria.builder()
                .city("Warsaw")
                .voivodeship("Mazowieckie")
                .developer("Test Developer") // This would normally be handled by SimpleSearchStrategy
                .minPrice(BigDecimal.valueOf(300000)) // This would normally be handled by AdvancedSearchStrategy
                .build();

        // When
        PageResponse<Mieszkanie> result = locationSearchStrategy.search(criteria, pageable);

        // Then
        verify(criteriaQuery).where(predicatesCaptor.capture());
        Predicate[] predicates = predicatesCaptor.getValue();
        // Only location criteria should be used in this strategy
        assertEquals(2, predicates.length);

        verify(root, atLeastOnce()).get("city");
        verify(root, atLeastOnce()).get("voivodeship");
        verify(root, never()).get("developer");
        verify(root, never()).get("price");

        verify(criteriaBuilder, atLeastOnce()).equal(any(), eq("Warsaw"));
        verify(criteriaBuilder, atLeastOnce()).equal(any(), eq("Mazowieckie"));

        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
    }
}
