package com.common.core.dto;

import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.WriteTable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExcelData<T,R> {

    private T data;

    private List<R> detailList;

    // 表格文件名
    private String filename;

    //多个sheet数据，从第二个sheet开始，不支持模板
    private List<SheetData> sheetDataList;

}

