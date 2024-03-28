package com.erp.server.tms.listener;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.tms.dto.CfgReconciliationFieldDTO;
import com.erp.model.tms.entity.DictBasicEntity;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @description: 报关对账单配置模板监听
 * @author Will
 * @date: 2024/3/27 12:01
 */
public class DeclareReconciliationConfigExcelListener extends AnalysisEventListener<JSONObject> {

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

    /**
     * 字段配置
     */
    private Map<String, CfgReconciliationFieldDTO.ErpFieldViewDTO> map;

    /**
     * 字段配置字典
     */
    private  List<DictBasicEntity> dictList;

    public DeclareReconciliationConfigExcelListener(Map<String, CfgReconciliationFieldDTO.ErpFieldViewDTO> map,List<DictBasicEntity> dictList) {
        this.map = map;
        this.dictList = dictList;
    }

   /**
    * @description: 每解析一行数据回调一遍
    * @author Will
    * @date: 2023/3/7 11:22
    * @param excelDTO 导入信息
    * @param analysisContext
    */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invoke(JSONObject excelDTO, AnalysisContext analysisContext) {
        List<String> errorMsgList = new ArrayList<>();
        if (ObjectUtil.isEmpty(map)) {
            errorMsgList.add("未发现字段配置");
        } else {
            for (Map.Entry<String, Object> entry : excelDTO.entrySet()) {
                CfgReconciliationFieldDTO.ErpFieldViewDTO erpFieldViewDTO = map.get(entry.getKey());
                if (ObjectUtil.isEmpty(erpFieldViewDTO)) {
                    errorMsgList.add("未发现该字段配置项");
                    continue;
                }
                //字段编码
                String fieldCode = dictList.stream().filter(obj -> StrUtil.equals(obj.getName(), erpFieldViewDTO.getErpFieldName()))
                        .findFirst()
                        .flatMap(obj -> Optional.ofNullable(obj.getCode())).orElse("");
                if (StrUtil.isBlank(fieldCode)) {
                    errorMsgList.add("未发现该字段配置项编码");
                }
            }

        }




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

    }
}
