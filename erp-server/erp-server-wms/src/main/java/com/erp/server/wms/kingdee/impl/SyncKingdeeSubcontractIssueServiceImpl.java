package com.erp.server.wms.kingdee.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.business.dto.DmpPushTaskFeignDTO;
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
import com.erp.model.scm.entity.SubcontractOrderDetailEntity;
import com.erp.model.scm.entity.SubcontractOrderEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.wms.entity.SubcontractIssueDetailEntity;
import com.erp.model.wms.entity.SubcontractIssueEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.entity.WmsPushMsgEntity;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.ScmTaskFeign;
import com.erp.server.wms.kingdee.SyncKingdeeSubcontractIssueService;
import com.erp.server.wms.service.SubcontractIssueDetailService;
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
 * @description: 同步委外发料单
 * @author Will
 * @date: 2024/1/26 11:00
 */
@Slf4j
@Service
public class SyncKingdeeSubcontractIssueServiceImpl implements SyncKingdeeSubcontractIssueService {
    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private SubcontractIssueDetailService subcontractIssueDetailService;

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private ScmTaskFeign scmTaskFeign;

    @Resource
    private DmpMqFeign dmpMqFeign;
    
    @Resource
    private WmsPushMsgService wmsPushMsgService;

    @Resource
    private DmpTaskFeign dmpTaskFeign;


    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public DmpPushTaskEntity syncDataToKingdee(SubcontractIssueEntity entity, String operate) {
        Map<String, Object> resultMap = new HashMap<>();

        //暂时不推送
        if (Boolean.TRUE) {
            return null;
        }

        //金蝶id
        resultMap.put("syncKingdeeId", entity.getSyncKingdeeId());
        //业务id
        resultMap.put("id", entity.getId());
        //单据编号
        resultMap.put("code", entity.getCode());
        //操作（枚举SyncKingdeeOperateEnum）
        resultMap.put("operate", operate);
        //删除操作
        if (SyncOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
            return saveTask(entity,operate,resultMap);
        }
        //日期
        resultMap.put("date", LocalDateTimeUtil.format(entity.getDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        //委外发料明细
        List<SubcontractIssueDetailEntity> detailList = subcontractIssueDetailService.listByMainIds(Arrays.asList(entity.getId()));
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_SUBCONTRACT_ISSUE_NOT_EXIST);
        }
        //委外订单
        List<SubcontractOrderEntity> subcontractOrderList = scmTaskFeign.listSubcontractOrderByIds(Arrays.asList(entity.getSubcontractOrderId()));
        if (CollectionUtils.isEmpty(subcontractOrderList)) {
            throw new ServiceException(ApiError.ERROR_98073);
        }
        //委外订单明细
        List<SubcontractOrderDetailEntity> subcontractOrderDetailList = scmTaskFeign.listSubcontractDetailByMainIds(Arrays.asList(entity.getSourceId()));

        //委外组织
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(Arrays.asList(subcontractOrderList.get(0).getSubcontractOrgId()));
        if (CollectionUtils.isEmpty(accountingCompanyList)) {
           throw new ServiceException(ApiError.ERROR_RECEIVE_ORG_NOT_FOUND);
        }
        resultMap.put("orgCode", accountingCompanyList.get(0).getCode());
        //仓库
        List<String> warehouseIdList = detailList.stream().map(SubcontractIssueDetailEntity::getWarehouseId).collect(Collectors.toList());
        List<WarehouseEntity> warehouseList = warehouseService.listByIds(warehouseIdList);

        //查询供应商信息
        SupplierEntity supplierEntity = scmTaskFeign.getSupplierById(entity.getSupplierId());
        //供应商编码
        resultMap.put("supplierCode", supplierEntity.getCode());

        //是否支持下推仓位
        List<CfgSettingDTO.WarehouseLocationSettingDTO> pushKingdeeList = dmpTaskFeign.isPushKingdeeWarehouseLocation(warehouseIdList);

        List<JSONObject> list = new ArrayList<>();
        for (SubcontractIssueDetailEntity detail : detailList) {
            JSONObject jsonObject = new JSONObject();
            //SKU
            jsonObject.set("skuNo", detail.getSkuNo());
            //发料数量
            jsonObject.set("issueQty", detail.getIssueQty());
            //领料数量
            jsonObject.set("receiveQty", detail.getReceiveQty());

            //仓库编码
            String warehouseCode = warehouseList.stream().filter(obj -> obj.getId().equals(detail.getWarehouseId()))
                    .findFirst().flatMap(obj -> Optional.ofNullable(obj.getKingdeeWarehouseCode())).orElse(null);
            //仓库
            jsonObject.set("warehouseCode", warehouseCode);
            //组织
            jsonObject.put("orgCode", accountingCompanyList.get(0).getCode());
            //是否下推仓位
            Boolean isPush = pushKingdeeList.stream().filter(obj -> StrUtil.equals(obj.getWarehouseId(), detail.getWarehouseId()))
                    .map(CfgSettingDTO.WarehouseLocationSettingDTO::getIsPush).findFirst().orElse(Boolean.FALSE);
            if (isPush) {
                //仓位
                jsonObject.set("warehouseLocation", detail.getWarehouseLocation());
            }

            //委外主表编码
            jsonObject.set("subCode", subcontractOrderList.get(0).getCode());
            //委外主表金蝶id
            jsonObject.set("subKingdeeId", subcontractOrderList.get(0).getSyncKingdeeId());
            //委外明细金蝶id
            String parentId = subcontractOrderDetailList.stream().filter(obj -> StrUtil.equals(obj.getId(), detail.getSourceDetailId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getParentId())).orElse("");
            String subKingdeeDetailId = subcontractOrderDetailList.stream().filter(obj -> StrUtil.equals(obj.getId(), parentId)).findFirst().flatMap(obj -> Optional.ofNullable(obj.getKingdeeDetailId())).orElse("");
            jsonObject.set("subKingdeeDetailId", subKingdeeDetailId);
            list.add(jsonObject);
        }
        resultMap.put("list", list);
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
    private DmpPushTaskEntity saveTask (SubcontractIssueEntity entity, String operate, Map<String, Object> resultMap) {
    	SettingEnum settingEnum = SettingEnum.NEW_DMP_PUSH_SWTICH_LIST;
        List<CfgSettingEntity> list = FeignQuery.create(CfgSettingEntity.class)
        		.eq(CfgSettingEntity::getKey, SourceTypeEnum.SUBCONTRACT_ISSUE.getCode())
        		.eq(CfgSettingEntity::getType, settingEnum.getType())
        		.eq(CfgSettingEntity::getValue, "1")
        		.list();
        if(CollUtil.isEmpty(list)) {
        	//添加推送任务
            DmpPushTaskFeignDTO dmpSyncTaskDTO = new DmpPushTaskFeignDTO();
            dmpSyncTaskDTO.setSourceId(entity.getId());
            dmpSyncTaskDTO.setSourceCode(entity.getCode());
            dmpSyncTaskDTO.setSourceType(SourceTypeEnum.SUBCONTRACT_ISSUE.getCode());
            dmpSyncTaskDTO.setMqTopic(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC);
            dmpSyncTaskDTO.setMqTag(RocketMqTagEnum.KINGDEE_SUBCONTRACT_ISSUE_TAG.getName());
            dmpSyncTaskDTO.setMqData(JSONUtil.toJsonStr(resultMap));
            dmpSyncTaskDTO.setSourcePlatformName(PlatformEnum.ERP.getDesc());
            dmpSyncTaskDTO.setTargetPlatformName(PlatformEnum.KINGDEE.getDesc());
            dmpSyncTaskDTO.setSyncOperate(operate);
            dmpSyncTaskDTO.setParentId(entity.getSourceId());
            return dmpMqFeign.saveTask(dmpSyncTaskDTO);
        }
        
        WmsPushMsgEntity wmsPushMsgEntity = new WmsPushMsgEntity();
        wmsPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.KINGDEE.getCode());
        wmsPushMsgEntity.setSourceType(SourceTypeEnum.SUBCONTRACT_ISSUE.getCode());
        wmsPushMsgEntity.setSourceId(entity.getId());
        wmsPushMsgEntity.setSourceCode(entity.getCode());
        wmsPushMsgEntity.setSyncOperate(operate);
        wmsPushMsgEntity.setPushData(JSON.toJSONString(resultMap));
        wmsPushMsgEntity.setParentId(entity.getSourceId());
        
        wmsPushMsgService.save(wmsPushMsgEntity);
        
        return null;
    }
}
