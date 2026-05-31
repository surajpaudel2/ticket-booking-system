package com.tbs.eventservice.exception;

/** Thrown when a requested resource does not exist in the database. */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Constructs a ResourceNotFoundException for a named resource type and its ID.
     *
     * @param resource the entity type name (e.g. "Season")
     * @param id       the ID that was not found
     */
    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " not found with id: " + id);
    }
}
