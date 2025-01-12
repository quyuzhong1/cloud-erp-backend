package com.erp.server.plm.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.plm.dto.ProductDetailUpdateExcelDTO;
import com.erp.model.plm.dto.ProductInfoDTO;
import com.erp.model.plm.entity.BasicCategoryEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.enums.ProductDetailStatusEnum;
import lombok.Getter;
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

    public ProductDetailUpdateExcelListener(List<BasicCategoryEntity> categoryList, Map<String, String> applicationCategoryMap, List<ProductDetailEntity> productDetailEntityList) {
        this.categoryList = categoryList;
        this.applicationCategoryMap = applicationCategoryMap;
        this.productDetailEntityList = productDetailEntityList;
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
        if (ProductDetailStatusEnum.WAIT_CONFIRM.getCode().equals(productBy.getStatus())
                || ProductDetailStatusEnum.APPROVAL_ING.getCode().equals(productBy.getStatus())
                || ProductDetailStatusEnum.APPROVAL_PASS.getCode().equals(productBy.getStatus())) {
            data.setErrorMsg("仅{待提交，审核不通过}的状态下可导入修改");
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
            if (!ObjectUtils.isEmpty(secondaryCategoryEntity)) {
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
                }
            } else {
                productSpuBaseInfoDTO.setCategoryId(basicCategoryEntity.getId());
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
