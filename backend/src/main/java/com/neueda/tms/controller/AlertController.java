package com.neueda.tms.controller;

import com.neueda.tms.dto.*;
import com.neueda.tms.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @GetMapping
    public ResponseEntity<PageResponse<AlertDTO.Response>> search(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        return ResponseEntity.ok(alertService.searchAlerts(
                status, severity, fromDate, toDate, search, page, size, sortBy, sortDir));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlertDTO.Response> getAlert(@PathVariable Long id) {
        return ResponseEntity.ok(alertService.getAlert(id));
    }

    @GetMapping("/{id}/audit")
    public ResponseEntity<List<AuditTrailDTO>> getAuditTrail(@PathVariable Long id) {
        return ResponseEntity.ok(alertService.getAuditTrail(id));
    }

    @GetMapping("/stats")
    public ResponseEntity<AlertDTO.StatsResponse> getStats() {
        return ResponseEntity.ok(alertService.getStats());
    }

    @GetMapping("/investigation")
    public ResponseEntity<List<AlertDTO.Response>> getForwardedAlerts() {
        return ResponseEntity.ok(alertService.getForwardedAlerts());
    }

    @PostMapping("/{id}/forward")
    public ResponseEntity<AlertDTO.Response> forward(
            @PathVariable Long id,
            @RequestBody(required = false) AlertDTO.ActionRequest request,
            @AuthenticationPrincipal UserDetails operator) {
        if (request == null) request = new AlertDTO.ActionRequest();
        return ResponseEntity.ok(alertService.forwardAlert(id, request, operator.getUsername()));
    }

    @PostMapping("/{id}/dismiss")
    public ResponseEntity<AlertDTO.Response> dismiss(
            @PathVariable Long id,
            @RequestBody(required = false) AlertDTO.ActionRequest request,
            @AuthenticationPrincipal UserDetails operator) {
        if (request == null) request = new AlertDTO.ActionRequest();
        return ResponseEntity.ok(alertService.dismissAlert(id, request, operator.getUsername()));
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<AlertDTO.Response> close(
            @PathVariable Long id,
            @RequestBody(required = false) AlertDTO.ActionRequest request,
            @AuthenticationPrincipal UserDetails operator) {
        if (request == null) request = new AlertDTO.ActionRequest();
        return ResponseEntity.ok(alertService.closeAlert(id, request, operator.getUsername()));
    }
}
