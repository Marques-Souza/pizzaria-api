package br.com.pizzaria.exception;

public class OrderNotSentException extends RuntimeException {
    public OrderNotSentException(String message) {
        super(message);
    }
}
