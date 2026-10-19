package br.com.baozi.store.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = {ClienteController.class, ProdutoController.class, PedidoController.class})
public class IntegridadeReferencialHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail tratarConflito(DataIntegrityViolationException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "A operação conflita com os dados relacionados. Remova os pedidos antes de excluir clientes ou produtos.");
    }
}
