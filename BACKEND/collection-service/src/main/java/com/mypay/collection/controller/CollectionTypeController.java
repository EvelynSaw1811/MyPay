package com.mypay.collection.controller;

import com.mypay.collection.dto.CollectionTypeRequest;
import com.mypay.collection.dto.CollectionTypeResponse;
import com.mypay.collection.entity.CollectionType;
import com.mypay.collection.repository.CollectionTypeRepository;
import com.mypay.common.context.RequestContextHolder;
import com.mypay.common.dto.ApiResponse;
import com.mypay.common.exception.ConflictException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/collections/types")
@RequiredArgsConstructor
public class CollectionTypeController {

    private final CollectionTypeRepository collectionTypeRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CollectionTypeResponse>>> list() {
        String userId = RequestContextHolder.currentUserId();
        List<CollectionTypeResponse> types = collectionTypeRepository
                .findByCollectionTypeUserId(userId)
                .stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(types));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CollectionTypeResponse>> create(
            @Valid @RequestBody CollectionTypeRequest request) {
        String userId = RequestContextHolder.currentUserId();
        String name = request.getName().trim();
        if (collectionTypeRepository.existsByCollectionTypeUserIdAndCollectionTypeNameIgnoreCase(userId, name)) {
            throw new ConflictException("Collection type already exists: " + name);
        }
        CollectionType type = collectionTypeRepository.save(CollectionType.builder()
                .collectionTypeUserId(userId)
                .collectionTypeName(name)
                .collectionTypeSystem(false)
                .build());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Collection type created", toResponse(type)));
    }

    @DeleteMapping("/{collectionTypeId}")
    public ResponseEntity<Void> delete(@PathVariable String collectionTypeId) {
        String userId = RequestContextHolder.currentUserId();
        CollectionType type = collectionTypeRepository.findById(collectionTypeId)
                .orElseThrow(() -> new com.mypay.common.exception.ResourceNotFoundException(
                        "Collection type not found: " + collectionTypeId));
        if (!userId.equals(type.getCollectionTypeUserId())) {
            throw new com.mypay.common.exception.ForbiddenException("You can only delete your own collection types");
        }
        collectionTypeRepository.delete(type);
        return ResponseEntity.noContent().build();
    }

    private CollectionTypeResponse toResponse(CollectionType type) {
        return CollectionTypeResponse.builder()
                .collectionTypeId(type.getCollectionTypeId())
                .name(type.getCollectionTypeName())
                .system(type.isCollectionTypeSystem())
                .build();
    }
}
