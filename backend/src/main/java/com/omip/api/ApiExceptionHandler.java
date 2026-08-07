package com.omip.api;
import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import java.time.Instant; import java.util.Map;
@RestControllerAdvice public class ApiExceptionHandler {
 @ExceptionHandler(IllegalArgumentException.class) ResponseEntity<Map<String,Object>> invalid(IllegalArgumentException e){return ResponseEntity.badRequest().body(Map.of("timestamp",Instant.now().toString(),"error",e.getMessage()));}
}
