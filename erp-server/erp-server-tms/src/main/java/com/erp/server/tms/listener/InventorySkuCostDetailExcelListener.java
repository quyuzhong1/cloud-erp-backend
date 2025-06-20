package com.erp.server.tms.listener;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.tms.dto.InventorySkuCostDetailDTO;
import com.erp.model.tms.dto.excel.InventorySkuCostDetailExcelDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.rpc.wms.feign.WmsWarehouseFeign;
import com.erp.server.tms.convert.InventorySkuCostConverter;
import lombok.Getter;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 期初头程分摊
 */
public class InventorySkuCostDetailExcelListener extends AnalysisEventListener<InventorySkuCostDetailExcelDTO> {
    private final PlmTaskFeign plmTaskFeign = SpringUtil.getBean(PlmTaskFeign.class);
    private final WmsTaskFeign wmsTaskFeign = SpringUtil.getBean(WmsTaskFeign.class);
    /**
     * 错误信息
     */
    @Getter
    private List<InventorySkuCostDetailExcelDTO> errorList = new ArrayList<>();
    /**
     * 全部数据（用于判断导入是否为空）
     */
    private final List<InventorySkuCostDetailExcelDTO> allList = new ArrayList<>();

    /**
     * 成功信息
     */
    @Getter
    private List<InventorySkuCostDetailDTO.AddDTO> successList = new ArrayList<>();
    private List<InventorySkuCostDetailDTO.AddDTO> detailList;
    public InventorySkuCostDetailExcelListener(List<InventorySkuCostDetailDTO.AddDTO> detailList) {
        this.detailList = CollectionUtils.isEmpty(detailList) ? Collections.emptyList() : detailList;
    }

   /**
    * @description: 每解析一行数据回调一遍
    * @author zdy
    * @date: 2023/3/7 11:22
    * @param excelDTO 导入信息
    * @param analysisContext
    */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invoke(InventorySkuCostDetailExcelDTO excelDTO, AnalysisContext analysisContext) {
        List<String> errorMsgList = new ArrayList<>();
        InventorySkuCostDetailDTO.AddDTO addDTO = new InventorySkuCostDetailDTO.AddDTO();
        //基础验证
        List<String> msgList = FieldValidUtil.fieldValid(excelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        //判断是否存在明细
        if (CollectionUtils.isNotEmpty(allList)){
            InventorySkuCostDetailExcelDTO addDTO1 = allList.stream().filter(e -> Objects.equals(e.getSkuNo(), excelDTO.getSkuNo()) && Objects.equals(e.getWarehouseName(), excelDTO.getWarehouseName())).findFirst().orElse(null);
            if (Objects.nonNull(addDTO1)){
                errorMsgList.add(CharSequenceUtil.format("SKU【{}】已存在",addDTO1.getSkuNo()));
            }
        }
        if (CollectionUtils.isNotEmpty(detailList)){
            InventorySkuCostDetailDTO.AddDTO addDTO1 = detailList.stream().filter(e -> Objects.equals(e.getSkuNo(), excelDTO.getSkuNo()) && Objects.equals(e.getWarehouseName(), excelDTO.getWarehouseName())).findFirst().orElse(null);
            if (Objects.nonNull(addDTO1)){
                errorMsgList.add(CharSequenceUtil.format("SKU【{}】已存在",addDTO1.getSkuNo()));
            }
        }
        //存在错误数据则直接返回
        if (!errorMsgList.isEmpty()) {
            excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(excelDTO);
            return;
        }
        //添加数据用于判断是否为空
        allList.add(excelDTO);
    }

    public List<InventorySkuCostDetailExcelDTO> getExcelDateList(){
        return allList;
    }

    /**
     * @description: 数据全部解析完后删除明细
     * @author zdy
     * @date: 2024/8/14 15:32
     * @param analysisContext
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        if(CollUtil.isEmpty(allList)){
            return;
        }
        List<String> warehouseNameList = allList.stream().map(InventorySkuCostDetailExcelDTO::getWarehouseName).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<WarehouseDTO.ListDTO> warehouseEntityList = CollUtil.isEmpty(warehouseNameList) ? Collections.emptyList() : wmsTaskFeign.listWarehouseByNameList(warehouseNameList);
        //仓库Map
        Map<String,WarehouseDTO.ListDTO> warehouseEntityHashMap = new HashMap<>();
        warehouseEntityHashMap = warehouseEntityList.stream().collect(Collectors.toMap(WarehouseDTO.ListDTO::getName, Function.identity()));
        List<String> skuNoList = allList.stream().map(InventorySkuCostDetailExcelDTO::getSkuNo).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> productDetailEntityList = plmTaskFeign.listBySkuNos(skuNoList);
        //产品Map
        Map<String,ProductDetailEntity> productDetailEntityMap = new HashMap<>();
        productDetailEntityMap = productDetailEntityList.stream().collect(Collectors.toMap(ProductDetailEntity::getSkuNo,Function.identity()));

        for (InventorySkuCostDetailExcelDTO excelDTO : allList){
            WarehouseDTO.ListDTO warehouseEntity = warehouseEntityHashMap.getOrDefault(excelDTO.getWarehouseName(), null);
            if (Objects.isNull(warehouseEntity)){
                excelDTO.setErrorMsg(CharSequenceUtil.format("仓库【{}】不存在",excelDTO.getWarehouseName()));
                errorList.add(excelDTO);
                continue;
            }
            ProductDetailEntity productDetailEntity = productDetailEntityMap.getOrDefault(excelDTO.getSkuNo(), null);
            if (Objects.isNull(productDetailEntity)){
                excelDTO.setErrorMsg(CharSequenceUtil.format("SKU【{}】不存在",excelDTO.getSkuNo()));
                errorList.add(excelDTO);
                continue;
            }
            InventorySkuCostDetailDTO.AddDTO addDTO = InventorySkuCostConverter.INSTANCE.excelToAddDTO(excelDTO);
            addDTO.setWarehouseId(warehouseEntity.getId());
            addDTO.setWarehouseName(warehouseEntity.getName());
            addDTO.setSkuId(productDetailEntity.getId());
            addDTO.setProductName(productDetailEntity.getName());
            addDTO.setUnit(productDetailEntity.getUnitName());
            if (CharSequenceUtil.isBlank(addDTO.getUnit())){
                addDTO.setUnit("Pcs");
            }
            successList.add(addDTO);
        }
    }
}
