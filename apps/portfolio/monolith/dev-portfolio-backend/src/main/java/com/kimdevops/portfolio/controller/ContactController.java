package com.kimdevops.portfolio.controller;

import com.kimdevops.portfolio.dto.ApiResponse;
import com.kimdevops.portfolio.dto.ContactDTO;
import com.kimdevops.portfolio.service.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/contacts")
public class ContactController {
    @Autowired
    private ContactService contactService;

    @PostMapping
    public ResponseEntity<ApiResponse<ContactDTO>> createContact(@RequestBody Map<String, String> request) {
        try {
            ContactDTO contact = contactService.createContact(
                    request.get("name"),
                    request.get("email"),
                    request.get("subject"),
                    request.get("message")
            );
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(contact));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ContactDTO>>> getContacts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<ContactDTO> contacts = contactService.getContacts(page, size);
            return ResponseEntity.ok(ApiResponse.success(contacts));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ContactDTO>> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        try {
            ContactDTO contact = contactService.updateStatus(id, status);
            return ResponseEntity.ok(ApiResponse.success(contact));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}
