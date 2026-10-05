package com.sri.apisentinel.openapidiff;

import com.sri.apisentinel.commonmodel.ChangeType;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OpenApiDiffer {

    public List<SchemaChange> diff(OpenAPI oldApi, OpenAPI newApi) {
        List<SchemaChange> changes = new ArrayList<>();

        Map<String, PathItem> oldPaths = oldApi.getPaths() == null ? Map.of() : oldApi.getPaths();
        Map<String, PathItem> newPaths = newApi.getPaths() == null ? Map.of() : newApi.getPaths();

        for (String path : oldPaths.keySet()) {
            if (!newPaths.containsKey(path)) {
                changes.add(SchemaChange.breaking(path, "*", ChangeType.ENDPOINT_REMOVED, null,
                        "Endpoint %s was removed".formatted(path)));
            } else {
                changes.addAll(diffOperations(path, oldPaths.get(path), newPaths.get(path)));
            }
        }
        for (String path : newPaths.keySet()) {
            if (!oldPaths.containsKey(path)) {
                changes.add(SchemaChange.nonBreaking(path, "*", ChangeType.ENDPOINT_ADDED, null,
                        "Endpoint %s was added".formatted(path)));
            }
        }

        Map<String, Schema> oldSchemas = schemas(oldApi);
        Map<String, Schema> newSchemas = schemas(newApi);
        for (var entry : oldSchemas.entrySet()) {
            Schema newSchema = newSchemas.get(entry.getKey());
            if (newSchema == null) {
                changes.add(SchemaChange.breaking("#/components/schemas/" + entry.getKey(),
                        null, ChangeType.SCHEMA_RENAMED, entry.getKey(),
                        "Schema %s was removed".formatted(entry.getKey())));
            } else {
                changes.addAll(diffSchema(entry.getKey(), entry.getValue(), newSchema));
            }
        }

        return changes;
    }

    private Map<String, Schema> schemas(OpenAPI api) {
        if (api.getComponents() == null || api.getComponents().getSchemas() == null) {
            return Map.of();
        }
        return api.getComponents().getSchemas();
    }

    private List<SchemaChange> diffOperations(String path, PathItem oldItem, PathItem newItem) {
        List<SchemaChange> changes = new ArrayList<>();
        for (var method : PathItem.HttpMethod.values()) {
            Operation oldOp = oldItem.readOperationsMap().get(method);
            Operation newOp = newItem.readOperationsMap().get(method);
            if (oldOp != null && newOp == null) {
                changes.add(SchemaChange.breaking(path, method.name(), ChangeType.OPERATION_REMOVED, null,
                        "Operation %s %s was removed".formatted(method, path)));
            } else if (oldOp == null && newOp != null) {
                changes.add(SchemaChange.nonBreaking(path, method.name(), ChangeType.OPERATION_ADDED, null,
                        "Operation %s %s was added".formatted(method, path)));
            } else if (oldOp != null) {
                changes.addAll(diffParameters(path, method.name(), oldOp, newOp));
            }
        }
        return changes;
    }

    private List<SchemaChange> diffParameters(String path, String op, Operation oldOp, Operation newOp) {
        List<SchemaChange> changes = new ArrayList<>();
        List<Parameter> oldParams = oldOp.getParameters() == null ? List.of() : oldOp.getParameters();
        List<Parameter> newParams = newOp.getParameters() == null ? List.of() : newOp.getParameters();

        Set<String> oldNames = new HashSet<>();
        for (Parameter p : oldParams) oldNames.add(p.getName());
        Set<String> newNames = new HashSet<>();
        for (Parameter p : newParams) newNames.add(p.getName());

        for (String name : oldNames) {
            if (!newNames.contains(name)) {
                changes.add(SchemaChange.breaking(path, op, ChangeType.PARAMETER_REMOVED, name,
                        "Parameter '%s' was removed from %s %s".formatted(name, op, path)));
            }
        }
        for (Parameter p : newParams) {
            if (!oldNames.contains(p.getName())) {
                boolean required = Boolean.TRUE.equals(p.getRequired());
                if (required) {
                    changes.add(SchemaChange.breaking(path, op, ChangeType.PARAMETER_ADDED, p.getName(),
                            "Required parameter '%s' was added to %s %s".formatted(p.getName(), op, path)));
                } else {
                    changes.add(SchemaChange.nonBreaking(path, op, ChangeType.PARAMETER_ADDED, p.getName(),
                            "Optional parameter '%s' was added to %s %s".formatted(p.getName(), op, path)));
                }
            }
        }
        return changes;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private List<SchemaChange> diffSchema(String name, Schema oldSchema, Schema newSchema) {
        List<SchemaChange> changes = new ArrayList<>();
        String basePath = "#/components/schemas/" + name;

        Map<String, Schema> oldProps = oldSchema.getProperties() == null ? Map.of() : oldSchema.getProperties();
        Map<String, Schema> newProps = newSchema.getProperties() == null ? Map.of() : newSchema.getProperties();
        Set<String> oldRequired = oldSchema.getRequired() == null ? Set.of() : new HashSet<>(oldSchema.getRequired());
        Set<String> newRequired = newSchema.getRequired() == null ? Set.of() : new HashSet<>(newSchema.getRequired());

        for (var entry : oldProps.entrySet()) {
            String propName = entry.getKey();
            Schema newProp = newProps.get(propName);
            boolean wasRequired = oldRequired.contains(propName);
            if (newProp == null) {
                ChangeType type = wasRequired ? ChangeType.REQUIRED_PROPERTY_REMOVED : ChangeType.PROPERTY_REMOVED;
                changes.add(SchemaChange.breaking(basePath, null, type, propName,
                        "Property '%s' removed from schema '%s'".formatted(propName, name)));
            } else {
                String oldType = String.valueOf(entry.getValue().getType());
                String newType = String.valueOf(newProp.getType());
                if (oldType != null && !oldType.equals(newType)) {
                    changes.add(SchemaChange.breaking(basePath, null, ChangeType.PROPERTY_TYPE_CHANGED, propName,
                            "Property '%s' changed type from %s to %s".formatted(propName, oldType, newType)));
                }
                changes.addAll(diffEnums(basePath, propName, entry.getValue(), newProp));
            }
        }
        for (String propName : newProps.keySet()) {
            if (!oldProps.containsKey(propName)) {
                boolean nowRequired = newRequired.contains(propName);
                if (nowRequired) {
                    changes.add(SchemaChange.breaking(basePath, null, ChangeType.REQUIRED_PROPERTY_ADDED, propName,
                            "Required property '%s' added to schema '%s'".formatted(propName, name)));
                } else {
                    changes.add(SchemaChange.nonBreaking(basePath, null, ChangeType.PROPERTY_ADDED, propName,
                            "Optional property '%s' added to schema '%s'".formatted(propName, name)));
                }
            }
        }
        for (String req : newRequired) {
            if (oldProps.containsKey(req) && !oldRequired.contains(req)) {
                changes.add(SchemaChange.breaking(basePath, null, ChangeType.REQUIRED_PROPERTY_ADDED, req,
                        "Property '%s' became required".formatted(req)));
            }
        }
        return changes;
    }

    @SuppressWarnings("rawtypes")
    private List<SchemaChange> diffEnums(String basePath, String propName, Schema oldSchema, Schema newSchema) {
        List oldEnum = oldSchema.getEnum() == null ? Collections.emptyList() : oldSchema.getEnum();
        List newEnum = newSchema.getEnum() == null ? Collections.emptyList() : newSchema.getEnum();
        List<SchemaChange> changes = new ArrayList<>();
        for (Object v : oldEnum) {
            if (!newEnum.contains(v)) {
                changes.add(SchemaChange.breaking(basePath, null, ChangeType.ENUM_VALUE_REMOVED, propName,
                        "Enum value '%s' removed from '%s'".formatted(v, propName)));
            }
        }
        for (Object v : newEnum) {
            if (!oldEnum.contains(v)) {
                changes.add(SchemaChange.nonBreaking(basePath, null, ChangeType.ENUM_VALUE_ADDED, propName,
                        "Enum value '%s' added to '%s'".formatted(v, propName)));
            }
        }
        return changes;
    }
}