package com.erp.server.dmp.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.common.core.utils.StrUtils;
import com.erp.model.dmp.dto.mabang.MabangInOutStockDTO;
import com.erp.model.dmp.entity.DmpWarehouseMappingEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.wms.entity.TransferInfoDetailEntity;
import com.erp.model.wms.entity.TransferInfoEntity;
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
     * 验证直接调拨单同步马帮出入库是否在监控仓库
     * @param inWarehouseCode
     * @param outWarehouseCode
     * @param warehouseMap
     * @param warehouseNameList
     * @return
     */
    public static Map<String, Object> checkTransferInOutWarehouse(String inWarehouseCode, String outWarehouseCode,
                                                                  Map<String, DmpWarehouseMappingEntity> warehouseMap,
                                                                  List<String> warehouseNameList) {
        Map<String, Object> resultMap = Maps.newHashMap();
        String inWarehouseName = "", outWarehouseName = "";
        if(Objects.isNull(warehouseMap.get(inWarehouseCode)) && Objects.isNull(warehouseMap.get(outWarehouseCode))) {
            String msg = StrUtil.format("ERP直接调拨单同步到马帮出入库调入仓和调出仓在马帮未找到映射，调入仓【{}】，调出仓【{}】,不需要推送马帮出入库", inWarehouseCode, outWarehouseCode);
            log.info(msg);
            resultMap.put("stop", true);
            return resultMap;
        }
        if(Objects.nonNull(warehouseMap.get(inWarehouseCode))) {
            inWarehouseName = warehouseMap.get(inWarehouseCode).getWarehouseName();
        }
        if(Objects.nonNull(warehouseMap.get(outWarehouseCode))) {
            outWarehouseName = warehouseMap.get(outWarehouseCode).getWarehouseName();
        }
        if(!warehouseNameList.contains(inWarehouseName) && !warehouseNameList.contains(outWarehouseName)) {
            String msg = StrUtil.format("ERP直接调拨单同步到马帮出入库调入仓和调出仓不在监控仓库范围内，调入仓【{}】，调出仓【{}】,不需要推送马帮出入库", inWarehouseName, outWarehouseName);
            log.info(msg);
            resultMap.put("stop", true);
            return resultMap;
        }
        resultMap.put("stop", false);
        resultMap.put("inWarehouseCode", inWarehouseName);
        resultMap.put("outWarehouseCode", outWarehouseName);
        return resultMap;
    }


    /**
     * 填充马帮出入库实体
     * @param warehouseCode
     * @param warehouseName
     * @param productDetailList
     * @param transferInfo
     * @param transferDetailList
     * @return
     */
    public static MabangInOutStockDTO fillMabangInOutStock(String warehouseCode, String warehouseName,
                                                           List<ProductDetailEntity> productDetailList,
                                                           TransferInfoEntity transferInfo, List<TransferInfoDetailEntity> transferDetailList) {

        List<MabangInOutStockDTO.SkuItem> data = Lists.newArrayList();
        MabangInOutStockDTO mabangInOutStockDTO = new MabangInOutStockDTO();
        mabangInOutStockDTO.setWarehouseCode(warehouseCode);
        mabangInOutStockDTO.setWarehouseName(warehouseName);
        mabangInOutStockDTO.setEmployeeName(StrUtils.null2EmptyWithTrim(transferInfo.getCreateUserName()));
        mabangInOutStockDTO.setRemark(StrUtil.format("ERP同步：{}", StrUtils.null2EmptyWithTrim(transferInfo.getCode()) ));

        transferDetailList.stream().forEach(transferSku->{
            MabangInOutStockDTO.SkuItem skuItem = new MabangInOutStockDTO.SkuItem();
            skuItem.setStockSku(transferSku.getSkuNo());
            if (CollUtil.isNotEmpty(productDetailList)) {
                String productName = productDetailList.stream().filter(e -> Objects.equals(e.getId(), transferSku.getSkuId())).map(ProductDetailEntity::getName).findFirst().orElse(null);
                skuItem.setProductName(productName);
            }
            skuItem.setQuantity(StrUtils.null2EmptyWithTrim(transferSku.getQty()));
            skuItem.setGridCode(StrUtils.null2EmptyWithTrim(transferSku.getInWarehouseLocation()));
            skuItem.setSourceDetailId(transferSku.getId());
            data.add(skuItem);
        });
        mabangInOutStockDTO.setData(data);
        return mabangInOutStockDTO;
    }

}