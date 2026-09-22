package com.example.carsharingapp.controller;

import com.example.carsharingapp.dto.rental.*;
import com.example.carsharingapp.service.rental.RentalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Rental management", description = "Endpoints for management rentals")
@RequiredArgsConstructor
@RestController
@RequestMapping("/rentals")
public class RentalController {
    private final RentalService rentalService;

    @PostMapping
    @Operation(summary = "Create a rental", description = "Create a rental of car")
    public RentalDto save(@RequestBody @Valid RentalRequestDto requestDto,
                          Authentication authentication) {
        return rentalService.save(requestDto, authentication);
    }

    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @GetMapping
    @Operation(summary = "Search rentals", description = "Search rentals by id and/or status")
    public List<RentalDto> search(RentalSearchParameters searchParameters) {
        return rentalService.search(searchParameters);
    }

    @GetMapping("/list")
    @Operation(summary = "Get all rentals",
            description = "Get all rentals of authenticated user (User gets his rentals)")
    public List<RentalInfoForUserDto> findAll(Authentication authentication) {
        return rentalService.findAll(authentication);
    }

    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @GetMapping("/{id}")
    @Operation(summary = "Get the rental", description = "Get the rental by id")
    public RentalDto findById(@PathVariable Long id) {
        return rentalService.findById(id);
    }

    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @PutMapping("/return")
    @Operation(summary = "Return", description = "Change status of rental")
    public void setReturn(@RequestBody @Valid RentalReturnDto returnDto) {
        rentalService.setReturn(returnDto);
    }
}
