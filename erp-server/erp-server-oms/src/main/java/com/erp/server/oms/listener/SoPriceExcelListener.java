package com.erp.server.oms.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.oms.dto.SoPriceDTO;
import com.erp.model.oms.dto.excel.ImportSoPriceExcelDTO;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.server.oms.service.SoPriceDetailService;
import com.erp.server.oms.service.SoPriceService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 销售价目
 * @author will
 * @date 2025/3/25 14:55
 */
@Slf4j
public class SoPriceExcelListener extends AnalysisEventListener<ImportSoPriceExcelDTO> {


    /**
     * 导入数据，用于判断导入是否为空
     */
    private final List<ImportSoPriceExcelDTO> allList = new ArrayList<>();

    /**
     * 导入错误数据
     */
    @Getter
    private final List<ImportSoPriceExcelDTO> errorList = new ArrayList<>();

    /**
     * 导入正确数据
     */
    @Getter
    private final List<ImportSoPriceExcelDTO> successList = new ArrayList<>();


    @Override
    public void invoke(ImportSoPriceExcelDTO importExcelDTO, AnalysisContext analysisContext) {

        //添加数据用于判断是否为空
        allList.add(importExcelDTO);

        //注解验证信息
        List<String> errorMsgList = new ArrayList<>();
        List<String> msgList = FieldValidUtil.fieldValid(importExcelDTO);
        if (com.baomidou.mybatisplus.core.toolkit.CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        //存在错误数据则直接返回
        if (!errorMsgList.isEmpty()) {
            importExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(importExcelDTO);
            return;
        }
        successList.add(importExcelDTO);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }

    public List<ImportSoPriceExcelDTO> getErrorList() {
        return errorList;
    }

}