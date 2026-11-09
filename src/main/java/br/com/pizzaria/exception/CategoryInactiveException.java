package br.com.pizzaria.exception;

public class CategoryInactiveException extends RuntimeException {
    public CategoryInactiveException(String message) {
        super(message);
    }
}
