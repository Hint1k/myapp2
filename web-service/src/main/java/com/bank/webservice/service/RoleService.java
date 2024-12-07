package com.bank.webservice.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;

public interface RoleService {

    void addRoleToModel(HttpServletRequest request, Model model);
}