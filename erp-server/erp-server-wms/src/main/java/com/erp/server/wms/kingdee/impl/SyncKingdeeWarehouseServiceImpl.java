package com.erp.server.wms.kingdee.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.exception.ServiceException;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.erp.model.dmp.entity.CfgSettingEntity;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.wms.entity.DictBasicEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.entity.WarehouseLocationEntity;
import com.erp.model.wms.entity.WmsPushMsgEntity;
import com.erp.model.wms.enums.WarehouseLocationTypeEnum;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.kingdee.SyncKingdeeWarehouseService;
import com.erp.server.wms.service.DictBasicService;
import com.erp.server.wms.service.WarehouseLocationService;
import com.erp.server.wms.service.WarehouseService;
import com.erp.server.wms.service.WmsPushMsgService;

import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 同步金蝶采购退货单
 * @Author Luo_WG
 * @Date 2023/4/24 11:22
 **/
@Slf4j
@Service
public class SyncKingdeeWarehouseServiceImpl implements SyncKingdeeWarehouseService {
    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private DmpMqFeign dmpMqFeign;

    @Resource
    private WarehouseLocationService warehouseLocationService;

    @Resource
    private WmsPushMsgService wmsPushMsgService;
    
    @Resource
    private WarehouseService warehouseService;
    
