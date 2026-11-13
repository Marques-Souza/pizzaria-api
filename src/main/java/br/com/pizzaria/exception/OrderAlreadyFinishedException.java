package br.com.pizzaria.exception;

public class OrderAlreadyFinishedException extends RuntimeException {
    public OrderAlreadyFinishedException(String message) {
        super(message);
    }
}
