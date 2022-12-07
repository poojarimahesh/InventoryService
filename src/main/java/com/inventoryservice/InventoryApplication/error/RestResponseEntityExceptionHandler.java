package com.inventoryservice.InventoryApplication.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@ResponseStatus
@ResponseBody
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {
	
	@ExceptionHandler(ProductNotFoundException.class)
	public ResponseEntity<ProductNotFoundError> productNotFoundException (ProductNotFoundException exception,WebRequest request){
		
		ProductNotFoundError message = new ProductNotFoundError(HttpStatus.NOT_FOUND, exception.getMessage());
		
//		HttpStatus.FORBIDDEN -> 403
		
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
	}
	
	@ExceptionHandler(NotAuthorizedException.class)
	public ResponseEntity<NotAuthorizedError> notAuthorizedException (NotAuthorizedException exception,WebRequest request){
		
		NotAuthorizedError message = new NotAuthorizedError(HttpStatus.UNAUTHORIZED, exception.getMessage());
		
//		HttpStatus.FORBIDDEN -> 403
		
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(message);
	}
	
	@ExceptionHandler(TokenExpiredException.class)
	public ResponseEntity<TokenExpiredError> tokenExpiredException (TokenExpiredException exception,WebRequest request){
		
		TokenExpiredError message = new TokenExpiredError(HttpStatus.GATEWAY_TIMEOUT, exception.getMessage());
		
//		HttpStatus.FORBIDDEN -> 403
		
		return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(message);
	}

}
