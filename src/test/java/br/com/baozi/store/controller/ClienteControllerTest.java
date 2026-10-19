package br.com.baozi.store.controller;

import java.time.LocalDate;

import br.com.baozi.store.model.Cliente;
import br.com.baozi.store.repository.ClienteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClienteController.class)
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClienteRepository clienteRepository;

    @Test
    void deveCadastrarClienteERetornar201ComIdGerado() throws Exception {
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> {
            Cliente cliente = invocation.getArgument(0);
            assertThat(cliente.getId()).isNull();
            assertThat(cliente.getNome()).isEqualTo("Raul1234567");
            assertThat(cliente.getClienteDesde()).isEqualTo(LocalDate.of(2026, 1, 1));
            ReflectionTestUtils.setField(cliente, "id", 1L);
            return cliente;
        });

        mockMvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"id": 999, "nome": "Raul1234567", "clienteDesde": "2026-01-01"}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Raul1234567"))
                .andExpect(jsonPath("$.clienteDesde").value("2026-01-01"));

        verify(clienteRepository).save(any(Cliente.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{}",
            "{\"nome\": null, \"clienteDesde\": \"2026-01-01\"}",
            "{\"nome\": \"\", \"clienteDesde\": \"2026-01-01\"}",
            "{\"nome\": \"   \", \"clienteDesde\": \"2026-01-01\"}",
            "{\"nome\": \"Raul\"}",
            "{\"nome\": \"Raul\", \"clienteDesde\": null}",
            "{\"nome\": \"Raul\", \"clienteDesde\": \"data-invalida\"}",
            "{\"nome\": \"Raul\", \"clienteDesde\": \"2026-02-30\"}",
            "{",
            "null"
    })
    void deveRejeitarJsonInvalidoSemPersistir(String json) throws Exception {
        rejeitar(json);
    }

    @Test
    void deveRejeitarNomeMaiorQue255Caracteres() throws Exception {
        rejeitar("{\"nome\": \"" + "a".repeat(256) + "\", \"clienteDesde\": \"2026-01-01\"}");
    }

    @Test
    void deveRejeitarDataFutura() throws Exception {
        rejeitar("{\"nome\": \"Raul\", \"clienteDesde\": \"" + LocalDate.now().plusDays(1) + "\"}");
    }

    @Test
    void deveAceitarNomeCom255CaracteresEDataAtual() throws Exception {
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));
        mockMvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\": \"" + "a".repeat(255) + "\", \"clienteDesde\": \"" + LocalDate.now() + "\"}"))
                .andExpect(status().isCreated());
        verify(clienteRepository).save(any(Cliente.class));
    }

    private void rejeitar(String json) throws Exception {
        mockMvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(clienteRepository);
    }
}
