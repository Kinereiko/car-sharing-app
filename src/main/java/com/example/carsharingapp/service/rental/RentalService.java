package com.example.carsharingapp.service.rental;

import com.example.carsharingapp.dto.rental.*;
import org.springframework.security.core.Authentication;
import java.util.List;

public interface RentalService {
    RentalDto save(RentalRequestDto requestDto, Authentication authentication);

    List<RentalDto> search(RentalSearchParameters params);

    List<RentalInfoForUserDto> findAll(Authentication authentication);

    RentalDto findById(Long id);

    void setReturn(RentalReturnDto returnDto);
}
