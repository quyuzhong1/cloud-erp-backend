package com.erp.server.mrp.listener;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.core.utils.FieldValidUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 销售预估月销导入
 * @author will
 * @date 2024/8/30 16:59
 */
@Getter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SalesEstimateExcelListener extends AnalysisEventListener<Map<Integer,String>> {


    /**
     * 导入数据，用于判断导入是否为空
     */
    private final List<JSONObject> allList = new ArrayList<>();

    /**
     * 导入错误数据
     */
    private final List<JSONObject> errorList = new ArrayList<>();

    /**
     * 导入正确数据
     */
    private final List<JSONObject> successList = new ArrayList<>();

    private Map<Integer,String> headMap;

    private List<String> headList;

    @Override
    public void invoke(Map<Integer,String> map, AnalysisContext analysisContext) {
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
        allList.add(excelDTO);
        //存在错误数据则直接返回
        if (errorMsgList.size() > 0) {
            String errorMsg = FieldValidUtil.getMsgSort(errorMsgList);
            excelDTO.set("错误信息",errorMsg);
            errorList.add(excelDTO);
            return;
        }

        successList.add(excelDTO);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }

    @Override
    public void invokeHeadMap(Map<Integer,String> map, AnalysisContext analysisContext) {
        List<String> headList = map.values().stream().map(obj -> obj.toString()).collect(Collectors.toList());
        headList.add("错误信息");
        map.put(map.size(),"错误信息");
        this.headMap = map;
        this.headList = headList;
    }

    public List<String> getHeadList() {
        return headList;
    }
}
