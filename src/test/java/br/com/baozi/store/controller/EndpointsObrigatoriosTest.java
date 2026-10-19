package br.com.baozi.store.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import br.com.baozi.store.model.Cliente;
import br.com.baozi.store.model.Pedido;
import br.com.baozi.store.model.Produto;
import br.com.baozi.store.repository.ClienteRepository;
import br.com.baozi.store.repository.PedidoRepository;
import br.com.baozi.store.repository.ProdutoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({ClienteController.class, ProdutoController.class, PedidoController.class})
class EndpointsObrigatoriosTest {

    @Autowired private MockMvc mvc;
    @MockitoBean private ClienteRepository clientes;
    @MockitoBean private ProdutoRepository produtos;
    @MockitoBean private PedidoRepository pedidos;

    @ParameterizedTest
    @ValueSource(strings = {"clientes", "produtos", "pedidos"})
    void listarVazioRetorna200ComArray(String recurso) throws Exception {
        preparar(recurso, false);
        mvc.perform(get("/" + recurso)).andExpect(status().isOk()).andExpect(content().json("[]"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"clientes", "produtos", "pedidos"})
    void listarRetornaRecursosEmJson(String recurso) throws Exception {
        preparar(recurso, true);
        mvc.perform(get("/" + recurso)).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].id").value(1));
    }

