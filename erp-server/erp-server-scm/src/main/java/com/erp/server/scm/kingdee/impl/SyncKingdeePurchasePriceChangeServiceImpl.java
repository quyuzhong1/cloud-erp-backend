package com.erp.server.scm.kingdee.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.dto.DmpSyncTaskDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.enums.SyncStatusEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.common.core.utils.StrUtils;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.scm.entity.*;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.scm.kingdee.SyncKingdeePurchasePriceChangeService;
import com.erp.server.scm.service.PurchasePriceChangeDetailService;
import com.erp.server.scm.service.PurchasePriceDetailService;
import com.erp.server.scm.service.PurchasePriceService;
import com.erp.server.scm.service.SupplierService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Will
 * @version 1.0

 * @date 2023/4/4 12:25
 */
@Slf4j
@Service
public class SyncKingdeePurchasePriceChangeServiceImpl implements SyncKingdeePurchasePriceChangeService {

    @Resource
    private PurchasePriceService purchasePriceService;

    @Resource
    private PurchasePriceChangeDetailService purchasePriceChangeDetailService;

    @Resource
    private PurchasePriceDetailService purchasePriceDetailService;

    @Resource
    private SupplierService supplierService;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private DmpMqFeign dmpMqFeign;


    /**
     * 组装数据发送到金蝶
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public void syncDataToKingdee(PurchasePriceChangeEntity entity, String operate) {
        Map<String, Object> resultMap = new HashMap<>();

        List<PurchasePriceChangeDetailEntity> purchasePriceChangeDetailEntities = purchasePriceChangeDetailService.listByMainIdList(Arrays.asList(entity.getId()));
        List<String> purchasePriceDetailId = purchasePriceChangeDetailEntities.stream().map(req -> req.getPurchasePriceDetailId()).distinct().collect(Collectors.toList());

        //如果上游单据未发送成功则无需发送
        List<PurchasePriceDetailEntity> purchasePriceDetailEntities = purchasePriceDetailService.listByIds(purchasePriceDetailId);
        //采购价目明细表数据
        if (CollectionUtils.isEmpty(purchasePriceDetailEntities)) {
            throw new ServiceException(ApiError.ERROR_98024);
        }
        String purchasePriceIdStr = purchasePriceDetailEntities.stream().map(req -> req.getPurchasePriceId()).distinct().collect(Collectors.joining(","));

        //业务id
        resultMap.put("id",entity.getId());
        //编码
        resultMap.put("code",entity.getCode());
        //名称
        resultMap.put("name",entity.getCode());
        //金蝶id
        resultMap.put("syncKingdeeId",entity.getSyncKingdeeId());
        //操作（枚举SyncKingdeeOperateEnum）
        resultMap.put("operate", operate);

        //删除操作
        if (SyncOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
            sendMqAndSaveTask(entity,operate,resultMap, purchasePriceIdStr);
            return;
        }
        //调价原因
        resultMap.put("reason",entity.getReason());
        //调价日期
        resultMap.put("adjustDate", LocalDateTimeUtil.format(entity.getAdjustDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        //组织机构编码
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(Arrays.asList(entity.getPurchaseOrgId()));
        if (CollectionUtils.isNotEmpty(accountingCompanyList)) {
            //采购组织
            String orgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getPurchaseOrgId()))
                    .findFirst().flatMap(obj -> Optional.ofNullable(obj.getCode())).orElse(null);
            resultMap.put("purchaseOrgCode", orgCode);
        }

        //调价明细
        List<PurchasePriceChangeDetailEntity> details = purchasePriceChangeDetailService.listByPurchasePriceChangeId(entity.getId());
        if (CollectionUtils.isEmpty(details)) {
            return;
        }
        List<String> detailIds = details.stream().map(PurchasePriceChangeDetailEntity::getPurchasePriceDetailId).collect(Collectors.toList());

        List<PurchasePriceDetailEntity> purchasePriceDetailList = purchasePriceDetailService.listByIds(detailIds);
        //价目明细
        if (CollectionUtils.isEmpty(purchasePriceDetailList)) {
            throw new ServiceException(ApiError.ERROR_98024);
        }

        //查询供应商
        List<String> supplierIds = details.stream().map(req -> req.getSupplierId()).distinct().collect(Collectors.toList());
        List<SupplierEntity> supplierEntities = supplierService.listByIds(supplierIds);
        if (CollectionUtils.isEmpty(supplierEntities)) {
            return;
        }


        List<JSONObject> list = new ArrayList<>();
        for (PurchasePriceChangeDetailEntity detailEntity : details) {

            PurchasePriceDetailEntity purchasePriceDetailEntity = purchasePriceDetailList.stream().filter(obj -> obj.getId().equals(detailEntity.getPurchasePriceDetailId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(purchasePriceDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_98024);
            }

            JSONObject jsonObject = new JSONObject();
            //采购价目编号
            jsonObject.set("CGJM_code",detailEntity.getCJJMCode());
            //采购价目明细金蝶id
            jsonObject.set("kingdeeDetailId",purchasePriceDetailEntity.getKingdeeDetailId());
            //供应商编号
            SupplierEntity supplierEntity = supplierEntities.stream().filter(req -> detailEntity.getSupplierId().equals(req.getId())).findFirst().orElse(null);
            jsonObject.set("supplierCode",supplierEntity.getCode());
            //从
            jsonObject.set("minQty",detailEntity.getMinQty());
            //至
            jsonObject.set("maxQty",detailEntity.getMaxQty());

            jsonObject.set("skuNo",detailEntity.getSkuNo());
            jsonObject.set("beforeTaxPrice",purchasePriceDetailEntity.getTaxPrice());
            jsonObject.set("afterTaxPrice",detailEntity.getTaxPrice());
            jsonObject.set("beforeTaxRate",MathUtil.multiply(purchasePriceDetailEntity.getTaxRate(),MathUtil.BigDecimal_100));
            jsonObject.set("afterTaxRate",MathUtil.multiply(detailEntity.getTaxRate(),MathUtil.BigDecimal_100));
            jsonObject.set("effectiveDate", LocalDateTimeUtil.format(detailEntity.getEffectiveDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            jsonObject.set("expireDate",LocalDateTimeUtil.format(detailEntity.getEffectiveDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            list.add(jsonObject);
        }
        resultMap.put("list",list);

        //生成任务
        sendMqAndSaveTask(entity,operate,resultMap, purchasePriceIdStr);
    }

    /**
     * @description: 生成任务
     * @author Will
     * @date: 2023/10/16 9:17
     * @param entity
     * @param operate
     * @param resultMap
     */
    private void sendMqAndSaveTask (PurchasePriceChangeEntity entity, String operate, Map<String, Object> resultMap, String purchasePriceIdStr) {
        //添加推送任务
        DmpPushTaskFeignDTO dmpSyncTaskDTO = new DmpPushTaskFeignDTO();
        dmpSyncTaskDTO.setSourceId(entity.getId());
        dmpSyncTaskDTO.setSourceCode(entity.getCode());
        dmpSyncTaskDTO.setSourceType(SourceTypeEnum.PURCHASE_PRICE_CHANGE.getCode());
        dmpSyncTaskDTO.setMqTopic(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC);
        dmpSyncTaskDTO.setMqTag(RocketMqTagEnum.KINGDEE_PURCHASE_PRICE_CHANGE_TAG.getName());
        dmpSyncTaskDTO.setMqData(JSONUtil.toJsonStr(resultMap));
        dmpSyncTaskDTO.setSourcePlatformName(PlatformEnum.ERP.getDesc());
        dmpSyncTaskDTO.setTargetPlatformName(PlatformEnum.KINGDEE.getDesc());
        dmpSyncTaskDTO.setSyncOperate(operate);
        //多个ID用','拼接
        dmpSyncTaskDTO.setParentId(purchasePriceIdStr);
        dmpMqFeign.sendMqAndSaveTask(dmpSyncTaskDTO);
    }
}
