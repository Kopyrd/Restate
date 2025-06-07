package com.example.restate.service.impl;

import com.example.restate.config.IntegrationTestConfig;
import com.example.restate.dto.MieszkanieSearchCriteria;
import com.example.restate.dto.PageResponse;
import com.example.restate.dto.UpdateMieszkanieDTO;
import com.example.restate.entity.Mieszkanie;
import com.example.restate.exception.ResourceNotFoundException;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class MieszkanieServiceIntegrationTest extends IntegrationTestConfig {

    @Autowired
    private MieszkanieServiceImpl mieszkanieService;

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

    @Test
    void findAll_ShouldReturnAllMieszkania() {
        // When
        List<Mieszkanie> result = mieszkanieService.findAll();

        // Then
        assertEquals(3, result.size());
        assertTrue(result.stream().anyMatch(m -> m.getDeveloper().equals("LuxDev")));
        assertTrue(result.stream().anyMatch(m -> m.getDeveloper().equals("StandardDev")));
        assertTrue(result.stream().anyMatch(m -> m.getDeveloper().equals("BudgetDev")));
    }

    @Test
    void findAll_WithPagination_ShouldReturnPagedResults() {
        // Given
        Pageable pageable = PageRequest.of(0, 2, Sort.by("price").descending());

        // When
        PageResponse<Mieszkanie> result = mieszkanieService.findAll(pageable);

        // Then
        assertEquals(2, result.getContent().size());
        assertEquals(3, result.getTotalElements());
        assertEquals(2, result.getTotalPages());
        assertEquals("LuxDev", result.getContent().get(0).getDeveloper()); // Most expensive first
    }

    @Test
    void findById_ExistingId_ShouldReturnMieszkanie() {
        // Given
        Mieszkanie saved = mieszkanieRepository.findAll().get(0);

        // When
        Optional<Mieszkanie> result = mieszkanieService.findById(saved.getId());

        // Then
        assertTrue(result.isPresent());
        assertEquals(saved.getDeveloper(), result.get().getDeveloper());
    }

    @Test
    void findById_NonExistingId_ShouldReturnEmpty() {
        // When
        Optional<Mieszkanie> result = mieszkanieService.findById(999999);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void save_NewMieszkanie_ShouldSaveSuccessfully() {
        // Given
        Mieszkanie newMieszkanie = new Mieszkanie();
        newMieszkanie.setDeveloper("NewDev");
        newMieszkanie.setInvestment("New Investment");
        newMieszkanie.setNumber("D301");
        newMieszkanie.setArea(BigDecimal.valueOf(70.0));
        newMieszkanie.setPrice(BigDecimal.valueOf(500000));
        newMieszkanie.setStatus(Mieszkanie.Status.AVAILABLE);

        // When
        Mieszkanie saved = mieszkanieService.save(newMieszkanie);

        // Then
        assertNotNull(saved.getId());
        assertEquals("NewDev", saved.getDeveloper());
        assertEquals(4, mieszkanieRepository.count());
    }

    @Test
    void update_ExistingMieszkanie_ShouldUpdateSuccessfully() {
        // Given
        Mieszkanie existing = mieszkanieRepository.findAll().get(0);
        Integer id = existing.getId();

        Mieszkanie updated = new Mieszkanie();
        updated.setDeveloper("UpdatedDev");
        updated.setInvestment("Updated Investment");
        updated.setNumber(existing.getNumber());
        updated.setArea(existing.getArea());
        updated.setPrice(BigDecimal.valueOf(800000));
        updated.setStatus(existing.getStatus());
        updated.setDescription("Updated description");

        // When
        Mieszkanie result = mieszkanieService.update(id, updated);

        // Then
        assertEquals(id, result.getId());
        assertEquals("UpdatedDev", result.getDeveloper());
        assertEquals("Updated Investment", result.getInvestment());
        assertEquals(BigDecimal.valueOf(800000), result.getPrice());
        assertEquals("Updated description", result.getDescription());
    }

    @Test
    void update_NonExistingMieszkanie_ShouldThrowException() {
        // Given
        Mieszkanie updated = new Mieszkanie();
        updated.setDeveloper("UpdatedDev");
        updated.setInvestment("Updated Investment");

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            mieszkanieService.update(999999, updated);
        });
    }

    @Test
    void deleteById_ExistingId_ShouldDeleteSuccessfully() {
        // Given
        Mieszkanie toDelete = mieszkanieRepository.findAll().get(0);
        Integer id = toDelete.getId();

        // When
        mieszkanieService.deleteById(id);

        // Then
        assertEquals(2, mieszkanieRepository.count());
        assertFalse(mieszkanieRepository.existsById(id));
    }

    @Test
    void deleteById_NonExistingId_ShouldThrowException() {
        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            mieszkanieService.deleteById(999999);
        });
    }

    @Test
    void findByDeveloper_ExistingDeveloper_ShouldReturnMieszkania() {
        // When
        List<Mieszkanie> result = mieszkanieService.findByDeveloper("LuxDev");

        // Then
        assertEquals(1, result.size());
        assertEquals("Golden Heights", result.get(0).getInvestment());
    }

    @Test
    void findByDeveloper_WithPagination_ShouldReturnPagedResults() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        PageResponse<Mieszkanie> result = mieszkanieService.findByDeveloper("LuxDev", pageable);

        // Then
        assertEquals(1, result.getContent().size());
        assertEquals(1, result.getTotalElements());
        assertEquals("Golden Heights", result.getContent().get(0).getInvestment());
    }

    @Test
    void findByInvestment_ExistingInvestment_ShouldReturnMieszkania() {
        // When
        List<Mieszkanie> result = mieszkanieService.findByInvestment("City Park");

        // Then
        assertEquals(1, result.size());
        assertEquals("StandardDev", result.get(0).getDeveloper());
    }

    @Test
    void findByInvestment_WithPagination_ShouldReturnPagedResults() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        PageResponse<Mieszkanie> result = mieszkanieService.findByInvestment("City Park", pageable);

        // Then
        assertEquals(1, result.getContent().size());
        assertEquals(1, result.getTotalElements());
        assertEquals("StandardDev", result.getContent().get(0).getDeveloper());
    }

    @Test
    void findByPriceRange_ShouldReturnMieszkaniaInRange() {
        // When
        List<Mieszkanie> result = mieszkanieService.findByPriceRange(
                BigDecimal.valueOf(400000), BigDecimal.valueOf(500000));

        // Then
        assertEquals(1, result.size());
        assertEquals("StandardDev", result.get(0).getDeveloper());
    }

    @Test
    void findByPriceRange_WithPagination_ShouldReturnPagedResults() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        PageResponse<Mieszkanie> result = mieszkanieService.findByPriceRange(
                BigDecimal.valueOf(400000), BigDecimal.valueOf(500000), pageable);

        // Then
        assertEquals(1, result.getContent().size());
        assertEquals(1, result.getTotalElements());
        assertEquals("StandardDev", result.getContent().get(0).getDeveloper());
    }

    @Test
    void searchByCriteria_WithDeveloper_ShouldReturnFilteredResults() {
        // Given
        MieszkanieSearchCriteria criteria = MieszkanieSearchCriteria.builder()
                .developer("LuxDev")
                .build();

        // When
        List<Mieszkanie> result = mieszkanieService.searchByCriteria(criteria);

        // Then
        assertEquals(1, result.size());
        assertEquals("LuxDev", result.get(0).getDeveloper());
    }

    @Test
    void searchByCriteria_WithMultipleCriteria_ShouldReturnFilteredResults() {
        // Given
        MieszkanieSearchCriteria criteria = MieszkanieSearchCriteria.builder()
                .city("Warszawa")
                .minPrice(BigDecimal.valueOf(400000))
                .maxPrice(BigDecimal.valueOf(800000))
                .minArea(BigDecimal.valueOf(60.0))
                .build();

        // When
        List<Mieszkanie> result = mieszkanieService.searchByCriteria(criteria);

        // Then
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(m -> m.getDeveloper().equals("LuxDev")));
        assertTrue(result.stream().anyMatch(m -> m.getDeveloper().equals("StandardDev")));
    }

    @Test
    void searchByCriteria_WithPagination_ShouldReturnPagedResults() {
        // Given
        // First, let's get all apartments in Warsaw to understand what we're working with
        MieszkanieSearchCriteria criteria = MieszkanieSearchCriteria.builder()
                .city("Warszawa")
                .build();
        List<Mieszkanie> allWarszawaMieszkania = mieszkanieService.searchByCriteria(criteria);

        // Skip test if there are no apartments in Warsaw
        if (allWarszawaMieszkania.isEmpty()) {
            System.out.println("[DEBUG_LOG] No apartments in Warsaw, skipping test");
            return;
        }

        // Now test with pagination
        Pageable pageable = PageRequest.of(0, 1, Sort.by("price").descending());
        PageResponse<Mieszkanie> result = mieszkanieService.searchByCriteria(criteria, pageable);

        // Then
        assertEquals(1, result.getContent().size(), "Page size should be 1");
        assertTrue(result.getTotalElements() >= 1, "Should have at least one result");
        assertTrue(result.getTotalPages() >= 1, "Should have at least one page");

        // Find the most expensive apartment in Warsaw
        Mieszkanie mostExpensive = allWarszawaMieszkania.stream()
                .max((m1, m2) -> m1.getPrice().compareTo(m2.getPrice()))
                .orElseThrow();

        // Verify that the first result is the most expensive apartment
        assertEquals(mostExpensive.getId(), result.getContent().get(0).getId(), 
                "First result should be the most expensive apartment");
    }

    @Test
    void getAllDevelopers_ShouldReturnAllDevelopers() {
        // When
        List<String> result = mieszkanieService.getAllDevelopers();

        // Then
        assertTrue(result.contains("LuxDev"));
        assertTrue(result.contains("StandardDev"));
        assertTrue(result.contains("BudgetDev"));
    }

    @Test
    void changeStatus_ExistingId_ShouldUpdateStatus() {
        // Given
        Mieszkanie mieszkanie = mieszkanieRepository.findAll().get(0);
        Integer id = mieszkanie.getId();


        Mieszkanie result = mieszkanieService.changeStatus(id, Mieszkanie.Status.SOLD);


        assertEquals(Mieszkanie.Status.SOLD, result.getStatus());


        Mieszkanie updated = mieszkanieRepository.findById(id).orElse(null);
        assertNotNull(updated);
        assertEquals(Mieszkanie.Status.SOLD, updated.getStatus());
    }

    @Test
    void changeStatus_NonExistingId_ShouldThrowException() {

        assertThrows(ResourceNotFoundException.class, () -> {
            mieszkanieService.changeStatus(999999, Mieszkanie.Status.SOLD);
        });
    }

    @Test
    void updateFromDTO_ExistingId_ShouldUpdateFields() {
        // Given
        Mieszkanie existing = mieszkanieRepository.findAll().get(0);
        Integer id = existing.getId();

        UpdateMieszkanieDTO dto = new UpdateMieszkanieDTO();
        dto.setPrice(BigDecimal.valueOf(800000));
        dto.setDescription("Updated from DTO");
        dto.setCity("New City");

        Mieszkanie result = mieszkanieService.updateFromDTO(id, dto);

        assertEquals(id, result.getId());
        assertEquals(BigDecimal.valueOf(800000), result.getPrice());
        assertEquals("Updated from DTO", result.getDescription());
        assertEquals("New City", result.getCity());
        // Fields not in DTO should remain unchanged
        assertEquals(existing.getDeveloper(), result.getDeveloper());
    }

    @Test
    void updateFromDTO_NonExistingId_ShouldThrowException() {
        // Given
        UpdateMieszkanieDTO dto = new UpdateMieszkanieDTO();
        dto.setPrice(BigDecimal.valueOf(800000));

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            mieszkanieService.updateFromDTO(999999, dto);
        });
    }