    /**
     * 发送消息同步金蝶
     * @Author Luo_WG
     * @Date 2023/4/24 11:27
     * @param entity
     * @param operate
     * @return void
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public DmpPushTaskEntity syncDataToKingdee(WarehouseEntity entity, String operate) {
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
    private DmpPushTaskEntity saveTask (WarehouseEntity entity, String operate, Map<String, Object> resultMap) {
    	SettingEnum settingEnum = SettingEnum.NEW_DMP_PUSH_SWTICH_LIST;
        List<CfgSettingEntity> list = FeignQuery.create(CfgSettingEntity.class)
        		.eq(CfgSettingEntity::getKey, SourceTypeEnum.WAREHOUSE.getCode())
        		.eq(CfgSettingEntity::getType, settingEnum.getType())
        		.eq(CfgSettingEntity::getValue, "1")
        		.list();
        if(CollUtil.isEmpty(list)) {
        	//添加推送任务
            DmpPushTaskFeignDTO dmpSyncTaskDTO = new DmpPushTaskFeignDTO();
            dmpSyncTaskDTO.setSourceId(entity.getId());
            dmpSyncTaskDTO.setSourceCode(entity.getKingdeeWarehouseCode());
            dmpSyncTaskDTO.setSourceType(SourceTypeEnum.WAREHOUSE.getCode());
            dmpSyncTaskDTO.setMqTopic(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC);
            dmpSyncTaskDTO.setMqTag(RocketMqTagEnum.KINGDEE_WAREHOUSE_TAG.getName());
            dmpSyncTaskDTO.setMqData(JSONUtil.toJsonStr(resultMap));
            dmpSyncTaskDTO.setSourcePlatformName(PlatformEnum.ERP.getDesc());
            dmpSyncTaskDTO.setTargetPlatformName(PlatformEnum.KINGDEE.getDesc());
            dmpSyncTaskDTO.setSyncOperate(operate);
            return dmpMqFeign.saveTask(dmpSyncTaskDTO);
        }
        
        WmsPushMsgEntity wmsPushMsgEntity = new WmsPushMsgEntity();
        wmsPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.KINGDEE.getCode());
        wmsPushMsgEntity.setSourceType(SourceTypeEnum.WAREHOUSE.getCode());
        wmsPushMsgEntity.setSourceId(entity.getId());
        wmsPushMsgEntity.setSourceCode(entity.getKingdeeWarehouseCode());
        wmsPushMsgEntity.setSyncOperate(operate);
        wmsPushMsgEntity.setPushData(JSON.toJSONString(resultMap));
        
        wmsPushMsgService.save(wmsPushMsgEntity);
        
        return null;
    }

	@Override
	public Map<String, Object> newSyncDataToKingdee(WarehouseEntity paramEntity, String operate) {
		WarehouseEntity entity = warehouseService.getById(paramEntity.getId());
		Map<String, Object> resultMap = new HashMap<>();

        //金蝶id
        resultMap.put("syncKingdeeId",entity.getSyncKingdeeId());
        //业务id
        resultMap.put("id",entity.getId());
        //仓库名称
        resultMap.put("name",entity.getName());
        //金蝶编号
        resultMap.put("code",entity.getKingdeeWarehouseCode());
        //操作（枚举SyncKingdeeOperateEnum）
        resultMap.put("operate", operate);
        //删除操作
        if (SyncOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
            return resultMap;
        }

        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(Collections.singletonList(entity.getOrgId()));
        if (CollectionUtils.isNotEmpty(accountingCompanyList)) {
            String orgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getOrgId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getCode())).orElse("");
            String orgKingdeeId = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getOrgId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getFlagId())).orElse("");

            //仓库组织
            resultMap.put("orgCode",orgCode);

            //组织的金蝶id
            resultMap.put("createOrgId",orgKingdeeId);
        }

        //仓库地址
        resultMap.put("address",entity.getAddress());
        //仓库电话
        resultMap.put("tel",entity.getContactTelNumber());
        //是否禁用
        resultMap.put("disabled",entity.getDisabled());
        //是否启用仓位
        resultMap.put("isEnableLocation",entity.getIsEnableLocation());


        //审核未通过、非反审核不推送
//        if (!ApproveStatusEnum.APPROVE.getStatus().equals(entity.getApproveStatus().getStatus()) && !SyncOperateEnum.OPERATE_DISAPPROVE.getCode().equals(operate)) {
//            return null;
//        }

        //仓库类型
        DictBasicEntity type = dictBasicService.getById(entity.getTypeId());
        if (ObjectUtils.isNotEmpty(type)) {
            resultMap.put("type",type.getValue());
        }
        //仓库负责人
        FindUserDTO findUserDTO = sysUserFeign.getUserByUserId(entity.getChargeId());
        if (ObjectUtils.isNotEmpty(findUserDTO)) {
            resultMap.put("chargeCode",findUserDTO.getCode());
        }
        //查询仓位
        List<WarehouseLocationEntity> warehouseLocationList = warehouseLocationService.listByWarehouseIds(Collections.singletonList(entity.getId()));
        if (CollectionUtils.isNotEmpty(warehouseLocationList) && entity.getIsEnableLocation()) {
            //区域
            List<WarehouseLocationEntity> areaList = warehouseLocationList.stream().filter(obj -> Objects.equals(entity.getId(),obj.getWarehouseId()) && WarehouseLocationTypeEnum.AREA.getCode().equals(obj.getType())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(areaList)) {
                log.error("仓库对应区域未找到，code = {},name = {}",entity.getKingdeeWarehouseCode(),entity.getName());
                throw new ServiceException("仓库对应区域未找到");
            }
            //仓位
            List<WarehouseLocationEntity> locationList = warehouseLocationList.stream().filter(obj -> Objects.equals(entity.getId(),obj.getWarehouseId()) && WarehouseLocationTypeEnum.LOCATION.getCode().equals(obj.getType())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(locationList)) {
                log.error("仓库对应仓位未找到，code = {},name = {}",entity.getKingdeeWarehouseCode(),entity.getName());
                throw new ServiceException("仓库对应仓位未找到");
            }
            List<JSONObject> areaJsonList = new ArrayList<>();
            for (WarehouseLocationEntity area : areaList) {
                JSONObject areaJson = new JSONObject();
                areaJson.set("code",area.getCode());

                //区域下仓位
                List<WarehouseLocationEntity> childLocationList = locationList.stream().filter(obj -> area.getId().equals(obj.getParentId())).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(locationList)) {
                    log.error("仓库对应区域下仓位未找到，code = {},name = {},area = {}",entity.getKingdeeWarehouseCode(),entity.getName(),area.getCode());
                    throw new ServiceException("仓库对应区域下仓位未找到");
                }
                List<JSONObject> locationJsonList = new ArrayList<>();
                for (WarehouseLocationEntity childLocation : childLocationList) {
                    JSONObject childLocationJson = new JSONObject();
                    childLocationJson.set("code",childLocation.getCode());
                    locationJsonList.add(childLocationJson);
                }
                areaJson.set("locationJsonList",locationJsonList);
                areaJsonList.add(areaJson);
            }
            resultMap.put("areaJsonList",areaJsonList);
        }
        return resultMap;
	}
}
