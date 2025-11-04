package com.erp.server.scm.kingdee.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.message.enums.ApiModuleTypeEnum;
import com.erp.server.scm.kingdee.SyncKingdeeService;
import com.erp.server.scm.service.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author Will
 * @version 1.0

 * @date 2023/3/10 14:43
 */
@Service
public class SyncKingdeeServiceImpl implements SyncKingdeeService {

    @Resource
    private PurchaseOrderService purchaseOrderService;

    @Resource
    private PurchasePriceService purchasePriceService;

    @Resource
    private PurchasePriceChangeService purchasePriceChangeService;

    @Resource
    private SupplierService supplierService;

    @Resource
    private PurchasePriceDetailService purchasePriceDetailService;

    @Resource
    private SubcontractOrderService subcontractOrderService;

    @Resource
    private SubcontractChangeService subcontractChangeService;

    @Resource
    private SubcontractOrderDetailService subcontractOrderDetailService;

    @Resource
    private PurchaseOrderDetailService purchaseOrderDetailService;

    @Resource
    private PurchaseChangeService purchaseChangeService;

    @Resource
    private AssetPurchaseOrderService assetPurchaseOrderService;

    @Resource
    private AssetPurchaseOrderDetailService assetPurchaseOrderDetailService;

    @Override
    public void updateBusinessSyncKingdeeStatus(Map<String, Object> params) {
        //模块类型编码
        String code = (String) params.get("code");
        //业务id
        String businessId = (String)params.get("businessId");
        //更新状态
        String status = (String)params.get("status");
        //金蝶id
        String syncKingdeeId = (String)params.get("kingdeeId");
        //明细数据
        Object details = params.get("details");

        //采购订单
        if (ApiModuleTypeEnum.PURCHASE_ORDER.getCode().toString().equals(code)) {
            if (ObjectUtils.isNotEmpty(details)) {
                JSONArray list = JSONUtil.parseArray(JSONUtil.toJsonStr(params.get("details")));
                purchaseOrderDetailService.updateKingdeeDetailId(list);
                return;
            }
            purchaseOrderService.updateSyncKingdeeId(businessId,syncKingdeeId);
        }
        //采购变更
        if (ApiModuleTypeEnum.PURCHASE_CHANGE.getCode().toString().equals(code)) {
            purchaseChangeService.updateSyncKingdeeStatus(businessId,syncKingdeeId);
        }
        //采购价目表
        if (ApiModuleTypeEnum.PURCHASE_PRICE.getCode().toString().equals(code)) {
            if (ObjectUtils.isNotEmpty(details)) {
                JSONArray list = JSONUtil.parseArray(JSONUtil.toJsonStr(params.get("details")));
                purchasePriceDetailService.updateKingdeeDetailId(list);
                return;
            }
            purchasePriceService.updateSyncKingdeeId(businessId,syncKingdeeId);
        }
        //采购调价表
        if (ApiModuleTypeEnum.PURCHASE_PRICE_CHANGE.getCode().toString().equals(code)) {
            purchasePriceChangeService.updateSyncKingdeeId(businessId,syncKingdeeId);
        }
        //供应商表
        if (ApiModuleTypeEnum.SUPPLIER.getCode().toString().equals(code)) {
            supplierService.updateSyncKingdeeId(businessId,syncKingdeeId);
        }
        //委外订单
        if (ApiModuleTypeEnum.SUBCONTRACT_ORDER.getCode().toString().equals(code)) {
            if (ObjectUtils.isNotEmpty(details)) {
                JSONArray list = JSONUtil.parseArray(JSONUtil.toJsonStr(params.get("details")));
                subcontractOrderDetailService.updateKingdeeDetailId(list);
                return;
            }
            subcontractOrderService.updateSyncKingdeeId(businessId,syncKingdeeId);
        }
        //委外变更单
        if (ApiModuleTypeEnum.SUBCONTRACT_CHAGE.getCode().toString().equals(code)) {
            subcontractChangeService.updateSyncKingdeeId(businessId,syncKingdeeId);
        }
        //资产采购订单
        if (ApiModuleTypeEnum.ASSET_PURCHASE_ORDER.getCode().toString().equals(code)) {
            if (ObjectUtils.isNotEmpty(details)) {
                JSONArray list = JSONUtil.parseArray(JSONUtil.toJsonStr(params.get("details")));
                assetPurchaseOrderDetailService.updateKingdeeDetailId(list);
                return;
            }
            assetPurchaseOrderService.updateSyncKingdeeId(businessId,syncKingdeeId);
        }

    }
}