@Test
void updateFromDTO_WithAllFields_ShouldUpdateAllFields() {
    // Given
    Mieszkanie existing = mieszkanieRepository.findAll().get(0);
    Integer id = existing.getId();
    
    UpdateMieszkanieDTO dto = UpdateMieszkanieDTO.builder()
            .developer("UpdatedDev")
            .investment("Updated Investment")
            .number("U001")
            .area(BigDecimal.valueOf(95.5))
            .price(BigDecimal.valueOf(850000))
            .voivodeship("Małopolskie")
            .city("Kraków")
            .district("Stare Miasto")
            .floor(15)
            .description("Completely updated apartment description")
            .build();

    // When
    Mieszkanie result = mieszkanieService.updateFromDTO(id, dto);

    // Then
    assertEquals("UpdatedDev", result.getDeveloper());
    assertEquals("Updated Investment", result.getInvestment());
    assertEquals("U001", result.getNumber());
    assertEquals(BigDecimal.valueOf(95.5), result.getArea());
    assertEquals(BigDecimal.valueOf(850000), result.getPrice());
    assertEquals("Małopolskie", result.getVoivodeship());
    assertEquals("Kraków", result.getCity());
    assertEquals("Stare Miasto", result.getDistrict());
    assertEquals(15, result.getFloor());
    assertEquals("Completely updated apartment description", result.getDescription());
    
    // Verify in database
    Mieszkanie fromDb = mieszkanieRepository.findById(id).orElseThrow();
    assertEquals("UpdatedDev", fromDb.getDeveloper());
    assertEquals("Kraków", fromDb.getCity());
}

