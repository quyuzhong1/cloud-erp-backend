package com.erp.server.plm.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.plm.dto.ProductDetailUpdateExcelDTO;
import com.erp.model.plm.dto.ProductInfoDTO;
import com.erp.model.plm.entity.BasicCategoryEntity;
import com.erp.model.plm.entity.BasicProductBuEntity;
import com.erp.model.plm.entity.ProductBrandEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.entity.ProductRDTTeamEntity;
import com.erp.server.plm.service.BasicProductBuService;
import com.erp.server.plm.service.ProductBrandService;
import com.erp.server.plm.service.ProductRDTTeamService;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
public class ProductDetailUpdateExcelListener extends AnalysisEventListener<ProductDetailUpdateExcelDTO> {


    private List<BasicCategoryEntity> categoryList;
    private Map<String, String> applicationCategoryMap;
    private List<ProductDetailEntity> productDetailEntityList;
    private ProductBrandService productBrandService;
    private ProductRDTTeamService productRDTTeamService;
    private BasicProductBuService basicProductBuService;

    /**
     * 错误信息
     */

    private List<ProductDetailUpdateExcelDTO> errorList = new ArrayList<>();
    /**
     * 全部数据（用于判断导入是否为空）
     */
    private List<ProductDetailUpdateExcelDTO> dataList = new ArrayList<>();
    /**
     * 成功信息
     */
    private List<ProductInfoDTO> successList = new ArrayList<>();

    public ProductDetailUpdateExcelListener(List<BasicCategoryEntity> categoryList, Map<String, String> applicationCategoryMap, List<ProductDetailEntity> productDetailEntityList, ProductBrandService productBrandService, ProductRDTTeamService productRDTTeamService, BasicProductBuService basicProductBuService) {
        this.categoryList = categoryList;
        this.applicationCategoryMap = applicationCategoryMap;
        this.productDetailEntityList = productDetailEntityList;
        this.productBrandService = productBrandService;
        this.productRDTTeamService = productRDTTeamService;
        this.basicProductBuService = basicProductBuService;
    }
    /**
     * @Description 每解析一行数据回调一遍
     * @Author Luo_WG
     * @Date 2022/9/27 14:49
     * @param1 productDetailExcelDTO: 导入信息
     * @param2 analysisContext: 解析器上下文
     **/
    @Override
    public void invoke(ProductDetailUpdateExcelDTO data, AnalysisContext analysisContext) {
        ProductInfoDTO productSpuBaseInfoDTO = new ProductInfoDTO();
        //注解验证信息
        List<String> msgList = FieldValidUtil.fieldValid(data);
        if (!org.springframework.util.CollectionUtils.isEmpty(msgList)) {
            data.setErrorMsg(String.join(",", msgList));
            errorList.add(data);
            return;
        }
        ProductDetailEntity productBy = productDetailEntityList.stream().filter(req -> req.getSkuNo().equals(data.getSkuNo())).findFirst().orElse(null);
        if (ObjectUtils.isEmpty(productBy)) {
            data.setErrorMsg("sku不存在");
            errorList.add(data);
            return;
        }
        //产品分类
        String category = data.getMainCategory();
        BasicCategoryEntity basicCategoryEntity = categoryList.stream().filter(req -> req.getName().equals(category) && req.getPid().equals("0")).findFirst().orElse(null);
        //二级品类
        String secondaryCategory = data.getSecondaryCategory();
        BasicCategoryEntity secondaryCategoryEntity = categoryList.stream().filter(req -> req.getName().equals(secondaryCategory) && !req.getPid().equals("0")).findFirst().orElse(null);

        if (ObjectUtils.isEmpty(basicCategoryEntity)) {
            if (!ObjectUtils.isEmpty(secondaryCategoryEntity) || !ObjectUtils.isEmpty(category)) {
                data.setErrorMsg("产品分类一级类目不存在");
                errorList.add(data);
                return;
            }
        } else {
            if (!ObjectUtils.isEmpty(secondaryCategoryEntity)) {
                if (!basicCategoryEntity.getId().equals(secondaryCategoryEntity.getPid())) {
                    data.setErrorMsg("产品分类一级类目和二级类目的关系不匹配");
                    errorList.add(data);
                    return;
                } else {
                    productSpuBaseInfoDTO.setCategoryId(secondaryCategoryEntity.getId());
                    productSpuBaseInfoDTO.setCategory(secondaryCategoryEntity.getName());
                }
            } else if (ObjectUtils.isEmpty(secondaryCategoryEntity) && !ObjectUtils.isEmpty(secondaryCategory)){
                data.setErrorMsg("产品分类二级类目不存在");
                errorList.add(data);
                return;
            } else {
                productSpuBaseInfoDTO.setCategoryId(basicCategoryEntity.getId());
                productSpuBaseInfoDTO.setCategory(basicCategoryEntity.getName());
            }
        }
        String applicationCategoryId = null;
        if (!ObjectUtils.isEmpty(data.getApplicationCategoryName())) {
            applicationCategoryId = applicationCategoryMap.get(data.getApplicationCategoryName());
            if (ObjectUtils.isEmpty(applicationCategoryId)) {
                data.setErrorMsg("应用分类不存在");
                errorList.add(data);
                return;
            }
        }
        productSpuBaseInfoDTO.setApplicationCategoryId(applicationCategoryId);
        
        //品牌处理
        if (StringUtils.isNotBlank(data.getBrandName())) {
            ProductBrandEntity productBrand = productBrandService.getByName(data.getBrandName());
            if (ObjectUtils.isEmpty(productBrand)) {
                data.setErrorMsg("产品品牌在系统中未找到");
                errorList.add(data);
                return;
            } else {
                productSpuBaseInfoDTO.setBrandId(productBrand.getId());
                productSpuBaseInfoDTO.setBrandName(productBrand.getName());
            }
        }
        
        //研发团队处理
        if (StringUtils.isNotBlank(data.getRdtTeamName())) {
            ProductRDTTeamEntity productRDTTeam = productRDTTeamService.getByName(data.getRdtTeamName());
            if (ObjectUtils.isEmpty(productRDTTeam)) {
                data.setErrorMsg("研发团队在系统中未找到");
                errorList.add(data);
                return;
            } else {
                productSpuBaseInfoDTO.setRdtTeamId(productRDTTeam.getId());
                productSpuBaseInfoDTO.setRdtTeamName(productRDTTeam.getName());
            }
        }
        
        //BU线处理
        if (StringUtils.isNotBlank(data.getBuName())) {
            String buName = data.getBuName().trim();
            BasicProductBuEntity basicProductBuEntity = basicProductBuService.getByName(buName);
            if (ObjectUtils.isEmpty(basicProductBuEntity)) {
                data.setErrorMsg("BU线【" + buName + "】在系统中未找到");
                errorList.add(data);
                return;
            } else {
                productSpuBaseInfoDTO.setBuId(basicProductBuEntity.getId());
                productSpuBaseInfoDTO.setBuName(basicProductBuEntity.getName());
            }
        }
        
        //存在错误数据则直接返回
        if (!CollectionUtils.isEmpty(errorList)) {
            return;
        }
        productSpuBaseInfoDTO.setId(productBy.getProductId());
        successList.add(productSpuBaseInfoDTO);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {

    }


}
