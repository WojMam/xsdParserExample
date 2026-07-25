/**
 * Core domain layer containing models, ports, and services.
 * 
 * <p>This package follows the Hexagonal Architecture (Ports and Adapters) pattern,
 * providing the domain model and interfaces that the infrastructure layer implements.</p>
 * 
 * <h2>Sub-packages</h2>
 * <ul>
 *   <li>{@code model} - Immutable value objects representing domain concepts</li>
 *   <li>{@code port} - Interface contracts for external dependencies</li>
 *   <li>{@code service} - Domain services orchestrating business logic</li>
 *   <li>{@code exception} - Domain-specific exception hierarchy</li>
 * </ul>
 * 
 * @see com.example.xsdparser.core.model
 * @see com.example.xsdparser.core.port
 * @see com.example.xsdparser.core.service
 */
package com.example.xsdparser.core;
