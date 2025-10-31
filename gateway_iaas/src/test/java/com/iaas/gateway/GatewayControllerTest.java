package com.iaas.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GatewayControllerTest {

    @Autowired
    private MockMvc mockMvc;


    @Test
    void registroYListadoDeProveedoresFunciona() throws Exception {
        mockMvc.perform(post("/providers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Proveedor 1\",\"endpoint\":\"http://localhost:9000\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre", is("Proveedor 1")));

        mockMvc.perform(get("/providers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombre", is("Proveedor 1")));
    }

    @Test
    void inferenciaUsaProveedorRegistrado() throws Exception {
        mockMvc.perform(post("/providers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Proveedor 2\",\"endpoint\":\"http://localhost:9001\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/inference")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"prompt\":\"Hola\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido", is("[Mock] Hola")));
    }
}
