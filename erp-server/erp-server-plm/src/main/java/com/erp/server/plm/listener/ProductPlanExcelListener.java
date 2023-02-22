package com.erp.server.plm.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.FieldValidUtil;
import com.common.business.dto.FindUserDTO;
import com.erp.model.plm.dto.excel.ProductPlanExcelDTO;
import com.erp.model.plm.entity.BasicDictEntity;
import com.erp.model.plm.entity.ProductPlanEntity;
import com.erp.model.plm.enums.BasicDictTypeEnum;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.plm.service.BasicDictService;
import com.erp.server.plm.service.ProductPlanService;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/2/21 10:16
 */
public class ProductPlanExcelListener extends AnalysisEventListener<ProductPlanExcelDTO> {

    private SysUserFeign sysUserFeign;

    private ProductPlanService productPlanService;

    private BasicDictService basicDictService;

    /**
     * 错误数据返回集合
     */
    private List<ProductPlanExcelDTO> list;

    /**
     * 导入数据，用于判断导入是否为空
     */
    private List<ProductPlanExcelDTO> dataList = new ArrayList<>();

    public ProductPlanExcelListener(ProductPlanService productPlanService,BasicDictService basicDictService,SysUserFeign sysUserFeign) {
        this.productPlanService = productPlanService;
        this.sysUserFeign = sysUserFeign;
        this.list = new ArrayList<>();
    }

    @Override
    public void invoke(ProductPlanExcelDTO productPlanExcelDTO, AnalysisContext analysisContext) {
        //列表返回错误信息
        List<String> errorMsgList = new ArrayList<>();
        //添加数据用于判断是否为空
        dataList.add(productPlanExcelDTO);
        //注解验证信息
        List<String> msgList = FieldValidUtil.fieldValid(productPlanExcelDTO);
        if (CollectionUtils.isEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        FindUserDTO charge = sysUserFeign.getUserByUserName(productPlanExcelDTO.getChargeName());
        if (ObjectUtils.isEmpty(charge)) {
            errorMsgList.add("产品经理在系统中未找到");
        }

        BasicDictEntity productBrand = basicDictService.checkBasicDict(BasicDictTypeEnum.PRODUCT_BRAND.getCode(), productPlanExcelDTO.getBrandName());
        if (ObjectUtils.isEmpty(productBrand)) {
            errorMsgList.add("产品品牌在系统中未找到");
        }

        BasicDictEntity productProperty = basicDictService.checkBasicDict(BasicDictTypeEnum.PRODUCT_PROPERTY.getCode(), productPlanExcelDTO.getProperty());
        if (ObjectUtils.isEmpty(productProperty)) {
            errorMsgList.add("产品属性在系统中未找到");
        }

        String errStr = "";
        if (errorMsgList.size() > 0) {
            for (int i = 0; i < errorMsgList.size(); i++) {
                Integer indexTemp = i + 1;
                errStr = errStr + indexTemp + "、" + errorMsgList.get(i) + "；";
            }
            productPlanExcelDTO.setErrorMsg(errStr);
            list.add(productPlanExcelDTO);
            return;
        }

        ProductPlanEntity productPlanEntity = new ProductPlanEntity();
        BeanMapperUtils.copy(productPlanExcelDTO,productPlanEntity);
        productPlanEntity.setYear(Integer.valueOf(productPlanExcelDTO.getYear()));
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }

    public List<ProductPlanExcelDTO> getDateList(){
        return list;
    }

    public List<ProductPlanExcelDTO> getExcelDateList(){
        return dataList;
    }
}
