package br.com.pizzaria.exception;

public class OrderEmptyException extends RuntimeException {
    public OrderEmptyException(String message) {
        super(message);
    }
}
