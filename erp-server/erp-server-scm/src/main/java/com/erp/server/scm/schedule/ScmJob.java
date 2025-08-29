package com.erp.server.scm.schedule;

import cn.hutool.core.bean.BeanUtil;

import com.erp.model.scm.dto.KingdeePaymentConditionDTO;
import com.erp.model.scm.entity.KingdeePaymentConditionEntity;
import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;
import com.erp.sdk.third.kingdee.utils.KingdeePushModuleEnum;
import com.erp.server.scm.rocketmq.sync.wms.WmsSyncPurchaseService;
import com.erp.server.scm.service.KingdeePaymentConditionService;
import com.erp.server.scm.service.PurchaseOrderDetailService;
import com.erp.server.scm.service.PurchaseOrderService;
import com.erp.server.scm.service.PurchaseOrderSupplierService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * scm定时器
 * @Author Luo_WG
 * @Date 2023/6/19 12:59
 **/
@Component
@Slf4j
public class ScmJob {
    @Resource
    private WmsSyncPurchaseService WmsSyncPurchaseService;

    @Resource
    private PurchaseOrderService purchaseOrderService;

    @Resource
    private PurchaseOrderDetailService purchaseOrderDetailService;

    @Resource
    private PurchaseOrderSupplierService purchaseOrderSupplierService;

    @Resource
    private KingdeePaymentConditionService kingdeePaymentConditionService;

    /**
     * 同步采购信息到wms
     */
    @XxlJob("syncPurchaseToWms")
    public void syncPurchaseToWms() {
//        WmsSyncPurchaseService.syncPurchaseOrderToWms(purchaseOrderService.list());
//        WmsSyncPurchaseService.syncPurchaseOrderDetailToWms(purchaseOrderDetailService.list());
//        WmsSyncPurchaseService.syncPurchaseOrderSupplierToWms(purchaseOrderSupplierService.list());
    }


    /**
     * 采购订单自动确认
     */
    @XxlJob("purchaseOrderAutoConfirm")
    public void purchaseOrderAutoConfirm() {
        XxlJobHelper.log("=====采购订单自动确认 开始任务=====");
        long start = System.currentTimeMillis();
        purchaseOrderService.purchaseOrderAutoConfirm();
        long end = System.currentTimeMillis();
        XxlJobHelper.log("主线程花费时间：{}", (end - start));
        XxlJobHelper.log("=====采购订单自动确认 结束任务=====");
    }

    /**
     * 同步金蝶付款条件
     */
    @XxlJob("syncKingdeePaymentCondition")
    public void syncKingdeePaymentCondition() {
        XxlJobHelper.log("=====同步金蝶付款条件 开始任务=====");
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.BD_PAYMENTCONDITION.getCode());
        //查询
        String fieldKeys = "FID,FNumber,FName,FFORBIDSTATUS,FDOCUMENTSTATUS";
        // 当前页数
        Integer pageIndex = 0;
        // 每次最多获取100条
        Integer pageSize = 1000;
        Boolean dataSign = true;
        List<KingdeePaymentConditionDTO.KingdeeDTO> paymentConditionList = new ArrayList<>(20);
        while (dataSign) {
            List<Map<String, Object>> result = apiUtils.queryList("", fieldKeys, pageSize, pageIndex, 0);
            if (result.size() < pageSize) {
                dataSign = false;
            }
            List<KingdeePaymentConditionDTO.KingdeeDTO> entityList = result.stream().map(entity ->
                    BeanUtil.toBean(entity, KingdeePaymentConditionDTO.KingdeeDTO.class)).collect(Collectors.toList());
            paymentConditionList.addAll(entityList);
            pageIndex++;
        }
        //数据库存在的
        List<KingdeePaymentConditionEntity> dbList = kingdeePaymentConditionService.list();
        //这个是查询到的
        List<String> queryKingdeeIds = paymentConditionList.stream().map(KingdeePaymentConditionDTO.KingdeeDTO::getKingdeeId).collect(Collectors.toList());
        //表示这些是删除的 那就要禁用
        List<String> disableIds = dbList.stream().filter(d -> !queryKingdeeIds.contains(d.getKingdeeId())).map(KingdeePaymentConditionEntity::getId).collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(disableIds)){
            kingdeePaymentConditionService.updateDisable(disableIds,true);
        }
        List<KingdeePaymentConditionEntity> saveOrUpdateList = new ArrayList<>(20);
        for (KingdeePaymentConditionDTO.KingdeeDTO item : paymentConditionList) {
            String kingdeeId = item.getKingdeeId();
            String code = item.getCode();
            String name = item.getName();
            String kingdeeStatus = item.getKingdeeStatus();
            //禁用状态
            String kingdeeDisabledStatus = item.getKingdeeDisabledStatus();
            //B 表示禁用
            Boolean disabled = "B".equals(kingdeeDisabledStatus);
            //表示 未禁用
            if(!disabled){
                //表示未审核
                if(!"C".equals(kingdeeStatus)){
                    disabled=Boolean.TRUE;
                }
            }

            KingdeePaymentConditionEntity dbEntity = dbList.stream().filter(entity -> entity.getKingdeeId().equals(kingdeeId)).
                    findFirst().orElse(null);

            if (Objects.isNull(dbEntity)) {
                KingdeePaymentConditionEntity addEntity = new KingdeePaymentConditionEntity();
                addEntity.setKingdeeId(kingdeeId);
                addEntity.setCode(code);
                addEntity.setName(name);
                addEntity.setKingdeeStatus(kingdeeStatus);
                addEntity.setDisabled(disabled);
                saveOrUpdateList.add(addEntity);
            } else {
                //表示有 是否修改
                if (!dbEntity.getCode().equals(code) || !dbEntity.getName().equals(name) ||
                        !dbEntity.getKingdeeStatus().
                                equals(kingdeeStatus) || !dbEntity.getDisabled().equals(disabled)) {
                    dbEntity.setCode(code);
                    dbEntity.setName(name);
                    dbEntity.setKingdeeStatus(kingdeeStatus);
                    dbEntity.setDisabled(disabled);
                    saveOrUpdateList.add(dbEntity);
                }
            }

        }
        kingdeePaymentConditionService.saveOrUpdateBatch(saveOrUpdateList);

        XxlJobHelper.log("=====同步金蝶付款条件 结束任务=====");
    }


    /**
     * (供应商 + 采购订单 + sku )采购数量计算
     */
    @XxlJob("calSupplierPurchaseQty")
    public void calSupplierPurchaseQty() {
        XxlJobHelper.log("=====(供应商+采购订单+sku)采购数量计算 开始任务=====");
        long start = System.currentTimeMillis();
        purchaseOrderService.calSupplierPurchaseQty();
        long end = System.currentTimeMillis();
        XxlJobHelper.log("主线程花费时间：{}", (end - start));
        XxlJobHelper.log("=====(供应商+采购订单+sku)采购数量计算 结束任务=====");
    }

}
