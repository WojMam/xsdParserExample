package com.example.xsdparser.core.port;

import com.example.xsdparser.core.model.SchemaInfo;

import java.util.Collection;
import java.util.Optional;

/**
 * Port interface for managing XSD schema registrations.
 * Provides a registry of available schemas and their metadata.
 */
public interface SchemaRegistry {

    /**
     * Registers a schema in the registry.
     *
     * @param schemaInfo the schema information to register
     */
    void register(SchemaInfo schemaInfo);

    /**
     * Retrieves schema information by message type (e.g., "pacs.002").
     *
     * @param messageType the message type identifier
     * @return the schema info if found
     */
    Optional<SchemaInfo> findByMessageType(String messageType);

    /**
     * Retrieves schema information by full identifier (e.g., "pacs.002.001.16").
     *
     * @param fullIdentifier the full schema identifier
     * @return the schema info if found
     */
    Optional<SchemaInfo> findByFullIdentifier(String fullIdentifier);

    /**
     * Retrieves schema information by namespace URI.
     *
     * @param namespace the namespace URI
     * @return the schema info if found
     */
    Optional<SchemaInfo> findByNamespace(String namespace);

    /**
     * Returns all registered schemas.
     *
     * @return collection of all registered schema infos
     */
    Collection<SchemaInfo> getAllSchemas();

    /**
     * Returns all schemas for a given message type family (e.g., all pacs.002 versions).
     *
     * @param messageTypePrefix the message type prefix (e.g., "pacs.002")
     * @return collection of matching schema infos
     */
    Collection<SchemaInfo> findByMessageTypePrefix(String messageTypePrefix);

    /**
     * Checks if a schema with the given identifier is registered.
     *
     * @param fullIdentifier the full schema identifier
     * @return true if the schema is registered
     */
    boolean isRegistered(String fullIdentifier);

    /**
     * Removes a schema from the registry.
     *
     * @param fullIdentifier the full schema identifier to remove
     * @return true if the schema was removed
     */
    boolean unregister(String fullIdentifier);

    /**
     * Clears all registered schemas.
     */
    void clear();
}
