package com.example.xsdparser.infrastructure.schema;

import com.example.xsdparser.core.model.SchemaInfo;
import com.example.xsdparser.core.port.SchemaRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Flexible schema initializer that can:
 * 1. Auto-discover XSD files from the classpath
 * 2. Register schemas programmatically via SchemaDefinition
 * 3. Load schema definitions from configuration
 * 
 * This makes adding new XSD schemas much easier - just add the file
 * to the xsd/ directory and it will be auto-discovered.
 */
public class FlexibleSchemaInitializer {

    private static final Logger logger = LoggerFactory.getLogger(FlexibleSchemaInitializer.class);

    private static final String DEFAULT_XSD_PATH = "xsd";
    private static final String DEFAULT_NAMESPACE_PREFIX = "urn:iso:std:iso:20022:tech:xsd:";
    private static final Pattern XSD_FILENAME_PATTERN = Pattern.compile(
            "([a-z]+)\\.([0-9]+)\\.([0-9]+)\\.([0-9]+)\\.xsd"
    );

    private final SchemaRegistry registry;
    private final List<SchemaDefinition> additionalSchemas;
    private final String xsdResourcePath;
    private final boolean autoDiscover;

    private FlexibleSchemaInitializer(Builder builder) {
        this.registry = Objects.requireNonNull(builder.registry, "Registry is required");
        this.additionalSchemas = new ArrayList<>(builder.additionalSchemas);
        this.xsdResourcePath = builder.xsdResourcePath != null ? builder.xsdResourcePath : DEFAULT_XSD_PATH;
        this.autoDiscover = builder.autoDiscover;
    }

    /**
     * Initializes schemas based on configuration.
     * If auto-discover is enabled, scans the XSD directory for schema files.
     */
    public void initialize() {
        logger.info("Initializing schemas (auto-discover: {})", autoDiscover);

        if (autoDiscover) {
            discoverAndRegisterSchemas();
        }

        for (SchemaDefinition definition : additionalSchemas) {
            registerSchema(definition);
        }

        logger.info("Registered {} schemas total", registry.getAllSchemas().size());
    }

    private void discoverAndRegisterSchemas() {
        logger.debug("Auto-discovering XSD files from: {}", xsdResourcePath);

        try {
            List<String> xsdFiles = findXsdFiles();
            
            for (String filename : xsdFiles) {
                try {
                    SchemaDefinition definition = parseFilename(filename);
                    if (definition != null) {
                        registerSchema(definition);
                    }
                } catch (Exception e) {
                    logger.warn("Failed to parse XSD filename: {} - {}", filename, e.getMessage());
                }
            }
        } catch (Exception e) {
            logger.error("Failed to discover XSD files", e);
        }
    }

    private List<String> findXsdFiles() throws IOException, URISyntaxException {
        List<String> xsdFiles = new ArrayList<>();
        ClassLoader classLoader = getClass().getClassLoader();
        
        URL resourceUrl = classLoader.getResource(xsdResourcePath);
        if (resourceUrl == null) {
            logger.warn("XSD resource path not found: {}", xsdResourcePath);
            return xsdFiles;
        }

        URI uri = resourceUrl.toURI();
        
        if (uri.getScheme().equals("jar")) {
            try (FileSystem fileSystem = getFileSystem(uri)) {
                Path path = fileSystem.getPath(xsdResourcePath);
                xsdFiles.addAll(collectXsdFiles(path));
            }
        } else {
            Path path = Paths.get(uri);
            xsdFiles.addAll(collectXsdFiles(path));
        }

        return xsdFiles;
    }

    private FileSystem getFileSystem(URI uri) throws IOException {
        try {
            return FileSystems.getFileSystem(uri);
        } catch (FileSystemNotFoundException e) {
            return FileSystems.newFileSystem(uri, Collections.emptyMap());
        }
    }

