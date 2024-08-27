package com.erp.server.file.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
public class DynamicExcelDTO {

    private LinkedHashMap<String, String> headers;

    private Map<String, Object> data;

}
