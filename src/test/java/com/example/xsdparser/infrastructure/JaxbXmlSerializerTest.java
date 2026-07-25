package com.example.xsdparser.infrastructure;

import com.example.xsdparser.core.model.XmlDocument;
import com.example.xsdparser.infrastructure.xml.JaxbXmlSerializer;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for the JaxbXmlSerializer implementation.
 */
@DisplayName("JaxbXmlSerializer Tests")
class JaxbXmlSerializerTest {

    private JaxbXmlSerializer serializer;

    @BeforeEach
    void setUp() {
        serializer = new JaxbXmlSerializer();
    }

    @Test
    @DisplayName("should serialize simple object to XML")
    void shouldSerializeSimpleObject() {
        TestPerson person = new TestPerson("John", "Doe", 30);
        
        String xml = serializer.serialize(person);
        
        assertThat(xml)
                .contains("John")
                .contains("Doe")
                .contains("30");
    }

    @Test
    @DisplayName("should serialize formatted XML with proper indentation")
    void shouldSerializeFormattedXml() {
        TestPerson person = new TestPerson("Jane", "Smith", 25);
        
        String xml = serializer.serializeFormatted(person);
        
        assertThat(xml)
                .contains("\n")
                .contains("Jane")
                .contains("Smith");
    }

    @Test
    @DisplayName("should deserialize XML to object")
    void shouldDeserializeXmlToObject() {
        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <testPerson>
                    <firstName>Alice</firstName>
                    <lastName>Wonder</lastName>
                    <age>28</age>
                </testPerson>
                """;
        
        TestPerson person = serializer.deserialize(xml, TestPerson.class);
        
        assertThat(person.getFirstName()).isEqualTo("Alice");
        assertThat(person.getLastName()).isEqualTo("Wonder");
        assertThat(person.getAge()).isEqualTo(28);
    }

    @Test
    @DisplayName("should perform round-trip serialization correctly")
    void shouldPerformRoundTripSerialization() {
        TestPerson original = new TestPerson("Bob", "Builder", 45);
        
        String xml = serializer.serialize(original);
        TestPerson deserialized = serializer.deserialize(xml, TestPerson.class);
        
        assertThat(deserialized.getFirstName()).isEqualTo(original.getFirstName());
        assertThat(deserialized.getLastName()).isEqualTo(original.getLastName());
        assertThat(deserialized.getAge()).isEqualTo(original.getAge());
    }

    @Test
    @DisplayName("should deserialize to XmlDocument")
    void shouldDeserializeToXmlDocument() {
        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <testPerson>
                    <firstName>Charlie</firstName>
                    <lastName>Brown</lastName>
                    <age>8</age>
                </testPerson>
                """;
        
        XmlDocument<TestPerson> document = serializer.deserializeToDocument(xml, TestPerson.class);
        
        assertThat(document.getRootElement()).isNotNull();
        assertThat(document.getRootType()).isEqualTo(TestPerson.class);
        assertThat(document.getRootElement().getFirstName()).isEqualTo("Charlie");
    }

    @Test
    @DisplayName("should serialize XmlDocument")
    void shouldSerializeXmlDocument() {
        TestPerson person = new TestPerson("Diana", "Prince", 30);
        XmlDocument<TestPerson> document = XmlDocument.of(person, TestPerson.class);
        
        String xml = serializer.serialize(document);
        
        assertThat(xml)
                .contains("Diana")
                .contains("Prince");
    }

    @Test
    @DisplayName("should throw exception for invalid XML")
    void shouldThrowExceptionForInvalidXml() {
        String invalidXml = "<not-valid-xml>";
        
        assertThatThrownBy(() -> serializer.deserialize(invalidXml, TestPerson.class))
                .isInstanceOf(com.example.xsdparser.core.port.XmlSerializer.XmlSerializationException.class);
    }

    @Test
    @DisplayName("should cache JAXBContext for performance")
    void shouldCacheJaxbContext() {
        serializer.serialize(new TestPerson("Test1", "User1", 20));
        serializer.serialize(new TestPerson("Test2", "User2", 25));
        
        serializer.clearCache();
        
        String xml = serializer.serialize(new TestPerson("Test3", "User3", 30));
        assertThat(xml).contains("Test3");
    }

    @Test
    @DisplayName("should preload context for warm-up")
    void shouldPreloadContext() {
        serializer.preloadContext(TestPerson.class);
        
        String xml = serializer.serialize(new TestPerson("Preload", "Test", 1));
        assertThat(xml).contains("Preload");
    }

    @XmlRootElement(name = "testPerson")
    public static class TestPerson {
        private String firstName;
        private String lastName;
        private int age;

        public TestPerson() {}

        public TestPerson(String firstName, String lastName, int age) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.age = age;
        }

        @XmlElement
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        @XmlElement
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }

        @XmlElement
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
    }
}
