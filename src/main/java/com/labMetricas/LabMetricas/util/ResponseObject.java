package com.labMetricas.LabMetricas.util;

import com.labMetricas.LabMetricas.enums.TypeResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseObject {
    private String message;
    private Object data;
    private TypeResponse type;

    public ResponseObject(String message, TypeResponse type) {
        this.message = message;
        this.type = type;
    }
} 