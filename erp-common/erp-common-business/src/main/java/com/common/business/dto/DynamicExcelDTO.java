package com.common.business.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.List;

@Getter
@Setter
public class DynamicExcelDTO {

    private LinkedHashMap<String, String> headers;

    private List<LinkedHashMap<String, Object>> data;

    private String sheetName;
}
