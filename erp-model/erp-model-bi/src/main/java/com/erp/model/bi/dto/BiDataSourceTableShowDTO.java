package com.erp.model.bi.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/20 22:49
 */
@Data
public class BiDataSourceTableShowDTO {

    /**
     * 表头
     */
    private Map<String,String> heads;

    /**
     * 数据体
     */
    private List<Map<String,String>> dataList;
}
