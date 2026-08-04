package com.neueda.tms.service;

import com.neueda.tms.dto.RuleDTO;

import java.util.List;

public interface IRuleService {

    List<RuleDTO.Response> getAllRules();

    RuleDTO.Response getRule(Long id);

    RuleDTO.Response updateRule(Long id, RuleDTO.UpdateRequest request);
}
