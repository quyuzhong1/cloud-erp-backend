package com.common.core.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class ExcelData<T,R> {

    private T data;

    private List<R> detailList;

    // 表格文件名
    private String filename;

}
