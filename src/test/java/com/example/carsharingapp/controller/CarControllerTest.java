package com.example.carsharingapp.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.example.carsharingapp.config.SecurityConfig;
import com.example.carsharingapp.dto.car.CarDto;
import com.example.carsharingapp.dto.car.CarRequestDto;
import com.example.carsharingapp.dto.car.CarShortDto;
import com.example.carsharingapp.model.Car;
import com.example.carsharingapp.security.JwtAuthenticationFilter;
import com.example.carsharingapp.security.JwtUtil;
import com.example.carsharingapp.service.car.CarService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CarController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class CarControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CarService carService;
    @MockBean
    private JwtUtil jwtUtil;
    @MockBean
    private UserDetailsService userDetailsService;

    private CarRequestDto validRequest() {
        CarRequestDto dto = new CarRequestDto();
        dto.setModel("Model 3");
        dto.setBrand("Tesla");
        dto.setType(Car.TypeName.SEDAN);
        dto.setInventory(5);
        dto.setDailyFee(BigDecimal.valueOf(50));
        return dto;
    }

    private CarDto carDto(Long id) {
        CarDto dto = new CarDto();
        dto.setId(id);
        dto.setModel("Model 3");
        dto.setBrand("Tesla");
        dto.setType(Car.TypeName.SEDAN);
        dto.setInventory(5);
        dto.setDailyFee(BigDecimal.valueOf(50));
        return dto;
    }

    @Test
    @DisplayName("GET /cars is public and returns the list of cars")
    void findAll_Anonymous_ReturnsList() throws Exception {
        CarShortDto shortDto = new CarShortDto();
        shortDto.setId(1L);
        shortDto.setModel("Model 3");
        when(carService.findAll(any(Pageable.class))).thenReturn(List.of(shortDto));

        mockMvc.perform(get("/cars"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].model").value("Model 3"));
    }

    @Test
    @DisplayName("GET /cars/{id} without authentication returns 401")
    void findById_Anonymous_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/cars/1"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(carService);
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    @DisplayName("GET /cars/{id} returns the car for an authenticated user")
    void findById_User_ReturnsCar() throws Exception {
        when(carService.findById(1L)).thenReturn(carDto(1L));

        mockMvc.perform(get("/cars/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.brand").value("Tesla"))
                .andExpect(jsonPath("$.type").value("SEDAN"));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    @DisplayName("GET /cars/{id} returns 404 when the car does not exist")
    void findById_NotExisting_ReturnsNotFound() throws Exception {
        when(carService.findById(99L)).thenThrow(new EntityNotFoundException("Car not found"));

        mockMvc.perform(get("/cars/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Car not found"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("POST /cars as MANAGER creates a car")
    void save_Manager_ReturnsCar() throws Exception {
        when(carService.save(any(CarRequestDto.class))).thenReturn(carDto(1L));

        mockMvc.perform(post("/cars")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.model").value("Model 3"));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    @DisplayName("POST /cars as CUSTOMER is forbidden")
    void save_User_ReturnsForbidden() throws Exception {
        mockMvc.perform(post("/cars")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isForbidden());

        verify(carService, never()).save(any());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("POST /cars with blank model, null type and low fee returns 400")
    void save_InvalidBody_ReturnsBadRequest() throws Exception {
        CarRequestDto dto = validRequest();
        dto.setModel(" ");
        dto.setType(null);
        dto.setDailyFee(BigDecimal.ONE);

        mockMvc.perform(post("/cars")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.length()").value(3));

        verify(carService, never()).save(any());
    }

    @Test
    @DisplayName("POST /cars without authentication returns 401")
    void save_Anonymous_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/cars")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("PUT /cars/{id} as MANAGER updates the car")
    void update_Manager_ReturnsCar() throws Exception {
        when(carService.updateById(eq(1L), any(CarRequestDto.class))).thenReturn(carDto(1L));

        mockMvc.perform(put("/cars/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    @DisplayName("PUT /cars/{id} as CUSTOMER is forbidden")
    void update_User_ReturnsForbidden() throws Exception {
        mockMvc.perform(put("/cars/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isForbidden());

        verify(carService, never()).updateById(any(), any());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("PUT /cars/{id} with invalid body returns 400")
    void update_InvalidBody_ReturnsBadRequest() throws Exception {
        CarRequestDto dto = validRequest();
        dto.setBrand("");

        mockMvc.perform(put("/cars/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("DELETE /cars/{id} as MANAGER deletes the car")
    void delete_Manager_ReturnsOk() throws Exception {
        mockMvc.perform(delete("/cars/1"))
                .andExpect(status().isOk());

        verify(carService).deleteById(1L);
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    @DisplayName("DELETE /cars/{id} as CUSTOMER is forbidden")
    void delete_User_ReturnsForbidden() throws Exception {
        mockMvc.perform(delete("/cars/1"))
                .andExpect(status().isForbidden());

        verify(carService, never()).deleteById(any());
    }
}
