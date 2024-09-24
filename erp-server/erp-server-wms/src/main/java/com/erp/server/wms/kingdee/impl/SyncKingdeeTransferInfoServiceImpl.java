package com.erp.server.wms.kingdee.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.enums.ThirdPartySystemEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.erp.model.dmp.dto.CfgSettingDTO;
import com.erp.model.dmp.entity.CfgSettingEntity;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.wms.entity.TransferInfoDetailEntity;
import com.erp.model.wms.entity.TransferInfoEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.entity.WmsPushMsgEntity;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.kingdee.SyncKingdeeTransferInfoService;
import com.erp.server.wms.service.TransferInfoDetailService;
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
import java.util.stream.Stream;

/**
 * @description: 同步直接调拨单
 * @author Will
 * @date: 2023/5/24 18:56
 */
@Slf4j
@Service
public class SyncKingdeeTransferInfoServiceImpl implements SyncKingdeeTransferInfoService {
    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private TransferInfoDetailService transferInfoDetailService;

    @Resource
    private DmpMqFeign dmpMqFeign;

    @Resource
    private WarehouseService warehouseService;
    
    @Resource
    private WmsPushMsgService wmsPushMsgService;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public DmpPushTaskEntity syncDataToKingdee(TransferInfoEntity entity, String operate) {
        //生成任务
        return saveTask(entity,operate,this.newSyncDataToKingdee(entity, operate));
    }

    /**
     * @description: 生成任务
     * @author Will
     * @date: 2023/10/16 9:17
     * @param entity
     * @param operate
     * @param resultMap
     */
    private DmpPushTaskEntity saveTask (TransferInfoEntity entity, String operate, Map<String, Object> resultMap) {
    	SettingEnum settingEnum = SettingEnum.NEW_DMP_PUSH_SWTICH_LIST;
        List<CfgSettingEntity> list = FeignQuery.create(CfgSettingEntity.class)
        		.eq(CfgSettingEntity::getKey, SourceTypeEnum.TRANSFER_INFO.getCode())
        		.eq(CfgSettingEntity::getType, settingEnum.getType())
        		.eq(CfgSettingEntity::getValue, "1")
        		.list();
        if(CollUtil.isEmpty(list)) {
        	//添加推送任务
            DmpPushTaskFeignDTO dmpSyncTaskDTO = new DmpPushTaskFeignDTO();
            dmpSyncTaskDTO.setSourceId(entity.getId());
            dmpSyncTaskDTO.setSourceCode(entity.getCode());
            dmpSyncTaskDTO.setSourceType(SourceTypeEnum.TRANSFER_INFO.getCode());
            dmpSyncTaskDTO.setMqTopic(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC);
            dmpSyncTaskDTO.setMqTag(RocketMqTagEnum.KINGDEE_TRANSFER_INFO_TAG.getName());
            dmpSyncTaskDTO.setMqData(JSONUtil.toJsonStr(resultMap));
            dmpSyncTaskDTO.setSourcePlatformName(PlatformEnum.ERP.getDesc());
            dmpSyncTaskDTO.setTargetPlatformName(PlatformEnum.KINGDEE.getDesc());
            dmpSyncTaskDTO.setSyncOperate(operate);
            return dmpMqFeign.saveTask(dmpSyncTaskDTO);
        }
        
        WmsPushMsgEntity wmsPushMsgEntity = new WmsPushMsgEntity();
        wmsPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.KINGDEE.getCode());
        wmsPushMsgEntity.setSourceType(SourceTypeEnum.TRANSFER_INFO.getCode());
        wmsPushMsgEntity.setSourceId(entity.getId());
        wmsPushMsgEntity.setSourceCode(entity.getCode());
        wmsPushMsgEntity.setSyncOperate(operate);
        wmsPushMsgEntity.setPushData(JSON.toJSONString(resultMap));
        
        wmsPushMsgService.save(wmsPushMsgEntity);
        