@Test
void updateFromDTO_WithPartialFields_ShouldUpdateOnlySpecifiedFields() {
    // Given
    Mieszkanie existing = mieszkanieRepository.findAll().get(0);
    Integer id = existing.getId();
    String originalDeveloper = existing.getDeveloper();
    String originalCity = existing.getCity();
    
    UpdateMieszkanieDTO dto = UpdateMieszkanieDTO.builder()
            .price(BigDecimal.valueOf(999999))
            .description("Only price and description updated")
            .build();

    // When
    Mieszkanie result = mieszkanieService.updateFromDTO(id, dto);

    // Then
    assertEquals(BigDecimal.valueOf(999999), result.getPrice());
    assertEquals("Only price and description updated", result.getDescription());
    
    // Unchanged fields should remain the same
    assertEquals(originalDeveloper, result.getDeveloper());
    assertEquals(originalCity, result.getCity());
    assertEquals(existing.getInvestment(), result.getInvestment());
    assertEquals(existing.getArea(), result.getArea());
}

@Test
void updateFromDTO_WithNullFields_ShouldNotUpdateNullFields() {
    // Given
    Mieszkanie existing = mieszkanieRepository.findAll().get(0);
    Integer id = existing.getId();
    String originalDeveloper = existing.getDeveloper();
    BigDecimal originalPrice = existing.getPrice();
    
    UpdateMieszkanieDTO dto = UpdateMieszkanieDTO.builder()
            .developer(null)
            .investment(null)
            .number(null)
            .area(null)
            .price(null)
            .voivodeship(null)
            .city(null)
            .district(null)
            .floor(null)
            .description("Only description changed")
            .build();

    // When
    Mieszkanie result = mieszkanieService.updateFromDTO(id, dto);

    // Then
    assertEquals("Only description changed", result.getDescription());
    
    // All other fields should remain unchanged
    assertEquals(originalDeveloper, result.getDeveloper());
    assertEquals(originalPrice, result.getPrice());
    assertEquals(existing.getInvestment(), result.getInvestment());
    assertEquals(existing.getCity(), result.getCity());
}

