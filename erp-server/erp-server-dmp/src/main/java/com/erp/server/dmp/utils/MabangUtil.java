package com.erp.server.dmp.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.utils.RedisUtil;
import com.common.core.utils.StrUtils;
import com.common.business.constant.RedisCacheConstants;
import com.erp.model.dmp.dto.mabang.MabangInOutStockDTO;
import com.erp.model.dmp.mabang.RedisMabngSkuEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.inventory.InventoryInOutEnum;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
     * 马帮手工出入库明细最大条数
     */
    public static final int MAX_DETAIL_SIZE = 500;


    /**
     * 填充马帮出入库实体(直接调拨单)
     *
     * @param warehouseCode
     * @param warehouseName
     * @param productDetailList
     * @param transferInfo
     * @param transferDetailList
     * @param inOutType
     * @return
     */
    public static MabangInOutStockDTO fillMabangInOutStock(String warehouseCode, String warehouseName, String employeeName,
                                                           List<ProductDetailEntity> productDetailList,
                                                           TransferInfoEntity transferInfo, List<TransferInfoDetailEntity> transferDetailList,
                                                           String inOutType, String opType, RedisUtil redisUtil) {

        List<MabangInOutStockDTO.SkuItem> data = Lists.newArrayList();
        MabangInOutStockDTO mabangInOutStockDTO = new MabangInOutStockDTO();
        mabangInOutStockDTO.setErpSourceCode(transferInfo.getCode());
        mabangInOutStockDTO.setWarehouseCode(warehouseCode);
        mabangInOutStockDTO.setWarehouseName(warehouseName);
        mabangInOutStockDTO.setType(inOutType);
        mabangInOutStockDTO.setEmployeeName(StrUtils.null2EmptyWithTrim(employeeName));
        mabangInOutStockDTO.setRemark(StrUtil.format("ERP同步：{}", StrUtils.null2EmptyWithTrim(transferInfo.getCode()) ));
        mabangInOutStockDTO.setApproveType(opType);

        Map<String, MabangInOutStockDTO.SkuItem> skuItemMap = Maps.newHashMap();
        transferDetailList.stream().forEach(transferSku->{
            RedisMabngSkuEntity mabangSkuInfo = redisUtil.getHashMap(RedisCacheConstants.MABANG_FINANCIAL_SKU_LIST_KEY, transferSku.getSkuNo());
            String gridCode = "";
            // 审核
            if(Objects.equals(SyncOperateEnum.OPERATE_APPROVE.getCode(), opType)) {
                gridCode = StrUtils.null2EmptyWithTrim(Objects.equals(inOutType, InventoryInOutEnum.IN_STOCK.getCode() )? StrUtils.null2EmptyWithTrim(transferSku.getInWarehouseLocation()) : StrUtils.null2EmptyWithTrim(transferSku.getOutWarehouseLocation()));
            } else if(Objects.equals(SyncOperateEnum.OPERATE_DISAPPROVE.getCode(), opType)) {
                // 反审核
                gridCode = StrUtils.null2EmptyWithTrim(Objects.equals(inOutType, InventoryInOutEnum.IN_STOCK.getCode() )? StrUtils.null2EmptyWithTrim(transferSku.getOutWarehouseLocation()) : StrUtils.null2EmptyWithTrim(transferSku.getInWarehouseLocation()));
            }
            String skuNo = ObjectUtil.isNotEmpty(mabangSkuInfo) ? mabangSkuInfo.getStockSku() : transferSku.getSkuNo();
            String skuWareLocation = skuNo + "-" + gridCode;
            MabangInOutStockDTO.SkuItem skuItem;
            if(skuItemMap.containsKey(skuWareLocation)) {
                skuItem =   skuItemMap.get(skuWareLocation);
            } else {
                skuItem = new MabangInOutStockDTO.SkuItem();
            }

            if (CollUtil.isNotEmpty(productDetailList)) {
                String productName = productDetailList.stream().filter(e -> Objects.equals(e.getId(), transferSku.getSkuId())).map(ProductDetailEntity::getName).findFirst().orElse(null);
                skuItem.setProductName(productName);
            }
            skuItem.setStockSku(skuNo);
            skuItem.setGridCode(gridCode);

            Integer qty = transferSku.getQty();
            if(skuItemMap.containsKey(skuWareLocation)) {
                qty = Integer.valueOf(skuItem.getQuantity()) + qty;
            }
            skuItem.setQuantity(StrUtils.null2EmptyWithTrim(qty));
            skuItem.setSourceDetailId("");
            if(!skuItemMap.containsKey(skuWareLocation)) {
                skuItemMap.put(skuWareLocation, skuItem);
                data.add(skuItem);
            }
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

        Map<String, List<MachineDetailEntity>> machineDetailMap = machineDetailList.stream().collect(
                Collectors.groupingBy(r -> r.getSkuId() + "-" + StrUtils.null2EmptyWithTrim(r.getWarehouseLocation()), Collectors.toList()));

        machineDetailMap.forEach((key, multiList)->{
            MabangInOutStockDTO.SkuItem skuItem = new MabangInOutStockDTO.SkuItem();
            String skuNo = multiList.get(0).getSkuNo();
            String skuId = multiList.get(0).getSkuId();
            String warehouseLocation = StrUtils.null2EmptyWithTrim(multiList.get(0).getWarehouseLocation());
            int sumQty = multiList.stream().mapToInt(MachineDetailEntity::getQty).sum();
            skuItem.setStockSku(skuNo);
            if (CollUtil.isNotEmpty(productDetailList)) {
                String productName = productDetailList.stream().filter(e -> Objects.equals(e.getId(), skuId)).map(ProductDetailEntity::getName).findFirst().orElse(null);
                skuItem.setProductName(productName);
            }
            skuItem.setQuantity(StrUtils.null2EmptyWithTrim(sumQty));
            skuItem.setGridCode(StrUtils.null2EmptyWithTrim(warehouseLocation));
            skuItem.setSourceDetailId("");
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

        Map<String, List<MachineSubComponentsEntity>> machineDetailMap = machineSubList.stream().collect(
                Collectors.groupingBy(r -> r.getSkuId() + "-" + StrUtils.null2EmptyWithTrim(r.getWarehouseLocation()), Collectors.toList()));

        machineDetailMap.forEach((key, multiList)-> {
            MabangInOutStockDTO.SkuItem skuItem = new MabangInOutStockDTO.SkuItem();
            String skuNo = multiList.get(0).getSkuNo();
            String skuId = multiList.get(0).getSkuId();
            String warehouseLocation = StrUtils.null2EmptyWithTrim(multiList.get(0).getWarehouseLocation());
            int sumQty = multiList.stream().mapToInt(MachineSubComponentsEntity::getQty).sum();

            skuItem.setStockSku(skuNo);
            if (CollUtil.isNotEmpty(productDetailList)) {
                String productName = productDetailList.stream().filter(e -> Objects.equals(e.getId(), skuId)).map(ProductDetailEntity::getName).findFirst().orElse(null);
                skuItem.setProductName(productName);
            }
            skuItem.setQuantity(StrUtils.null2EmptyWithTrim(sumQty));
            skuItem.setGridCode(StrUtils.null2EmptyWithTrim(warehouseLocation ));
            skuItem.setSourceDetailId("");
            data.add(skuItem);
        });
        mabangInOutStockDTO.setData(data);
        return mabangInOutStockDTO;
    }

}