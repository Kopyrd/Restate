package com.example.restate.service.impl;

import com.example.restate.dto.MieszkanieSearchCriteria;
import com.example.restate.dto.PageResponse;
import com.example.restate.dto.UpdateMieszkanieDTO;
import com.example.restate.entity.Mieszkanie;
import com.example.restate.exception.ResourceNotFoundException;
import com.example.restate.repository.MieszkanieRepository;
import com.example.restate.service.MieszkanieService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class MieszkanieServiceImpl implements MieszkanieService {

    private final MieszkanieRepository mieszkanieRepository;
    private final EntityManager entityManager;

    @Override
    public List<Mieszkanie> findAll() {
        return mieszkanieRepository.findAll();
    }

    @Override
    public PageResponse<Mieszkanie> findAll(Pageable pageable) {
        Page<Mieszkanie> page = mieszkanieRepository.findAll(pageable);
        return convertToPageResponse(page);
    }

    @Override
    public Optional<Mieszkanie> findById(Integer id) {
        return mieszkanieRepository.findById(id);
    }

    @Override
    public Mieszkanie save(Mieszkanie mieszkanie) {
        return mieszkanieRepository.save(mieszkanie);
    }

    @Override
    public Mieszkanie update(Integer id, Mieszkanie mieszkanie) {
        Mieszkanie existing = mieszkanieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mieszkanie not found with id: " + id));

        existing.setDeveloper(mieszkanie.getDeveloper());
        existing.setInvestment(mieszkanie.getInvestment());
        existing.setNumber(mieszkanie.getNumber());
        existing.setArea(mieszkanie.getArea());
        existing.setPrice(mieszkanie.getPrice());
        existing.setStatus(mieszkanie.getStatus());
        existing.setDescription(mieszkanie.getDescription());

        return mieszkanieRepository.save(existing);
    }

    @Override
    public void deleteById(Integer id) {
        if (!mieszkanieRepository.existsById(id)) {
            throw new ResourceNotFoundException("Mieszkanie not found with id: " + id);
        }
        mieszkanieRepository.deleteById(id);
    }

    @Override
    public List<Mieszkanie> findByDeveloper(String developer) {
        return mieszkanieRepository.findByDeveloper(developer);
    }

    @Override
    public PageResponse<Mieszkanie> findByDeveloper(String developer, Pageable pageable) {
        Page<Mieszkanie> page = mieszkanieRepository.findByDeveloper(developer, pageable);
        return convertToPageResponse(page);
    }

    @Override
    public List<Mieszkanie> findByInvestment(String investment) {
        return mieszkanieRepository.findByInvestment(investment);
    }

    @Override
    public PageResponse<Mieszkanie> findByInvestment(String investment, Pageable pageable) {
        Page<Mieszkanie> page = mieszkanieRepository.findByInvestment(investment, pageable);
        return convertToPageResponse(page);
    }

    @Override
    public List<Mieszkanie> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return mieszkanieRepository.findByPriceRange(minPrice, maxPrice);
    }

    @Override
    public PageResponse<Mieszkanie> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        Page<Mieszkanie> page = mieszkanieRepository.findByPriceBetween(minPrice, maxPrice, pageable);
        return convertToPageResponse(page);
    }



    @Override
    public List<Mieszkanie> searchByCriteria(MieszkanieSearchCriteria criteria) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Mieszkanie> query = cb.createQuery(Mieszkanie.class);
        Root<Mieszkanie> mieszkanie = query.from(Mieszkanie.class);

        List<Predicate> predicates = buildPredicatesFromCriteria(cb, mieszkanie, criteria);

        query.where(predicates.toArray(new Predicate[0]));
        return entityManager.createQuery(query).getResultList();
    }

    @Override
    public PageResponse<Mieszkanie> searchByCriteria(MieszkanieSearchCriteria criteria, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Mieszkanie> query = cb.createQuery(Mieszkanie.class);
        Root<Mieszkanie> mieszkanie = query.from(Mieszkanie.class);

        List<Predicate> predicates = buildPredicatesFromCriteria(cb, mieszkanie, criteria);

        query.where(predicates.toArray(new Predicate[0]));

        // Count total results
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Mieszkanie> countRoot = countQuery.from(Mieszkanie.class);
        countQuery.select(cb.count(countRoot));
        countQuery.where(predicates.toArray(new Predicate[0]));
        Long totalElements = entityManager.createQuery(countQuery).getSingleResult();

        // Apply pagination
        TypedQuery<Mieszkanie> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<Mieszkanie> content = typedQuery.getResultList();

        // Create Page object
        Page<Mieszkanie> page = new PageImpl<>(content, pageable, totalElements);
        return convertToPageResponse(page);
    }

    private List<Predicate> buildPredicatesFromCriteria(CriteriaBuilder cb, Root<Mieszkanie> mieszkanie, MieszkanieSearchCriteria criteria) {
        List<Predicate> predicates = new ArrayList<>();

        if (criteria.getDeveloper() != null && !criteria.getDeveloper().isEmpty()) {
            predicates.add(cb.equal(mieszkanie.get("developer"), criteria.getDeveloper()));
        }

        if (criteria.getInvestment() != null && !criteria.getInvestment().isEmpty()) {
            predicates.add(cb.equal(mieszkanie.get("investment"), criteria.getInvestment()));
        }

        if (criteria.getMinPrice() != null) {
            predicates.add(cb.greaterThanOrEqualTo(mieszkanie.get("price"), criteria.getMinPrice()));
        }

        if (criteria.getMaxPrice() != null) {
            predicates.add(cb.lessThanOrEqualTo(mieszkanie.get("price"), criteria.getMaxPrice()));
        }

        if (criteria.getMinArea() != null) {
            predicates.add(cb.greaterThanOrEqualTo(mieszkanie.get("area"), criteria.getMinArea()));
        }

        if (criteria.getMaxArea() != null) {
            predicates.add(cb.lessThanOrEqualTo(mieszkanie.get("area"), criteria.getMaxArea()));
        }

        return predicates;
    }

    // Helper method to convert Spring Data Page to our custom PageResponse
    private <T> PageResponse<T> convertToPageResponse(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .first(page.isFirst())
                .build();
    }

    @Override
    public List<String> getAllDevelopers() {
        return mieszkanieRepository.findAllDevelopers();
    }


    @Override
    public Mieszkanie changeStatus(Integer id, Mieszkanie.Status status) {
        Mieszkanie mieszkanie = mieszkanieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mieszkanie not found with id: " + id));
        mieszkanie.setStatus(status);
        return mieszkanieRepository.save(mieszkanie);
    }

    @Override
    public Mieszkanie updateFromDTO(Integer id, UpdateMieszkanieDTO dto) {
        Mieszkanie existing = findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mieszkanie o ID " + id + " nie znalezione"));

        // Aktualizuj tylko niepuste pola
        if (dto.getDeveloper() != null) existing.setDeveloper(dto.getDeveloper());
        if (dto.getInvestment() != null) existing.setInvestment(dto.getInvestment());
        if (dto.getNumber() != null) existing.setNumber(dto.getNumber());
        if (dto.getArea() != null) existing.setArea(dto.getArea());
        if (dto.getPrice() != null) existing.setPrice(dto.getPrice());
        if (dto.getVoivodeship() != null) existing.setVoivodeship(dto.getVoivodeship());
        if (dto.getCity() != null) existing.setCity(dto.getCity());
        if (dto.getDistrict() != null) existing.setDistrict(dto.getDistrict());
        if (dto.getFloor() != null) existing.setFloor(dto.getFloor());
        if (dto.getDescription() != null) existing.setDescription(dto.getDescription());

        return save(existing);
    }


}