@Test
void updateFromDTO_WithEmptyDTO_ShouldNotChangeAnything() {
    // Given
    Mieszkanie existing = mieszkanieRepository.findAll().get(0);
    Integer id = existing.getId();
    String originalDeveloper = existing.getDeveloper();
    BigDecimal originalPrice = existing.getPrice();
    
    UpdateMieszkanieDTO dto = UpdateMieszkanieDTO.builder().build();

    // When
    Mieszkanie result = mieszkanieService.updateFromDTO(id, dto);

    assertEquals(originalDeveloper, result.getDeveloper());
    assertEquals(originalPrice, result.getPrice());
    assertEquals(existing.getInvestment(), result.getInvestment());
    assertEquals(existing.getArea(), result.getArea());
    assertEquals(existing.getCity(), result.getCity());
}

@Test
void updateFromDTO_WithNonExistentId_ShouldThrowResourceNotFoundException() {
    UpdateMieszkanieDTO dto = UpdateMieszkanieDTO.builder()
            .developer("Test Developer")
            .build();

    ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> mieszkanieService.updateFromDTO(999999, dto)
    );
    
    assertTrue(exception.getMessage().contains("Mieszkanie o ID 999999 nie znalezione"));
}

@Test
void updateFromDTO_WithZeroValues_ShouldUpdateToZeroValues() {
    Mieszkanie existing = mieszkanieRepository.findAll().get(0);
    Integer id = existing.getId();
    
    UpdateMieszkanieDTO dto = UpdateMieszkanieDTO.builder()
            .floor(0)
            .area(BigDecimal.valueOf(0.1))
            .price(BigDecimal.valueOf(1))
            .build();

    // When
    Mieszkanie result = mieszkanieService.updateFromDTO(id, dto);

    // Then
    assertEquals(0, result.getFloor());
    assertEquals(BigDecimal.valueOf(0.1), result.getArea());
    assertEquals(BigDecimal.valueOf(1), result.getPrice());
}


