package com.erp.server.wms.kingdee.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.erp.model.dmp.constant.DmpOutputConstant;
import com.erp.model.dmp.dto.CfgSettingDTO;
import com.erp.model.dmp.entity.CfgSettingEntity;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.wms.entity.*;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.kingdee.SyncKingdeeTransferInService;
import com.erp.server.wms.service.TransferOutDetailService;
import com.erp.server.wms.service.TransferOutService;
import com.erp.server.wms.service.WarehouseService;
import com.erp.server.wms.service.WmsPushMsgService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @description: 同步直接调拨单
 * @author Will
 * @date: 2023/5/24 18:56
 */
@Slf4j
@Service
public class SyncKingdeeTransferInServiceImpl implements SyncKingdeeTransferInService {
    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private DmpMqFeign dmpMqFeign;

    @Resource
    private WarehouseService warehouseService;
    
    @Resource
    private WmsPushMsgService wmsPushMsgService;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private TransferOutService transferOutService;

    @Resource
    private TransferOutDetailService transferOutDetailService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public DmpPushTaskEntity syncDataToKingdee(TransferInEntity entity, List<TransferInDetailEntity> detailList, String operate) {
        //生成任务
    	if(!SyncOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
    		return saveTask(entity, operate, DmpOutputConstant.getQuerySyncMap());
    	}else {
    		return saveTask(entity, operate, this.newSyncDataToKingdee(entity , detailList , operate));
    	}
    }

    /**
     * @description: 生成任务
     * @author Will
     * @date: 2023/10/16 9:17
     * @param entity
     * @param operate
     * @param resultMap
     */
    private DmpPushTaskEntity saveTask (TransferInEntity entity, String operate, Map<String, Object> resultMap) {
    	SettingEnum settingEnum = SettingEnum.NEW_DMP_PUSH_SWTICH_LIST;
        List<CfgSettingEntity> list = FeignQuery.create(CfgSettingEntity.class)
        		.eq(CfgSettingEntity::getKey, SourceTypeEnum.TRANSFER_IN.getCode())
        		.eq(CfgSettingEntity::getType, settingEnum.getType())
        		.eq(CfgSettingEntity::getValue, "1")
        		.list();
        if(CollUtil.isEmpty(list)) {
        	//添加推送任务
            DmpPushTaskFeignDTO dmpSyncTaskDTO = new DmpPushTaskFeignDTO();
            dmpSyncTaskDTO.setSourceId(entity.getId());
            dmpSyncTaskDTO.setSourceCode(entity.getCode());
            dmpSyncTaskDTO.setSourceType(SourceTypeEnum.TRANSFER_IN.getCode());
            dmpSyncTaskDTO.setMqTopic(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC);
            dmpSyncTaskDTO.setMqTag(RocketMqTagEnum.KINGDEE_TRANSFER_IN_TAG.getName());
            dmpSyncTaskDTO.setMqData(JSONUtil.toJsonStr(resultMap));
            dmpSyncTaskDTO.setSourcePlatformName(PlatformEnum.ERP.getDesc());
            dmpSyncTaskDTO.setTargetPlatformName(PlatformEnum.KINGDEE.getDesc());
            dmpSyncTaskDTO.setSyncOperate(operate);
            return dmpMqFeign.saveTask(dmpSyncTaskDTO);
        }
        
        WmsPushMsgEntity wmsPushMsgEntity = new WmsPushMsgEntity();
        wmsPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.KINGDEE.getCode());
        wmsPushMsgEntity.setSourceType(SourceTypeEnum.TRANSFER_IN.getCode());
        wmsPushMsgEntity.setSourceId(entity.getId());
        wmsPushMsgEntity.setSourceCode(entity.getCode());
        wmsPushMsgEntity.setSyncOperate(operate);
        wmsPushMsgEntity.setPushData(JSON.toJSONString(resultMap));
        
        wmsPushMsgService.save(wmsPushMsgEntity);
        
