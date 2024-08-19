package com.erp.server.wms.kingdee.impl;

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
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.erp.model.dmp.dto.CfgSettingDTO;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.sys.dto.DeptKingdeeDTO;
import com.erp.model.sys.dto.KingdeePostDTO;
import com.erp.model.sys.entity.KingdeeDepartmentEntity;
import com.erp.model.wms.entity.OtherOutstockCustomerEntity;
import com.erp.model.wms.entity.OtherOutstockDetailEntity;
import com.erp.model.wms.entity.OtherOutstockEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.sys.feign.KingdeeFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.kingdee.SyncKingdeeOtherOutstockService;
import com.erp.server.wms.service.OtherOutstockCustomerService;
import com.erp.server.wms.service.OtherOutstockDetailService;
import com.erp.server.wms.service.WarehouseService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @description: 同步其他入库单
 * @author Will
 * @date: 2023/5/24 18:56
 */
@Slf4j
@Service
public class SyncKingdeeOtherOutstockServiceImpl implements SyncKingdeeOtherOutstockService {
    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private OtherOutstockDetailService otherOutstockDetailService;
    @Resource
    private OtherOutstockCustomerService otherOutstockCustomerService;

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private KingdeeFeign kingdeeFeign;

    @Resource
    private DmpMqFeign dmpMqFeign;

