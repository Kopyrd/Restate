package com.example.restate.controller;

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

@RestController
@RequestMapping("/api/mieszkania")
@RequiredArgsConstructor
@Tag(name = "Mieszkania", description = "Endpoints for managing apartments")
public class MieszkanieController {

    private final MieszkanieService mieszkanieService;

    @GetMapping
    @Operation(summary = "Get all apartments")
    public ResponseEntity<List<Mieszkanie>> getAllMieszkania() {
        return ResponseEntity.ok(mieszkanieService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get apartment by ID")
    public ResponseEntity<Mieszkanie> getMieszkanieById(
            @Parameter(description = "Apartment ID") @PathVariable Integer id) {
        return mieszkanieService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create new apartment", description = "Admin only")
    public ResponseEntity<Mieszkanie> createMieszkanie(@Valid @RequestBody Mieszkanie mieszkanie) {
        Mieszkanie created = mieszkanieService.save(mieszkanie);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update apartment", description = "Admin only")
    public ResponseEntity<Mieszkanie> updateMieszkanie(
            @PathVariable Integer id,
            @Valid @RequestBody Mieszkanie mieszkanie) {
        Mieszkanie updated = mieszkanieService.update(id, mieszkanie);
        return ResponseEntity.ok(updated);
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
    public ResponseEntity<List<Mieszkanie>> getByDeveloper(@PathVariable String developer) {
        return ResponseEntity.ok(mieszkanieService.findByDeveloper(developer));
    }

    @GetMapping("/investment/{investment}")
    @Operation(summary = "Get apartments by investment")
    public ResponseEntity<List<Mieszkanie>> getByInvestment(@PathVariable String investment) {
        return ResponseEntity.ok(mieszkanieService.findByInvestment(investment));
    }

    @GetMapping("/rooms/{rooms}")
    @Operation(summary = "Get apartments by number of rooms")
    public ResponseEntity<List<Mieszkanie>> getByRooms(@PathVariable Integer rooms) {
        return ResponseEntity.ok(mieszkanieService.findByRooms(rooms));
    }

    @GetMapping("/price-range")
    @Operation(summary = "Get apartments by price range")
    public ResponseEntity<List<Mieszkanie>> getByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice) {
        return ResponseEntity.ok(mieszkanieService.findByPriceRange(minPrice, maxPrice));
    }

    @GetMapping("/area-range")
    @Operation(summary = "Get apartments by area range")
    public ResponseEntity<List<Mieszkanie>> getByAreaRange(
            @RequestParam BigDecimal minArea,
            @RequestParam BigDecimal maxArea) {
        return ResponseEntity.ok(mieszkanieService.findByAreaRange(minArea, maxArea));
    }

    @PostMapping("/search")
    @Operation(summary = "Search apartments by multiple criteria")
    public ResponseEntity<List<Mieszkanie>> searchByCriteria(@RequestBody MieszkanieSearchCriteria criteria) {
        return ResponseEntity.ok(mieszkanieService.searchByCriteria(criteria));
    }

    @GetMapping("/developers")
    @Operation(summary = "Get all unique developers")
    public ResponseEntity<List<String>> getAllDevelopers() {
        return ResponseEntity.ok(mieszkanieService.getAllDevelopers());
    }

    @GetMapping("/developers/{developer}/investments")
    @Operation(summary = "Get investments by developer")
    public ResponseEntity<List<String>> getInvestmentsByDeveloper(@PathVariable String developer) {
        return ResponseEntity.ok(mieszkanieService.getInvestmentsByDeveloper(developer));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Change apartment status", description = "Admin only")
    public ResponseEntity<Mieszkanie> changeStatus(
            @PathVariable Integer id,
            @RequestParam Mieszkanie.Status status) {
        return ResponseEntity.ok(mieszkanieService.changeStatus(id, status));
    }
}