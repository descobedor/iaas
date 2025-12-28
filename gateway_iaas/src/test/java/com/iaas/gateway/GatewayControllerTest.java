package com.iaas.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class GatewayControllerTest {

    @Autowired
    private MockMvc mockMvc;


    @Test
    void registerAndListProvidersWork() throws Exception {
        mockMvc.perform(post("/providers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Provider 1\",\"endpoint\":\"http://localhost:9000\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Provider 1")));

        mockMvc.perform(get("/providers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Provider 1")));
    }

    @Test
    void inferenceUsesRegisteredProvider() throws Exception {
        mockMvc.perform(post("/providers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Provider 2\",\"endpoint\":\"http://localhost:9001\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/inference")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"prompt\":\"Hola\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", is("[Mock] Hola")));
    }
}
