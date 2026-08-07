package com.example.carsharingapp.repository.specification.rental;

import com.example.carsharingapp.dto.rental.RentalSearchParameters;
import com.example.carsharingapp.model.Rental;
import com.example.carsharingapp.repository.specification.SpecificationBuilder;
import com.example.carsharingapp.repository.specification.SpecificationProviderManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class RentalSpecificationBuilder implements SpecificationBuilder<Rental> {
    private static final String ID_KEY = "id";
    private static final String IS_ACTIVE_KEY = "isActive";
    private final SpecificationProviderManager<Rental> rentalSpecificationProviderManager;

    @Override
    public Specification<Rental> build(RentalSearchParameters searchParameters) {
        Specification<Rental> spec = (root, query, cb) -> cb.conjunction();

        if (searchParameters.ids() != null && searchParameters.ids().length > 0) {
            spec = spec.and(rentalSpecificationProviderManager.getSpecificationProvider(ID_KEY)
                    .getSpecification(searchParameters.ids()));
        }
        if (searchParameters.isActive() != null && searchParameters.isActive().length > 0) {
            spec = spec.and(rentalSpecificationProviderManager.getSpecificationProvider(IS_ACTIVE_KEY)
                    .getSpecification(searchParameters.isActive()));
        }
        return spec;
    }
}
