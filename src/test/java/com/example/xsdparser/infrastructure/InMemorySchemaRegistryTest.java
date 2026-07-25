package com.example.xsdparser.infrastructure;

import com.example.xsdparser.core.model.SchemaInfo;
import com.example.xsdparser.infrastructure.schema.InMemorySchemaRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the InMemorySchemaRegistry implementation.
 */
@DisplayName("InMemorySchemaRegistry Tests")
class InMemorySchemaRegistryTest {

    private InMemorySchemaRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new InMemorySchemaRegistry();
    }

    @Test
    @DisplayName("should register and find schema by message type")
    void shouldRegisterAndFindByMessageType() {
        SchemaInfo schema = createTestSchema("pacs.002", "001.16");
        
        registry.register(schema);
        
        assertThat(registry.findByMessageType("pacs.002")).contains(schema);
    }

    @Test
    @DisplayName("should find schema by full identifier")
    void shouldFindByFullIdentifier() {
        SchemaInfo schema = createTestSchema("pacs.008", "001.14");
        registry.register(schema);
        
        assertThat(registry.findByFullIdentifier("pacs.008.001.14")).contains(schema);
    }

    @Test
    @DisplayName("should find schema by namespace")
    void shouldFindByNamespace() {
        SchemaInfo schema = createTestSchema("pacs.003", "001.12");
        registry.register(schema);
        
        String namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.003.001.12";
        assertThat(registry.findByNamespace(namespace)).contains(schema);
    }

    @Test
    @DisplayName("should return empty when schema not found")
    void shouldReturnEmptyWhenNotFound() {
        assertThat(registry.findByMessageType("nonexistent")).isEmpty();
        assertThat(registry.findByFullIdentifier("nonexistent")).isEmpty();
        assertThat(registry.findByNamespace("nonexistent")).isEmpty();
    }

    @Test
    @DisplayName("should return all registered schemas")
    void shouldReturnAllSchemas() {
        registry.register(createTestSchema("pacs.002", "001.16"));
        registry.register(createTestSchema("pacs.008", "001.14"));
        registry.register(createTestSchema("pacs.009", "001.13"));
        
        Collection<SchemaInfo> all = registry.getAllSchemas();
        
        assertThat(all).hasSize(3);
    }

    @Test
    @DisplayName("should find schemas by message type prefix")
    void shouldFindByMessageTypePrefix() {
        registry.register(createTestSchema("pacs.002", "001.16"));
        registry.register(createTestSchema("pacs.002", "001.15"));
        registry.register(createTestSchema("pacs.008", "001.14"));
        
        Collection<SchemaInfo> pacsSchemas = registry.findByMessageTypePrefix("pacs");
        
        assertThat(pacsSchemas).hasSize(3);
    }

    @Test
    @DisplayName("should check if schema is registered")
    void shouldCheckIfRegistered() {
        registry.register(createTestSchema("pacs.002", "001.16"));
        
        assertThat(registry.isRegistered("pacs.002.001.16")).isTrue();
        assertThat(registry.isRegistered("pacs.999.001.01")).isFalse();
    }

    @Test
    @DisplayName("should unregister schema")
    void shouldUnregisterSchema() {
        registry.register(createTestSchema("pacs.002", "001.16"));
        
        boolean removed = registry.unregister("pacs.002.001.16");
        
        assertThat(removed).isTrue();
        assertThat(registry.isRegistered("pacs.002.001.16")).isFalse();
    }

    @Test
    @DisplayName("should return false when unregistering non-existent schema")
    void shouldReturnFalseWhenUnregisteringNonExistent() {
        boolean removed = registry.unregister("nonexistent");
        
        assertThat(removed).isFalse();
    }

    @Test
    @DisplayName("should clear all schemas")
    void shouldClearAllSchemas() {
        registry.register(createTestSchema("pacs.002", "001.16"));
        registry.register(createTestSchema("pacs.008", "001.14"));
        
        registry.clear();
        
        assertThat(registry.getAllSchemas()).isEmpty();
        assertThat(registry.size()).isZero();
    }

    @Test
    @DisplayName("should report correct size")
    void shouldReportCorrectSize() {
        assertThat(registry.size()).isZero();
        
        registry.register(createTestSchema("pacs.002", "001.16"));
        assertThat(registry.size()).isEqualTo(1);
        
        registry.register(createTestSchema("pacs.008", "001.14"));
        assertThat(registry.size()).isEqualTo(2);
    }

    private SchemaInfo createTestSchema(String messageType, String version) {
        return SchemaInfo.builder()
                .messageType(messageType)
                .version(version)
                .namespace("urn:iso:std:iso:20022:tech:xsd:" + messageType + "." + version)
                .description("Test schema for " + messageType)
                .build();
    }
}
