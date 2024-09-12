package com.erp.server.wms.kingdee.impl;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
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
import com.erp.model.dmp.dto.CfgSettingDTO;
import com.erp.model.dmp.entity.CfgSettingEntity;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.sys.dto.KingdeePostDTO;
import com.erp.model.wms.entity.MachineDetailEntity;
import com.erp.model.wms.entity.MachineInfoEntity;
import com.erp.model.wms.entity.MachineSubComponentsEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.entity.WmsPushMsgEntity;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.kingdee.SyncKingdeeMachineInfoService;
import com.erp.server.wms.service.MachineDetailService;
import com.erp.server.wms.service.MachineSubComponentsService;
import com.erp.server.wms.service.WarehouseService;
import com.erp.server.wms.service.WmsPushMsgService;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;

/**
 * @description: 同步其他入库单
 * @author Will
 * @date: 2023/5/24 18:56
 */
@Slf4j
@Service
public class SyncKingdeeMachineInfoServiceImpl implements SyncKingdeeMachineInfoService {
    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private MachineDetailService machineDetailService;

    @Resource
    private DmpMqFeign dmpMqFeign;

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private MachineSubComponentsService machineSubComponentsService;

    @Resource
    private WmsPushMsgService wmsPushMsgService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public DmpPushTaskEntity syncDataToKingdee(MachineInfoEntity entity, String operate) {
        Map<String, Object> resultMap = new HashMap<>();

        //金蝶id
        resultMap.put("syncKingdeeId", entity.getSyncKingdeeId());
        //业务id
        resultMap.put("id", entity.getId());
        //其他出库单号
        resultMap.put("code", entity.getCode());
        //操作（枚举SyncKingdeeOperateEnum）
        resultMap.put("operate", operate);

        //删除操作
        if (SyncOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
            return saveTask(entity,operate,resultMap);
        }
        //加工明细
        List<MachineDetailEntity> detailList = machineDetailService.listByMainId(entity.getId());
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_99052);
        }
        List<String> detailIds = detailList.stream().map(MachineDetailEntity::getId).collect(Collectors.toList());

        //子件明细
        List<MachineSubComponentsEntity> machineSubComponentsList = machineSubComponentsService.listByDetailIds(detailIds);
        if (CollectionUtils.isEmpty(machineSubComponentsList)) {
            throw new ServiceException(ApiError.ERROR_99053);
        }
        List<String> warehouseIds = machineSubComponentsList.stream().map(MachineSubComponentsEntity::getWarehouseId).collect(Collectors.toList());
        warehouseIds.add(entity.getWarehouseId());

        //所有仓库
        List<WarehouseEntity> warehouseList = warehouseService.listByIds(warehouseIds);

        //员工岗位
        List<KingdeePostDTO.UserKingdeePostInfoDTO> userKingdeePostInfoList = sysUserFeign.listUserKingdeePostByUserIds(Arrays.asList(entity.getReceiverId()));


        //其他出库类型
        resultMap.put("type", entity.getType());
        //出库日期
        resultMap.put("billDate", LocalDateTimeUtil.format(entity.getBillDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        if (CollectionUtils.isNotEmpty(userKingdeePostInfoList)) {
            //领料人
            String receiverCode = userKingdeePostInfoList.stream().filter(obj -> obj.getUserId().equals(entity.getReceiverId()))
                    .findFirst().flatMap(obj -> Optional.ofNullable(obj.getKingdeePostCode())).orElse(null);
            resultMap.put("receiverCode", receiverCode);
        }

        if (StringUtils.isNotBlank(entity.getWarehouseKeeperId())) {
            //仓管员编码
            FindUserDTO findUserDTO = sysUserFeign.getUserByUserId(entity.getWarehouseKeeperId());
            if (ObjectUtils.isNotEmpty(findUserDTO)) {
                resultMap.put("warehouseKeeperCode",findUserDTO.getCode());
            }
        }


        //事务类型
        resultMap.put("workType", entity.getWorkType());

        String inventoryOrgCode = "";
        String receiveOrgCode = "";
        //组织机构编码
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(Arrays.asList(entity.getInventoryOrgId(),entity.getReceiveOrgId()));
        if (CollectionUtils.isNotEmpty(accountingCompanyList)) {

            inventoryOrgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getInventoryOrgId()))
                    .findFirst().flatMap(obj -> Optional.ofNullable(obj.getCode())).orElse(null);

            receiveOrgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getReceiveOrgId()))
                    .findFirst().flatMap(obj -> Optional.ofNullable(obj.getCode())).orElse(null);
        }
        //库存组织编码
        resultMap.put("receiveOrgCode", receiveOrgCode);
        //领料组织编码
        resultMap.put("inventoryOrgCode", inventoryOrgCode);

        //是否支持下推仓位
        List<CfgSettingDTO.WarehouseLocationSettingDTO> pushKingdeeList = dmpTaskFeign.isPushKingdeeWarehouseLocation(warehouseIds);

        List<JSONObject> list = new ArrayList<>();
        for (MachineDetailEntity detail : detailList) {
            JSONObject jsonObject = new JSONObject();
            //SKU
            jsonObject.set("skuNo", detail.getSkuNo());
            //实发数量
            jsonObject.set("qty", detail.getQty());
            //单位
            jsonObject.set("unit", detail.getUnit());

            if (CollectionUtils.isNotEmpty(warehouseList)) {
                //仓库编码
                String warehouseCode = warehouseList.stream().filter(obj -> obj.getId().equals(entity.getWarehouseId()))
                        .findFirst().flatMap(obj -> Optional.ofNullable(obj.getKingdeeWarehouseCode())).orElse(null);
                //调出仓库
                jsonObject.set("warehouseCode", warehouseCode);
            }
            //是否下推仓位
            Boolean isPush = pushKingdeeList.stream().filter(obj -> StrUtil.equals(obj.getWarehouseId(), entity.getWarehouseId()))
                    .map(CfgSettingDTO.WarehouseLocationSettingDTO::getIsPush).findFirst().orElse(Boolean.FALSE);
            if (isPush) {
                //仓位
                jsonObject.set("warehouseLocation", detail.getWarehouseLocation());
            }


            String referenceVersion = detail.getSkuNo() + "_" + detail.getReferenceVersion();
            //参照版本
            jsonObject.set("referenceVersion", referenceVersion);
            //库存组织编码
            jsonObject.set("inventoryOrgCode", inventoryOrgCode);
            //领料组织编码
            jsonObject.set("receiveOrgCode", receiveOrgCode);
            //备注
            jsonObject.set("remark", detail.getRemark());

            //子件
            List<MachineSubComponentsEntity> componentsList = machineSubComponentsList.stream().filter(obj -> obj.getDetailId().equals(detail.getId())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(componentsList)) {
                continue;
            }
            //子件信息录入
            List<JSONObject> subComponents = new ArrayList<>();
            for (MachineSubComponentsEntity machineSubComponents : componentsList) {
                JSONObject subObject = new JSONObject();
                subObject.set("skuNo",machineSubComponents.getSkuNo());
                subObject.set("unit",machineSubComponents.getUnit());
                subObject.set("qty",machineSubComponents.getQty());
                subObject.set("warehouseLocation",machineSubComponents.getWarehouseLocation());
                if (CollectionUtils.isNotEmpty(warehouseList)) {
                    //仓库编码
                    String warehouseCode = warehouseList.stream().filter(obj -> obj.getId().equals(machineSubComponents.getWarehouseId()))
                            .findFirst().flatMap(obj -> Optional.ofNullable(obj.getKingdeeWarehouseCode())).orElse("");
                    //子件调出仓库
                    subObject.set("warehouseCode", warehouseCode);
                }
                //库存组织编码
                subObject.set("inventoryOrgCode", inventoryOrgCode);
                //领料组织编码
                subObject.set("receiveOrgCode", receiveOrgCode);
                //备注
                subObject.set("remark", machineSubComponents.getRemark());
                subComponents.add(subObject);
            }
            jsonObject.set("subComponentsList",subComponents);
            list.add(jsonObject);
        }
        resultMap.put("detailList", list);

        //生成任务
        return saveTask(entity,operate,resultMap);
    }

    /**
     * @description: 生成任务
     * @author Will
     * @date: 2023/10/16 9:17
     * @param entity
     * @param operate
     * @param resultMap
     */
    private DmpPushTaskEntity saveTask (MachineInfoEntity entity, String operate, Map<String, Object> resultMap) {
    	SettingEnum settingEnum = SettingEnum.NEW_DMP_PUSH_SWTICH_LIST;
        List<CfgSettingEntity> list = FeignQuery.create(CfgSettingEntity.class)
        		.eq(CfgSettingEntity::getKey, SourceTypeEnum.MACHINE_INFO.getCode())
        		.eq(CfgSettingEntity::getType, settingEnum.getType())
        		.eq(CfgSettingEntity::getValue, "1")
        		.list();
        if(CollUtil.isEmpty(list)) {
        	//添加推送任务
            DmpPushTaskFeignDTO dmpSyncTaskDTO = new DmpPushTaskFeignDTO();
            dmpSyncTaskDTO.setSourceId(entity.getId());
            dmpSyncTaskDTO.setSourceCode(entity.getCode());
            dmpSyncTaskDTO.setSourceType(SourceTypeEnum.MACHINE_INFO.getCode());
            dmpSyncTaskDTO.setMqTopic(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC);
            dmpSyncTaskDTO.setMqTag(RocketMqTagEnum.KINGDEE_MACHINE_INFO_TAG.getName());
            dmpSyncTaskDTO.setMqData(JSONUtil.toJsonStr(resultMap));
            dmpSyncTaskDTO.setSourcePlatformName(PlatformEnum.ERP.getDesc());
            dmpSyncTaskDTO.setTargetPlatformName(PlatformEnum.KINGDEE.getDesc());
            dmpSyncTaskDTO.setSyncOperate(operate);
            return dmpMqFeign.saveTask(dmpSyncTaskDTO);
        }

    	WmsPushMsgEntity wmsPushMsgEntity = new WmsPushMsgEntity();
        wmsPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.KINGDEE.getCode());
        wmsPushMsgEntity.setSourceType(SourceTypeEnum.MACHINE_INFO.getCode());
        wmsPushMsgEntity.setSourceId(entity.getId());
        wmsPushMsgEntity.setSourceCode(entity.getCode());
        wmsPushMsgEntity.setSyncOperate(operate);
        wmsPushMsgEntity.setPushData(JSON.toJSONString(resultMap));

        wmsPushMsgService.save(wmsPushMsgEntity);

        return null;
    }
}
