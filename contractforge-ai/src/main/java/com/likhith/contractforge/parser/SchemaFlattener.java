package com.likhith.contractforge.parser;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.likhith.contractforge.model.FieldSnapshot;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;

/**
 * Recursively resolves local {@code #/components/schemas/...} references and
 * flattens an object schema into a deterministic, alphabetically-sorted list of
 * leaf {@link FieldSnapshot}s. Pure object containers (e.g. "scheduleDetails")
 * are walked but never emitted themselves - only fields that a caller would
 * actually need to set a value for produce a snapshot.
 *
 * <p>Cycle safety: a schema name is pushed onto {@code refChain} only while its
 * properties are being walked, and popped immediately after (standard DFS
 * backtracking). If the same schema name is encountered again while it is still
 * on the chain, that is a genuine circular reference (e.g. Customer -> Address
 * -> Customer) and recursion stops there. The same schema reused in two
 * unrelated branches (e.g. MoneyAmountDto used for both "amount" and
 * "tipAmount") is fine, since each branch pops the name before the next begins.
 */
@Component
public class SchemaFlattener {

    private static final Logger log = LoggerFactory.getLogger(SchemaFlattener.class);

    public List<FieldSnapshot> flatten(Schema<?> rootSchema, OpenAPI openApi) {
        List<FieldSnapshot> fields = new ArrayList<>();
        processSchema("", null, rootSchema, false, fields, openApi, new LinkedHashSet<>());
        fields.sort(Comparator.comparing(FieldSnapshot::jsonPath));
        return fields;
    }

    private void processSchema(String jsonPath, String fieldName, Schema<?> schema, boolean required,
            List<FieldSnapshot> out, OpenAPI openApi, Set<String> refChain) {

        String refName = schema.get$ref() != null ? simpleRefName(schema.get$ref()) : null;
        Schema<?> target = schema;

        if (refName != null) {
            if (refChain.contains(refName)) {
                log.debug("Circular reference at '{}' -> #/components/schemas/{}; stopping recursion",
                        jsonPath, refName);
                return;
            }
            target = lookupSchema(refName, openApi);
            if (target == null) {
                log.warn("Unresolved schema reference '#/components/schemas/{}' from '{}'", refName, jsonPath);
                return;
            }
        }

        boolean isArray = "array".equals(target.getType());
        boolean isObject = isObjectLike(target);

        if (refName != null) {
            refChain.add(refName);
        }
        try {
            if (isArray) {
                emitArrayField(jsonPath, fieldName, target, required, out, openApi, refChain);
            } else if (isObject) {
                walkObjectProperties(jsonPath, target, out, openApi, refChain);
            } else {
                out.add(buildLeaf(jsonPath, fieldName, target, required));
            }
        } finally {
            if (refName != null) {
                refChain.remove(refName);
            }
        }
    }

    private void walkObjectProperties(String pathPrefix, Schema<?> objectSchema, List<FieldSnapshot> out,
            OpenAPI openApi, Set<String> refChain) {

        Map<String, Schema> properties = objectSchema.getProperties();
        if (properties == null || properties.isEmpty()) {
            log.debug("Object schema at '{}' has no declared properties; nothing to flatten",
                    pathPrefix.isEmpty() ? "<root>" : pathPrefix);
            return;
        }

        Set<String> requiredNames = objectSchema.getRequired() == null
                ? Set.of()
                : new HashSet<>(objectSchema.getRequired());

        for (Map.Entry<String, Schema> entry : properties.entrySet()) {
            String name = entry.getKey();
            String childPath = pathPrefix.isEmpty() ? name : pathPrefix + "." + name;
            processSchema(childPath, name, entry.getValue(), requiredNames.contains(name), out, openApi, refChain);
        }
    }

    private void emitArrayField(String jsonPath, String fieldName, Schema<?> arraySchema, boolean required,
            List<FieldSnapshot> out, OpenAPI openApi, Set<String> refChain) {

        Schema<?> itemsSchema = arraySchema.getItems();
        String itemType = itemsSchema == null ? "unknown" : describeType(itemsSchema, openApi);

        out.add(new FieldSnapshot(
                jsonPath, fieldName, "array", arraySchema.getFormat(), required, isNullable(arraySchema),
                arraySchema.getDescription(), stringify(arraySchema.getExample()), enumValues(arraySchema),
                arraySchema.getMinimum(), arraySchema.getMaximum(), arraySchema.getMinLength(),
                arraySchema.getMaxLength(), arraySchema.getPattern(), true, itemType));

        if (itemsSchema == null) {
            return;
        }

        // Scalar/enum array items are already fully described by the FieldSnapshot above
        // (via arrayItemType); only object items need further recursion for their own fields.
        String itemRefName = itemsSchema.get$ref() != null ? simpleRefName(itemsSchema.get$ref()) : null;
        Schema<?> resolvedItem = itemsSchema;

        if (itemRefName != null) {
            if (refChain.contains(itemRefName)) {
                log.debug("Circular reference at '{}[]' -> #/components/schemas/{}; stopping recursion",
                        jsonPath, itemRefName);
                return;
            }
            resolvedItem = lookupSchema(itemRefName, openApi);
            if (resolvedItem == null) {
                log.warn("Unresolved schema reference '#/components/schemas/{}' from '{}[]'", itemRefName, jsonPath);
                return;
            }
        }

        if (!isObjectLike(resolvedItem)) {
            return;
        }

        if (itemRefName != null) {
            refChain.add(itemRefName);
        }
        try {
            walkObjectProperties(jsonPath + "[]", resolvedItem, out, openApi, refChain);
        } finally {
            if (itemRefName != null) {
                refChain.remove(itemRefName);
            }
        }
    }

    private FieldSnapshot buildLeaf(String jsonPath, String fieldName, Schema<?> schema, boolean required) {
        String type = schema.getType() != null ? schema.getType() : "object";
        return new FieldSnapshot(
                jsonPath, fieldName, type, schema.getFormat(), required, isNullable(schema),
                schema.getDescription(), stringify(schema.getExample()), enumValues(schema),
                schema.getMinimum(), schema.getMaximum(), schema.getMinLength(), schema.getMaxLength(),
                schema.getPattern(), false, null);
    }

    /** One-level type label used for arrayItemType, without recursing into the item's own fields. */
    private String describeType(Schema<?> schema, OpenAPI openApi) {
        Schema<?> target = schema;
        if (schema.get$ref() != null) {
            target = lookupSchema(simpleRefName(schema.get$ref()), openApi);
            if (target == null) {
                return "unknown";
            }
        }
        if (isObjectLike(target)) {
            return "object";
        }
        return target.getType() != null ? target.getType() : "unknown";
    }

    private boolean isObjectLike(Schema<?> schema) {
        return "object".equals(schema.getType()) || (schema.getType() == null && schema.getProperties() != null);
    }

    private boolean isNullable(Schema<?> schema) {
        return Boolean.TRUE.equals(schema.getNullable());
    }

    private String stringify(Object example) {
        return example == null ? null : String.valueOf(example);
    }

    private List<String> enumValues(Schema<?> schema) {
        if (schema.getEnum() == null || schema.getEnum().isEmpty()) {
            return List.of();
        }
        return schema.getEnum().stream().map(String::valueOf).sorted().toList();
    }

    private Schema<?> lookupSchema(String refName, OpenAPI openApi) {
        if (openApi.getComponents() == null || openApi.getComponents().getSchemas() == null) {
            return null;
        }
        return openApi.getComponents().getSchemas().get(refName);
    }

    private String simpleRefName(String ref) {
        return ref.substring(ref.lastIndexOf('/') + 1);
    }
}