@Test
void searchByCriteria_WithDeveloperOnly_ShouldReturnMatchingMieszkania() {
    // Given
    MieszkanieSearchCriteria criteria = MieszkanieSearchCriteria.builder()
            .developer("LuxDev")
            .build();

    // When
    List<Mieszkanie> result = mieszkanieService.searchByCriteria(criteria);

    // Then
    assertEquals(1, result.size());
    assertEquals("LuxDev", result.get(0).getDeveloper());
}

@Test
void searchByCriteria_WithInvestmentOnly_ShouldReturnMatchingMieszkania() {
    // Given
    MieszkanieSearchCriteria criteria = MieszkanieSearchCriteria.builder()
            .investment("City Park")
            .build();

    // When
    List<Mieszkanie> result = mieszkanieService.searchByCriteria(criteria);

    // Then
    assertEquals(1, result.size());
    assertEquals("City Park", result.get(0).getInvestment());
}

@Test
void searchByCriteria_WithPriceRange_ShouldReturnMieszkaniaInRange() {
    // Given
    MieszkanieSearchCriteria criteria = MieszkanieSearchCriteria.builder()
            .minPrice(BigDecimal.valueOf(400000))
            .maxPrice(BigDecimal.valueOf(500000))
            .build();

    // When
    List<Mieszkanie> result = mieszkanieService.searchByCriteria(criteria);

    // Then
    assertEquals(1, result.size());
    assertTrue(result.get(0).getPrice().compareTo(BigDecimal.valueOf(400000)) >= 0);
    assertTrue(result.get(0).getPrice().compareTo(BigDecimal.valueOf(500000)) <= 0);
}

@Test
void searchByCriteria_WithAreaRange_ShouldReturnMieszkaniaInRange() {
    // Given
    MieszkanieSearchCriteria criteria = MieszkanieSearchCriteria.builder()
            .minArea(BigDecimal.valueOf(60.0))
            .maxArea(BigDecimal.valueOf(90.0))
            .build();

    List<Mieszkanie> result = mieszkanieService.searchByCriteria(criteria);

    assertEquals(2, result.size()); // LuxDev (85.5) and StandardDev (62.0)
    assertTrue(result.stream().allMatch(m -> 
        m.getArea().compareTo(BigDecimal.valueOf(60.0)) >= 0 && 
        m.getArea().compareTo(BigDecimal.valueOf(90.0)) <= 0));
}

@Test
void searchByCriteria_WithMinPriceOnly_ShouldReturnMieszkaniaAboveMinPrice() {
    // Given
    MieszkanieSearchCriteria criteria = MieszkanieSearchCriteria.builder()
            .minPrice(BigDecimal.valueOf(500000))
            .build();


    List<Mieszkanie> result = mieszkanieService.searchByCriteria(criteria);

    assertEquals(1, result.size());
    assertEquals("LuxDev", result.get(0).getDeveloper());
    assertTrue(result.get(0).getPrice().compareTo(BigDecimal.valueOf(500000)) >= 0);
}

@Test
void searchByCriteria_WithMaxPriceOnly_ShouldReturnMieszkaniaBelowMaxPrice() {
    MieszkanieSearchCriteria criteria = MieszkanieSearchCriteria.builder()
            .maxPrice(BigDecimal.valueOf(300000))
            .build();


    List<Mieszkanie> result = mieszkanieService.searchByCriteria(criteria);

    assertEquals(1, result.size());
    assertEquals("BudgetDev", result.get(0).getDeveloper());
    assertTrue(result.get(0).getPrice().compareTo(BigDecimal.valueOf(300000)) <= 0);
}

