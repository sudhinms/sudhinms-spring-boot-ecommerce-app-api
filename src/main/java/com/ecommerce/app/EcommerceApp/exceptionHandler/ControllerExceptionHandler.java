package com.ecommerce.app.EcommerceApp.exceptionHandler;

import com.ecommerce.app.EcommerceApp.exceptions.FileReadWriteException;
import com.ecommerce.app.EcommerceApp.exceptions.PasswordNotMatchException;
import com.ecommerce.app.EcommerceApp.exceptions.ProductNotFoundException;
import org.springframework.http.*;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler {

    @Nullable
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ProblemDetail problemDetail=ProblemDetail.forStatus(HttpStatusCode.valueOf(status.value()));
        problemDetail.setProperty("Invalid_Argument","Argument format is incorrect");
        problemDetail.setTitle("MethodArgumentNotValidException");
        problemDetail.setDetail(ex.getMessage());
        return new ResponseEntity<>(problemDetail, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PasswordNotMatchException.class)
    public ProblemDetail handlePasswordNotValid(Exception e){
        ProblemDetail problemDetail=ProblemDetail.forStatus(HttpStatusCode.valueOf(422));
        problemDetail.setDetail(e.getMessage());
        problemDetail.setTitle("PasswordNotMatchException");
        problemDetail.setProperty("Invalid_password","Password not match required criteria");
        return problemDetail;
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ProblemDetail handleProductNotFound(Exception e){
        ProblemDetail problemDetail=ProblemDetail.forStatus(HttpStatusCode.valueOf(404));
        problemDetail.setDetail(e.getMessage());
        problemDetail.setTitle("ProductNotFoundException");
        problemDetail.setProperty("Data_Not_Found","Product not found");
        return problemDetail;
    }
    @ExceptionHandler(FileReadWriteException.class)
    public ProblemDetail handleFileUpload(Exception e){
        ProblemDetail problemDetail=ProblemDetail.forStatus(HttpStatusCode.valueOf(404));
        problemDetail.setDetail(e.getMessage());
        problemDetail.setTitle("FileReadWriteException");
        problemDetail.setProperty("File_Upload_Error","error while uploading file");
        return problemDetail;
    }
}
