package com.erp.server.plm.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.plm.dto.excel.ProductWarehouseLocationExcelDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.enums.ProductDetailStatusEnum;
import com.erp.server.plm.service.ProductDetailService;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 导入产品仓位信息
 * @Author Luo_WG
 * @Date 2023/11/10 14:10
 **/
public class ProductWarehouseLocationListener extends AnalysisEventListener<ProductWarehouseLocationExcelDTO> {
    private List<ProductDetailEntity> productDetailEntityList;
    private ProductDetailService productDetailService;

    private List<ProductWarehouseLocationExcelDTO> dataList = new ArrayList<>();
    private List<String> idList = new ArrayList<>();

    private List<ProductWarehouseLocationExcelDTO> list;
    public ProductWarehouseLocationListener(List<ProductDetailEntity> productDetailEntityList, ProductDetailService productDetailService) {
        this.productDetailEntityList = productDetailEntityList;
        this.productDetailService = productDetailService;
        this.list = new ArrayList<>();
    }

    /**
     * 每解析一行数据回调一遍
     * @param dto: 导入信息
     * @param analysisContext: 解析器上下文
     **/
    @Override
    public void invoke(ProductWarehouseLocationExcelDTO dto, AnalysisContext analysisContext) {
        String errStr = "";
        List<String> errorMsgList = new ArrayList<>();
        //添加数据用于判断是否为空
        dataList.add(dto);
        //字段格式校验
        List<String> msgList = FieldValidUtil.fieldValid(dto);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }

        ProductDetailEntity entity = productDetailEntityList.stream().filter(req -> ProductDetailStatusEnum.APPROVAL_PASS.getCode().equals(req.getStatus()) && req.getSkuNo().equals(dto.getSkuNo())).findFirst().orElse(null);
        if (ObjectUtils.isEmpty(entity)) {
            errorMsgList.add("sku编号不存在或未审核通过！");
        }else{
            //判断重复数据
            if (idList.contains(entity.getId())){
                errorMsgList.add("sku编号重复！");
            }else{
                idList.add(entity.getId());
            }
        }
        String warehouseLocation = dto.getWarehouseLocation();
        String warehouseLocationLarge = dto.getWarehouseLocationLarge();
        if (StringUtils.isBlank(warehouseLocation) && StringUtils.isBlank(warehouseLocationLarge)) {
            errorMsgList.add("推荐仓位不能全部为空！");
        }

        if (errorMsgList.size() > 0) {
            for (int i = 0; i < errorMsgList.size(); i++) {
                Integer indexTemp = i + 1;
                errStr = errStr + indexTemp + "、" + errorMsgList.get(i) + "；";
            }
            dto.setErrorMsg(errStr);
            list.add(dto);
            return;
        }


        if (StringUtils.isBlank(warehouseLocation)){
            warehouseLocation=entity.getWarehouseLocation();
        }
        if (StringUtils.isBlank(warehouseLocationLarge)){
            warehouseLocationLarge=entity.getWarehouseLocationLarge();
        }
        productDetailService.updateWarehouseLocationById(entity.getId(), warehouseLocation, warehouseLocationLarge);
    }

    public List<ProductWarehouseLocationExcelDTO> getDateList() {
        return list;
    }

    public List<ProductWarehouseLocationExcelDTO> getExcelDateList() {
        return dataList;
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        return;
    }
}
