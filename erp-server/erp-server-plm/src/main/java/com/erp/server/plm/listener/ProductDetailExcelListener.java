package com.erp.server.plm.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.business.dto.FindUserDTO;
import com.common.core.enums.ApiError;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.plm.dto.ProductDetailExcelDTO;
import com.common.core.utils.LengthConverterUtil;
import com.common.core.utils.MathUtil;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.BasicCategoryEntity;
import com.erp.model.plm.entity.BasicDictEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.entity.ProductUnitEntity;
import com.erp.model.plm.enums.*;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.rpc.scm.feign.ScmTaskFeign;
import com.erp.server.plm.service.BasicDictService;
import com.erp.server.plm.service.ProductDetailService;

import java.util.ArrayList;
import java.util.List;

public class ProductDetailExcelListener extends AnalysisEventListener<ProductDetailExcelDTO> {
    /**
     * 错误信息
     */
    private List<ProductDetailExcelDTO> errorList = new ArrayList<>();
    /**
     * 全部数据（用于判断导入是否为空）
     */
    private List<ProductDetailExcelDTO> dataList = new ArrayList<>();
    /**
     * 成功信息
     */
    private List<ProductDetailExcelDTO> successList = new ArrayList<>();

    public ProductDetailExcelListener() {

    }

    /**
     * @Description 每解析一行数据回调一遍
     * @Author Luo_WG
     * @Date 2022/9/27 14:49
     * @param1 productDetailExcelDTO: 导入信息
     * @param2 analysisContext: 解析器上下文
     **/
    @Override
    public void invoke(ProductDetailExcelDTO dto, AnalysisContext analysisContext) {
        List<String> errorMsgList = new ArrayList<>();

        //基础验证
        List<String> msgList = FieldValidUtil.fieldValid(dto);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        //添加数据用于判断是否为空
        dataList.add(dto);
        //存在错误数据则直接返回
        if (errorMsgList.size() > 0) {
            dto.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(dto);
            return;
        }
        successList.add(dto);
    }

    /**
     * @param analysisContext: 解析器上下文
     * @Description 全部解析完回调此方法
     * @Author Luo_WG
     * @Date 2022/9/27 14:49
     **/
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }

    public List<ProductDetailExcelDTO> getErrorList(){
        return errorList;
    }

    public List<ProductDetailExcelDTO> getSuccessList(){
        return successList;
    }

    public List<ProductDetailExcelDTO> getExcelDateList() {
        return dataList;
    }

}
