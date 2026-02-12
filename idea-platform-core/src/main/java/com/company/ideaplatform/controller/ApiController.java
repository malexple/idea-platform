package com.company.ideaplatform.controller;

import com.company.ideaplatform.service.DivisionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

    private final DivisionService divisionService;

    @GetMapping("/divisions/{divisionId}/tribes")
    public ResponseEntity<List<Map<String, Object>>> getTribesByDivision(@PathVariable Long divisionId) {
        var tribes = divisionService.getTribesByDivision(divisionId).stream()
                .map(t -> Map.<String, Object>of("id", t.getId(), "name", t.getName()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(tribes);
    }

    @GetMapping("/tribes/{tribeId}/teams")
    public ResponseEntity<List<Map<String, Object>>> getTeamsByTribe(@PathVariable Long tribeId) {
        var teams = divisionService.getTeamsByTribe(tribeId).stream()
                .map(t -> Map.<String, Object>of("id", t.getId(), "name", t.getName()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(teams);
    }
}
