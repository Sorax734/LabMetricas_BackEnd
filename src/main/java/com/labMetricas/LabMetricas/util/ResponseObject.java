package com.labMetricas.LabMetricas.util;

import com.labMetricas.LabMetricas.enums.TypeResponse;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ResponseObject {
    private String message;
    private Object data;
    private TypeResponse type;

    public ResponseObject(String message, TypeResponse type) {
        this.message = message;
        this.type = type;
    }

    public ResponseObject(String message, Object data, TypeResponse type) {
        this.message = message;
        this.data = data;
        this.type = type;
    }
} 