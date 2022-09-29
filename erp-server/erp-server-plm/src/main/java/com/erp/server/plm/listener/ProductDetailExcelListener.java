package com.erp.server.plm.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.core.utils.BeanMapper;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.BasicCategoryEntity;
import com.erp.model.plm.entity.BasicDictEntity;
import com.erp.model.plm.entity.ProductInfoEntity;
import com.erp.server.plm.enums.SaleMethodEnum;
import com.erp.server.plm.service.BasicCategoryService;
import com.erp.server.plm.service.BasicDictService;
import com.erp.server.plm.service.ProductDetailService;
import com.erp.server.plm.service.ProductInfoService;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProductDetailExcelListener extends AnalysisEventListener<ProductDetailExcelDTO> {
    private Integer importType;

    private ProductDetailService productDetailService;

    private ProductInfoService productInfoService;

    private BasicCategoryService basicCategoryService;

    private BasicDictService basicDictService;

    private List<ProductDetailExcelDTO> list;

    public ProductDetailExcelListener(Integer importType, ProductDetailService productDetailService, ProductInfoService productInfoService,
                                      BasicCategoryService basicCategoryService, BasicDictService basicDictService) {
        this.importType = importType;
        this.productDetailService = productDetailService;
        this.productInfoService = productInfoService;
        this.basicCategoryService = basicCategoryService;
        this.basicDictService = basicDictService;
        this.list = new ArrayList<>();
    }

    /**
     * @Description  每解析一行数据回调一遍
     * @Author Luo_WG
     * @Date 2022/9/27 14:49
     * @param1 productDetailExcelDTO: 导入信息
     * @param2 analysisContext: 解析器上下文
     **/
    @Override
    public void invoke(ProductDetailExcelDTO dto, AnalysisContext analysisContext) {
        if (StringUtils.isBlank(dto.getName())) {
            dto.setErrorMsg("产品名称不能为空");
            list.add(dto);
            return;
        }

        //根据产品名称查询产品信息
        ProductDetailShowDTO productByName = productDetailService.getProductByName(dto.getName());
        ProductInfoDTO productInfoDTO = new ProductInfoDTO();
        //sku信息
        ProductSkuBaseInfoDTO productSkuBaseInfoDTO = new ProductSkuBaseInfoDTO();
        // 判断是修改还是新增 1：新增 2：修改
        if (importType == 2) {
            if (ObjectUtils.isEmpty(productByName)) {
                dto.setErrorMsg("没有找到这个sku，请导入新增");
                list.add(dto);
                return;
            }
            productInfoDTO.setId(productByName.getId());
            productSkuBaseInfoDTO.setId(productByName.getSkuId());
            if (!productDetailService.checkSkuNo(dto.getSkuNo())) {
                dto.setErrorMsg("sku不存在，请选择导入新增");
                list.add(dto);
                return;
            }
            if (ObjectUtils.isEmpty(productByName)) {
                if (!productByName.getSkuNo().equals(dto.getSkuNo())) {
                    dto.setErrorMsg(ApiError.ERROR_95007.msg);
                    list.add(dto);
                    return;
                }
            }

        } else {
            //sku重复
            if (productDetailService.checkSkuNo(dto.getSkuNo())) {
                dto.setErrorMsg(ApiError.ERROR_95015.msg);
                list.add(dto);
                return;
            }
            if (!ObjectUtils.isEmpty(productByName)) {
                dto.setErrorMsg(ApiError.ERROR_95007.msg);
                list.add(dto);
            }
        }

        //销售方式不正确
        if(StringUtils.isNotBlank(dto.getSaleMethod())){
            String[] saleMethodList = dto.getSaleMethod().split(",");
            for (String saleMethod : saleMethodList) {
                if (SaleMethodEnum.getCodeByName(saleMethod) == null) {
                    dto.setErrorMsg("销售方式不正确");
                    list.add(dto);
                    return;
                }
            }
        }

        //存在侵权风险
        String pirateRisk = dto.getPirateRisk();
        if(StringUtils.isNotBlank(pirateRisk)){
            if(!pirateRisk.equals("有风险") && !pirateRisk.equals("无风险")){
                dto.setErrorMsg("存在侵权风险：有风险 或者 无风险");
                list.add(dto);
                return;
            }
        }

        ProductNoSpecDTO productNoSpecDTO = new ProductNoSpecDTO();
        //spu信息

        if(pirateRisk.equals("有风险")){
            productInfoDTO.setPirateRisk(1);
        } else {
            productInfoDTO.setPirateRisk(2);
        }

        productInfoDTO.setName(dto.getName());
        productInfoDTO.setApprovalStatus(4);

/*        BasicCategoryEntity categoryByName = basicCategoryService.getCategoryByName(dto.getCategory());
        if (!ObjectUtils.isEmpty(categoryByName)) {
            productInfoDTO.setChargeId("产品类别Id");
            productInfoDTO.setCategory("产品类别");
        }*/

        productInfoDTO.setSpecType(1);

        productInfoDTO.setChargeName("产品经理");
        productInfoDTO.setChargeId("产品经理id");
        productInfoDTO.setMaterials(dto.getMaterials());

        //产品等级 没有
        productInfoDTO.setGrade("");

        BasicDictEntity productBrand = basicDictService.checkBasicDict("productBrand", dto.getBrandName());
        if (ObjectUtils.isEmpty(productBrand)) {
            dto.setErrorMsg("产品品牌在系统中未找到");
            list.add(dto);
            return;
        }
        productInfoDTO.setBrandId(productBrand.getId());
        productInfoDTO.setBrandName(productBrand.getValue());
        BasicDictEntity productProperty = basicDictService.checkBasicDict("productProperty", dto.getProperty());
        if (ObjectUtils.isEmpty(productProperty)) {
            dto.setErrorMsg("产品属性在系统中未找到");
            list.add(dto);
            return;
        }
        productInfoDTO.setProperty(productProperty.getValue());
        productInfoDTO.setPropertyId(productProperty.getId());


        BeanMapper.copy(dto, productSkuBaseInfoDTO);
        productSkuBaseInfoDTO.setProductState(2);
        productSkuBaseInfoDTO.setProductId("");
        productNoSpecDTO.getProductBaseInfoDTO();

        ProductBaseInfoDTO productBaseInfoDTO = new ProductBaseInfoDTO();
        productBaseInfoDTO.setProductSpuBaseInfoDTO(productInfoDTO);
        productBaseInfoDTO.setProductSkuBaseInfoDTO(productSkuBaseInfoDTO);
        productNoSpecDTO.setProductBaseInfoDTO(productBaseInfoDTO);

        //产品成本信息表
        ProductCostDTO productCostDTO = new ProductCostDTO();
        BeanMapper.copy(dto, productCostDTO);
        productNoSpecDTO.setProductCostDTO(productCostDTO);
        //采购信息信息
        ProductPurchaseDTO productPurchaseDTO = new ProductPurchaseDTO();
        BeanMapper.copy(dto, productPurchaseDTO);
        productNoSpecDTO.setProductPurchaseDTO(productPurchaseDTO);
        //产品销售信息
        ProductSaleDTO productSaleDTO = new ProductSaleDTO();
        BeanMapper.copy(dto, productSaleDTO);
        productNoSpecDTO.setProductSaleDTO(productSaleDTO);
        //产品物流信息
        ProductLogisticsDTO productLogisticsDTO = new ProductLogisticsDTO();
        BeanMapper.copy(dto, productLogisticsDTO);
        productNoSpecDTO.setProductLogisticsDTO(productLogisticsDTO);
        //产品包装信息
        ProductPackDTO productPackDTO = new ProductPackDTO();
        BeanMapper.copy(dto, productPackDTO);
        productNoSpecDTO.setProductPackDTO(productPackDTO);
        //产品证书信息
        ProductCertificateDTO productCertificateDTO = new ProductCertificateDTO();
        BeanMapper.copy(dto, productCertificateDTO);
        productNoSpecDTO.setProductCertificateDTO(productCertificateDTO);
        productDetailService.inportExcel(productNoSpecDTO);
    }

    public List<ProductDetailExcelDTO> getDateList(){
        return list;
    }

    /**
     * @Description 全部解析完回调此方法
     * @Author Luo_WG
     * @Date 2022/9/27 14:49
     * @param analysisContext: 解析器上下文
     **/
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}