    @ParameterizedTest
    @ValueSource(strings = {"clientes", "produtos", "pedidos"})
    void consultarExistenteRetorna200(String recurso) throws Exception {
        preparar(recurso, true);
        mvc.perform(get("/" + recurso + "/1")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1));
    }

    @ParameterizedTest
    @ValueSource(strings = {"clientes", "produtos", "pedidos"})
    void consultarInexistenteRetorna404(String recurso) throws Exception {
        preparar(recurso, false);
        mvc.perform(get("/" + recurso + "/1")).andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @ValueSource(strings = {"clientes", "produtos", "pedidos"})
    void excluirExistenteRetorna204SemCorpo(String recurso) throws Exception {
        Object entidade = preparar(recurso, true);
        mvc.perform(delete("/" + recurso + "/1")).andExpect(status().isNoContent()).andExpect(content().string(""));
        switch (recurso) {
            case "clientes" -> verify(clientes).delete((Cliente) entidade);
            case "produtos" -> verify(produtos).delete((Produto) entidade);
            case "pedidos" -> verify(pedidos).delete((Pedido) entidade);
            default -> throw new IllegalArgumentException(recurso);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"clientes", "produtos", "pedidos"})
    void excluirInexistenteRetorna404SemExcluir(String recurso) throws Exception {
        preparar(recurso, false);
        mvc.perform(delete("/" + recurso + "/1")).andExpect(status().isNotFound());
        verify(clientes, never()).delete(any());
        verify(produtos, never()).delete(any());
        verify(pedidos, never()).delete(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"clientes", "produtos", "pedidos"})
    void idNaoNumericoRetorna400(String recurso) throws Exception {
        mvc.perform(get("/" + recurso + "/abc")).andExpect(status().isBadRequest());
        mvc.perform(delete("/" + recurso + "/abc")).andExpect(status().isBadRequest());
        verifyNoInteractions(clientes, produtos, pedidos);
    }

    @ParameterizedTest
    @ValueSource(strings = {"clientes", "produtos"})
    void excluirRecursoReferenciadoRetorna409(String recurso) throws Exception {
        preparar(recurso, true);
        if (recurso.equals("clientes")) {
            doThrow(new DataIntegrityViolationException("FK")).when(clientes).delete(any(Cliente.class));
        } else {
            doThrow(new DataIntegrityViolationException("FK")).when(produtos).delete(any(Produto.class));
        }
        mvc.perform(delete("/" + recurso + "/1")).andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
        verifyNoInteractions(pedidos);
    }

    @Test
    void criarProdutoRetorna201ComIdGeradoIgnorandoIdEnviado() throws Exception {
        when(produtos.save(any(Produto.class))).thenAnswer(invocation -> {
            Produto produto = invocation.getArgument(0);
            assertThat(produto.getId()).isNull();
            assertThat(produto.getNome()).isEqualTo("Baozi");
            assertThat(produto.getPreco()).isEqualByComparingTo("12.50");
            assertThat(produto.getEstoque()).isTrue();
            ReflectionTestUtils.setField(produto, "id", 1L);
            return produto;
        });
        mvc.perform(post("/produtos").contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":99,\"nome\":\"Baozi\",\"preco\":12.50,\"estoque\":true}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Baozi"))
                .andExpect(jsonPath("$.preco").value(12.5)).andExpect(jsonPath("$.estoque").value(true));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{}", "null", "{",
            "{\"nome\":null,\"preco\":1,\"estoque\":true}",
            "{\"nome\":\"\",\"preco\":1,\"estoque\":true}",
            "{\"nome\":\"   \",\"preco\":1,\"estoque\":true}",
            "{\"nome\":\"Baozi\",\"estoque\":true}",
            "{\"nome\":\"Baozi\",\"preco\":null,\"estoque\":true}",
            "{\"nome\":\"Baozi\",\"preco\":0,\"estoque\":true}",
            "{\"nome\":\"Baozi\",\"preco\":-1,\"estoque\":true}",
            "{\"nome\":\"Baozi\",\"preco\":1.001,\"estoque\":true}",
            "{\"nome\":\"Baozi\",\"preco\":10000000000,\"estoque\":true}",
            "{\"nome\":\"Baozi\",\"preco\":1}",
            "{\"nome\":\"Baozi\",\"preco\":1,\"estoque\":null}"
    })
    void produtoInvalidoRetorna400SemPersistir(String json) throws Exception {
        mvc.perform(post("/produtos").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(produtos);
    }

    @Test
    void produtoComNomeLongoRetorna400() throws Exception {
        mvc.perform(post("/produtos").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"" + "a".repeat(256) + "\",\"preco\":1,\"estoque\":true}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(produtos);
    }

    @Test
    void produtoAceitaLimitesEEstoqueFalse() throws Exception {
        when(produtos.save(any(Produto.class))).thenAnswer(i -> i.getArgument(0));
        mvc.perform(post("/produtos").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"" + "a".repeat(255) + "\",\"preco\":9999999999.99,\"estoque\":false}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.estoque").value(false));
        verify(produtos).save(any(Produto.class));
    }

    @Test
    void criarPedidoRetorna201ComReferenciasEIdGerado() throws Exception {
        when(clientes.existsById(1L)).thenReturn(true);
        when(produtos.existsById(2L)).thenReturn(true);
        when(pedidos.save(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido pedido = invocation.getArgument(0);
            assertThat(pedido.getId()).isNull();
            assertThat(pedido.getClienteId()).isEqualTo(1L);
            assertThat(pedido.getProdutoId()).isEqualTo(2L);
            assertThat(pedido.getQuantidade()).isEqualTo(3);
            ReflectionTestUtils.setField(pedido, "id", 7L);
            return pedido;
        });
        mvc.perform(post("/pedidos").contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":99,\"clienteId\":1,\"produtoId\":2,\"quantidade\":3}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.clienteId").value(1)).andExpect(jsonPath("$.produtoId").value(2))
                .andExpect(jsonPath("$.quantidade").value(3));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{}", "null", "{",
            "{\"produtoId\":2,\"quantidade\":1}",
            "{\"clienteId\":null,\"produtoId\":2,\"quantidade\":1}",
            "{\"clienteId\":0,\"produtoId\":2,\"quantidade\":1}",
            "{\"clienteId\":-1,\"produtoId\":2,\"quantidade\":1}",
            "{\"clienteId\":1,\"quantidade\":1}",
            "{\"clienteId\":1,\"produtoId\":null,\"quantidade\":1}",
            "{\"clienteId\":1,\"produtoId\":0,\"quantidade\":1}",
            "{\"clienteId\":1,\"produtoId\":-2,\"quantidade\":1}",
            "{\"clienteId\":1,\"produtoId\":2}",
            "{\"clienteId\":1,\"produtoId\":2,\"quantidade\":null}",
            "{\"clienteId\":1,\"produtoId\":2,\"quantidade\":0}",
            "{\"clienteId\":1,\"produtoId\":2,\"quantidade\":-1}",
            "{\"clienteId\":1,\"produtoId\":2,\"quantidade\":1.5}",
            "{\"clienteId\":1.5,\"produtoId\":2,\"quantidade\":1}",
            "{\"clienteId\":1,\"produtoId\":2.5,\"quantidade\":1}",
            "{\"clienteId\":1,\"produtoId\":2,\"quantidade\":2147483648}"
    })
    void pedidoInvalidoRetorna400SemAcessarBanco(String json) throws Exception {
        mvc.perform(post("/pedidos").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(clientes, produtos, pedidos);
    }

    @Test
    void pedidoComClienteInexistenteRetorna404() throws Exception {
        mvc.perform(post("/pedidos").contentType(MediaType.APPLICATION_JSON)
                .content("{\"clienteId\":1,\"produtoId\":2,\"quantidade\":1}"))
                .andExpect(status().isNotFound());
        verifyNoInteractions(produtos, pedidos);
    }

    @Test
    void pedidoComProdutoInexistenteRetorna404() throws Exception {
        when(clientes.existsById(1L)).thenReturn(true);
        mvc.perform(post("/pedidos").contentType(MediaType.APPLICATION_JSON)
                .content("{\"clienteId\":1,\"produtoId\":2,\"quantidade\":1}"))
                .andExpect(status().isNotFound());
        verifyNoInteractions(pedidos);
    }

    @Test
    void referenciaExcluidaEntreConsultaEPersistenciaRetorna409() throws Exception {
        when(clientes.existsById(1L)).thenReturn(true);
        when(produtos.existsById(2L)).thenReturn(true);
        when(pedidos.save(any(Pedido.class))).thenThrow(new DataIntegrityViolationException("FK"));
        mvc.perform(post("/pedidos").contentType(MediaType.APPLICATION_JSON)
                .content("{\"clienteId\":1,\"produtoId\":2,\"quantidade\":1}"))
                .andExpect(status().isConflict());
    }

    private Object preparar(String recurso, boolean existe) {
        switch (recurso) {
            case "clientes" -> {
                Cliente cliente = new Cliente();
                cliente.setNome("Raul1234567");
                cliente.setClienteDesde(LocalDate.of(2026, 1, 1));
                ReflectionTestUtils.setField(cliente, "id", 1L);
                when(clientes.findById(1L)).thenReturn(existe ? Optional.of(cliente) : Optional.empty());
                when(clientes.findAll()).thenReturn(existe ? List.of(cliente) : List.of());
                return cliente;
            }
            case "produtos" -> {
                Produto produto = new Produto();
                produto.setNome("Baozi");
                produto.setPreco(new BigDecimal("12.50"));
                produto.setEstoque(true);
                ReflectionTestUtils.setField(produto, "id", 1L);
                when(produtos.findById(1L)).thenReturn(existe ? Optional.of(produto) : Optional.empty());
                when(produtos.findAll()).thenReturn(existe ? List.of(produto) : List.of());
                return produto;
            }
            case "pedidos" -> {
                Pedido pedido = new Pedido();
                pedido.setClienteId(1L);
                pedido.setProdutoId(2L);
                pedido.setQuantidade(3);
                ReflectionTestUtils.setField(pedido, "id", 1L);
                when(pedidos.findById(1L)).thenReturn(existe ? Optional.of(pedido) : Optional.empty());
                when(pedidos.findAll()).thenReturn(existe ? List.of(pedido) : List.of());
                return pedido;
            }
            default -> throw new IllegalArgumentException(recurso);
        }
    }
}
