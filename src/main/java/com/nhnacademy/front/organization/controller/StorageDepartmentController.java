package com.nhnacademy.front.organization.controller;

import com.nhnacademy.front.organization.client.StorageDepartmentApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequiredArgsConstructor
@RequestMapping("/departments/{department-id}/storages/{storage-id}")
public class StorageDepartmentController {
    private final StorageDepartmentApiClient storageDepartmentApiClient;

    @PostMapping
    public ResponseEntity<Void> addStorage(@PathVariable("department-id") Long departmentId,
                                           @PathVariable("storage-id") Long storageId) {
        storageDepartmentApiClient.addStorage(departmentId, storageId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> removeStorage(@PathVariable("department-id") Long departmentId,
                                              @PathVariable("storage-id") Long storageId) {
        storageDepartmentApiClient.removeStorage(departmentId, storageId);
        return ResponseEntity.noContent().build();
    }
}
