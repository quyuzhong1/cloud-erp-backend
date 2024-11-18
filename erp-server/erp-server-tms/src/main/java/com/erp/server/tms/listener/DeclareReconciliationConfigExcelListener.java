package com.erp.server.tms.listener;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.core.utils.FieldValidUtil;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @description: 报关对账单配置模板监听
 * @author Will
 * @date: 2024/3/27 12:01
 */
public class DeclareReconciliationConfigExcelListener extends AnalysisEventListener<Map<Integer,String>> {

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



   /**
    * @description: 每解析一行数据回调一遍
    * @author Will
    * @date: 2023/3/7 11:22
    * @param map 导入信息
    * @param analysisContext
    */
    @Override
    public void invoke(Map<Integer,String>  map, AnalysisContext analysisContext) {
        List<String> errorMsgList = new ArrayList<>();
        //当导入的最后一列数据都是空时map无值导致表头size和map.size不一致，所以需要添加表头一致的数据
        for (Map.Entry<Integer,String> entry : headMap.entrySet()) {
            String value = map.get(entry.getKey());
            if (ObjectUtil.isEmpty(value)) {
                map.put(entry.getKey(),"");
            }
        }
        JSONObject excelDTO = new JSONObject(map);
        //添加数据用于判断是否为空
        dataList.add(excelDTO);
        //存在错误数据则直接返回
        if (errorMsgList.size() > 0) {
            String errorMsg = FieldValidUtil.getMsgSort(errorMsgList);
            excelDTO.set("错误信息",errorMsg);
            errorList.add(excelDTO);
            return;
        }

        successList.add(excelDTO);
    }

    public List<JSONObject> getErrorList(){
        return errorList;
    }

    public List<JSONObject> getSuccessList(){
        return successList;
    }

    public List<JSONObject> getExcelDateList(){
        return dataList;
    }

    /**
     * @description: 数据全部解析完后删除明细
     * @author Will
     * @date: 2023/3/7 15:32
     * @param analysisContext
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        if(CollectionUtils.isEmpty(successList)){
            return;
        }
    }

    @Override
    public void invokeHeadMap(Map<Integer,String> map, AnalysisContext analysisContext) {
        List<String> currencyHeadList = map.values().stream().map(obj -> obj.toString()).collect(Collectors.toList());
        currencyHeadList.add("错误信息");
        this.headMap = map;
        this.headList = currencyHeadList;
    }

    public List<String> getHeadList() {
        return headList;
    }
}
