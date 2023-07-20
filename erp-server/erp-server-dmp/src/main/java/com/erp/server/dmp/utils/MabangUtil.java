package com.erp.server.dmp.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.enums.SyncKingdeeOperateEnum;
import com.common.core.utils.StrUtils;
import com.erp.model.dmp.dto.mabang.MabangInOutStockDTO;
import com.erp.model.dmp.entity.DmpWarehouseMappingEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.inventory.InventoryInOutEnum;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @CreateTime: 2023-06-28  11:46
 * @Author: zhangchunlin
 */
@Slf4j
public class MabangUtil {

    /**
     * 马帮出入库操作员
     */
    public static final String MABANG_EMPLOYEE_NAME = "API同步";

    /**
     * 直接调拨单同步马帮出入库是否在监控仓库
     */
    public static final String SEND_STOP = "stop";

    /**
     * 调入仓编码
     */
    public static final String IN_WAREHOUSE_CODE = "inWarehouseCode";

    /**
     * 调出仓编码
     */
    public static final String OUT_WAREHOUSE_CODE = "outWarehouseCode";

    /**
     * 分隔符
     */
    public static final String SEPARATOR = ",";


    /**
     * 填充马帮出入库实体(直接调拨单)
     * @param warehouseCode
     * @param warehouseName
     * @param productDetailList
     * @param transferInfo
     * @param transferDetailList
     * @param inOutType
     * @return
     */
    public static MabangInOutStockDTO fillMabangInOutStock(String warehouseCode, String warehouseName,String employeeName,
                                                           List<ProductDetailEntity> productDetailList,
                                                           TransferInfoEntity transferInfo, List<TransferInfoDetailEntity> transferDetailList,
                                                           String inOutType, String opType) {

        List<MabangInOutStockDTO.SkuItem> data = Lists.newArrayList();
        MabangInOutStockDTO mabangInOutStockDTO = new MabangInOutStockDTO();
        mabangInOutStockDTO.setErpSourceCode(transferInfo.getCode());
        mabangInOutStockDTO.setWarehouseCode(warehouseCode);
        mabangInOutStockDTO.setWarehouseName(warehouseName);
        mabangInOutStockDTO.setType(inOutType);
        mabangInOutStockDTO.setEmployeeName(StrUtils.null2EmptyWithTrim(employeeName));
        mabangInOutStockDTO.setRemark(StrUtil.format("ERP同步：{}", StrUtils.null2EmptyWithTrim(transferInfo.getCode()) ));
        mabangInOutStockDTO.setApproveType(opType);

        transferDetailList.stream().forEach(transferSku->{
            MabangInOutStockDTO.SkuItem skuItem = new MabangInOutStockDTO.SkuItem();
            skuItem.setStockSku(transferSku.getSkuNo());
            if (CollUtil.isNotEmpty(productDetailList)) {
                String productName = productDetailList.stream().filter(e -> Objects.equals(e.getId(), transferSku.getSkuId())).map(ProductDetailEntity::getName).findFirst().orElse(null);
                skuItem.setProductName(productName);
            }
            skuItem.setQuantity(StrUtils.null2EmptyWithTrim(transferSku.getQty()));
            // 审核
            if(Objects.equals(SyncKingdeeOperateEnum.OPERATE_APPROVE.getCode(), opType)) {
                skuItem.setGridCode(StrUtils.null2EmptyWithTrim(Objects.equals(inOutType, InventoryInOutEnum.IN_STOCK.getCode() )? StrUtils.null2EmptyWithTrim(transferSku.getInWarehouseLocation()) : StrUtils.null2EmptyWithTrim(transferSku.getOutWarehouseLocation())));
            } else if(Objects.equals(SyncKingdeeOperateEnum.OPERATE_DISAPPROVE.getCode(), opType)) {
                // 反审核
                skuItem.setGridCode(StrUtils.null2EmptyWithTrim(Objects.equals(inOutType, InventoryInOutEnum.IN_STOCK.getCode() )? StrUtils.null2EmptyWithTrim(transferSku.getOutWarehouseLocation()) : StrUtils.null2EmptyWithTrim(transferSku.getInWarehouseLocation())));
            }
            skuItem.setSourceDetailId(transferSku.getId());
            data.add(skuItem);
        });
        mabangInOutStockDTO.setData(data);
        return mabangInOutStockDTO;
    }

