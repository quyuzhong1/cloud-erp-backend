package com.erp.server.scm.listener;

import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.business.enums.ImportCommonTypeEnum;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.scm.dto.excel.SupplierImportExcelDTO;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lambda
 * @Classname SupplierExcelListener

 * @Date 2023-03-30 9:46
 * @Created by yl
 */
@Getter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SupplierExcelListener extends AnalysisEventListener<SupplierImportExcelDTO> {


    /**
     * 导入数据，用于判断导入是否为空
     */
    private final List<SupplierImportExcelDTO> allList = new ArrayList<>();

    /**
     * 导入错误数据
     */
    private final List<SupplierImportExcelDTO> errorList = new ArrayList<>();

    /**
     * 导入正确数据
     */
    private final List<SupplierImportExcelDTO> successList = new ArrayList<>();

    private String type;

    public  SupplierExcelListener (String type) {
        this.type = type;
    }

    /**
     * 每解析一行数据回调一遍
     *
     * @param analysisContext
     * @return void
     * @author yl
     * @date 2023-03-30 9:50
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invoke(SupplierImportExcelDTO importExcelDTO, AnalysisContext analysisContext) {

        //添加数据用于判断是否为空
        allList.add(importExcelDTO);
        //更新全部时添加基础校验
        if (ImportCommonTypeEnum.UPDATE_ALL.getCode().equals(type)) {
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
        } else {
            //校验供应商名称
            if (CharSequenceUtil.isBlank(importExcelDTO.getName())) {
                importExcelDTO.setErrorMsg("供应商名称不能为空");
                errorList.add(importExcelDTO);
                return;
            }
        }
        successList.add(importExcelDTO);
    }


    /**
     * 数据全部解析完成后执行
     *
     * @param analysisContext
     * @return void
     * @author yl
     * @date 2023-03-30 9:51
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {


    }


    /**
     * 获取错误信息
     *
     * @param
     * @return java.util.List<com.erp.model.scm.dto.excel.SupplierImportExcelDTO>
     * @author yl
     * @date 2023-03-30 16:13
     */
    public List<SupplierImportExcelDTO> getErrorList() {
        return errorList;
    }



}
