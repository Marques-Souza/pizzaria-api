package br.com.pizzaria.exception;

public class OrderNotDraftException extends RuntimeException {
    public OrderNotDraftException(String message) {
        super(message);
    }
}