    @Resource
    private DmpTaskFeign dmpTaskFeign;


    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public DmpPushTaskEntity syncDataToKingdee(OtherOutstockEntity entity, String operate) {
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

        //其他出库类型
        resultMap.put("type", entity.getType());
        resultMap.put("outType", entity.getOutType());
        // 主表备注
        if (StringUtils.isNotBlank(entity.getRemark())){
            resultMap.put("remark", entity.getRemark());
        }

        //出库日期
        resultMap.put("billDate", LocalDateTimeUtil.format(entity.getBillDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")) );
        //仓库
        List<WarehouseEntity> warehouseList = warehouseService.listByIds(Arrays.asList(entity.getWarehouseId()));

        //员工岗位
        List<KingdeePostDTO.UserKingdeePostInfoDTO> userKingdeePostInfoList = sysUserFeign.listUserKingdeePostByUserIds(Arrays.asList(entity.getReceiverId()));

        if (CollectionUtils.isNotEmpty(userKingdeePostInfoList)) {
            //领料人
            String receiverCode = userKingdeePostInfoList.stream().filter(obj -> obj.getUserId().equals(entity.getReceiverId()))
                    .findFirst().flatMap(obj -> Optional.ofNullable(obj.getKingdeePostCode())).orElse(null);
            resultMap.put("receiverCode", receiverCode);
        }

        //仓管员编码
        if (StringUtils.isNotBlank(entity.getWarehouseKeeperId())) {
            FindUserDTO findUserDTO = sysUserFeign.getUserByUserId(entity.getWarehouseKeeperId());
            if (ObjectUtils.isNotEmpty(findUserDTO)) {
                resultMap.put("warehouseKeeperCode", findUserDTO.getCode());
            }
        }

        //库存方向
        resultMap.put("inventoryDirection", entity.getInventoryDirection());

        //组织机构编码
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(Arrays.asList(entity.getInventoryOrgId(),entity.getReceiveOrgId()));
        if (CollectionUtils.isNotEmpty(accountingCompanyList)) {
            //库存组织编码
            String inventoryOrgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getInventoryOrgId()))
                    .findFirst().flatMap(obj -> Optional.ofNullable(obj.getCode())).orElse(null);
            resultMap.put("inventoryOrgCode", inventoryOrgCode);
            //收料组织编码
            String receiveOrgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getReceiveOrgId()))
                    .findFirst().flatMap(obj -> Optional.ofNullable(obj.getCode())).orElse(null);
            resultMap.put("receiveOrgCode", receiveOrgCode);
        }

        //部门
        if  (StringUtils.isNotBlank(entity.getDeptId())) {
            DeptKingdeeDTO.FindDeptKingdeeDTO dto = new DeptKingdeeDTO.FindDeptKingdeeDTO();
            dto.setDeptId(entity.getDeptId());
            dto.setOrgId(entity.getReceiveOrgId());
            KingdeeDepartmentEntity deptKingdee = kingdeeFeign.getDeptKingdee(dto);
            if (ObjectUtils.isNotEmpty(deptKingdee)) {
                resultMap.put("deptCode", deptKingdee.getKingdeeDeptCode());
            }
        }


        List<OtherOutstockDetailEntity> detailList = otherOutstockDetailService.listByMainId(entity.getId());
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_99062);
        }
        //客户信息
        OtherOutstockCustomerEntity customerEntity = otherOutstockCustomerService.getByMainId(entity.getId());
        if (Objects.nonNull(customerEntity)) {
            resultMap.put("customerCode", customerEntity.getCustomerCode());
        }
        //是否支持下推仓位
        List<CfgSettingDTO.WarehouseLocationSettingDTO> pushKingdeeList = dmpTaskFeign.isPushKingdeeWarehouseLocation(Arrays.asList(entity.getWarehouseId()));

        List<JSONObject> list = new ArrayList<>();
        for (OtherOutstockDetailEntity detail : detailList) {
            JSONObject jsonObject = new JSONObject();
            //SKU
            jsonObject.set("skuNo", detail.getSkuNo());
            //实发数量
            jsonObject.set("actualQty", detail.getActualQty());
            //单位
            jsonObject.set("unit", detail.getUnit());

            if (CollectionUtils.isNotEmpty(warehouseList)) {
                //仓库编码
                String warehouseCode = warehouseList.stream().filter(obj -> obj.getId().equals(entity.getWarehouseId()))
                        .findFirst().flatMap(obj -> Optional.ofNullable(obj.getKingdeeWarehouseCode())).orElse(null);

                //调出仓库
                jsonObject.set("warehouseCode", warehouseCode);
            }
            if (CollectionUtils.isNotEmpty(accountingCompanyList)) {
                //库存组织编码
                String inventoryOrgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getInventoryOrgId()))
                        .findFirst().flatMap(obj -> Optional.ofNullable(obj.getCode())).orElse(null);
                jsonObject.set("inventoryOrgCode", inventoryOrgCode);
                //收料组织编码
                String receiveOrgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getReceiveOrgId()))
                        .findFirst().flatMap(obj -> Optional.ofNullable(obj.getCode())).orElse(null);
                jsonObject.set("receiveOrgCode", receiveOrgCode);
            }
            //是否下推仓位
            Boolean isPush = pushKingdeeList.stream().filter(obj -> StrUtil.equals(obj.getWarehouseId(), entity.getWarehouseId()))
                    .map(CfgSettingDTO.WarehouseLocationSettingDTO::getIsPush).findFirst().orElse(Boolean.FALSE);
            if (isPush) {
                //仓位
                jsonObject.set("warehouseLocation", detail.getWarehouseLocation());
            }

            //备注
            jsonObject.set("remark", detail.getRemark());

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
    private DmpPushTaskEntity saveTask (OtherOutstockEntity entity, String operate, Map<String, Object> resultMap) {
        //添加推送任务
        DmpPushTaskFeignDTO dmpSyncTaskDTO = new DmpPushTaskFeignDTO();
        dmpSyncTaskDTO.setSourceId(entity.getId());
        dmpSyncTaskDTO.setSourceCode(entity.getCode());
        dmpSyncTaskDTO.setSourceType(SourceTypeEnum.OTHER_OUTSTOCK.getCode());
        dmpSyncTaskDTO.setMqTopic(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC);
        dmpSyncTaskDTO.setMqTag(RocketMqTagEnum.KINGDEE_OTHER_OUTSTOCK_TAG.getName());
        dmpSyncTaskDTO.setMqData(JSONUtil.toJsonStr(resultMap));
        dmpSyncTaskDTO.setSourcePlatformName(PlatformEnum.ERP.getDesc());
        dmpSyncTaskDTO.setTargetPlatformName(PlatformEnum.KINGDEE.getDesc());
        dmpSyncTaskDTO.setSyncOperate(operate);
        return dmpMqFeign.saveTask(dmpSyncTaskDTO);
    }
}