    private List<String> collectXsdFiles(Path directory) throws IOException {
        List<String> files = new ArrayList<>();
        
        if (Files.exists(directory) && Files.isDirectory(directory)) {
            try (Stream<Path> stream = Files.list(directory)) {
                stream.filter(path -> path.toString().endsWith(".xsd"))
                      .map(path -> path.getFileName().toString())
                      .forEach(files::add);
            }
        }
        
        return files;
    }

    private SchemaDefinition parseFilename(String filename) {
        Matcher matcher = XSD_FILENAME_PATTERN.matcher(filename);
        
        if (matcher.matches()) {
            String messageFamily = matcher.group(1);
            String messageNumber = matcher.group(2);
            String versionPart = matcher.group(3) + "." + matcher.group(4);
            
            String messageType = messageFamily + "." + messageNumber;
            
            return SchemaDefinition.builder()
                    .messageType(messageType)
                    .version(versionPart)
                    .description("Auto-discovered from " + filename)
                    .build();
        }
        
        logger.debug("Filename does not match expected pattern: {}", filename);
        return null;
    }

    private void registerSchema(SchemaDefinition definition) {
        if (registry.isRegistered(definition.getFullIdentifier())) {
            logger.debug("Schema already registered: {}", definition.getFullIdentifier());
            return;
        }

        SchemaInfo schemaInfo = SchemaInfo.builder()
                .messageType(definition.getMessageType())
                .version(definition.getVersion())
                .namespace(definition.getNamespace())
                .schemaPath(Paths.get(definition.getSchemaResourcePath()))
                .description(definition.getDescription())
                .build();

        registry.register(schemaInfo);
        logger.debug("Registered schema: {}", definition.getFullIdentifier());
    }

    /**
     * Creates a builder for configuring the schema initializer.
     */
    public static Builder builder(SchemaRegistry registry) {
        return new Builder(registry);
    }

    /**
     * Creates an initializer with auto-discovery enabled.
     */
    public static FlexibleSchemaInitializer withAutoDiscovery(SchemaRegistry registry) {
        return new Builder(registry)
                .autoDiscover(true)
                .build();
    }

    /**
     * Builder for configuring FlexibleSchemaInitializer.
     */
    public static class Builder {
        private final SchemaRegistry registry;
        private final List<SchemaDefinition> additionalSchemas = new ArrayList<>();
        private String xsdResourcePath;
        private boolean autoDiscover = true;

        private Builder(SchemaRegistry registry) {
            this.registry = registry;
        }

        /**
         * Sets the resource path to scan for XSD files.
         */
        public Builder xsdResourcePath(String path) {
            this.xsdResourcePath = path;
            return this;
        }

        /**
         * Enables or disables auto-discovery of XSD files.
         */
        public Builder autoDiscover(boolean autoDiscover) {
            this.autoDiscover = autoDiscover;
            return this;
        }

        /**
         * Adds a schema definition to register.
         */
        public Builder addSchema(SchemaDefinition definition) {
            this.additionalSchemas.add(definition);
            return this;
        }

        /**
         * Adds a PACS schema definition.
         */
        public Builder addPacsSchema(String number, String version, String description) {
            return addSchema(SchemaDefinition.pacs(number, version)
                    .description(description)
                    .build());
        }

        /**
         * Adds a PAIN schema definition.
         */
        public Builder addPainSchema(String number, String version, String description) {
            return addSchema(SchemaDefinition.pain(number, version)
                    .description(description)
                    .build());
        }

        /**
         * Adds a CAMT schema definition.
         */
        public Builder addCamtSchema(String number, String version, String description) {
            return addSchema(SchemaDefinition.camt(number, version)
                    .description(description)
                    .build());
        }

        /**
         * Adds multiple schema definitions.
         */
        public Builder addSchemas(Collection<SchemaDefinition> definitions) {
            this.additionalSchemas.addAll(definitions);
            return this;
        }

        public FlexibleSchemaInitializer build() {
            return new FlexibleSchemaInitializer(this);
        }
    }
}
