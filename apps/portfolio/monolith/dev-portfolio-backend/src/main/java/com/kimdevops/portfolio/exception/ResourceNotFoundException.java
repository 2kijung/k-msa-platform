package com.kimdevops.portfolio.exception;

public class ResourceNotFoundException extends RuntimeException {
    
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public static ResourceNotFoundException projectNotFound(Long id) {
        return new ResourceNotFoundException("Project with ID " + id + " not found");
    }
    
    public static ResourceNotFoundException contactNotFound(Long id) {
        return new ResourceNotFoundException("Contact with ID " + id + " not found");
    }
    
    public static ResourceNotFoundException userNotFound(String username) {
        return new ResourceNotFoundException("User with username " + username + " not found");
    }
}
