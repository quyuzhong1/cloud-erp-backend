package com.common.core.dto;

import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.WriteTable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SheetData<T>{

    private WriteSheet writeSheet;

    //头信息
    private WriteTable headWriteTable;

    //明细信息
    private WriteTable detailWriteTable;

    private List<T> detailDataList;

    private List<List<String>> headDataList;
}

