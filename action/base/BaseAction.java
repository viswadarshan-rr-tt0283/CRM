package com.viswa.crm.action.base;

import com.opensymphony.xwork2.ActionSupport;
import com.viswa.crm.dto.auth.LoginResponse;
import com.viswa.crm.security.strategy.RoleStrategy;
import com.viswa.crm.security.strategy.RoleStrategyProvider;
import org.apache.struts2.interceptor.SessionAware;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

// This application follows SOLID principles
// And all necessary Design Patterns
// MVC pattern
public abstract class BaseAction extends ActionSupport implements SessionAware {

    protected Map<String, Object> session;

    public static final String USER_SESSION_KEY = "LOGGED_IN_USER";

    @Autowired
    private RoleStrategyProvider roleStrategyProvider;

    @Override
    public void setSession(Map<String, Object> session) {
        this.session = session;
    }

    public LoginResponse getCurrentUser() {
        return (LoginResponse) session.get(USER_SESSION_KEY);
    }

    public boolean isAuthenticated() {
        return getCurrentUser() != null;
    }

    protected RoleStrategy role() {
        if (!isAuthenticated()) {
            throw new RuntimeException("Not authenticated");
        }
        return roleStrategyProvider.getStrategy(
                getCurrentUser().getRoleName()
        );
    }

    protected void clearSession() {
        if (session != null) {
            session.clear();
        }
    }
}