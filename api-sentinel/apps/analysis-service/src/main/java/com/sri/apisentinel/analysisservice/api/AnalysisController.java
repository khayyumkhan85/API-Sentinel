package com.sri.apisentinel.analysisservice.api;

import com.sri.apisentinel.analysisservice.service.AnalysisService;
import com.sri.apisentinel.commonmodel.ApiConsumer;
import com.sri.apisentinel.commonmodel.ContractChange;
import com.sri.apisentinel.commonmodel.ImpactAssessment;
import com.sri.apisentinel.commonmodel.MigrationPlan;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analysis")
public class AnalysisController {

    private final AnalysisService service;

    public AnalysisController(AnalysisService service) {
        this.service = service;
    }

    @PostMapping("/impact")
    public ImpactAssessment impact(@RequestBody ImpactRequest req) {
        return service.assess(req.changes(), req.consumers());
    }

    @PostMapping("/risk")
    public RiskResponse risk(@RequestBody RiskRequest req) {
        int score = service.risk(req.changes(), req.consumerCount());
        return new RiskResponse(score);
    }

    @PostMapping("/migration-plan")
    public MigrationPlan plan(@RequestBody PlanRequest req) {
        return service.migrationPlan(req.contractId(), req.apiName(), req.currentVersion(),
                req.changes(), req.consumers());
    }

    public record ImpactRequest(List<ContractChange> changes, List<ApiConsumer> consumers) {}
    public record RiskRequest(List<ContractChange> changes, int consumerCount) {}
    public record RiskResponse(int riskScore) {}
    public record PlanRequest(UUID contractId, String apiName, String currentVersion,
                              List<ContractChange> changes, List<String> consumers) {}
}