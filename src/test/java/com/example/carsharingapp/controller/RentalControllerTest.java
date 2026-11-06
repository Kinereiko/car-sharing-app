package com.example.carsharingapp.controller;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.example.carsharingapp.config.SecurityConfig;
import com.example.carsharingapp.dto.car.CarDto;
import com.example.carsharingapp.dto.car.CarInfoForUserDto;
import com.example.carsharingapp.dto.rental.RentalDto;
import com.example.carsharingapp.dto.rental.RentalInfoForUserDto;
import com.example.carsharingapp.dto.rental.RentalRequestDto;
import com.example.carsharingapp.dto.rental.RentalReturnDto;
import com.example.carsharingapp.dto.rental.RentalSearchParameters;
import com.example.carsharingapp.model.Car;
import com.example.carsharingapp.security.JwtAuthenticationFilter;
import com.example.carsharingapp.security.JwtUtil;
import com.example.carsharingapp.service.rental.RentalService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RentalController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class RentalControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RentalService rentalService;
    @MockBean
    private JwtUtil jwtUtil;
    @MockBean
    private UserDetailsService userDetailsService;

    private RentalRequestDto request(Long carId, int days) {
        RentalRequestDto dto = new RentalRequestDto();
        dto.setCarId(carId);
        dto.setRentedDays(days);
        return dto;
    }

    private RentalDto rentalDto(Long id) {
        CarDto car = new CarDto();
        car.setId(1L);
        car.setModel("Model 3");
        car.setBrand("Tesla");
        car.setType(Car.TypeName.SEDAN);
        car.setInventory(4);
        car.setDailyFee(BigDecimal.valueOf(50));

        RentalDto dto = new RentalDto();
        dto.setId(id);
        dto.setRentalDate(LocalDate.of(2026, 11, 1));
        dto.setReturnDate(LocalDate.of(2026, 11, 4));
        dto.setCarDto(car);
        dto.setUserId(7L);
        return dto;
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    @DisplayName("POST /rentals creates a rental for an authenticated user")
    void save_ValidRequest_ReturnsRental() throws Exception {
        when(rentalService.save(any(RentalRequestDto.class), any(Authentication.class)))
                .thenReturn(rentalDto(1L));

        mockMvc.perform(post("/rentals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request(1L, 3))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(7))
                .andExpect(jsonPath("$.carDto.brand").value("Tesla"));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    @DisplayName("POST /rentals with rentedDays outside 1..10 returns 400")
    void save_RentedDaysOutOfRange_ReturnsBadRequest() throws Exception {
        for (int days : new int[] {0, 11}) {
            mockMvc.perform(post("/rentals")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request(1L, days))))
                    .andExpect(status().isBadRequest());
        }
        verifyNoInteractions(rentalService);
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    @DisplayName("POST /rentals without carId returns 400")
    void save_MissingCarId_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/rentals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rentedDays\": 3}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(rentalService);
    }

    @Test
    @DisplayName("POST /rentals without authentication returns 401")
    void save_Anonymous_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/rentals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request(1L, 3))))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(rentalService);
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("GET /rentals as MANAGER passes search parameters to the service")
    void search_Manager_ReturnsList() throws Exception {
        when(rentalService.search(any(RentalSearchParameters.class)))
                .thenReturn(List.of(rentalDto(1L)));

        mockMvc.perform(get("/rentals")
                        .param("ids", "1", "2")
                        .param("isActive", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1));

        ArgumentCaptor<RentalSearchParameters> captor =
                ArgumentCaptor.forClass(RentalSearchParameters.class);
        verify(rentalService).search(captor.capture());
        assertArrayEquals(new String[] {"1", "2"}, captor.getValue().ids());
        assertArrayEquals(new String[] {"true"}, captor.getValue().isActive());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    @DisplayName("GET /rentals as CUSTOMER is forbidden")
    void search_User_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/rentals"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(rentalService);
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    @DisplayName("GET /rentals/list returns the rentals of the current user")
    void findAll_User_ReturnsOwnRentals() throws Exception {
        CarInfoForUserDto car = new CarInfoForUserDto();
        car.setModel("Model 3");
        car.setBrand("Tesla");
        car.setType(Car.TypeName.SEDAN);
        car.setDailyFee(BigDecimal.valueOf(50));
        RentalInfoForUserDto dto = new RentalInfoForUserDto();
        dto.setRentalDate(LocalDate.of(2026, 11, 1));
        dto.setReturnDate(LocalDate.of(2026, 11, 4));
        dto.setCarDto(car);
        when(rentalService.findAll(any(Authentication.class))).thenReturn(List.of(dto));

        mockMvc.perform(get("/rentals/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].rentalDate").value("2026-11-01"))
                .andExpect(jsonPath("$[0].carDto.model").value("Model 3"));
    }

    @Test
    @DisplayName("GET /rentals/list without authentication returns 401")
    void findAll_Anonymous_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/rentals/list"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("GET /rentals/{id} as MANAGER returns the rental")
    void findById_Manager_ReturnsRental() throws Exception {
        when(rentalService.findById(1L)).thenReturn(rentalDto(1L));

        mockMvc.perform(get("/rentals/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.returnDate").value("2026-11-04"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("GET /rentals/{id} returns 404 when the rental does not exist")
    void findById_NotExisting_ReturnsNotFound() throws Exception {
        when(rentalService.findById(99L))
                .thenThrow(new EntityNotFoundException("Rental not found"));

        mockMvc.perform(get("/rentals/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Rental not found"));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    @DisplayName("GET /rentals/{id} as CUSTOMER is forbidden")
    void findById_User_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/rentals/1"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(rentalService);
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("PUT /rentals/return as MANAGER sets the return")
    void setReturn_Manager_ReturnsOk() throws Exception {
        RentalReturnDto dto = new RentalReturnDto();
        dto.setId(1L);
        dto.setActualReturnDate(LocalDate.of(2026, 11, 4));

        mockMvc.perform(put("/rentals/return")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        ArgumentCaptor<RentalReturnDto> captor = ArgumentCaptor.forClass(RentalReturnDto.class);
        verify(rentalService).setReturn(captor.capture());
        org.junit.jupiter.api.Assertions.assertEquals(1L, captor.getValue().getId());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("PUT /rentals/return without id returns 400")
    void setReturn_MissingId_ReturnsBadRequest() throws Exception {
        mockMvc.perform(put("/rentals/return")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"actualReturnDate\": \"2026-11-04\"}"))
                .andExpect(status().isBadRequest());

        verify(rentalService, never()).setReturn(any());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    @DisplayName("PUT /rentals/return as CUSTOMER is forbidden")
    void setReturn_User_ReturnsForbidden() throws Exception {
        RentalReturnDto dto = new RentalReturnDto();
        dto.setId(1L);

        mockMvc.perform(put("/rentals/return")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());

        verify(rentalService, never()).setReturn(any());
    }
}