        return null;
    }

	@Override
	public Map<String, Object> newSyncDataToKingdee(TransferInfoEntity entity, String operate) {
		//第三方马帮拉取数据无推送
        if (ThirdPartySystemEnum.ENUM_MB.getCode().equals(entity.getThirdPartySystem())) {
            return null;
        }

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

        //调拨类型
        resultMap.put("type", entity.getType());
        //调拨日期
        resultMap.put("billDate", LocalDateTimeUtil.format(entity.getBillDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        //调拨方向
        resultMap.put("transferDirection", entity.getTransferDirection());
        //备注
        resultMap.put("remark", entity.getRemark());

        if (StringUtils.isNotBlank(entity.getWarehouseKeeperId())) {
            //仓管员编码
            FindUserDTO findUserDTO = sysUserFeign.getUserByUserId(entity.getWarehouseKeeperId());
            if (ObjectUtils.isNotEmpty(findUserDTO)) {
                resultMap.put("warehouseKeeperCode",findUserDTO.getCode());
            }
        }

        //组织机构编码
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(Arrays.asList(entity.getInOrgId(), entity.getOutOrgId()));
        if (CollectionUtils.isEmpty(accountingCompanyList)) {
            throw new ServiceException(ApiError.ERROR_9014);
        }
        //调入组织机构编码
        String inOrgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getInOrgId()))
                .findFirst().flatMap(obj -> Optional.ofNullable(obj.getCode())).orElse(null);
        resultMap.put("inOrgCode", inOrgCode);

        //调出组织机构编码
        String outOrgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getOutOrgId()))
                .findFirst().flatMap(obj -> Optional.ofNullable(obj.getCode())).orElse(null);
        resultMap.put("outOrgCode", outOrgCode);


        List<TransferInfoDetailEntity> detailList = transferInfoDetailService.listByMainId(entity.getId());
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_99048);
        }

        //获取sku的id集合
        List<String> skuIdList = detailList.stream().map(TransferInfoDetailEntity::getSkuId).collect(Collectors.toList());
        //根据ids查询sku信息
        List<ProductDetailEntity> detailEntityList = plmTaskFeign.getByIdList(skuIdList);
        if (CollectionUtils.isEmpty(detailEntityList)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }

        List<String> warehouseIds = detailList.stream().flatMap(obj -> Stream.of(obj.getInWarehouseId(), obj.getOutWarehouseId())).collect(Collectors.toList());

        //仓库
        List<WarehouseEntity> warehouseList = warehouseService.listByIds(warehouseIds);

        //是否支持下推仓位
        List<CfgSettingDTO.WarehouseLocationSettingDTO> pushKingdeeList = dmpTaskFeign.isPushKingdeeWarehouseLocation(warehouseIds);

        List<JSONObject> list = new ArrayList<>();
        for (TransferInfoDetailEntity detail : detailList) {
            JSONObject jsonObject = new JSONObject();
            //SKU
            jsonObject.set("skuNo", detail.getSkuNo());
            ProductDetailEntity productDetailEntity = detailEntityList.stream().filter(entityClass -> entityClass.getId().equals(detail.getSkuId())).findFirst().orElse(new ProductDetailEntity());
            //产品名称
            jsonObject.set("productName", productDetailEntity.getName());
            //调拨数量
            jsonObject.set("qty", detail.getQty());
            //单位
            jsonObject.set("unit", detail.getUnit());
            //调入组织机构编码
            jsonObject.set("inOrgCode", inOrgCode);
            //调出组织机构编码
            jsonObject.set("outOrgCode", outOrgCode);

            if (CollectionUtils.isNotEmpty(warehouseList)) {
                //调入仓库编码
                String inWarehouseCode = warehouseList.stream().filter(obj -> obj.getId().equals(detail.getInWarehouseId()))
                        .findFirst().flatMap(obj -> Optional.ofNullable(obj.getKingdeeWarehouseCode())).orElse(null);
                //调出仓库编码
                String outWarehouseCode = warehouseList.stream().filter(obj -> obj.getId().equals(detail.getOutWarehouseId()))
                        .findFirst().flatMap(obj -> Optional.ofNullable(obj.getKingdeeWarehouseCode())).orElse(null);
                //调入仓库
                jsonObject.set("inWarehouseCode", inWarehouseCode);
                //调出仓库
                jsonObject.set("outWarehouseCode", outWarehouseCode);
            }
            //是否下推调入仓位
            Boolean isPushIn = pushKingdeeList.stream().filter(obj -> StrUtil.equals(obj.getWarehouseId(), detail.getInWarehouseId()))
                    .map(CfgSettingDTO.WarehouseLocationSettingDTO::getIsPush).findFirst().orElse(Boolean.FALSE);
            if (isPushIn) {
                //调入仓位
                jsonObject.set("inWarehouseLocation", detail.getInWarehouseLocation());
            }
            //是否下推调出仓位
            Boolean isPushOut = pushKingdeeList.stream().filter(obj -> StrUtil.equals(obj.getWarehouseId(), detail.getOutWarehouseId()))
                    .map(CfgSettingDTO.WarehouseLocationSettingDTO::getIsPush).findFirst().orElse(Boolean.FALSE);
            if (isPushOut) {
                //调出仓位
                jsonObject.set("outWarehouseLocation", detail.getOutWarehouseLocation());
            }

            //备注
            jsonObject.set("remark", detail.getRemark());

            list.add(jsonObject);
        }
        resultMap.put("list", list);
        return resultMap;
	}
}
