package com.example.carsharingapp.service.rental;

import com.example.carsharingapp.dto.rental.*;
import java.util.List;
import org.springframework.security.core.Authentication;

public interface RentalService {
    RentalDto save(RentalRequestDto requestDto, Authentication authentication);

    List<RentalDto> search(RentalSearchParameters params);

    List<RentalInfoForUserDto> findAll(Authentication authentication);

    RentalDto findById(Long id);

    void setReturn(RentalReturnDto returnDto);
}
