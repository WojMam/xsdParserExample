/**
 * Port interfaces defining contracts for external dependencies.
 * 
 * <p>These interfaces follow the Dependency Inversion Principle - the core domain
 * depends on these abstractions rather than concrete implementations. The infrastructure
 * layer provides the actual implementations.</p>
 * 
 * <h2>Key Interfaces</h2>
 * <ul>
 *   <li>{@link com.example.xsdparser.core.port.XmlSerializer} - XML serialization/deserialization</li>
 *   <li>{@link com.example.xsdparser.core.port.XmlValidator} - XML validation against XSD schemas</li>
 *   <li>{@link com.example.xsdparser.core.port.SchemaRegistry} - Schema registration and lookup</li>
 * </ul>
 * 
 * @see com.example.xsdparser.core.port.XmlSerializer
 * @see com.example.xsdparser.core.port.XmlValidator
 * @see com.example.xsdparser.core.port.SchemaRegistry
 */
package com.example.xsdparser.core.port;
