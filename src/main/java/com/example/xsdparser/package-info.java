/**
 * XSD Parser Example Application.
 * 
 * <p>This package contains the main entry point and configuration for the XSD Parser
 * application. The application demonstrates how to use XSD files to dynamically generate
 * Java models for ISO 20022 PACS (Payments Clearing and Settlement) messages.</p>
 * 
 * <h2>Key Components</h2>
 * <ul>
 *   <li>{@link com.example.xsdparser.XsdParserApplication} - Main application entry point</li>
 *   <li>{@link com.example.xsdparser.XsdParserConfig} - Dependency injection configuration</li>
 * </ul>
 * 
 * <h2>Package Structure</h2>
 * <ul>
 *   <li>{@code core} - Domain models, ports, and services</li>
 *   <li>{@code application} - Use cases and factories</li>
 *   <li>{@code infrastructure} - Port implementations</li>
 *   <li>{@code generated} - JAXB-generated classes from XSD schemas</li>
 * </ul>
 * 
 * @see com.example.xsdparser.XsdParserApplication
 * @see com.example.xsdparser.XsdParserConfig
 */
package com.example.xsdparser;
