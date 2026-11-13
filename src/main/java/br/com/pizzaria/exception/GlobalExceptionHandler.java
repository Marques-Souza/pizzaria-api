package br.com.pizzaria.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidImageException.class)
    public ProblemDetail handlerInvalidImage(InvalidImageException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Invalid Image");
        return  problem;
    }

    @ExceptionHandler(ImageStorageException.class)
    public ProblemDetail handleImageStorage(ImageStorageException ex){
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        problem.setTitle("Image Storage Error");
        return  problem;
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ProblemDetail handleMaxUploadSize(MaxUploadSizeExceededException ex){
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONTENT_TOO_LARGE, ex.getMessage());
        problem.setTitle("Max Upload Size Exceeded");
        return  problem;
    }

    @ExceptionHandler(CategoryAlreadyExistsException.class)
    public ProblemDetail handlerAlreadyExists(CategoryAlreadyExistsException ex){
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Category Already Exists");
        return  problem;

    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public ProblemDetail handlerNotFound(CategoryNotFoundException ex){
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Category Not Found");
        return  problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handlerValidation(MethodArgumentNotValidException ex){
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Validation failed");
        problem.setTitle("Invalid request");
        problem.setProperty("errors", errors);
        return  problem;
    }

    @ExceptionHandler(CategoryInactiveException.class)
    public ProblemDetail handlerCategoryInactive(CategoryInactiveException ex){
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Category is inactive");
        return  problem;
    }

    @ExceptionHandler(ProductAlreadyExistsException.class)
    public ProblemDetail handlerAlreadyExists(ProductAlreadyExistsException ex){
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Product Already Exists");
        return  problem;

    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ProblemDetail handlerNotFound(ProductNotFoundException ex){
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Product Not Found");
        return  problem;
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ProblemDetail handlerOrderNotFound(OrderNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Order Not Found");
        return problem;
    }

    @ExceptionHandler(OrderItemNotFoundException.class)
    public ProblemDetail handlerOrderItemNotFound(OrderItemNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Order Item Not Found");
        return problem;
    }

    @ExceptionHandler(OrderNotDraftException.class)
    public ProblemDetail handlerOrderNotDraft(OrderNotDraftException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Order Is Not Draft");
        return problem;
    }

    @ExceptionHandler(OrderEmptyException.class)
    public ProblemDetail handlerOrderEmpty(OrderEmptyException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Order Has No Items");
        return problem;
    }

    @ExceptionHandler({OrderNotSentException.class, OrderAlreadyFinishedException.class})
    public ProblemDetail handlerOrderStateConflict(RuntimeException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Order State Conflict");
        return problem;
    }

    @ExceptionHandler(ProductInactiveException.class)
    public ProblemDetail handlerProductInactive(ProductInactiveException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Product Inactive");
        return problem;
    }

    @ExceptionHandler(ProductUpdateEmptyException.class)
    public ProblemDetail handleProductUpdateEmpty(ProductUpdateEmptyException ex){
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Empty Product Update");
        return problem;
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ProblemDetail handlerAlreadyExists(UserAlreadyExistsException ex){
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("User Already Exists");
        return  problem;

    }

    @ExceptionHandler(UserNotFoundException.class)
    public ProblemDetail handlerNotFound(UserNotFoundException ex){
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("User Not Found");
        return  problem;
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ProblemDetail handleInvalidCredentials(InvalidCredentialsException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED, ex.getMessage());
        problem.setTitle("Invalid credentials");
        return problem;
    }
}