@Test
void searchByCriteria_WithMinAreaOnly_ShouldReturnMieszkaniaAboveMinArea() {
    // Given
    MieszkanieSearchCriteria criteria = MieszkanieSearchCriteria.builder()
            .minArea(BigDecimal.valueOf(80.0))
            .build();

    List<Mieszkanie> result = mieszkanieService.searchByCriteria(criteria);

    assertEquals(1, result.size());
    assertEquals("LuxDev", result.get(0).getDeveloper());
    assertTrue(result.get(0).getArea().compareTo(BigDecimal.valueOf(80.0)) >= 0);
}

@Test
void searchByCriteria_WithMaxAreaOnly_ShouldReturnMieszkaniaBelowMaxArea() {
    // Given
    MieszkanieSearchCriteria criteria = MieszkanieSearchCriteria.builder()
            .maxArea(BigDecimal.valueOf(50.0))
            .build();

    // When
    List<Mieszkanie> result = mieszkanieService.searchByCriteria(criteria);

    // Then
    assertEquals(1, result.size());
    assertEquals("BudgetDev", result.get(0).getDeveloper());
    assertTrue(result.get(0).getArea().compareTo(BigDecimal.valueOf(50.0)) <= 0);
}

@Test
void searchByCriteria_WithMultipleCriteria_ShouldReturnMatchingMieszkania() {
    // Given
    MieszkanieSearchCriteria criteria = MieszkanieSearchCriteria.builder()
            .developer("LuxDev")
            .minPrice(BigDecimal.valueOf(700000))
            .minArea(BigDecimal.valueOf(80.0))
            .build();

    // When
    List<Mieszkanie> result = mieszkanieService.searchByCriteria(criteria);

    // Then
    assertEquals(1, result.size());
    Mieszkanie found = result.get(0);
    assertEquals("LuxDev", found.getDeveloper());
    assertTrue(found.getPrice().compareTo(BigDecimal.valueOf(700000)) >= 0);
    assertTrue(found.getArea().compareTo(BigDecimal.valueOf(80.0)) >= 0);
}

@Test
void searchByCriteria_WithEmptyCriteria_ShouldReturnAllMieszkania() {
    MieszkanieSearchCriteria criteria = MieszkanieSearchCriteria.builder().build();


    List<Mieszkanie> result = mieszkanieService.searchByCriteria(criteria);


    assertEquals(3, result.size());
}

@Test
void searchByCriteria_WithEmptyStringCriteria_ShouldIgnoreEmptyStrings() {
    // Given
    MieszkanieSearchCriteria criteria = MieszkanieSearchCriteria.builder()
            .developer("")
            .investment("")
            .minPrice(BigDecimal.valueOf(400000))
            .build();


    List<Mieszkanie> result = mieszkanieService.searchByCriteria(criteria);


    assertEquals(2, result.size()); // Should ignore empty developer and investment
    assertTrue(result.stream().allMatch(m -> 
        m.getPrice().compareTo(BigDecimal.valueOf(400000)) >= 0));
}

@Test
void searchByCriteria_WithNullStringCriteria_ShouldIgnoreNullStrings() {
    // Given
    MieszkanieSearchCriteria criteria = MieszkanieSearchCriteria.builder()
            .developer(null)
            .investment(null)
            .maxPrice(BigDecimal.valueOf(500000))
            .build();


    List<Mieszkanie> result = mieszkanieService.searchByCriteria(criteria);


    assertEquals(2, result.size());
    assertTrue(result.stream().allMatch(m -> 
        m.getPrice().compareTo(BigDecimal.valueOf(500000)) <= 0));
}


@Test
void searchByCriteria_WithStrictRangeFilters_ShouldReturnNoResults() {
    // Given - impossible criteria
    MieszkanieSearchCriteria criteria = MieszkanieSearchCriteria.builder()
            .minPrice(BigDecimal.valueOf(1000000))
            .maxPrice(BigDecimal.valueOf(2000000))
            .build();

    List<Mieszkanie> result = mieszkanieService.searchByCriteria(criteria);

    assertEquals(0, result.size());
}

@Test
void searchByCriteria_WithInvalidDeveloper_ShouldReturnNoResults() {
    MieszkanieSearchCriteria criteria = MieszkanieSearchCriteria.builder()
            .developer("NonExistentDeveloper")
            .build();

    List<Mieszkanie> result = mieszkanieService.searchByCriteria(criteria);

    assertEquals(0, result.size());
}
}