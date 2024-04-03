package com.erp.server.tms.listener;

import cn.hutool.json.JSONObject;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.core.utils.FieldValidUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;



/**
 * 头程对账单配置模板监听
 *
 * @author Jim
 * {@code @date:} 2024/03/22
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FirstMileReconciliationConfigExcelListener extends AnalysisEventListener<Map<Integer,String>> {

    /**
     * 错误信息
     */
    private List<JSONObject> errorList = new ArrayList<>();
    /**
     * 全部数据（用于判断导入是否为空）
     */
    private List<JSONObject> dataList = new ArrayList<>();

    /**
     * 成功信息
     */
    private List<JSONObject> successList = new ArrayList<>();

    private Map<Integer,String> headMap;

    private List<String> headList;


    public FirstMileReconciliationConfigExcelListener() {
    }

   /**
    * @description: 每解析一行数据回调一遍
    * @author Will
    * @date: 2023/3/7 11:22
    * @param map 导入信息
    * @param analysisContext
    */
    @Override
    public void invoke(Map<Integer,String>  map, AnalysisContext analysisContext) {
        JSONObject excelDTO = new JSONObject(map);
        List<String> errorMsgList = new ArrayList<>();

        //添加数据用于判断是否为空
        dataList.add(excelDTO);
        //存在错误数据则直接返回
        if (!errorMsgList.isEmpty()) {
            String errorMsg = FieldValidUtil.getMsgSort(errorMsgList);
            excelDTO.set("错误信息",errorMsg);
            errorList.add(excelDTO);
            return;
        }

        successList.add(excelDTO);
    }


    /**
     * 数据全部解析完后删除明细
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }

    @Override
    public void invokeHeadMap(Map<Integer,String> map, AnalysisContext analysisContext) {
        List<String> headList = map.values().stream().map(obj -> obj.toString()).collect(Collectors.toList());
        headList.add("错误信息");
        this.headMap = map;
        this.headList = headList;
    }
}
