package com.iftm.client.resources;

import com.iftm.client.dto.ClientDTO;
import com.iftm.client.services.ClientService;
import com.iftm.client.services.exceptions.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Instant;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ClientResourcesTestsIT {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private ClientService clientService;

        @Autowired
        private ObjectMapper objectMapper;

        private Long existingId;
        private Long nonExistingId;
        private ClientDTO clientDTO;

        @BeforeEach
        void setUp() {
                existingId = 1L;
                nonExistingId = 100L;
                clientDTO = new ClientDTO(null, "Clarice Lispector", "10919444522", 3800.0,
                                Instant.parse("1960-04-13T07:50:00Z"), 2);
        }

        @Test
        void findAllShouldReturnAllClients() throws Exception {
                ResultActions result = mockMvc.perform(get("/clients")
                                .accept(MediaType.APPLICATION_JSON));

                result.andExpect(status().isOk());
        }

        @Test
        void findByIdShouldReturnClientWhenIdExists() throws Exception {
                ResultActions result = mockMvc.perform(get("/clients/{id}", existingId)
                                .accept(MediaType.APPLICATION_JSON));

                result.andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(existingId))
                                .andExpect(jsonPath("$.name").value("Clarice Lispector"))
                                .andExpect(jsonPath("$.cpf").value("10919444522"));
        }

        @Test
        void findByIdShouldReturnNotFoundWhenIdDoesNotExist() throws Exception {
                ResultActions result = mockMvc.perform(get("/clients/{id}", nonExistingId)
                                .accept(MediaType.APPLICATION_JSON));

                result.andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.error").value("Resource not found"))
                                .andExpect(jsonPath("$.message").value("Entity not found"))
                                .andExpect(jsonPath("$.path").value("/clients/" + nonExistingId));
        }

        @Test
        void findByIncomeShouldReturnClientsWithIncome() throws Exception {
                Double income = 3800.0;
                ResultActions result = mockMvc.perform(get("/clients/income/")
                                .param("income", String.valueOf(income))
                                .accept(MediaType.APPLICATION_JSON));

                result.andExpect(status().isOk())
                                .andExpect(jsonPath("$.content").exists())
                                .andExpect(jsonPath("$.content[0].income").value(income));
        }

        @Test
        void findByIncomeGreaterThanShouldReturnClientsWithIncomeGreaterThan() throws Exception {
                Double income = 3000.0;
                ResultActions result = mockMvc.perform(get("/clients/income-greater-than/")
                                .param("income", String.valueOf(income))
                                .accept(MediaType.APPLICATION_JSON));

                result.andExpect(status().isOk())
                                .andExpect(jsonPath("$.content").exists())
                                .andExpect(jsonPath("$.content[0].income").value(3800.0));
        }

        @Test
        void findByCPFLikeShouldReturnClientsWithCPFLike() throws Exception {
                String cpf = "109194";
                ResultActions result = mockMvc.perform(get("/clients/cpf-like/")
                                .param("cpf", cpf)
                                .accept(MediaType.APPLICATION_JSON));

                result.andExpect(status().isOk())
                                .andExpect(jsonPath("$.content").exists())
                                .andExpect(jsonPath("$.content[0].cpf").value("10919444522"));
        }

        @Test
        void insertShouldCreateClientAndReturnCreated() throws Exception {
                String json = objectMapper.writeValueAsString(clientDTO);

                ResultActions result = mockMvc.perform(post("/clients/")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON));

                result.andExpect(status().isCreated())
                                .andExpect(jsonPath("$.name").value("Clarice Lispector"))
                                .andExpect(jsonPath("$.cpf").value("10919444522"));
        }

        @Test
        void deleteShouldReturnNoContentWhenIdExists() throws Exception {
                ResultActions result = mockMvc.perform(delete("/clients/{id}", existingId)
                                .accept(MediaType.APPLICATION_JSON));

                result.andExpect(status().isNoContent());
        }

        @Test
        void deleteShouldReturnNotFoundWhenIdDoesNotExist() throws Exception {
                ResultActions result = mockMvc.perform(delete("/clients/{id}", nonExistingId)
                                .accept(MediaType.APPLICATION_JSON));

                result.andExpect(status().isNotFound());
        }

        @Test
        void updateShouldReturnClientWhenIdExists() throws Exception {
                clientDTO.setName("Clarice Updated");
                String json = objectMapper.writeValueAsString(clientDTO);

                ResultActions result = mockMvc.perform(put("/clients/{id}", existingId)
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON));

                result.andExpect(status().isOk())
                                .andExpect(jsonPath("$.name").value("Clarice Updated"))
                                .andExpect(jsonPath("$.cpf").value("10919444522"));
        }

        @Test
        void updateShouldReturnNotFoundWhenIdDoesNotExist() throws Exception {
                clientDTO.setName("Clarice Updated");
                String json = objectMapper.writeValueAsString(clientDTO);

                ResultActions result = mockMvc.perform(put("/clients/{id}", nonExistingId)
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON));

                result.andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.error").value("Resource not found"));
        }

        @Test
        void testFindById_withExistingId_shouldReturnClient() throws Exception {
                Long testExistingId = 1L;
                ClientDTO mockClient = new ClientDTO(testExistingId, "Clarice Lispector", "10919444522", 3800.0,
                                Instant.parse("1960-04-13T07:50:00Z"), 2);

                when(clientService.findById(testExistingId)).thenReturn(mockClient);

                mockMvc.perform(get("/clients/{id}", testExistingId)
                                .accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(testExistingId))
                                .andExpect(jsonPath("$.name").value("Clarice Lispector"));
        }

        @Test
        void testFindById_withNonExistingId_shouldReturnNotFound() throws Exception {
                Long testNonExistingId = 99L;

                when(clientService.findById(testNonExistingId))
                                .thenThrow(new ResourceNotFoundException("Client not found"));

                mockMvc.perform(get("/clients/{id}", testNonExistingId)
                                .accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isNotFound());
        }

}