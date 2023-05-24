package com.erp.server.wms.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.StrUtils;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.wms.dto.excel.ImportInitStockExcelDTO;
import com.erp.model.wms.dto.inventory.InitStockDetailDTO;
import org.apache.commons.collections4.CollectionUtils;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 期初库存明细导入监听器
 */
public class InitStockDetailExcelListener extends AnalysisEventListener<ImportInitStockExcelDTO> {

    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/M/d");

    /**
     * sku数据
     */
    private List<SkuVO> skuList;


    /**
     * 成功的数据
     */
    private List<InitStockDetailDTO.AddDTO> successList = new ArrayList<>();


    /**
     * 导入错误数据
     */
    private List<ImportInitStockExcelDTO> errorList = new ArrayList<>();


    public InitStockDetailExcelListener(List<SkuVO> skuList) {
        this.skuList = skuList;
    }

    /**
     * 解析每一行回调
     * @param importInitStockExcelDTO
     * @param analysisContext
     */
    @Override
    public void invoke(ImportInitStockExcelDTO importInitStockExcelDTO, AnalysisContext analysisContext) {
        InitStockDetailDTO.AddDTO addDTO = new InitStockDetailDTO.AddDTO();
        //注解验证信息
        List<String> errorMsgList = new ArrayList<>();
        List<String> msgList = FieldValidUtil.fieldValid(importInitStockExcelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        if (CollectionUtils.isEmpty(skuList)) {
            errorMsgList.add("系统中未发现已审核SKU");
        } else {
            SkuVO skuEntity = skuList.stream().filter(obj -> obj.getSkuNo().equals(importInitStockExcelDTO.getSkuNo())).findFirst().orElse(null);
            if (Objects.isNull(skuEntity)) {
                errorMsgList.add("sku有误");
            }
            if (skuEntity != null) {
                if(!StrUtils.isInteger(importInitStockExcelDTO.getQty())) {
                    errorMsgList.add("期初数量只能为正数");
                } else {
                    addDTO.setQty(Integer.valueOf(importInitStockExcelDTO.getQty()));
                    addDTO.setSkuId(skuEntity.getSkuId());
                    addDTO.setSkuNo(skuEntity.getSkuNo());
                    addDTO.setProductName(skuEntity.getSpuName());
                    addDTO.setWarehouseLocation(importInitStockExcelDTO.getWarehouseLocation());
                    addDTO.setRemark(StrUtils.null2EmptyWithTrim(importInitStockExcelDTO.getRemark()));
                    successList.add(addDTO);
                }
            }
        }

        //存在错误数据则直接返回
        if (errorMsgList.size() > 0) {
            importInitStockExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(importInitStockExcelDTO);
            return;
        }

    }


    /**
     * 数据全部解析完成调用
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }


    public List<ImportInitStockExcelDTO> getErrorList() {
        return errorList;
    }


    public List<InitStockDetailDTO.AddDTO> getSuccessList() {
        return successList;
    }
}