        return null;
    }

	@Override
	public Map<String, Object> newSyncDataToKingdee(TransferInEntity entity, List<TransferInDetailEntity> detailList, String operate) {
        Map<String, Object> resultMap = new HashMap<>();
        //金蝶id
        resultMap.put("syncKingdeeId", entity.getSyncKingdeeId());
        //业务id
        resultMap.put("id", entity.getId());
        //调拨单号
        resultMap.put("code", entity.getCode());
        //操作（枚举SyncKingdeeOperateEnum）
        resultMap.put("operate", operate);
        //删除操作
        if (SyncOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
            return resultMap;
        }
        //查询分步式调出
        TransferOutEntity transferOutEntity = transferOutService.getById(entity.getSourceId());
        if (ObjectUtil.isEmpty(transferOutEntity)) {
            throw new ServiceException("分步式调出未找到");
        }
        //分步式调出明细
        List<TransferOutDetailEntity> transferOutDetailList = transferOutDetailService.listByMainId(transferOutEntity.getId());
        if (ObjectUtil.isEmpty(transferOutDetailList)) {
            throw new ServiceException("分步式调出明细未找到");
        }
        //调拨类型
        resultMap.put("type", entity.getTransferType().getKingdeeCode());
        //调拨日期
        resultMap.put("billDate", LocalDateTimeUtil.format(entity.getBillDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        //调拨方向
        resultMap.put("transferDirection", entity.getTransferDirection().getKingdeeCode());
        //备注
        resultMap.put("remark", entity.getRemark());

        if (CharSequenceUtil.isNotBlank(entity.getWarehouseKeeperId())) {
            //仓管员编码
            FindUserDTO findUserDTO = sysUserFeign.getUserByUserId(entity.getWarehouseKeeperId());
            if (ObjectUtils.isNotEmpty(findUserDTO)) {
                resultMap.put("warehouseKeeperCode",findUserDTO.getCode());
            }
        }
        //组织机构编码
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(Arrays.asList(transferOutEntity.getInOrgId(), transferOutEntity.getOutOrgId()));
        if (CollectionUtils.isEmpty(accountingCompanyList)) {
            throw new ServiceException(ApiError.ERROR_9014);
        }

        //调入组织机构编码
        String inOrgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(transferOutEntity.getInOrgId()))
                .findFirst().flatMap(obj -> Optional.ofNullable(obj.getCode())).orElse(null);
        resultMap.put("inOrgCode", inOrgCode);

        //调出组织机构编码
        String outOrgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(transferOutEntity.getOutOrgId()))
                .findFirst().flatMap(obj -> Optional.ofNullable(obj.getCode())).orElse(null);
        resultMap.put("outOrgCode", outOrgCode);

        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_99048);
        }

        //获取sku的id集合
        List<String> skuIdList = detailList.stream().map(TransferInDetailEntity::getSkuId).collect(Collectors.toList());
        //根据ids查询sku信息
        List<ProductDetailEntity> detailEntityList = plmTaskFeign.getByIdList(skuIdList);
        if (CollectionUtils.isEmpty(detailEntityList)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }
        List<String> warehouseIds = Arrays.asList(entity.getInWarehouseId(), entity.getOutWarehouseId());
        //仓库
        List<WarehouseEntity> warehouseList = warehouseService.listByIds(warehouseIds);
        //调入仓库编码
        String inWarehouseCode = warehouseList.stream().filter(obj -> obj.getId().equals(entity.getInWarehouseId()))
                .findFirst().flatMap(obj -> Optional.ofNullable(obj.getKingdeeWarehouseCode())).orElse(null);
        //调出仓库编码
        String outWarehouseCode = warehouseList.stream().filter(obj -> obj.getId().equals(entity.getOutWarehouseId()))
                .findFirst().flatMap(obj -> Optional.ofNullable(obj.getKingdeeWarehouseCode())).orElse(null);

        //是否支持下推仓位
        List<CfgSettingDTO.WarehouseLocationSettingDTO> pushKingdeeList = dmpTaskFeign.isPushKingdeeWarehouseLocation(warehouseIds);

        List<JSONObject> list = new ArrayList<>();
        for (TransferInDetailEntity detail : detailList) {
            JSONObject jsonObject = new JSONObject();
            //SKU
            jsonObject.set("skuNo", detail.getSkuNo());
            ProductDetailEntity productDetailEntity = detailEntityList.stream().filter(entityClass -> entityClass.getId().equals(detail.getSkuId())).findFirst().orElse(new ProductDetailEntity());
            //产品名称
            jsonObject.set("productName", productDetailEntity.getName());
            //调拨数量
            jsonObject.set("qty", detail.getQty());
            //调入仓库
            jsonObject.set("inWarehouseCode", inWarehouseCode);
            //调出仓库
            jsonObject.set("outWarehouseCode", outWarehouseCode);
            jsonObject.set("inOrgCode", inOrgCode);
            jsonObject.set("outOrgCode", outOrgCode);
            //来源单号
            jsonObject.set("sourceCode", transferOutEntity.getCode());

            //是否下推调出仓位
            Boolean isPushOut = pushKingdeeList.stream().filter(obj -> CharSequenceUtil.equals(obj.getWarehouseId(), entity.getOutWarehouseId()))
                    .map(CfgSettingDTO.WarehouseLocationSettingDTO::getIsPush).findFirst().orElse(Boolean.FALSE);
            if (isPushOut) {
                //调出仓位
                jsonObject.set("outWarehouseLocation", detail.getOutWarehouseLocation());
            }
            //是否下推调入仓位
            Boolean isPushIn = pushKingdeeList.stream().filter(obj -> CharSequenceUtil.equals(obj.getWarehouseId(), entity.getInWarehouseId()))
                    .map(CfgSettingDTO.WarehouseLocationSettingDTO::getIsPush).findFirst().orElse(Boolean.FALSE);
            if (isPushIn) {
                //调入仓位
                jsonObject.set("inWarehouseLocation", detail.getInWarehouseLocation());
            }
            //明细id
            jsonObject.set("detailId", detail.getId());
            //备注
            jsonObject.set("remark", detail.getRemark());

            List<Map<String,Object>> refList = new ArrayList<>();
            JSONObject refJsonObject = new JSONObject();
            if (ObjectUtils.isNotEmpty(transferOutEntity)) {
                refJsonObject.set("refKingdeeId",transferOutEntity.getSyncKingdeeId());
                if (CollectionUtils.isNotEmpty(transferOutDetailList)) {
                    String subDetailKingdeeId = transferOutDetailList.stream()
                            .filter(obj -> obj.getId().equals(detail.getSourceDetailId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getKingdeeDetailId()))
                            .orElse("");
                    refJsonObject.set("refDetailKingdeeId",subDetailKingdeeId);
                }
                refList.add(refJsonObject);
                jsonObject.set("refList",refList);
            }
            list.add(jsonObject);
        }
        resultMap.put("list", list);
        return resultMap;
	}
}
