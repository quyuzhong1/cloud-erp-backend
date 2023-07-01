package com.erp.server.wms.rocketmq.sync.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.enums.ErpServerModuleEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.common.core.utils.StrUtils;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.entity.DmpBomEntity;
import com.erp.model.dmp.entity.DmpFbaDeliveryDetailEntity;
import com.erp.model.dmp.entity.DmpFbaDeliveryEntity;
import com.erp.model.msg.dto.WarnMsgInfoDTO;
import com.erp.model.plm.entity.BomInfoEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.wms.dto.MachineDetailDTO;
import com.erp.model.wms.dto.MachineInfoDTO;
import com.erp.model.wms.dto.MachineSubComponentsDTO;
import com.erp.model.wms.entity.MachineInfoEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.enums.MachineTypeEnum;
import com.erp.model.wms.enums.WorkTypeEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.rocketmq.sync.SyncFbaDeliveryService;
import com.erp.server.wms.service.MachineInfoService;
import com.erp.server.wms.service.WarehouseService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * FBA发货单同步生成加工单
 * @CreateTime: 2023-06-30  14:36
 * @Author: zhangchunlin
 */
@Service
@Slf4j
public class SyncFbaDeliveryServiceImpl implements SyncFbaDeliveryService {

    @Autowired
    private MachineInfoService machineInfoService;

    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private PlmTaskFeign plmTaskFeign;

    @Autowired
    private MQProducerService mqProducerService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void syncFbaDelivery(DmpFbaDeliveryEntity entity, String sourceType,  String syncTaskId) {
        // 判断是否已经存在（一个FBA发货单不会生成多个加工单）
        List<MachineInfoEntity> machineInfoEntityList =  machineInfoService.findBySourceTypeAndSourceCode(sourceType, entity.getDeliveryNo());
        if(CollUtil.isEmpty(machineInfoEntityList)) {
          this.addMachineFromFbaDelivery(entity, sourceType, syncTaskId);
        } else {
            // 已经存在判断现有

        }

        // 以前是待配货，变成了已作废，ERP这边的加工单需要先反审核，再作废

        // 如果变更了数量，ERP这边不是审核通过，则需要修改ERP这边的加工单数量
    }

