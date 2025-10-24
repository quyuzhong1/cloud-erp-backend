package com.erp.server.plm.rocketmq.sync.kingdee.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.erp.model.dmp.constant.DmpOutputConstant;
import com.erp.model.dmp.entity.CfgSettingEntity;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.plm.entity.AssetPurchaseOrderDetailEntity;
import com.erp.model.plm.entity.AssetPurchaseOrderEntity;
import com.erp.model.plm.entity.AssetPurchaseOrderSupplierEntity;
import com.erp.model.plm.entity.PlmPushMsgEntity;
import com.erp.model.scm.entity.*;
import com.erp.model.sys.dto.DeptKingdeeDTO;
import com.erp.model.sys.dto.KingdeeBusinessOperatorDTO;
import com.erp.model.sys.dto.KingdeeOperatorRefPostDTO;
import com.erp.model.sys.entity.KingdeeDepartmentEntity;
import com.erp.model.sys.enums.KingdeeBusinessOperatorTypeEnum;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.scm.feign.SupplierFeign;
import com.erp.rpc.sys.feign.KingdeeFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.plm.rocketmq.sync.kingdee.SyncKingdeeAssetPurchaseService;
import com.erp.server.plm.service.AssetPurchaseOrderDetailService;
import com.erp.server.plm.service.AssetPurchaseOrderSupplierService;
import com.erp.server.plm.service.PlmPushMsgService;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @Author: wtr
 * @Date: 2025/10/23 14:27
 * @Param:
 * @Return:
 * @Description:
 **/
@Service
public class SyncKingdeeAssetPurchaseServiceImpl implements SyncKingdeeAssetPurchaseService {

    @Resource
    private PlmPushMsgService plmPushMsgService;

    @Resource
    private AssetPurchaseOrderSupplierService assetPurchaseOrderSupplierService;

    @Resource
    private AssetPurchaseOrderDetailService assetPurchaseOrderDetailService;

    @Resource
    private SupplierFeign supplierFeign;

    @Resource
    private DmpMqFeign dmpMqFeign;

    @Resource
    private KingdeeFeign kingdeeFeign;

    @Resource
    private SysUserFeign sysUserFeign;

