package br.com.pizzaria.exception;

public class ProductUpdateEmptyException extends RuntimeException {
    public ProductUpdateEmptyException(String message) {
        super(message);
    }
}