    /**
     * FBA发货单新增加工单
     * @param entity
     * @param sourceType
     */
    private void addMachineFromFbaDelivery(DmpFbaDeliveryEntity entity, String sourceType, String syncTaskId) {
        MachineInfoDTO.AddDTO addDTO = new  MachineInfoDTO.AddDTO();
        addDTO.setBillDate(LocalDate.now());
        addDTO.setWorkType(WorkTypeEnum.ASSEMBLE.getCode());
        // 仓管员 取不到马帮的员工信息
        addDTO.setType(MachineTypeEnum.ORDINARY.getCode());
        List<WarehouseEntity> warehouseEntityList = warehouseService.listByKingdeeCodeList(Arrays.asList(entity.getWarehouseCode()));
        if(CollUtil.isEmpty(warehouseEntityList)) {
            this.sendNotice(syncTaskId, StrUtil.format("仓库【{}】在ERP中不存在", entity.getWarehouseCode()));
            throw new ServiceException(ApiError.ERROR_99076, entity.getWarehouseCode());
        }
        WarehouseEntity warehouseEntity = warehouseEntityList.get(0);
        addDTO.setWarehouseId(warehouseEntity.getId());
        addDTO.setSourceType(sourceType);
        addDTO.setSourceId(entity.getDeliveryId());
        addDTO.setSourceCode(entity.getDeliveryNo());

        // 加工单明细信息
        List<MachineDetailDTO.AddDTO> detailList = Lists.newArrayList();

        List<String> parentSkuNos = entity.getItemList().stream().map(DmpFbaDeliveryDetailEntity::getSkuNo).distinct().collect(Collectors.toList());

        // 查询BOM信息
        List<BomInfoEntity> skuList = plmTaskFeign.listBomByParentSkuNos(parentSkuNos);

        for(DmpFbaDeliveryDetailEntity dmpFbaDeliveryDetailEntity : entity.getItemList()) {
            MachineDetailDTO.AddDTO member = new MachineDetailDTO.AddDTO();
            String parentSkuNo = dmpFbaDeliveryDetailEntity.getSkuNo();
            member.setQty(dmpFbaDeliveryDetailEntity.getDeliveryNum());
            member.setSkuNo(parentSkuNo);

            String parentSkuId = skuList.stream().filter(s -> s.getParentSkuNo().equals(dmpFbaDeliveryDetailEntity.getSkuNo())).
                    findFirst().map(BomInfoEntity::getParentSkuId).orElse("");
            if (StringUtils.isBlank(parentSkuId)) {
                this.sendNotice(syncTaskId, StrUtil.format("加工单父级SKU【{}】在ERP中不存在", parentSkuNo));
                throw new ServiceException(ApiError.ERROR_NOT_FOUND_SKU,dmpFbaDeliveryDetailEntity.getSkuNo());
            }
            member.setSkuId(parentSkuId);

            //版本
            Integer version = skuList.stream().filter(obj -> obj.getParentSkuId().equals(member.getSkuId())).map(BomInfoEntity::getBomVersion).findFirst().orElse(MathUtil.ZERO);
            member.setReferenceVersion(version);

            // 子件明细
            List<DmpBomEntity> bomList = dmpFbaDeliveryDetailEntity.getBomList();
            List<String> subSkuNos = bomList.stream().map(DmpBomEntity::getSkuNo).distinct().collect(Collectors.toList());
            List<SkuVO> subSkuList = plmTaskFeign.listBySkuNoList(subSkuNos);

            List<MachineSubComponentsDTO.AddDTO> subComponentsList = Lists.newArrayListWithExpectedSize(bomList.size());

            for (DmpBomEntity dmpBomEntity : bomList) {
                MachineSubComponentsDTO.AddDTO subDTO = new MachineSubComponentsDTO.AddDTO();

                String subSkuId = subSkuList.stream().filter(s -> s.getSkuNo().equals(dmpBomEntity.getSkuNo())).findFirst().map(SkuVO::getSkuId).orElse("");
                if(StrUtils.isEmpty(subSkuId)) {
                    this.sendNotice(syncTaskId, StrUtil.format("加工单子级SKU【{}】在ERP中不存在，对应的父级SKU【{}】", dmpBomEntity.getSkuNo(), parentSkuNo));
                    throw new ServiceException(ApiError.ERROR_NOT_FOUND_SKU,dmpBomEntity.getSkuNo());
                }
                subDTO.setSkuId(subSkuId);
                subDTO.setSkuNo(dmpBomEntity.getSkuNo());
                subDTO.setQty(dmpBomEntity.getQty() * member.getQty());
                subDTO.setWarehouseId(warehouseEntity.getId());
                subDTO.setRemark(StrUtil.format("同步ERP：FBA发货单号{}", addDTO.getSourceCode()));
                subComponentsList.add(subDTO);
            }
            member.setSubComponentsList(subComponentsList);

            detailList.add(member);
        }
        addDTO.setDetailList(detailList);
        machineInfoService.add(addDTO);
    }

    /**
     * 发送异常通知
     * @param syncTaskId
     * @param errInfo
     */
    private void sendNotice(String syncTaskId, String errInfo) {
        WarnMsgInfoDTO warnMsgInfoDTO = new WarnMsgInfoDTO();
        warnMsgInfoDTO.setTitle("FBA发货单生成ERP加工单异常");
        warnMsgInfoDTO.setErpServerModuleEnum(ErpServerModuleEnum.ERP_SERVER_DMP);
        warnMsgInfoDTO.setBizName("FBA发货单生成ERP加工单");
        warnMsgInfoDTO.setTableName("dmp_sync_task");
        warnMsgInfoDTO.setTableId(syncTaskId);
        warnMsgInfoDTO.setKeyInfo(errInfo);
        mqProducerService.sendWarnMsg(warnMsgInfoDTO);
    }

}