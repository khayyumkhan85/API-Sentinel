package com.sri.apisentinel.contractservice.api;

import com.sri.apisentinel.commonmodel.ApiContract;
import com.sri.apisentinel.commonmodel.ContractChange;
import com.sri.apisentinel.contractservice.service.ContractAnalysisService;
import com.sri.apisentinel.contractservice.service.ContractService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/contracts")
public class ContractController {

    private final ContractService contractService;
    private final ContractAnalysisService analysisService;

    public ContractController(ContractService contractService, ContractAnalysisService analysisService) {
        this.contractService = contractService;
        this.analysisService = analysisService;
    }

    @PostMapping
    public ResponseEntity<ApiContract> register(@Valid @RequestBody RegisterContractRequest req) {
        ApiContract created = contractService.register(req.name(), req.version(),
                req.specification(), req.owner(), req.repository());
        return ResponseEntity.created(URI.create("/api/v1/contracts/" + created.id())).body(created);
    }

    @GetMapping("/{id}")
    public ApiContract get(@PathVariable UUID id) {
        return contractService.get(id);
    }

    @GetMapping
    public List<ApiContract> list(@RequestParam(required = false) String name) {
        return name == null ? contractService.listAll() : contractService.listByName(name);
    }

    @PostMapping("/{oldId}/compare/{newId}")
    public List<ContractChange> compare(@PathVariable UUID oldId, @PathVariable UUID newId) {
        return analysisService.compare(oldId, newId);
    }

    public record RegisterContractRequest(
            @NotBlank String name,
            @NotBlank String version,
            @NotBlank String specification,
            String owner,
            String repository
    ) {}
}