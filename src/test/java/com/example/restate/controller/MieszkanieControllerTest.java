package com.example.restate.controller;

import com.example.restate.dto.CreateMieszkanieDTO;
import com.example.restate.dto.MieszkanieDTO;
import com.example.restate.dto.MieszkanieSearchCriteria;
import com.example.restate.dto.PageResponse;
import com.example.restate.dto.UpdateMieszkanieDTO;
import com.example.restate.entity.Mieszkanie;
import com.example.restate.service.MieszkanieService;
import com.example.restate.service.search.SearchContext;
import com.example.restate.service.search.SearchStrategy;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import com.example.restate.config.WebMvcTestConfig;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MieszkanieController.class)
@Import(WebMvcTestConfig.class)
class MieszkanieControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MieszkanieService mieszkanieService;

    @MockBean
    private SearchContext searchContext;

    @Autowired
    private ObjectMapper objectMapper;

    private Mieszkanie mieszkanie;
    private MieszkanieDTO mieszkanieDTO;
    private List<Mieszkanie> mieszkanieList;
    private PageResponse<Mieszkanie> pageResponse;

    @BeforeEach
    void setUp() {
        // Setup test data
        mieszkanie = new Mieszkanie();
        mieszkanie.setId(1);
        mieszkanie.setDeveloper("Test Developer");
        mieszkanie.setInvestment("Test Investment");
        mieszkanie.setNumber("A1");
        mieszkanie.setArea(BigDecimal.valueOf(75.5));
        mieszkanie.setPrice(BigDecimal.valueOf(500000));
        mieszkanie.setVoivodeship("Test Voivodeship");
        mieszkanie.setCity("Test City");
        mieszkanie.setDistrict("Test District");
        mieszkanie.setFloor(2);
        mieszkanie.setDescription("Test Description");
        mieszkanie.setStatus(Mieszkanie.Status.AVAILABLE);

        mieszkanieDTO = MieszkanieDTO.fromEntity(mieszkanie);

        mieszkanieList = new ArrayList<>();
        mieszkanieList.add(mieszkanie);

        pageResponse = PageResponse.<Mieszkanie>builder()
                .content(mieszkanieList)
                .pageNumber(0)
                .pageSize(10)
                .totalElements(1L)
                .totalPages(1)
                .last(true)
                .first(true)
                .build();
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllMieszkania_ShouldReturnPageOfMieszkania() throws Exception {
        // Given
        when(mieszkanieService.findAll(any(Pageable.class))).thenReturn(pageResponse);

        // When & Then
        mockMvc.perform(get("/api/mieszkania")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.content[0].developer", is("Test Developer")))
                .andExpect(jsonPath("$.pageNumber", is(0)))
                .andExpect(jsonPath("$.totalElements", is(1)));

        verify(mieszkanieService, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllMieszkania_WithCustomPaginationAndSorting_ShouldReturnPageOfMieszkania() throws Exception {
        // Given
        PageRequest customPageable = PageRequest.of(2, 5, Sort.by(Sort.Direction.DESC, "price"));

        PageResponse<Mieszkanie> customPageResponse = PageResponse.<Mieszkanie>builder()
                .content(mieszkanieList)
                .pageNumber(2)
                .pageSize(5)
                .totalElements(11L)
                .totalPages(3)
                .last(false)
                .first(false)
                .build();

        when(mieszkanieService.findAll(any(Pageable.class))).thenReturn(customPageResponse);

        // When & Then
        mockMvc.perform(get("/api/mieszkania")
                        .param("page", "2")
                        .param("size", "5")
                        .param("sortBy", "price")
                        .param("sortDir", "desc")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.pageNumber", is(2)))
                .andExpect(jsonPath("$.pageSize", is(5)))
                .andExpect(jsonPath("$.totalElements", is(11)))
                .andExpect(jsonPath("$.totalPages", is(3)))
                .andExpect(jsonPath("$.last", is(false)))
                .andExpect(jsonPath("$.first", is(false)));

        verify(mieszkanieService, times(1)).findAll(eq(customPageable));
    }

    @Test
    void getMieszkanieById_WhenMieszkanieExists_ShouldReturnMieszkanie() throws Exception {
        // Given
        when(mieszkanieService.findById(1)).thenReturn(Optional.of(mieszkanie));

        // When & Then
        mockMvc.perform(get("/api/mieszkania/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.developer", is("Test Developer")))
                .andExpect(jsonPath("$.investment", is("Test Investment")));

        verify(mieszkanieService, times(1)).findById(1);
    }

    @Test
    void getMieszkanieById_WhenMieszkanieDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Given
        when(mieszkanieService.findById(999)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/mieszkania/999")
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(mieszkanieService, times(1)).findById(999);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createMieszkanie_ShouldCreateAndReturnMieszkanie() throws Exception {
        // Given
        CreateMieszkanieDTO createDTO = new CreateMieszkanieDTO();
        createDTO.setDeveloper("Test Developer");
        createDTO.setInvestment("Test Investment");
        createDTO.setNumber("A1");
        createDTO.setArea(BigDecimal.valueOf(75.5));
        createDTO.setPrice(BigDecimal.valueOf(500000));
        createDTO.setVoivodeship("Test Voivodeship");
        createDTO.setCity("Test City");
        createDTO.setDistrict("Test District");
        createDTO.setFloor(2);
        createDTO.setDescription("Test Description");

        when(mieszkanieService.save(any(Mieszkanie.class))).thenReturn(mieszkanie);

        // When & Then
        mockMvc.perform(post("/api/mieszkania")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.developer", is("Test Developer")))
                .andExpect(jsonPath("$.investment", is("Test Investment")));

        verify(mieszkanieService, times(1)).save(any(Mieszkanie.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createMieszkanie_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Given
        CreateMieszkanieDTO invalidDTO = new CreateMieszkanieDTO();
        // Missing required fields

        // When & Then
        mockMvc.perform(post("/api/mieszkania")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());

        verify(mieszkanieService, times(0)).save(any(Mieszkanie.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateMieszkanie_ShouldUpdateAndReturnMieszkanie() throws Exception {
        // Given
        UpdateMieszkanieDTO updateDTO = new UpdateMieszkanieDTO();
        updateDTO.setDeveloper("Updated Developer");
        updateDTO.setPrice(BigDecimal.valueOf(600000));

        Mieszkanie updatedMieszkanie = new Mieszkanie();
        updatedMieszkanie.setId(1);
        updatedMieszkanie.setDeveloper("Updated Developer");
        updatedMieszkanie.setInvestment("Test Investment");
        updatedMieszkanie.setPrice(BigDecimal.valueOf(600000));
        updatedMieszkanie.setStatus(Mieszkanie.Status.AVAILABLE);

        when(mieszkanieService.updateFromDTO(eq(1), any(UpdateMieszkanieDTO.class))).thenReturn(updatedMieszkanie);

        // When & Then
        mockMvc.perform(put("/api/mieszkania/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.developer", is("Updated Developer")))
                .andExpect(jsonPath("$.price", is(600000)));

        verify(mieszkanieService, times(1)).updateFromDTO(eq(1), any(UpdateMieszkanieDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteMieszkanie_ShouldDeleteMieszkanie() throws Exception {
        // Given
        doNothing().when(mieszkanieService).deleteById(1);

        // When & Then
        mockMvc.perform(delete("/api/mieszkania/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(mieszkanieService, times(1)).deleteById(1);
    }

    @Test
    @WithMockUser(roles = "USER")
    void getByInvestment_ShouldReturnMieszkaniaByInvestment() throws Exception {
        // Given
        when(searchContext.executeSearch(
                eq(SearchStrategy.SearchType.SIMPLE),
                any(MieszkanieSearchCriteria.class),
                any(Pageable.class))).thenReturn(pageResponse);

        // When & Then
        mockMvc.perform(get("/api/mieszkania/investment/Test Investment")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.content[0].investment", is("Test Investment")));

        verify(searchContext, times(1)).executeSearch(
                eq(SearchStrategy.SearchType.SIMPLE),
                any(MieszkanieSearchCriteria.class),
                any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getByPriceRange_ShouldReturnMieszkaniaByPriceRange() throws Exception {
        // Given
        when(searchContext.executeSearch(
                eq(SearchStrategy.SearchType.ADVANCED),
                any(MieszkanieSearchCriteria.class),
                any(Pageable.class))).thenReturn(pageResponse);

        // When & Then
        mockMvc.perform(get("/api/mieszkania/price-range")
                        .param("minPrice", "400000")
                        .param("maxPrice", "600000")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.content[0].price", is(500000)));

        verify(searchContext, times(1)).executeSearch(
                eq(SearchStrategy.SearchType.ADVANCED),
                any(MieszkanieSearchCriteria.class),
                any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void searchByCriteria_ShouldReturnMieszkaniaByCriteria() throws Exception {
        // Given
        MieszkanieSearchCriteria criteria = MieszkanieSearchCriteria.builder()
                .city("Test City")
                .minPrice(BigDecimal.valueOf(400000))
                .maxPrice(BigDecimal.valueOf(600000))
                .build();

        when(searchContext.executeSearch(
                any(SearchStrategy.SearchType.class),
                any(MieszkanieSearchCriteria.class),
                any(Pageable.class))).thenReturn(pageResponse);

        // When & Then
        mockMvc.perform(post("/api/mieszkania/search")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(criteria)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.content[0].city", is("Test City")));

        verify(searchContext, times(1)).executeSearch(
                any(SearchStrategy.SearchType.class),
                any(MieszkanieSearchCriteria.class),
                any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void searchByCriteria_WithLocationOnly_ShouldUseLocationStrategy() throws Exception {
        // Given
        MieszkanieSearchCriteria criteria = MieszkanieSearchCriteria.builder()
                .city("Test City")
                .district("Test District")
                .voivodeship("Test Voivodeship")
                .build();

        when(searchContext.executeSearch(
                eq(SearchStrategy.SearchType.BY_LOCATION),
                any(MieszkanieSearchCriteria.class),
                any(Pageable.class))).thenReturn(pageResponse);

        // When & Then
        mockMvc.perform(post("/api/mieszkania/search")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(criteria)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(1)));

        verify(searchContext, times(1)).executeSearch(
                eq(SearchStrategy.SearchType.BY_LOCATION),
                any(MieszkanieSearchCriteria.class),
                any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void searchByCriteria_WithAdvancedCriteria_ShouldUseAdvancedStrategy() throws Exception {
        // Given
        MieszkanieSearchCriteria criteria = MieszkanieSearchCriteria.builder()
                .developer("Test Developer")
                .minPrice(BigDecimal.valueOf(400000))  // This makes it use ADVANCED strategy
                .build();

        when(searchContext.executeSearch(
                eq(SearchStrategy.SearchType.ADVANCED),
                any(MieszkanieSearchCriteria.class),
                any(Pageable.class))).thenReturn(pageResponse);

        // When & Then
        mockMvc.perform(post("/api/mieszkania/search")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(criteria)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(1)));

        verify(searchContext, times(1)).executeSearch(
                eq(SearchStrategy.SearchType.ADVANCED),
                any(MieszkanieSearchCriteria.class),
                any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void changeStatus_ShouldChangeAndReturnMieszkanieWithNewStatus() throws Exception {
        // Given
        Mieszkanie updatedMieszkanie = new Mieszkanie();
        updatedMieszkanie.setId(1);
        updatedMieszkanie.setDeveloper("Test Developer");
        updatedMieszkanie.setStatus(Mieszkanie.Status.SOLD);

        when(mieszkanieService.changeStatus(eq(1), any(Mieszkanie.Status.class))).thenReturn(updatedMieszkanie);

        // When & Then
        mockMvc.perform(patch("/api/mieszkania/1/status")
                        .param("status", "SOLD")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("SOLD")));

        verify(mieszkanieService, times(1)).changeStatus(eq(1), eq(Mieszkanie.Status.SOLD));
    }

    @Test
    @WithMockUser(roles = "USER")
    void searchByCriteria_WithExplicitStrategy_ShouldUseProvidedStrategy() throws Exception {
        // Given
        MieszkanieSearchCriteria criteria = MieszkanieSearchCriteria.builder()
                .developer("Test Developer")
                .build();

        when(searchContext.executeSearch(
                eq(SearchStrategy.SearchType.BY_LOCATION),
                any(MieszkanieSearchCriteria.class),
                any(Pageable.class))).thenReturn(pageResponse);

        // When & Then
        mockMvc.perform(post("/api/mieszkania/search")
                        .param("strategy", "BY_LOCATION")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(criteria)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(1)));

        verify(searchContext, times(1)).executeSearch(
                eq(SearchStrategy.SearchType.BY_LOCATION),
                any(MieszkanieSearchCriteria.class),
                any(Pageable.class));
    }
}
