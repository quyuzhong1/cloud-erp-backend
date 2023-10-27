package com.erp.server.wms.kingdee.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.message.enums.ApiModuleTypeEnum;
import com.erp.server.wms.kingdee.SyncKingdeeService;
import com.erp.server.wms.service.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Map;

/**
 * @author Will
 * @version 1.0

 * @date 2023/3/10 14:43
 */
@Service
public class SyncKingdeeServiceImpl implements SyncKingdeeService {

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private TransferInfoService transferInfoService;

    @Resource
    private OtherInstockService otherInstockService;

    @Resource
    private OtherOutstockService otherOutstockService;

    @Resource
    private PurchaseReturnOrderService purchaseReturnOrderService;

    @Resource
    private PoInstockService poInstockService;

    @Resource
    private PoInstockDetailService poInstockDetailService;

    @Resource
    private SoOutstockService soOutstockService;

    @Resource
    private SoReturnInstockService soReturnInstockService;

    @Resource
    private MachineInfoService machineInfoService;

    @Resource
    private StocktakingProfitLossService stocktakingProfitLossService;

    @Resource
    private WarehouseReceiveService warehouseReceiveService;

    @Resource
    private WarehouseReceiveDetailService warehouseReceiveDetailService;

    @Override
    public void updateBusinessSyncKingdeeStatus(Map<String, Object> params) {
        //模块类型编码
        String code = (String)params.get("code");
        //业务id
        String businessId = (String)params.get("businessId");
        //更新状态
        String status = (String)params.get("status");
        //金蝶id
        String syncKingdeeId = (String)params.get("kingdeeId");
        //明细数据
        Object details = params.get("details");

        //仓库
        if (ApiModuleTypeEnum.WAREHOUSE_INFO.getCode().toString().equals(code)) {
            warehouseService.updateSyncKingdeeStatus(businessId,status,syncKingdeeId,null);
        }
        //调拨申请单
        if (ApiModuleTypeEnum.TRANSFER_INFO.getCode().toString().equals(code)) {
            transferInfoService.updateSyncKingdeeStatus(Arrays.asList(businessId),status,syncKingdeeId,null);
        }
        //其他入库单
        if (ApiModuleTypeEnum.OTHER_INSTOCK.getCode().toString().equals(code)) {
            otherInstockService.updateSyncKingdeeStatus(businessId,status,syncKingdeeId,null);
        }
        //其他出库单
        if (ApiModuleTypeEnum.OTHER_OUTSTOCK.getCode().toString().equals(code)) {
            otherOutstockService.updateSyncKingdeeStatus(businessId,status,syncKingdeeId,null);
        }
        //采购退货单
        if (ApiModuleTypeEnum.PURCHASE_RETURN_ORDER.getCode().toString().equals(code)) {
            purchaseReturnOrderService.updateSyncKingdeeStatus(businessId,status,syncKingdeeId,null);
        }
        //采购入库单
        if (ApiModuleTypeEnum.PURCHASE_STOCK_IN.getCode().toString().equals(code)) {
            if (ObjectUtils.isNotEmpty(details)) {
                JSONArray list = JSONUtil.parseArray(JSONUtil.toJsonStr(params.get("details")));
                poInstockDetailService.updateKingdeeDetailId(list);
                return;
            }
            poInstockService.updateSyncKingdeeStatus(businessId,status,syncKingdeeId,null);
        }
        //销售出库单
        if (ApiModuleTypeEnum.SO_OUTSTOCK.getCode().toString().equals(code)) {
            soOutstockService.updateSyncKingdeeStatus(businessId,status,syncKingdeeId,null);
        }
        //销售退货入库单
        if (ApiModuleTypeEnum.SO_RETURN.getCode().toString().equals(code)) {
            soReturnInstockService.updateSyncKingdeeStatus(businessId,status,syncKingdeeId, null);
        }
        //加工单
        if (ApiModuleTypeEnum.MACHINE_INFO.getCode().toString().equals(code)) {
            machineInfoService.updateSyncKingdeeStatus(businessId,status,syncKingdeeId, null);
        }

        //盘盈单
        if (ApiModuleTypeEnum.STOCKTAKING_PROFIT.getCode().toString().equals(code)) {
            stocktakingProfitLossService.updateSyncKingdeeStatus(businessId,status,syncKingdeeId, null);
        }
        //盘亏单
        if (ApiModuleTypeEnum.STOCKTAKING_LOSS.getCode().toString().equals(code)) {
            stocktakingProfitLossService.updateSyncKingdeeStatus(businessId,status,syncKingdeeId, null);
        }
        //采购收货单
        if (ApiModuleTypeEnum.PO_RECEIVE.getCode().toString().equals(code)) {
            warehouseReceiveService.updateSyncKingdeeStatus(businessId,status,syncKingdeeId, null);
        }
        //采购收货单
        if (ApiModuleTypeEnum.PO_RECEIVE.getCode().toString().equals(code)) {
            if (ObjectUtils.isNotEmpty(details)) {
                JSONArray list = JSONUtil.parseArray(JSONUtil.toJsonStr(params.get("details")));
                warehouseReceiveDetailService.updateKingdeeDetailId(list);
                return;
            }
            warehouseReceiveService.updateSyncKingdeeStatus(businessId,status,syncKingdeeId, null);
        }
    }
}
