package com.erp.server.wms.kingdee.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncKingdeeStatusEnum;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.scm.entity.PurchaseOrderDetailEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.sys.dto.SysDepartmentUserNumberDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.ReturnModeEnum;
import com.erp.model.wms.enums.ReturnOrderSourceEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.ScmTaskFeign;
import com.erp.server.wms.kingdee.SyncKingdeeSoOutstockService;
import com.erp.server.wms.service.PurchaseReturnOrderDetailService;
import com.erp.server.wms.service.PurchaseReturnOrderService;
import com.erp.server.wms.service.WarehouseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 同步金蝶采购退货单
 *
 * @Author Luo_WG
 * @Date 2023/4/24 11:22
 **/
@Slf4j
@Service
public class SyncKingdeeSoOutstockServiceImpl implements SyncKingdeeSoOutstockService {

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private ScmTaskFeign scmTaskFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private PurchaseReturnOrderService purchaseReturnOrderService;

    @Resource
    private PurchaseReturnOrderDetailService purchaseReturnOrderDetailService;

    @Resource
    private MQProducerService mQProducerService;

    @Resource
    private WarehouseService warehouseService;

    /**
     * 发送消息同步金蝶
     *
     * @param entity
     * @param operate
     * @return void
     * @Author Luo_WG
     * @Date 2023/4/24 11:27
     **/
    @Override
    public void syncDataToKingdee(SoOutstockEntity entity, String operate) {
        Map<String, Object> resultMap = new HashMap<>();
        //金蝶id
        resultMap.put("syncKingdeeId", entity.getSyncKingdeeId());
        //业务id
        resultMap.put("id", entity.getId());
        //退货单号
        resultMap.put("code", entity.getCode());

        //退货单号
        resultMap.put("code", entity.getCode());




        //退货单明细
        List<PurchaseReturnOrderDetailEntity> detailList = purchaseReturnOrderDetailService.getDetailByMainId(entity.getId());
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }

        //获取sku的id集合
        List<String> skuIdList = detailList.stream().map(PurchaseReturnOrderDetailEntity::getSkuId).collect(Collectors.toList());
        //根据ids查询sku信息
        List<ProductDetailEntity> detailEntityList = plmTaskFeign.getByIdList(skuIdList);

        //获取界面传过来的采购单详情表id集合
        List<String> orderDetailIds = detailList.stream().map(PurchaseReturnOrderDetailEntity::getPurchaseOrderDetailId).collect(Collectors.toList());
        //根据ids查询采购单详情
        List<PurchaseOrderDetailEntity> purchaseOrderDetailEntities = scmTaskFeign.listPurchaseOrderDetailById(orderDetailIds);
        //获取仓库信息
        WarehouseEntity warehouseEntity = warehouseService.getById(entity.getWarehouseId());
        List<JSONObject> list = new ArrayList<>();

        resultMap.put("list", list);

        //操作（枚举SyncKingdeeOperateEnum）
        resultMap.put("operate", operate);

        //异步推送mq
        CompletableFuture.supplyAsync(() -> {
            SendResult result = mQProducerService.syncClassMsg(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, RocketMqTagEnum.KINGDEE_PURCHASE_RETURN_ORDER_TAG.getName(), resultMap, String.valueOf(resultMap.get("id")));
            if (result.getSendStatus().equals(SendStatus.SEND_OK)) {
                //mq发送成更新业务表状态及时间
                return purchaseReturnOrderService.updateSyncKingdeeStatus(entity.getId(), SyncKingdeeStatusEnum.IN_SYNC.getCode(), "", operate);
            }
            return Boolean.TRUE;
        });
    }
}
