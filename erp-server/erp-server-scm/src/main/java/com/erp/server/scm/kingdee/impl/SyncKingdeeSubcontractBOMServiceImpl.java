package com.erp.server.scm.kingdee.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.utils.ApplicationContextUtils;
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
import com.erp.model.scm.dto.SubcontractBOMDTO;
import com.erp.model.scm.entity.ScmPushMsgEntity;
import com.erp.model.scm.entity.SubcontractOrderDetailEntity;
import com.erp.model.scm.entity.SubcontractOrderEntity;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.scm.kingdee.SyncKingdeeSubcontractBOMService;
import com.erp.server.scm.service.ScmPushMsgService;
import com.erp.server.scm.service.SubcontractOrderDetailService;
import com.erp.server.scm.service.SubcontractOrderService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: wtr
 * @Date: 2026/1/19 8:42
 * @Param:
 * @Return:
 * @Description:
 **/
@Slf4j
@Service
public class SyncKingdeeSubcontractBOMServiceImpl implements SyncKingdeeSubcontractBOMService {

    @Resource
    private ScmPushMsgService scmPushMsgService;

    @Resource
    private SubcontractOrderService subcontractOrderService;

    @Resource
    private SubcontractOrderDetailService subcontractOrderDetailService;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private DmpMqFeign dmpMqFeign;

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    /**
     * 组装数据发送到金蝶。
     * <p>非 delete 操作推送完整 payload（含 list/仓库字段），不再使用 {@code isQuerySync}，
     * 以便 DMP 消费端按 detailId 组装仓库/仓位；Feign 查询在事务外执行，仅 saveTask 参与分布式事务。</p>
     */
    @Override
    public DmpPushTaskEntity syncDataToKingdee(SubcontractBOMDTO.KingdeeSubcontractBOMDTO dto, String operate) {
        Map<String, Object> resultMap = this.newSyncDataToKingdee(dto, operate);
        return ApplicationContextUtils.getBean(SyncKingdeeSubcontractBOMServiceImpl.class)
                .saveTaskInTransaction(dto, operate, resultMap);
    }

    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public DmpPushTaskEntity saveTaskInTransaction(SubcontractBOMDTO.KingdeeSubcontractBOMDTO dto, String operate,
            Map<String, Object> resultMap) {
        return saveTask(dto, operate, resultMap);
    }

    private DmpPushTaskEntity saveTask (SubcontractBOMDTO.KingdeeSubcontractBOMDTO dto, String operate, Map<String, Object> resultMap) {
        SettingEnum settingEnum = SettingEnum.NEW_DMP_PUSH_SWTICH_LIST;
        List<CfgSettingEntity> list = FeignQuery.create(CfgSettingEntity.class)
                .eq(CfgSettingEntity::getKey, SourceTypeEnum.SUBCONTRACT_BOM.getCode())
                .eq(CfgSettingEntity::getType, settingEnum.getType())
                .eq(CfgSettingEntity::getValue, "1")
                .list();

        if(CollUtil.isEmpty(list)) {
            //添加推送任务
            DmpPushTaskFeignDTO dmpSyncTaskDTO = new DmpPushTaskFeignDTO();

            dmpSyncTaskDTO.setSourceId(dto.getId());
            dmpSyncTaskDTO.setSourceCode(dto.getSourceCode());
            dmpSyncTaskDTO.setSourceType(SourceTypeEnum.SUBCONTRACT_BOM.getCode());
            dmpSyncTaskDTO.setMqTopic(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC);
            dmpSyncTaskDTO.setMqTag(RocketMqTagEnum.KINGDEE_SUBCONTRACT_BOM_TAG.getName());
            dmpSyncTaskDTO.setMqData(JSONUtil.toJsonStr(resultMap));
            dmpSyncTaskDTO.setSourcePlatformName(PlatformEnum.ERP.getDesc());
            dmpSyncTaskDTO.setTargetPlatformName(PlatformEnum.KINGDEE.getDesc());
            dmpSyncTaskDTO.setSyncOperate(operate);
            dmpSyncTaskDTO.setParentId(dto.getSourceId());
            return dmpMqFeign.saveTask(dmpSyncTaskDTO);
        }

        ScmPushMsgEntity scmPushMsgEntity = new ScmPushMsgEntity();
        scmPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.KINGDEE.getCode());
        scmPushMsgEntity.setSourceType(SourceTypeEnum.SUBCONTRACT_BOM.getCode());
        scmPushMsgEntity.setSourceId(dto.getId());
        scmPushMsgEntity.setSourceCode(dto.getSourceCode());
        scmPushMsgEntity.setSyncOperate(operate);
        scmPushMsgEntity.setPushData(JSON.toJSONString(resultMap));
        scmPushMsgEntity.setParentId(dto.getSourceId());
        scmPushMsgService.save(scmPushMsgEntity);