    /**
     * 填充马帮出入库实体(加工单父级sku)
     * @param warehouseCode
     * @param warehouseName
     * @param productDetailList
     * @param machineInfoEntity
     * @param machineDetailList
     * @param inOutType
     * @return
     */
    public static MabangInOutStockDTO fillMabangInOutStock(String warehouseCode, String warehouseName, String employeeName,
                                                           List<ProductDetailEntity> productDetailList,
                                                           MachineInfoEntity machineInfoEntity, List<MachineDetailEntity> machineDetailList,
                                                           String inOutType) {

        List<MabangInOutStockDTO.SkuItem> data = Lists.newArrayList();
        MabangInOutStockDTO mabangInOutStockDTO = new MabangInOutStockDTO();
        mabangInOutStockDTO.setErpSourceCode(machineInfoEntity.getCode());
        mabangInOutStockDTO.setWarehouseCode(warehouseCode);
        mabangInOutStockDTO.setWarehouseName(warehouseName);
        mabangInOutStockDTO.setType(inOutType);
        mabangInOutStockDTO.setEmployeeName(StrUtils.null2EmptyWithTrim(employeeName));
        mabangInOutStockDTO.setRemark(StrUtil.format("ERP同步：{}", StrUtils.null2EmptyWithTrim(machineInfoEntity.getCode()) ));

        machineDetailList.stream().forEach(parentMachine->{
            MabangInOutStockDTO.SkuItem skuItem = new MabangInOutStockDTO.SkuItem();
            skuItem.setStockSku(parentMachine.getSkuNo());
            if (CollUtil.isNotEmpty(productDetailList)) {
                String productName = productDetailList.stream().filter(e -> Objects.equals(e.getId(), parentMachine.getSkuId())).map(ProductDetailEntity::getName).findFirst().orElse(null);
                skuItem.setProductName(productName);
            }
            skuItem.setQuantity(StrUtils.null2EmptyWithTrim(parentMachine.getQty()));
            skuItem.setGridCode(StrUtils.null2EmptyWithTrim(parentMachine.getWarehouseLocation() ));
            skuItem.setSourceDetailId(parentMachine.getId());
            data.add(skuItem);
        });
        mabangInOutStockDTO.setData(data);
        return mabangInOutStockDTO;
    }

    /**
     * 填充马帮出入库实体(加工单子级sku)
     * @param warehouseCode
     * @param warehouseName
     * @param productDetailList
     * @param machineInfoEntity
     * @param machineSubList
     * @param inOutType
     * @return
     */
    public static MabangInOutStockDTO fillMabangInOutStockSub(String warehouseCode, String warehouseName, String employeeName,
                                                           List<ProductDetailEntity> productDetailList,
                                                           MachineInfoEntity machineInfoEntity, List<MachineSubComponentsEntity> machineSubList,
                                                           String inOutType) {

        List<MabangInOutStockDTO.SkuItem> data = Lists.newArrayList();
        MabangInOutStockDTO mabangInOutStockDTO = new MabangInOutStockDTO();
        mabangInOutStockDTO.setErpSourceCode(machineInfoEntity.getCode());
        mabangInOutStockDTO.setWarehouseCode(warehouseCode);
        mabangInOutStockDTO.setWarehouseName(warehouseName);
        mabangInOutStockDTO.setType(inOutType);
        mabangInOutStockDTO.setEmployeeName(StrUtils.null2EmptyWithTrim(employeeName));
        mabangInOutStockDTO.setRemark(StrUtil.format("ERP同步：{}", StrUtils.null2EmptyWithTrim(machineInfoEntity.getCode()) ));

        machineSubList.stream().forEach(subMachine->{
            MabangInOutStockDTO.SkuItem skuItem = new MabangInOutStockDTO.SkuItem();
            skuItem.setStockSku(subMachine.getSkuNo());
            if (CollUtil.isNotEmpty(productDetailList)) {
                String productName = productDetailList.stream().filter(e -> Objects.equals(e.getId(), subMachine.getSkuId())).map(ProductDetailEntity::getName).findFirst().orElse(null);
                skuItem.setProductName(productName);
            }
            skuItem.setQuantity(StrUtils.null2EmptyWithTrim(subMachine.getQty()));
            skuItem.setGridCode(StrUtils.null2EmptyWithTrim(subMachine.getWarehouseLocation() ));
            skuItem.setSourceDetailId(subMachine.getId());
            data.add(skuItem);
        });
        mabangInOutStockDTO.setData(data);
        return mabangInOutStockDTO;
    }

}