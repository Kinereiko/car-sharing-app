package com.example.carsharingapp.repository.specification;

import com.example.carsharingapp.dto.rental.RentalSearchParameters;
import org.springframework.data.jpa.domain.Specification;

public interface SpecificationBuilder<T> {
    Specification<T> build(RentalSearchParameters searchParameters);
}
