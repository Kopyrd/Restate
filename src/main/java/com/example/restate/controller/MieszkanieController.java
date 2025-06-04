package com.example.restate.controller;

import com.example.restate.dto.CreateMieszkanieDTO;
import com.example.restate.dto.MieszkanieDTO;
import com.example.restate.dto.UpdateMieszkanieDTO;
import com.example.restate.entity.Mieszkanie;
import com.example.restate.service.MieszkanieService;
import com.example.restate.dto.MieszkanieSearchCriteria;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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

    @GetMapping("/developer/{developer}")
    @Operation(summary = "Get apartments by developer")
    public ResponseEntity<List<MieszkanieDTO>> getByDeveloper(@PathVariable String developer) {
        List<MieszkanieDTO> dtos = mieszkanieService.findByDeveloper(developer)
                .stream()
                .map(MieszkanieDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/investment/{investment}")
    @Operation(summary = "Get apartments by investment")
    public ResponseEntity<List<MieszkanieDTO>> getByInvestment(@PathVariable String investment) {
        List<MieszkanieDTO> dtos = mieszkanieService.findByInvestment(investment)
                .stream()
                .map(MieszkanieDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/price-range")
    @Operation(summary = "Get apartments by price range")
    public ResponseEntity<List<MieszkanieDTO>> getByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice) {
        List<MieszkanieDTO> dtos = mieszkanieService.findByPriceRange(minPrice, maxPrice)
                .stream()
                .map(MieszkanieDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/area-range")
    @Operation(summary = "Get apartments by area range")
    public ResponseEntity<List<MieszkanieDTO>> getByAreaRange(
            @RequestParam BigDecimal minArea,
            @RequestParam BigDecimal maxArea) {
        List<MieszkanieDTO> dtos = mieszkanieService.findByAreaRange(minArea, maxArea)
                .stream()
                .map(MieszkanieDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/search")
    @Operation(summary = "Search apartments by multiple criteria")
    public ResponseEntity<List<MieszkanieDTO>> searchByCriteria(@RequestBody MieszkanieSearchCriteria criteria) {
        List<MieszkanieDTO> dtos = mieszkanieService.searchByCriteria(criteria)
                .stream()
                .map(MieszkanieDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/developers/{developer}/investments")
    @Operation(summary = "Get investments by developer")
    public ResponseEntity<List<String>> getInvestmentsByDeveloper(@PathVariable String developer) {
        return ResponseEntity.ok(mieszkanieService.getInvestmentsByDeveloper(developer));
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

    // Helper methods
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
}