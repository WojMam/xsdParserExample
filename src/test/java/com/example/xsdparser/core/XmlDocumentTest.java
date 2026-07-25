package com.example.xsdparser.core;

import com.example.xsdparser.core.model.XmlDocument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for the XmlDocument value object.
 */
@DisplayName("XmlDocument Tests")
class XmlDocumentTest {

    @Test
    @DisplayName("should create document with root element only")
    void shouldCreateDocumentWithRootElementOnly() {
        String content = "test content";
        
        XmlDocument<String> document = XmlDocument.of(content, String.class);
        
        assertThat(document.getRootElement()).isEqualTo(content);
        assertThat(document.getRootType()).isEqualTo(String.class);
        assertThat(document.getNamespace()).isEmpty();
        assertThat(document.getSchemaLocation()).isEmpty();
    }

    @Test
    @DisplayName("should create document with namespace")
    void shouldCreateDocumentWithNamespace() {
        String content = "test content";
        String namespace = "urn:test:namespace";
        
        XmlDocument<String> document = XmlDocument.of(content, namespace, String.class);
        
        assertThat(document.getRootElement()).isEqualTo(content);
        assertThat(document.getNamespace()).contains(namespace);
    }

    @Test
    @DisplayName("should create document with namespace and schema location")
    void shouldCreateDocumentWithNamespaceAndSchemaLocation() {
        String content = "test content";
        String namespace = "urn:test:namespace";
        String schemaLocation = "/path/to/schema.xsd";
        
        XmlDocument<String> document = XmlDocument.of(content, namespace, schemaLocation, String.class);
        
        assertThat(document.getRootElement()).isEqualTo(content);
        assertThat(document.getNamespace()).contains(namespace);
        assertThat(document.getSchemaLocation()).contains(schemaLocation);
    }

    @Test
    @DisplayName("should throw exception when root element is null")
    void shouldThrowExceptionWhenRootElementIsNull() {
        assertThatThrownBy(() -> XmlDocument.of(null, String.class))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Root element cannot be null");
    }

    @Test
    @DisplayName("should throw exception when root type is null")
    void shouldThrowExceptionWhenRootTypeIsNull() {
        assertThatThrownBy(() -> XmlDocument.of("content", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Root type cannot be null");
    }

    @Test
    @DisplayName("should be equal when content is the same")
    void shouldBeEqualWhenContentIsSame() {
        XmlDocument<String> doc1 = XmlDocument.of("content", "ns", String.class);
        XmlDocument<String> doc2 = XmlDocument.of("content", "ns", String.class);
        
        assertThat(doc1).isEqualTo(doc2);
        assertThat(doc1.hashCode()).isEqualTo(doc2.hashCode());
    }

    @Test
    @DisplayName("should provide meaningful toString")
    void shouldProvideMeaningfulToString() {
        XmlDocument<String> document = XmlDocument.of("content", "urn:test", String.class);
        
        String toString = document.toString();
        
        assertThat(toString)
                .contains("String")
                .contains("urn:test");
    }
}
