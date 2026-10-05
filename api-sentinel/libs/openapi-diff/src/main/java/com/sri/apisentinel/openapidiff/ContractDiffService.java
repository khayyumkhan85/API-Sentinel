package com.sri.apisentinel.openapidiff;

import com.sri.apisentinel.commonmodel.ChangeSeverity;
import com.sri.apisentinel.commonmodel.ContractChange;

import java.util.List;
import java.util.UUID;

public class ContractDiffService {

    private final OpenApiParser parser = new OpenApiParser();
    private final OpenApiDiffer differ = new OpenApiDiffer();

    public List<ContractChange> diff(UUID contractId, String oldSpec, String newSpec) {
        var oldApi = parser.parse(oldSpec);
        var newApi = parser.parse(newSpec);
        List<SchemaChange> schemaChanges = differ.diff(oldApi, newApi);
        return schemaChanges.stream()
                .map(c -> ContractChange.of(contractId, c.path(), c.operation(),
                        c.type().name(), c.severity(), c.breaking(), c.description()))
                .toList();
    }

    public ChangeSeverity highestSeverity(List<ContractChange> changes) {
        ChangeSeverity max = ChangeSeverity.NONE;
        for (ContractChange c : changes) {
            if (c.severity().atLeast(max)) {
                max = c.severity();
            }
        }
        return max;
    }
}