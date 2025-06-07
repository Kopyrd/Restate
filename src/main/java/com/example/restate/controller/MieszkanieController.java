package com.example.restate.controller;

import com.example.restate.dto.CreateMieszkanieDTO;
import com.example.restate.dto.MieszkanieDTO;
import com.example.restate.dto.PageResponse;
import com.example.restate.dto.UpdateMieszkanieDTO;
import com.example.restate.entity.Mieszkanie;
import com.example.restate.service.MieszkanieService;
import com.example.restate.dto.MieszkanieSearchCriteria;
import com.example.restate.service.search.SearchContext;
import com.example.restate.service.search.SearchStrategy;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/mieszkania")
@RequiredArgsConstructor
@Tag(name = "Mieszkania", description = "Endpoints for managing apartments")
public class MieszkanieController {

    private final MieszkanieService mieszkanieService;
    private final SearchContext searchContext;

    @GetMapping
    @Operation(summary = "Get all apartments")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<PageResponse<MieszkanieDTO>> getAllMieszkania(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        PageResponse<Mieszkanie> pageResponse = mieszkanieService.findAll(pageable);
        return ResponseEntity.ok(convertToPageResponseDTO(pageResponse));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get apartment by ID")
    public ResponseEntity<MieszkanieDTO> getMieszkanieById(
            @Parameter(description = "Apartment ID") @PathVariable Integer id) {
        return mieszkanieService.findById(id)
                .map(MieszkanieDTO::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create new apartment", description = "Admin only")
    public ResponseEntity<MieszkanieDTO> createMieszkanie(@Valid @RequestBody CreateMieszkanieDTO dto) {
        Mieszkanie mieszkanie = convertToEntity(dto);
        Mieszkanie created = mieszkanieService.save(mieszkanie);
        return ResponseEntity.status(HttpStatus.CREATED).body(MieszkanieDTO.fromEntity(created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update apartment", description = "Admin only")
    public ResponseEntity<MieszkanieDTO> updateMieszkanie(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateMieszkanieDTO dto) {
        Mieszkanie updated = mieszkanieService.updateFromDTO(id, dto);
        return ResponseEntity.ok(MieszkanieDTO.fromEntity(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete apartment", description = "Admin only")
    public ResponseEntity<Void> deleteMieszkanie(@PathVariable Integer id) {
        mieszkanieService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/investment/{investment}")
    @Operation(summary = "Get apartments by investment")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<PageResponse<MieszkanieDTO>> getByInvestment(
            @PathVariable String investment,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        MieszkanieSearchCriteria criteria = MieszkanieSearchCriteria.builder()
                .investment(investment)
                .build();

        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        PageResponse<Mieszkanie> mieszkaniePageResponse = searchContext.executeAutoSearch(criteria, pageable);
        return ResponseEntity.ok(convertToPageResponseDTO(mieszkaniePageResponse));
    }

    @GetMapping("/price-range")
    @Operation(summary = "Get apartments by price range")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<PageResponse<MieszkanieDTO>> getByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        MieszkanieSearchCriteria criteria = MieszkanieSearchCriteria.builder()
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .build();

        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        PageResponse<Mieszkanie> mieszkaniePageResponse = searchContext.executeAutoSearch(criteria, pageable);
        return ResponseEntity.ok(convertToPageResponseDTO(mieszkaniePageResponse));
    }

    @PostMapping("/search")
    @Operation(summary = "Search apartments by multiple criteria")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<PageResponse<MieszkanieDTO>> searchByCriteria(
            @RequestBody MieszkanieSearchCriteria criteria,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        PageResponse<Mieszkanie> mieszkaniePageResponse = searchContext.executeAutoSearch(criteria, pageable);
        return ResponseEntity.ok(convertToPageResponseDTO(mieszkaniePageResponse));
    }


    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Change apartment status", description = "Admin only")
    public ResponseEntity<MieszkanieDTO> changeStatus(
            @PathVariable Integer id,
            @RequestParam Mieszkanie.Status status) {
        Mieszkanie updated = mieszkanieService.changeStatus(id, status);
        return ResponseEntity.ok(MieszkanieDTO.fromEntity(updated));
    }

    // METODY POMOCNICZE
    private Mieszkanie convertToEntity(CreateMieszkanieDTO dto) {
        Mieszkanie mieszkanie = new Mieszkanie();
        mieszkanie.setDeveloper(dto.getDeveloper());
        mieszkanie.setInvestment(dto.getInvestment());
        mieszkanie.setNumber(dto.getNumber());
        mieszkanie.setArea(dto.getArea());
        mieszkanie.setPrice(dto.getPrice());
        mieszkanie.setVoivodeship(dto.getVoivodeship());
        mieszkanie.setCity(dto.getCity());
        mieszkanie.setDistrict(dto.getDistrict());
        mieszkanie.setFloor(dto.getFloor());
        mieszkanie.setDescription(dto.getDescription());
        mieszkanie.setStatus(Mieszkanie.Status.AVAILABLE);
        return mieszkanie;
    }


    private PageResponse<MieszkanieDTO> convertToPageResponseDTO(PageResponse<Mieszkanie> pageResponse) {
        List<MieszkanieDTO> dtos = pageResponse.getContent().stream()
                .map(MieszkanieDTO::fromEntity)
                .collect(Collectors.toList());

        return PageResponse.<MieszkanieDTO>builder()
                .content(dtos)
                .pageNumber(pageResponse.getPageNumber())
                .pageSize(pageResponse.getPageSize())
                .totalElements(pageResponse.getTotalElements())
                .totalPages(pageResponse.getTotalPages())
                .last(pageResponse.isLast())
                .first(pageResponse.isFirst())
                .build();
    }
}
