package com.example.xsdparser.application;

import com.example.xsdparser.XsdParserConfig;
import com.example.xsdparser.application.usecase.XmlConversionUseCase;
import com.example.xsdparser.core.model.XmlDocument;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for XmlConversionUseCase.
 * Demonstrates the high-level API for XML conversion operations.
 */
@DisplayName("XmlConversionUseCase Tests")
class XmlConversionUseCaseTest {

    private XmlConversionUseCase conversionUseCase;

    @BeforeEach
    void setUp() {
        XsdParserConfig.CustomConfig config = new XsdParserConfig.Builder()
                .skipSchemaInitialization()
                .build();
        conversionUseCase = config.getConversionUseCase();
    }

    @Test
    @DisplayName("should convert object to XML")
    void shouldConvertObjectToXml() {
        TestMessage message = new TestMessage("MSG001", "Test content");
        
        String xml = conversionUseCase.toXml(message);
        
        assertThat(xml)
                .contains("MSG001")
                .contains("Test content")
                .contains("testMessage");
    }

    @Test
    @DisplayName("should convert XML to object")
    void shouldConvertXmlToObject() {
        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <testMessage>
                    <messageId>MSG002</messageId>
                    <content>Parsed content</content>
                </testMessage>
                """;
        
        TestMessage message = conversionUseCase.fromXml(xml, TestMessage.class);
        
        assertThat(message.getMessageId()).isEqualTo("MSG002");
        assertThat(message.getContent()).isEqualTo("Parsed content");
    }

    @Test
    @DisplayName("should convert XML to document with metadata")
    void shouldConvertXmlToDocument() {
        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <testMessage>
                    <messageId>MSG003</messageId>
                    <content>Document content</content>
                </testMessage>
                """;
        
        XmlDocument<TestMessage> document = conversionUseCase.toDocument(xml, TestMessage.class);
        
        assertThat(document.getRootElement()).isNotNull();
        assertThat(document.getRootElement().getMessageId()).isEqualTo("MSG003");
    }

    @Test
    @DisplayName("should perform round-trip conversion")
    void shouldPerformRoundTripConversion() {
        TestMessage original = new TestMessage("ROUND001", "Round trip test");
        
        XmlConversionUseCase.RoundTripResult<TestMessage> result = 
                conversionUseCase.roundTrip(original, TestMessage.class);
        
        assertThat(result.original()).isNotNull();
        assertThat(result.xml()).contains("ROUND001");
        assertThat(result.deserialized()).isNotNull();
        assertThat(result.deserialized().getMessageId()).isEqualTo("ROUND001");
    }

    @XmlRootElement(name = "testMessage")
    public static class TestMessage {
        private String messageId;
        private String content;

        public TestMessage() {}

        public TestMessage(String messageId, String content) {
            this.messageId = messageId;
            this.content = content;
        }

        @XmlElement
        public String getMessageId() { return messageId; }
        public void setMessageId(String messageId) { this.messageId = messageId; }

        @XmlElement
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            TestMessage that = (TestMessage) obj;
            return java.util.Objects.equals(messageId, that.messageId) &&
                   java.util.Objects.equals(content, that.content);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(messageId, content);
        }
    }
}