    /**
     * 组装数据发送到金蝶
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public DmpPushTaskEntity syncDataToKingdee(AssetPurchaseOrderEntity entity, String operate) {
        //生成任务
        if(!SyncOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
            return saveTask(entity, operate, DmpOutputConstant.getQuerySyncMap());
        }else {
            return saveTask(entity, operate, this.newSyncDataToKingdee(entity, operate));
        }
    }

    private DmpPushTaskEntity saveTask (AssetPurchaseOrderEntity entity, String operate, Map<String, Object> resultMap) {
        SettingEnum settingEnum = SettingEnum.NEW_DMP_PUSH_SWTICH_LIST;
        List<CfgSettingEntity> list = FeignQuery.create(CfgSettingEntity.class)
                .eq(CfgSettingEntity::getKey, SourceTypeEnum.ASSET_PURCHASE_ORDER.getCode())
                .eq(CfgSettingEntity::getType, settingEnum.getType())
                .eq(CfgSettingEntity::getValue, "1")
                .list();
        if(CollUtil.isEmpty(list)) {
            //添加推送任务
            DmpPushTaskFeignDTO taskFeignDTO = new DmpPushTaskFeignDTO();
            taskFeignDTO.setSourceId(entity.getId());
            taskFeignDTO.setSourceCode(entity.getCode());
            taskFeignDTO.setSourceType(SourceTypeEnum.ASSET_PURCHASE_ORDER.getCode());
            taskFeignDTO.setMqTopic(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC);
            taskFeignDTO.setMqTag(RocketMqTagEnum.KINGDEE_PURCHASE_ORDER_TAG.getName());
            taskFeignDTO.setMqData(JSONUtil.toJsonStr(resultMap));
            taskFeignDTO.setSourcePlatformName(PlatformEnum.ERP.getDesc());
            taskFeignDTO.setTargetPlatformName(PlatformEnum.KINGDEE.getDesc());
            taskFeignDTO.setSyncOperate(operate);
            return dmpMqFeign.saveTask(taskFeignDTO);
        }

        PlmPushMsgEntity plmPushMsgEntity = new PlmPushMsgEntity();
        plmPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.KINGDEE.getCode());
        plmPushMsgEntity.setSourceType(SourceTypeEnum.ASSET_PURCHASE_ORDER.getCode());
        plmPushMsgEntity.setSourceId(entity.getId());
        plmPushMsgEntity.setSourceCode(entity.getCode());
        plmPushMsgEntity.setSyncOperate(operate);
        plmPushMsgEntity.setPushData(JSON.toJSONString(resultMap));

        plmPushMsgService.save(plmPushMsgEntity);

        return null;
    }

    @Override
    public Map<String, Object> newSyncDataToKingdee(AssetPurchaseOrderEntity entity, String operate) {
        Map<String, Object> resultMap = new HashMap<>();

        //业务id
        resultMap.put("id",entity.getId());
        //编码
        resultMap.put("code",entity.getCode());
        //金蝶id
        resultMap.put("syncKingdeeId",entity.getSyncKingdeeId());
        //操作（枚举SyncKingdeeOperateEnum）
        resultMap.put("operate", operate);

        //删除操作
        if (SyncOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
            return resultMap;
        }
        //采购日期
        resultMap.put("purchaseDate", LocalDateTimeUtil.format(entity.getPurchaseDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        //单据类型
        resultMap.put("type",entity.getOrderType());

        //查询采购供应商
        LambdaQueryWrapper<AssetPurchaseOrderSupplierEntity> supplierQueryWrapper = new LambdaQueryWrapper<>();
        supplierQueryWrapper.eq(AssetPurchaseOrderSupplierEntity::getAssetPurchaseOrderId,entity.getId())
                .eq(AssetPurchaseOrderSupplierEntity::getIsDeleted,Boolean.FALSE);
        AssetPurchaseOrderSupplierEntity purchaseOrderSupplierEntity = assetPurchaseOrderSupplierService.getOne(supplierQueryWrapper);
        if (ObjectUtils.isEmpty(purchaseOrderSupplierEntity)) {
            throw new ServiceException("未发现采购供应商信息");
        }
        SupplierEntity supplierEntity = supplierFeign.getSupplierById(purchaseOrderSupplierEntity.getSupplierId());
        if (ObjectUtils.isEmpty(supplierEntity)) {
            throw new ServiceException("未发现供应商信息");
        }

        //供应商编码
        resultMap.put("supplierCode",supplierEntity.getCode());

        //采购部门
        resultMap.put("purchaseDeptName",entity.getPurchaseDeptName());

        //获取用户部门id
        if (StringUtils.isNotBlank(entity.getPurchaseDeptId())) {
            DeptKingdeeDTO.FindDeptKingdeeDTO dto = new DeptKingdeeDTO.FindDeptKingdeeDTO();
            dto.setDeptId(entity.getPurchaseDeptId());
            dto.setOrgId(entity.getPurchaseOrgId());
            KingdeeDepartmentEntity deptKingdee = kingdeeFeign.getDeptKingdee(dto);
            if (ObjectUtils.isNotEmpty(deptKingdee)) {
                resultMap.put("purchaseDeptCode", deptKingdee.getKingdeeDeptCode());
            }
        }

        //采购员编码
        if (StringUtils.isNotBlank(entity.getPurchaseUserId())) {
            KingdeeBusinessOperatorDTO.FindBusinessOperatorDTO findBusinessOperator = new KingdeeBusinessOperatorDTO.FindBusinessOperatorDTO();
            findBusinessOperator.setOrgCode(entity.getPurchaseOrgId());
            findBusinessOperator.setUserId(entity.getPurchaseUserId());
            findBusinessOperator.setBusinessOperatorType(KingdeeBusinessOperatorTypeEnum.CGY.getCode());
            //获取员工业务信息
            KingdeeOperatorRefPostDTO.OperatorDTO kingSellerInfo = kingdeeFeign.getBusinessOperator(findBusinessOperator);
            //采购员
            if (!Objects.isNull(kingSellerInfo)) {
                resultMap.put("purchaseUserCode", kingSellerInfo.getUserPostCode());
                resultMap.put("purchaseUserName", kingSellerInfo.getUserName());
            }
        }
        //供应商联系人
        resultMap.put("contactName",purchaseOrderSupplierEntity.getContactName());

        if (ObjectUtils.isNotEmpty(purchaseOrderSupplierEntity.getPaymentCondition())) {
            //付款条件
            resultMap.put("paymentCondition",purchaseOrderSupplierEntity.getPaymentCondition());
        }

        //采购明细
        LambdaQueryWrapper<AssetPurchaseOrderDetailEntity> pusrchaseDetailQueryWrapper = new LambdaQueryWrapper<>();
        pusrchaseDetailQueryWrapper.eq(AssetPurchaseOrderDetailEntity::getMainId,entity.getId())
                .eq(AssetPurchaseOrderDetailEntity::getIsDeleted,Boolean.FALSE);
        List<AssetPurchaseOrderDetailEntity> details = assetPurchaseOrderDetailService.list(pusrchaseDetailQueryWrapper);
        if (CollectionUtils.isEmpty(details)) {
            throw new ServiceException(ApiError.ERROR_95298);
        }
        //组织机构编码
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(Arrays.asList(entity.getPurchaseOrgId()));
        if (CollectionUtils.isNotEmpty(accountingCompanyList)) {
            //采购组织编码
            String purchaseOrgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getPurchaseOrgId()))
                    .findFirst().flatMap(obj -> Optional.ofNullable(obj.getCode())).orElse(null);
            resultMap.put("purchaseOrgCode", purchaseOrgCode);
        }

        List<JSONObject> list = new ArrayList<>();
        for (AssetPurchaseOrderDetailEntity detailEntity : details) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.set("detailId",detailEntity.getId());
            jsonObject.set("skuNo",detailEntity.getAssetCode());
            jsonObject.set("purchaseQty",detailEntity.getPurchaseQty());
            jsonObject.set("planDeliveryDate",LocalDateTimeUtil.format(detailEntity.getPlanDeliveryDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")) );
            jsonObject.set("price", MathUtil.divide(detailEntity.getTaxPrice(),MathUtil.add(MathUtil.BigDecimal_1,detailEntity.getTaxRate())) );
            jsonObject.set("taxPrice",detailEntity.getTaxPrice());
            jsonObject.set("taxRate",MathUtil.multiplyWithTwo(detailEntity.getTaxRate(),MathUtil.BigDecimal_100));
            if (CollectionUtils.isNotEmpty(accountingCompanyList)) {
                //采购组织编码
                String purchaseOrgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getPurchaseOrgId()))
                        .findFirst().flatMap(obj -> Optional.ofNullable(obj.getCode())).orElse(null);
                jsonObject.set("purchaseOrgCode", purchaseOrgCode);
            }
            jsonObject.set("detailRemark",detailEntity.getRemark());
            list.add(jsonObject);
        }
        resultMap.put("list",list);
        return resultMap;
    }

}
