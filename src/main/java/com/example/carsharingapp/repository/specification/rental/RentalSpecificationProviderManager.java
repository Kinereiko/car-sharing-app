package com.example.carsharingapp.repository.specification.rental;

import com.example.carsharingapp.model.Rental;
import com.example.carsharingapp.repository.specification.SpecificationProvider;
import com.example.carsharingapp.repository.specification.SpecificationProviderManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class RentalSpecificationProviderManager implements SpecificationProviderManager<Rental> {
    private final List<SpecificationProvider<Rental>> rentalSpecificationProviders;

    @Override
    public SpecificationProvider<Rental> getSpecificationProvider(String key) {
        return rentalSpecificationProviders.stream()
                .filter(p -> p.getKey().equals(key))
                .findFirst()
                .orElseThrow(() ->
                        new RuntimeException("Can't find correct specification provider for key "
                                + key));
    }
}
