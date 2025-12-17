package com.viswa.crm.security.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoleStrategyProvider {

    private final RoleStrategy roleStrategy;

    public RoleStrategy getStrategy(String roleName) {
        return roleStrategy;
    }
}
