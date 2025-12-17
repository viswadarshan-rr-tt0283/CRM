package com.viswa.crm.dto.common;

import com.viswa.crm.dto.activity.ActivityResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;

    public void success(String message, T data) {
        this.success = true;
        this.message = message;
        this.data = data;
    }

    public void fail(String message) {
        this.success = false;
        this.message = message;
        this.data = null;
    }

}
