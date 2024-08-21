package com.erp.server.file.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;

@Getter
@Setter
public class DynamicExcelDTO {

    private LinkedHashMap<String, String> headers;

    private LinkedHashMap<String, Object> data;

}
