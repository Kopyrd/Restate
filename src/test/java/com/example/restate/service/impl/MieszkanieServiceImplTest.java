package com.example.restate.service.impl;

import com.example.restate.dto.MieszkanieSearchCriteria;
import com.example.restate.dto.PageResponse;
import com.example.restate.dto.UpdateMieszkanieDTO;
import com.example.restate.entity.Mieszkanie;
import com.example.restate.exception.ResourceNotFoundException;
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

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MieszkanieServiceImplTest {

    @Mock
    private MieszkanieRepository mieszkanieRepository;

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

    @Mock
    private Path<Object> path;

    @InjectMocks
    private MieszkanieServiceImpl mieszkanieService;

    private Mieszkanie mieszkanie1;
    private Mieszkanie mieszkanie2;
    private List<Mieszkanie> mieszkanieList;
    private Page<Mieszkanie> mieszkaniePage;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        // Setup test data
        mieszkanie1 = new Mieszkanie();
        mieszkanie1.setId(1);
        mieszkanie1.setDeveloper("Test Developer");
        mieszkanie1.setInvestment("Test Investment");
        mieszkanie1.setNumber("A1");
        mieszkanie1.setArea(BigDecimal.valueOf(75.5));
        mieszkanie1.setPrice(BigDecimal.valueOf(500000));
        mieszkanie1.setVoivodeship("Test Voivodeship");
        mieszkanie1.setCity("Test City");
        mieszkanie1.setDistrict("Test District");
        mieszkanie1.setFloor(2);
        mieszkanie1.setDescription("Test Description");
        mieszkanie1.setStatus(Mieszkanie.Status.AVAILABLE);

        mieszkanie2 = new Mieszkanie();
        mieszkanie2.setId(2);
        mieszkanie2.setDeveloper("Another Developer");
        mieszkanie2.setInvestment("Another Investment");
        mieszkanie2.setNumber("B2");
        mieszkanie2.setArea(BigDecimal.valueOf(100.0));
        mieszkanie2.setPrice(BigDecimal.valueOf(750000));
        mieszkanie2.setVoivodeship("Another Voivodeship");
        mieszkanie2.setCity("Another City");
        mieszkanie2.setDistrict("Another District");
        mieszkanie2.setFloor(3);
        mieszkanie2.setDescription("Another Description");
        mieszkanie2.setStatus(Mieszkanie.Status.RESERVED);

        mieszkanieList = Arrays.asList(mieszkanie1, mieszkanie2);
        pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));
        mieszkaniePage = new PageImpl<>(mieszkanieList, pageable, mieszkanieList.size());
    }

    @Test
    void findAll_ShouldReturnAllMieszkania() {
        // Given
        when(mieszkanieRepository.findAll()).thenReturn(mieszkanieList);

        // When
        List<Mieszkanie> result = mieszkanieService.findAll();

        // Then
        assertEquals(2, result.size());
        assertEquals(mieszkanie1.getId(), result.get(0).getId());
        assertEquals(mieszkanie2.getId(), result.get(1).getId());
        verify(mieszkanieRepository, times(1)).findAll();
    }

    @Test
    void findAll_WithPageable_ShouldReturnPageOfMieszkania() {
        // Given
        when(mieszkanieRepository.findAll(pageable)).thenReturn(mieszkaniePage);

        // When
        PageResponse<Mieszkanie> result = mieszkanieService.findAll(pageable);

        // Then
        assertEquals(2, result.getContent().size());
        assertEquals(0, result.getPageNumber());
        assertEquals(10, result.getPageSize());
        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        verify(mieszkanieRepository, times(1)).findAll(pageable);
    }

    @Test
    void findById_WhenMieszkanieExists_ShouldReturnMieszkanie() {
        // Given
        when(mieszkanieRepository.findById(1)).thenReturn(Optional.of(mieszkanie1));

        // When
        Optional<Mieszkanie> result = mieszkanieService.findById(1);

        // Then
        assertTrue(result.isPresent());
        assertEquals(mieszkanie1.getId(), result.get().getId());
        verify(mieszkanieRepository, times(1)).findById(1);
    }

    @Test
    void findById_WhenMieszkanieDoesNotExist_ShouldReturnEmpty() {
        // Given
        when(mieszkanieRepository.findById(999)).thenReturn(Optional.empty());

        // When
        Optional<Mieszkanie> result = mieszkanieService.findById(999);

        // Then
        assertFalse(result.isPresent());
        verify(mieszkanieRepository, times(1)).findById(999);
    }

    @Test
    void save_ShouldSaveAndReturnMieszkanie() {
        // Given
        when(mieszkanieRepository.save(mieszkanie1)).thenReturn(mieszkanie1);

        // When
        Mieszkanie result = mieszkanieService.save(mieszkanie1);

        // Then
        assertEquals(mieszkanie1.getId(), result.getId());
        verify(mieszkanieRepository, times(1)).save(mieszkanie1);
    }

    @Test
    void update_WhenMieszkanieExists_ShouldUpdateAndReturnMieszkanie() {
        // Given
        Mieszkanie updatedMieszkanie = new Mieszkanie();
        updatedMieszkanie.setDeveloper("Updated Developer");
        updatedMieszkanie.setPrice(BigDecimal.valueOf(600000));

        when(mieszkanieRepository.findById(1)).thenReturn(Optional.of(mieszkanie1));
        when(mieszkanieRepository.save(any(Mieszkanie.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Mieszkanie result = mieszkanieService.update(1, updatedMieszkanie);

        // Then
        assertEquals(1, result.getId());
        assertEquals("Updated Developer", result.getDeveloper());
        assertEquals(BigDecimal.valueOf(600000), result.getPrice());
        verify(mieszkanieRepository, times(1)).findById(1);
        verify(mieszkanieRepository, times(1)).save(any(Mieszkanie.class));
    }

    @Test
    void update_WhenMieszkanieDoesNotExist_ShouldThrowException() {
        // Given
        when(mieszkanieRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> mieszkanieService.update(999, mieszkanie1));
        verify(mieszkanieRepository, times(1)).findById(999);
        verify(mieszkanieRepository, never()).save(any(Mieszkanie.class));
    }

    @Test
    void deleteById_WhenMieszkanieExists_ShouldDeleteMieszkanie() {
        // Given
        when(mieszkanieRepository.existsById(1)).thenReturn(true);
        doNothing().when(mieszkanieRepository).deleteById(1);

        // When
        mieszkanieService.deleteById(1);

        // Then
        verify(mieszkanieRepository, times(1)).existsById(1);
        verify(mieszkanieRepository, times(1)).deleteById(1);
    }

    @Test
    void deleteById_WhenMieszkanieDoesNotExist_ShouldThrowException() {
        // Given
        when(mieszkanieRepository.existsById(999)).thenReturn(false);

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> mieszkanieService.deleteById(999));
        verify(mieszkanieRepository, times(1)).existsById(999);
        verify(mieszkanieRepository, never()).deleteById(anyInt());
    }

    @Test
    void findByDeveloper_ShouldReturnMieszkaniaByDeveloper() {
        // Given
        when(mieszkanieRepository.findByDeveloper("Test Developer")).thenReturn(List.of(mieszkanie1));

        // When
        List<Mieszkanie> result = mieszkanieService.findByDeveloper("Test Developer");

        // Then
        assertEquals(1, result.size());
        assertEquals("Test Developer", result.get(0).getDeveloper());
        verify(mieszkanieRepository, times(1)).findByDeveloper("Test Developer");
    }

    @Test
    void findByDeveloper_WithPageable_ShouldReturnPageOfMieszkaniaByDeveloper() {
        // Given
        Page<Mieszkanie> page = new PageImpl<>(List.of(mieszkanie1), pageable, 1);
        when(mieszkanieRepository.findByDeveloper("Test Developer", pageable)).thenReturn(page);

        // When
        PageResponse<Mieszkanie> result = mieszkanieService.findByDeveloper("Test Developer", pageable);

        // Then
        assertEquals(1, result.getContent().size());
        assertEquals("Test Developer", result.getContent().get(0).getDeveloper());
        verify(mieszkanieRepository, times(1)).findByDeveloper("Test Developer", pageable);
    }

    @Test
    void findByInvestment_ShouldReturnMieszkaniaByInvestment() {
        // Given
        when(mieszkanieRepository.findByInvestment("Test Investment")).thenReturn(List.of(mieszkanie1));

        // When
        List<Mieszkanie> result = mieszkanieService.findByInvestment("Test Investment");

        // Then
        assertEquals(1, result.size());
        assertEquals("Test Investment", result.get(0).getInvestment());
        verify(mieszkanieRepository, times(1)).findByInvestment("Test Investment");
    }

    @Test
    void findByInvestment_WithPageable_ShouldReturnPageOfMieszkaniaByInvestment() {
        // Given
        Page<Mieszkanie> page = new PageImpl<>(List.of(mieszkanie1), pageable, 1);
        when(mieszkanieRepository.findByInvestment("Test Investment", pageable)).thenReturn(page);

        // When
        PageResponse<Mieszkanie> result = mieszkanieService.findByInvestment("Test Investment", pageable);

        // Then
        assertEquals(1, result.getContent().size());
        assertEquals("Test Investment", result.getContent().get(0).getInvestment());
        verify(mieszkanieRepository, times(1)).findByInvestment("Test Investment", pageable);
    }

    @Test
    void findByPriceRange_ShouldReturnMieszkaniaByPriceRange() {
        // Given
        BigDecimal minPrice = BigDecimal.valueOf(400000);
        BigDecimal maxPrice = BigDecimal.valueOf(600000);
        when(mieszkanieRepository.findByPriceRange(minPrice, maxPrice)).thenReturn(List.of(mieszkanie1));

        // When
        List<Mieszkanie> result = mieszkanieService.findByPriceRange(minPrice, maxPrice);

        // Then
        assertEquals(1, result.size());
        assertEquals(BigDecimal.valueOf(500000), result.get(0).getPrice());
        verify(mieszkanieRepository, times(1)).findByPriceRange(minPrice, maxPrice);
    }

    @Test
    void findByPriceRange_WithPageable_ShouldReturnPageOfMieszkaniaByPriceRange() {
        // Given
        BigDecimal minPrice = BigDecimal.valueOf(400000);
        BigDecimal maxPrice = BigDecimal.valueOf(600000);
        Page<Mieszkanie> page = new PageImpl<>(List.of(mieszkanie1), pageable, 1);
        when(mieszkanieRepository.findByPriceBetween(minPrice, maxPrice, pageable)).thenReturn(page);

        // When
        PageResponse<Mieszkanie> result = mieszkanieService.findByPriceRange(minPrice, maxPrice, pageable);

        // Then
        assertEquals(1, result.getContent().size());
        assertEquals(BigDecimal.valueOf(500000), result.getContent().get(0).getPrice());
        verify(mieszkanieRepository, times(1)).findByPriceBetween(minPrice, maxPrice, pageable);
    }

    @Test
    void changeStatus_WhenMieszkanieExists_ShouldChangeStatusAndReturnMieszkanie() {
        // Given
        when(mieszkanieRepository.findById(1)).thenReturn(Optional.of(mieszkanie1));
        when(mieszkanieRepository.save(any(Mieszkanie.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Mieszkanie result = mieszkanieService.changeStatus(1, Mieszkanie.Status.SOLD);

        // Then
        assertEquals(Mieszkanie.Status.SOLD, result.getStatus());
        verify(mieszkanieRepository, times(1)).findById(1);
        verify(mieszkanieRepository, times(1)).save(any(Mieszkanie.class));
    }

    @Test
    void changeStatus_WhenMieszkanieDoesNotExist_ShouldThrowException() {
        // Given
        when(mieszkanieRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> mieszkanieService.changeStatus(999, Mieszkanie.Status.SOLD));
        verify(mieszkanieRepository, times(1)).findById(999);
        verify(mieszkanieRepository, never()).save(any(Mieszkanie.class));
    }

    @Test
    void updateFromDTO_WhenMieszkanieExists_ShouldUpdateAndReturnMieszkanie() {
        // Given
        UpdateMieszkanieDTO dto = new UpdateMieszkanieDTO();
        dto.setDeveloper("Updated Developer");
        dto.setPrice(BigDecimal.valueOf(600000));

        when(mieszkanieRepository.findById(1)).thenReturn(Optional.of(mieszkanie1));
        when(mieszkanieRepository.save(any(Mieszkanie.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Mieszkanie result = mieszkanieService.updateFromDTO(1, dto);

        // Then
        assertEquals("Updated Developer", result.getDeveloper());
        assertEquals(BigDecimal.valueOf(600000), result.getPrice());
        verify(mieszkanieRepository, times(1)).findById(1);
        verify(mieszkanieRepository, times(1)).save(any(Mieszkanie.class));
    }

    @Test
    void updateFromDTO_WhenMieszkanieDoesNotExist_ShouldThrowException() {
        // Given
        UpdateMieszkanieDTO dto = new UpdateMieszkanieDTO();
        when(mieszkanieRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> mieszkanieService.updateFromDTO(999, dto));
        verify(mieszkanieRepository, times(1)).findById(999);
        verify(mieszkanieRepository, never()).save(any(Mieszkanie.class));
    }

    @Test
    void getAllDevelopers_ShouldReturnAllDevelopers() {
        // Given
        List<String> developers = Arrays.asList("Test Developer", "Another Developer");
        when(mieszkanieRepository.findAllDevelopers()).thenReturn(developers);

        // When
        List<String> result = mieszkanieService.getAllDevelopers();

        // Then
        assertEquals(2, result.size());
        assertEquals("Test Developer", result.get(0));
        assertEquals("Another Developer", result.get(1));
        verify(mieszkanieRepository, times(1)).findAllDevelopers();
    }
}