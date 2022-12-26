package com.erp.server.bi.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.erp.model.bi.dto.BiTargetManagementImportExcelDTO;
import com.erp.model.dmp.entity.BiTargetManagementEntity;
import com.erp.model.plm.dto.BasicCategoryDTO;
import com.erp.model.plm.dto.ProductDetailDTO;
import com.erp.model.plm.dto.ProductInfoDTO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.bi.enums.TargetProductTypeEnum;
import com.erp.server.bi.enums.TargetTypeEnum;
import com.erp.server.bi.service.BiTargetManagementService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BiTargetManagementExcelListener extends AnalysisEventListener<BiTargetManagementImportExcelDTO> {

    private BiTargetManagementService biTargetManagementService;

    private PlmTaskFeign plmTaskFeign;

    private SysUserFeign sysUserFeign;

    private List<BiTargetManagementImportExcelDTO> list;

    public BiTargetManagementExcelListener(BiTargetManagementService biTargetManagementService, PlmTaskFeign plmTaskFeign
            , SysUserFeign sysUserFeign) {
        this.biTargetManagementService = biTargetManagementService;
        this.plmTaskFeign = plmTaskFeign;
        this.sysUserFeign = sysUserFeign;
        this.list = new ArrayList<>();
    }

   /**
    * @description: 每解析一行数据回调一遍
    * @author Will
    * @date: 2022/12/16 10:26
    * @param dto
    * @param analysisContext

    */
    @Override
    public void invoke(BiTargetManagementImportExcelDTO dto, AnalysisContext analysisContext) {
        List<String> errorMsgList = new ArrayList<>();

        BiTargetManagementEntity entity = new BiTargetManagementEntity();
        BeanUtils.copyProperties(dto,entity);
        if (ObjectUtils.isEmpty(dto.getYear())) {
            errorMsgList.add("年份不能为空");
        }
        if (StringUtils.isBlank(dto.getPlatformName())) {
            errorMsgList.add("平台名称不能为空");
        }
        if (StringUtils.isBlank(dto.getCategory())) {
            errorMsgList.add("品类不能为空");
        }
        //根据名称查询品类
        Map<String,String> categoryParams = new HashMap<>();
        categoryParams.put("name",dto.getCategory());
        BasicCategoryDTO categoryDto = plmTaskFeign.getCategoryByParam(categoryParams);
        if (ObjectUtils.isEmpty(categoryDto)) {
            errorMsgList.add("系统中不存在此品类");
        }
        if (StringUtils.isBlank(dto.getTargetTypeName())) {
            errorMsgList.add("销量/销售额不能为空");
        }
        if (StringUtils.isBlank(dto.getProductTypeName())) {
            errorMsgList.add("新老品不能为空");
        }
        if (StringUtils.isBlank(dto.getProductName())) {
            errorMsgList.add("产品名称不能为空");
        }
        if (StringUtils.isBlank(dto.getSkuNo()) && StringUtils.isBlank(dto.getSpuNo())) {
            errorMsgList.add("sku/spu至少填一个");
        }
        if (StringUtils.isNotBlank(dto.getSkuNo())) {
            //根据sku编号查询sku
            Map<String,String> skuParams = new HashMap<>();
            skuParams.put("skuNo",dto.getSkuNo());
            ProductDetailDTO productDetailDTO = plmTaskFeign.getSkuByParam(skuParams);
            if (ObjectUtils.isEmpty(productDetailDTO)) {
                errorMsgList.add("系统中不存在此sku编号");
            } else {
                //查询sku关联的spu
                Map<String,String> spuParams = new HashMap<>();
                spuParams.put("id",productDetailDTO.getProductId());
                ProductInfoDTO productInfoDTO = plmTaskFeign.getSpuByParam(spuParams);
                if (ObjectUtils.isEmpty(productInfoDTO)) {
                    errorMsgList.add("系统中未找的此sku编号对应的spu");
                } else {
                    if (!dto.getCategory().equals(productInfoDTO.getCategory())) {
                        errorMsgList.add("导入品类与产品品类不一致");
                    }
                    entity.setSpuId(productInfoDTO.getId());
                    entity.setSpuNo(productInfoDTO.getSpuNo());
                }
                entity.setSkuId(productDetailDTO.getId());
            }
        }
        if (StringUtils.isNotBlank(dto.getSpuNo())) {
            //根据spu编号查询spu
            Map<String,String> params = new HashMap<>();
            params.put("spuNo",dto.getSpuNo());
            ProductInfoDTO productInfoDTO = plmTaskFeign.getSpuByParam(params);
            if (ObjectUtils.isEmpty(productInfoDTO)) {
                errorMsgList.add("系统中不存在此spu编号");
            } else {
                if (!dto.getCategory().equals(productInfoDTO.getCategory())) {
                    errorMsgList.add("导入品类与产品品类不一致");
                }
                entity.setSpuId(productInfoDTO.getId());
            }
        }
        String errStr = "";
        if (errorMsgList.size() > 0) {
            for (int i = 0; i < errorMsgList.size(); i++) {
                Integer indexTemp = i + 1;
                errStr = errStr + indexTemp + "、" + errorMsgList.get(i) + "；";
            }
            dto.setErrorMsg(errStr);
            list.add(dto);
            return;
        }
        entity.setCategoryId(categoryDto.getId());
        entity.setTargetType(TargetTypeEnum.getCodeByName(dto.getTargetTypeName()));
        entity.setProductType(TargetProductTypeEnum.getCodeByName(dto.getProductTypeName()));
        //同一个平台、品类、销量/销售额、SKU（如果不存在则SPU代替）已存在的则修改
        BiTargetManagementEntity oldEntity = biTargetManagementService.getTargetByExcelData(entity);
        if (ObjectUtils.isNotEmpty(oldEntity)) {
            entity.setId(oldEntity.getId());
            biTargetManagementService.updateById(entity);
        } else {
            biTargetManagementService.save(entity);
        }
    }

    public List<BiTargetManagementImportExcelDTO> getDateList(){
        return list;
    }

    /**
     * @description: 全部解析完回调此方法
     * @author Will
     * @date: 2022/12/16 10:26
     * @param analysisContext

     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}
