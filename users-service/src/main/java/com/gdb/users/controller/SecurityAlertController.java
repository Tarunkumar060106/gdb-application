package com.gdb.users.controller;

import com.gdb.users.domain.model.SecurityAlertLog;
import com.gdb.users.repository.SecurityAlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for retrieving security alerts with pagination and sorting.
 */
@RestController
@RequestMapping("/api/v1/users/security-alerts")
@RequiredArgsConstructor
public class SecurityAlertController {

    private final SecurityAlertRepository alertRepository;

    @GetMapping
    public Page<SecurityAlertLog> getAlerts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "alertDate") String sortBy) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sortBy));
        return alertRepository.findAll(pageRequest);
    }
}
