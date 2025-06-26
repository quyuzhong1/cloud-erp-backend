
package com.erp.server.wms.listener;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.wms.dto.VirtualAdjustDetailDTO;
import com.erp.model.wms.dto.excel.VirtualAdjustDetailExcelDTO;
import com.erp.model.wms.entity.VirtualWarehouseEntity;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.service.VirtualWarehouseService;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Lambda
 * @Classname WarehouseExcelListener
 * @Date 2023-03-22 17:22
 * @Created by yl
 */
public class VirtualAdjustDetailExcelListener extends AnalysisEventListener<VirtualAdjustDetailExcelDTO> {
    private List<VirtualAdjustDetailDTO.AddDTO> detailList;
    /**
     * 导入正确数据
     */
    @Getter
    private List<VirtualAdjustDetailExcelDTO> successList = new ArrayList<>();

    /**
     * 导入数据，用于判断导入是否为空
     */
    @Getter
    private List<VirtualAdjustDetailExcelDTO> allList = new ArrayList<>();
    /**
     * 导入错误数据
     */
    @Getter
    private List<VirtualAdjustDetailExcelDTO> errorList = new ArrayList<>();

    private PlmTaskFeign plmTaskFeign = SpringUtil.getBean(PlmTaskFeign.class);
    private VirtualWarehouseService virtualWarehouseService = SpringUtil.getBean(VirtualWarehouseService.class);


    public VirtualAdjustDetailExcelListener(List<VirtualAdjustDetailDTO.AddDTO> detailList) {
        this.detailList = detailList;
    }

    /**
     * 每解析一行数据回调一遍
     *
     * @param excelDTO
     * @param analysisContext
     * @return void
     * @author zdy
     * @date 2024/4/18 9:09
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invoke(VirtualAdjustDetailExcelDTO excelDTO, AnalysisContext analysisContext) {
        //防止错误数据导入
        excelDTO.setErrorMsg(null);
        //添加数据用于判断是否为空
        List<String> msgList = FieldValidUtil.fieldValid(excelDTO);
        List<String> errorMsgList = new ArrayList<>(msgList);
        if (CollectionUtils.isNotEmpty(msgList)){
            excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(excelDTO);
            return;
        }
        try {
            String inventoryStatus = InventoryStatusEnum.getCodeByName(excelDTO.getInventoryStatusName());
            if (CharSequenceUtil.isBlank(inventoryStatus)){
                errorMsgList.add("库存状态不存在");
            }else {
                excelDTO.setInventoryStatus(inventoryStatus);
            }
        }catch (Exception e){
            errorMsgList.add("库存状态不存在");
        }
        try {
            Integer qty = Integer.valueOf(excelDTO.getQtyStr());
            excelDTO.setQty(qty);
        }catch (Exception e){
            errorMsgList.add("数量只能是数字");
        }
        //存在错误数据则直接返回
        if (!errorMsgList.isEmpty()) {
            excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(excelDTO);
        }
        allList.add(excelDTO);
    }


    /**
     * 数据全部解析完后执行
     *
     * @param analysisContext
     * @return void
     * @author yl
     * @date 2023-03-22 17:59
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        if(CollectionUtils.isEmpty(allList)){
            return;
        }
        List<String> skuNoList = allList.stream().filter(e -> CharSequenceUtil.isBlank(e.getErrorMsg())).map(VirtualAdjustDetailExcelDTO::getSkuNo)
                .filter(CharSequenceUtil::isNotBlank).collect(Collectors.toList());
        List<String> virtualWarehouseNameList = allList.stream().filter(e -> CharSequenceUtil.isBlank(e.getErrorMsg()))
                .map(VirtualAdjustDetailExcelDTO::getVirtualWarehouseName).filter(CharSequenceUtil::isNotBlank).collect(Collectors.toList());
        List<ProductDetailEntity> skuVOS = CollUtil.isNotEmpty(skuNoList) ? plmTaskFeign.listBySkuNos(skuNoList) : Collections.emptyList();
        List<VirtualWarehouseEntity> virtualWarehouseEntityList = virtualWarehouseService.listByNameList(virtualWarehouseNameList);
        for (VirtualAdjustDetailExcelDTO excelDTO : allList) {
            if (CharSequenceUtil.isNotBlank(excelDTO.getErrorMsg())){
                continue;
            }
            List<String> errorMsgList = new ArrayList<>();
            ProductDetailEntity productDetail = skuVOS.stream().filter(e -> Objects.equals(e.getSkuNo(), excelDTO.getSkuNo())).findFirst().orElse(null);
            if (Objects.isNull(productDetail)){
                errorMsgList.add("SKU不存在");
            }else {
                excelDTO.setSkuId(productDetail.getId());
                excelDTO.setProductName(productDetail.getName());
            }
            VirtualWarehouseEntity virtualWarehouseEntity = virtualWarehouseEntityList.stream().filter(e -> Objects.equals(e.getName(), excelDTO.getVirtualWarehouseName())).findFirst().orElse(null);
            if (Objects.isNull(virtualWarehouseEntity)){
                errorMsgList.add("虚拟仓库不存在");
            }else {
                excelDTO.setVirtualWarehouseId(virtualWarehouseEntity.getId());
            }
            if (CollUtil.isNotEmpty(detailList)){
                //记录是否在明细中存在
                VirtualAdjustDetailDTO.AddDTO addDTO = detailList.stream().filter(e -> e.getSkuId().equals(excelDTO.getSkuId()) && e.getVirtualWarehouseId().equals(excelDTO.getVirtualWarehouseId()) && e.getInventoryStatus().equals(excelDTO.getInventoryStatus())).findFirst().orElse(null);
                if (Objects.nonNull(addDTO)){
                    errorMsgList.add("该SKU-虚拟仓库-库存状态在明细中已存在");
                }
            }
            if (!errorMsgList.isEmpty()) {
                excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                errorList.add(excelDTO);
                continue;
            }
            successList.add(excelDTO);
        }

    }

}
