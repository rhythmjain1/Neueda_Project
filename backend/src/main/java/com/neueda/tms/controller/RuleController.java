package com.neueda.tms.controller;

import com.neueda.tms.dto.RuleDTO;
import com.neueda.tms.service.RuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rules")
@RequiredArgsConstructor
public class RuleController {

    private final RuleService ruleService;

    @GetMapping
    public ResponseEntity<List<RuleDTO.Response>> getAllRules() {
        return ResponseEntity.ok(ruleService.getAllRules());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RuleDTO.Response> getRule(@PathVariable Long id) {
        return ResponseEntity.ok(ruleService.getRule(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RuleDTO.Response> updateRule(
            @PathVariable Long id,
            @RequestBody RuleDTO.UpdateRequest request) {
        return ResponseEntity.ok(ruleService.updateRule(id, request));
    }
}
