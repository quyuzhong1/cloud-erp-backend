package com.common.core.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class MultiErrorExcelData{

    private Integer sheetNo;

    private String sheetName;

    private List<?> dataResult;

    private Class<?> clazz;

}
