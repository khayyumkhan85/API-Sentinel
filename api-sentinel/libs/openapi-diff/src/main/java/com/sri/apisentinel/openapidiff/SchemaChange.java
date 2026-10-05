package com.sri.apisentinel.openapidiff;

import com.sri.apisentinel.commonmodel.ChangeSeverity;
import com.sri.apisentinel.commonmodel.ChangeType;

public record SchemaChange(
        String path,
        String operation,
        ChangeType type,
        String propertyName,
        String oldValue,
        String newValue,
        ChangeSeverity severity,
        boolean breaking,
        String description
) {

    public static SchemaChange breaking(String path, String operation, ChangeType type,
                                        String propertyName, String description) {
        ChangeSeverity sev = switch (type) {
            case ENDPOINT_REMOVED, OPERATION_REMOVED, REQUIRED_PROPERTY_REMOVED -> ChangeSeverity.CRITICAL;
            case PROPERTY_TYPE_CHANGED, ENUM_VALUE_REMOVED, PARAMETER_ADDED -> ChangeSeverity.HIGH;
            default -> ChangeSeverity.MEDIUM;
        };
        return new SchemaChange(path, operation, type, propertyName, null, null, sev, true, description);
    }

    public static SchemaChange nonBreaking(String path, String operation, ChangeType type,
                                           String propertyName, String description) {
        ChangeSeverity sev = switch (type) {
            case ENDPOINT_ADDED, OPERATION_ADDED, PROPERTY_ADDED, ENUM_VALUE_ADDED, RESPONSE_CODE_ADDED -> ChangeSeverity.NONE;
            case DEPRECATION_ADDED -> ChangeSeverity.LOW;
            default -> ChangeSeverity.LOW;
        };
        return new SchemaChange(path, operation, type, propertyName, null, null, sev, false, description);
    }
}