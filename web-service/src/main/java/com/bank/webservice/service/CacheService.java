package com.bank.webservice.service;

import java.util.List;
import java.util.Set;

public interface CacheService {

    <T> List<T> getObjectsFromCache(Set<String> keys, Class<T> clazz);

    Set<String> getAllKeys(String prefix);
}