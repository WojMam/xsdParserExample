package com.example.xsdparser.infrastructure.schema;

import com.example.xsdparser.core.model.SchemaInfo;
import com.example.xsdparser.core.port.SchemaRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of the SchemaRegistry port.
 * Stores schema information in a thread-safe map for quick lookup.
 */
public class InMemorySchemaRegistry implements SchemaRegistry {

    private static final Logger logger = LoggerFactory.getLogger(InMemorySchemaRegistry.class);

    private final Map<String, SchemaInfo> schemasByIdentifier = new ConcurrentHashMap<>();
    private final Map<String, SchemaInfo> schemasByNamespace = new ConcurrentHashMap<>();
    private final Map<String, SchemaInfo> schemasByMessageType = new ConcurrentHashMap<>();

    @Override
    public void register(SchemaInfo schemaInfo) {
        String identifier = schemaInfo.getFullIdentifier();
        logger.debug("Registering schema: {}", identifier);
        
        schemasByIdentifier.put(identifier, schemaInfo);
        schemasByNamespace.put(schemaInfo.getNamespace(), schemaInfo);
        schemasByMessageType.put(schemaInfo.getMessageType(), schemaInfo);
    }

    @Override
    public Optional<SchemaInfo> findByMessageType(String messageType) {
        return Optional.ofNullable(schemasByMessageType.get(messageType));
    }

    @Override
    public Optional<SchemaInfo> findByFullIdentifier(String fullIdentifier) {
        return Optional.ofNullable(schemasByIdentifier.get(fullIdentifier));
    }

    @Override
    public Optional<SchemaInfo> findByNamespace(String namespace) {
        return Optional.ofNullable(schemasByNamespace.get(namespace));
    }

    @Override
    public Collection<SchemaInfo> getAllSchemas() {
        return schemasByIdentifier.values();
    }

    @Override
    public Collection<SchemaInfo> findByMessageTypePrefix(String messageTypePrefix) {
        return schemasByIdentifier.values().stream()
                .filter(info -> info.getMessageType().startsWith(messageTypePrefix))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isRegistered(String fullIdentifier) {
        return schemasByIdentifier.containsKey(fullIdentifier);
    }

    @Override
    public boolean unregister(String fullIdentifier) {
        SchemaInfo removed = schemasByIdentifier.remove(fullIdentifier);
        if (removed != null) {
            schemasByNamespace.remove(removed.getNamespace());
            schemasByMessageType.remove(removed.getMessageType());
            logger.debug("Unregistered schema: {}", fullIdentifier);
            return true;
        }
        return false;
    }

    @Override
    public void clear() {
        schemasByIdentifier.clear();
        schemasByNamespace.clear();
        schemasByMessageType.clear();
        logger.debug("Schema registry cleared");
    }

    /**
     * Returns the number of registered schemas.
     *
     * @return the count of registered schemas
     */
    public int size() {
        return schemasByIdentifier.size();
    }
}