        return null;
    }

    @Override
    public Map<String, Object> newSyncDataToKingdee(SubcontractBOMDTO.KingdeeSubcontractBOMDTO dto, String operate) {
        Map<String, Object> resultMap = new HashMap<>();
        //业务id
        resultMap.put("id",dto.getId());
        //操作（枚举SyncKingdeeOperateEnum）
        resultMap.put("operate", operate);
        //删除操作
        if (SyncOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
            return resultMap;
        }
        //委外用料清单号
        resultMap.put("FBillNo", dto.getCode());

        SubcontractOrderEntity subcontractOrderEntity = subcontractOrderService.getById(dto.getSourceId());
        if (subcontractOrderEntity == null) {
            throw new ServiceException(ApiError.PO_SUBCONTRACT_ORDER_NOT_FOUND);
        }
        String syncKingdeeId = StringUtils.isNotBlank(dto.getSyncKingdeeId())
                ? dto.getSyncKingdeeId()
                : subcontractOrderEntity.getSyncKingdeeId();
        resultMap.put("syncKingdeeId", syncKingdeeId);

        resultMap.put("TargetBillTypeId", "SUB_OutSrcBOMChange");

        SysAccountingCompanyEntity accountCompany = sysUserFeign.getCompanyById(subcontractOrderEntity.getSubcontractOrgId());
        if (Objects.nonNull(accountCompany)) {
            //委外组织
            resultMap.put("FSubOrgId", accountCompany.getCode());
        }
        //委外订单编号
        resultMap.put("FSubReqBillNO", subcontractOrderEntity.getCode());

        //委外订单类型
        resultMap.put("FSubReqType", "WWYLQDBGD01_SYS");

        List<SubcontractOrderDetailEntity> subcontractOrderDetailList = subcontractOrderDetailService.listByMainId(dto.getSourceId());

        List<String> warehouseIdList = subcontractOrderDetailList.stream()
                .map(SubcontractOrderDetailEntity::getWarehouseId)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        // 委外用料清单变更单允许无仓库/仓位降级推送（与同模块委外订单 fail-fast 策略不同）
        List<WarehouseDTO.UpdateDTO> warehouseList = loadWarehouseList(warehouseIdList);
        List<CfgSettingDTO.WarehouseLocationSettingDTO> pushKingdeeList = loadPushKingdeeLocationSettings(warehouseIdList);
        Map<String, WarehouseDTO.UpdateDTO> warehouseMap = CollectionUtils.isEmpty(warehouseList)
                ? Collections.emptyMap()
                : warehouseList.stream()
                        .filter(Objects::nonNull)
                        .filter(item -> StringUtils.isNotBlank(item.getId()))
                        .collect(Collectors.toMap(WarehouseDTO.UpdateDTO::getId, item -> item, (oldValue, newValue) -> oldValue));
        Map<String, Boolean> pushKingdeeMap = pushKingdeeList.stream()
                .filter(Objects::nonNull)
                .filter(item -> StringUtils.isNotBlank(item.getWarehouseId()))
                .collect(Collectors.toMap(CfgSettingDTO.WarehouseLocationSettingDTO::getWarehouseId,
                        item -> Boolean.TRUE.equals(item.getIsPush()), (oldValue, newValue) -> oldValue));
        Map<String, SubcontractOrderDetailEntity> detailMap = subcontractOrderDetailList.stream()
                .filter(item -> StringUtils.isNotBlank(item.getId()))
                .collect(Collectors.toMap(SubcontractOrderDetailEntity::getId, item -> item, (oldValue, newValue) -> oldValue));

        List<JSONObject> list = new ArrayList<>();
        for (SubcontractOrderDetailEntity subcontractOrderDetailEntity : subcontractOrderDetailList) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.set("detailId", subcontractOrderDetailEntity.getId());
            jsonObject.set("parentId", subcontractOrderDetailEntity.getParentId());
            //产品编码
            jsonObject.set("FMaterialID", subcontractOrderDetailEntity.getSkuNo());
            String warehouseId = subcontractOrderDetailEntity.getWarehouseId();
            String warehouseLocation = subcontractOrderDetailEntity.getWarehouseLocation();
            if (StringUtils.isBlank(warehouseId) && StringUtils.isNotBlank(subcontractOrderDetailEntity.getParentId())) {
                SubcontractOrderDetailEntity parentDetail = detailMap.get(subcontractOrderDetailEntity.getParentId());
                if (parentDetail != null) {
                    warehouseId = parentDetail.getWarehouseId();
                    warehouseLocation = parentDetail.getWarehouseLocation();
                }
            }
            if (StringUtils.isNotBlank(warehouseId)) {
                WarehouseDTO.UpdateDTO warehouse = warehouseMap.get(warehouseId);
                // 仓库未配置金蝶编码时跳过该明细仓库/仓位，不阻断整单推送
                if (warehouse == null || StringUtils.isBlank(warehouse.getKingdeeWarehouseCode())) {
                    log.warn("委外用料清单明细仓库映射为空，跳过仓库/仓位组装，detailId={}, skuNo={}, warehouseId={}",
                            subcontractOrderDetailEntity.getId(), subcontractOrderDetailEntity.getSkuNo(), warehouseId);
                } else {
                    jsonObject.set("warehouseCode", warehouse.getKingdeeWarehouseCode());
                    if (Boolean.TRUE.equals(pushKingdeeMap.get(warehouseId))) {
                        jsonObject.set("warehouseLocation", warehouseLocation);
                    }
                }
            }
            list.add(jsonObject);
        }
        resultMap.put("list", list);
        return resultMap;
    }

    /**
     * 批量查询仓库；Feign 调用失败会抛异常由上层重试，返回 null/空列表视为无可用仓库并降级跳过。
     */
    private List<WarehouseDTO.UpdateDTO> loadWarehouseList(List<String> warehouseIdList) {
        if (CollectionUtils.isEmpty(warehouseIdList)) {
            return Collections.emptyList();
        }
        List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listWarehouseByIds(warehouseIdList);
        if (warehouseList == null || CollectionUtils.isEmpty(warehouseList)) {
            log.warn("委外用料清单同步未查询到仓库信息，跳过仓库/仓位组装，warehouseIdList={}", warehouseIdList);
            return Collections.emptyList();
        }
        if (warehouseList.size() < warehouseIdList.size()) {
            log.warn("委外用料清单同步仓库信息不完整，请求数={}, 返回数={}", warehouseIdList.size(), warehouseList.size());
        }
        return warehouseList;
    }

    private List<CfgSettingDTO.WarehouseLocationSettingDTO> loadPushKingdeeLocationSettings(List<String> warehouseIdList) {
        if (CollectionUtils.isEmpty(warehouseIdList)) {
            return Collections.emptyList();
        }
        List<CfgSettingDTO.WarehouseLocationSettingDTO> pushKingdeeList = dmpTaskFeign.isPushKingdeeWarehouseLocation(warehouseIdList);
        return pushKingdeeList == null ? Collections.emptyList() : pushKingdeeList;
    }
}
