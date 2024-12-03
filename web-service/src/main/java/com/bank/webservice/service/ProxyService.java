package com.bank.webservice.service;

import org.springframework.ui.Model;

public interface ProxyService {

   void addUserInfoToModel(String token, Model model);
}