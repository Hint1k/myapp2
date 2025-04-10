package com.bank.webservice.service.impl;

import com.bank.webservice.service.RoleService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.List;

@Service
@Slf4j
public class RoleServiceImpl implements RoleService {

    public void addRoleToModel(HttpServletRequest request, Model model) {
        Object rolesObj = request.getAttribute("roles");
        if (rolesObj instanceof List<?>) {
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) rolesObj;
            String role = roles.isEmpty() ? null : roles.getFirst();
            log.info("Role = {}", role);
            model.addAttribute("role", role);
        } else {
            log.warn("Unexpected roles type: {}", rolesObj);
            model.addAttribute("role", null);
        }
    }
}