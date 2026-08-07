package com.example.carsharingapp.controller;

import com.example.carsharingapp.dto.rental.*;
import com.example.carsharingapp.service.rental.RentalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/rentals")
public class RentalController {
    private final RentalService rentalService;

    @PostMapping
    public RentalDto save(@RequestBody @Valid RentalRequestDto requestDto, Authentication authentication) {
        return rentalService.save(requestDto, authentication);
    }

    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @GetMapping
    public List<RentalDto> search(RentalSearchParameters searchParameters) {
        return rentalService.search(searchParameters);
    }

    @GetMapping("/list")
    public List<RentalInfoForUserDto> findAll(Authentication authentication) {
        return rentalService.findAll(authentication);
    }

    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @GetMapping("/{id}")
    public RentalDto findById(@PathVariable Long id) {
        return rentalService.findById(id);
    }

    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @PutMapping("/return")
    public void setReturn(@RequestBody @Valid RentalReturnDto returnDto) {
        rentalService.setReturn(returnDto);
    }
}
