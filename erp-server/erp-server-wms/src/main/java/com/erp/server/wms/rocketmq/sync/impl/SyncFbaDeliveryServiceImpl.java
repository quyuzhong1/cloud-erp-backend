package com.erp.server.wms.rocketmq.sync.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ErpServerModuleEnum;
import com.common.business.utils.RedisUtil;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.StrUtils;
import com.common.message.constant.RedisKeyConstant;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.entity.DmpFbaDeliveryDetailEntity;
import com.erp.model.dmp.entity.DmpFbaDeliveryEntity;
import com.erp.model.dmp.enums.FbaDeliveryStatusEnum;
import com.erp.model.dmp.mabang.RedisMabngSkuEntity;
import com.erp.model.msg.dto.WarnMsgInfoDTO;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.entity.BomInfoEntity;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.wms.dto.MachineDetailDTO;
import com.erp.model.wms.dto.MachineInfoDTO;
import com.erp.model.wms.dto.MachineSubComponentsDTO;
import com.erp.model.wms.entity.MachineDetailEntity;
import com.erp.model.wms.entity.MachineInfoEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.enums.MachineTypeEnum;
import com.erp.model.wms.enums.WorkTypeEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.rocketmq.sync.SyncFbaDeliveryService;
import com.erp.server.wms.service.MachineDetailService;
import com.erp.server.wms.service.MachineInfoService;
import com.erp.server.wms.service.WarehouseService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * FBA发货单同步生成加工单
 * @CreateTime: 2023-06-30  14:36
 * @Author: zhangchunlin
 */
@Service
@Slf4j
public class SyncFbaDeliveryServiceImpl implements SyncFbaDeliveryService {

    @Resource
    private MachineInfoService machineInfoService;

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private MQProducerService mqProducerService;

    @Resource
    private MachineDetailService machineDetailService;

    @Resource
    private RedisUtil redisUtil;

    /**
     * 马帮平台加工品JG-开头的对应ERP的加工组合品不是JG-开头的
     */
//    private static final String MACHINE_SKU_PREFIX = "JG-";

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void syncFbaDelivery(DmpFbaDeliveryEntity entity, String sourceType,  String syncTaskId) {
        // 加工品处理（如果时JG-开头的需要去掉）
        entity.getItemList().stream().forEach(item->{
            RedisMabngSkuEntity mabangSkuInfo = redisUtil.getHashMap(RedisKeyConstant.MABANG_STOCK_SKU_LIST_KEY, item.getSkuNo());
            if(Objects.isNull(mabangSkuInfo)) {
                log.warn("马帮FBA发货单【{}】的加工组合品SKU【{}】在马帮SKU列表中不存在", entity.getDeliveryNo(), item.getSkuNo());
                throw new ServiceException(ApiError.MABANG_SKU_NOT_EXIST, item.getSkuNo());
            }
            item.setSkuNo(mabangSkuInfo.getFinancial());
//            if(StrUtils.isNotEmpty(item.getSkuNo()) && item.getSkuNo().startsWith(MACHINE_SKU_PREFIX) ) {
//                log.warn("马帮FBA发货单【{}】的加工组合品SKU【{}】是以JG-开头的", entity.getDeliveryNo(), item.getSkuNo());
//                item.setSkuNo(StrUtil.removePrefix(item.getSkuNo(), MACHINE_SKU_PREFIX));
//            }
        });
        // 判断是否已经存在（一个FBA发货单不会生成多个加工单）
        log.warn("{}FBA发货单【{}】发货状态【{}】", entity.getPlatformSign(), entity.getDeliveryNo(), entity.getDeliveryStatus());
        List<MachineInfoEntity> machineInfoEntityList =  machineInfoService.findBySourceTypeAndSourceCode(sourceType, entity.getDeliveryNo());
        if(CollUtil.isEmpty(machineInfoEntityList)) {
           // 如果是待配货状态，则处理；作废状态则不处理
          if(entity.getDeliveryStatus() != null && entity.getDeliveryStatus().intValue() == FbaDeliveryStatusEnum.WAIT_DELIVERY.getCode()) {
              log.warn("{}FBA发货单【{}】发货状态【{}】,新增ERP加工单", entity.getPlatformSign(), entity.getDeliveryNo(), entity.getDeliveryStatus());
              this.addMachineFromFbaDelivery(entity, sourceType, syncTaskId);
          }
          return;
        }
        // 已经存在判断状态是否变成了作废，如果变成作废，ERP这边的加工单需要先反审核，再作废；如果变更了数量，ERP这边不是审核通过，则需要修改ERP这边的加工单数量
        MachineInfoEntity machineInfoEntity = machineInfoEntityList.get(0);
        WarehouseEntity warehouseEntity = warehouseService.getById(machineInfoEntity.getWarehouseId());
        ApproveStatusEnum approveStatusEnum = ApproveStatusEnum.getByStatus(machineInfoEntity.getApproveStatus());
        if(entity.getDeliveryStatus().intValue() == FbaDeliveryStatusEnum.WAIT_DELIVERY.getCode()) {
            // 判断发生改变的FBA发货单和ERP加工单是否发生仓库和数量是否改变
            boolean isKeyUpdate = this.checkFbaDeliveryMachineUpdate(entity, machineInfoEntity, warehouseEntity);
            if(!isKeyUpdate) {
                log.warn("FBA发货单【{}】对应加工单【{}】发生修改，仓库、SKU数量、SKU种类没有发生改变", entity.getDeliveryNo(), machineInfoEntity.getCode());
                return;
            }
            // WMS加工单已作废，暂不重新生成，仅作消息提醒
            if(Objects.equals(machineInfoEntity.getInvalidStatus(), Boolean.TRUE)) {
                log.warn("FBA发货单【{}】发生了改变，ERP加工单【{}】已作废，不修改ERP加工单信息", entity.getDeliveryNo(), machineInfoEntity.getCode());
                this.sendNotice(syncTaskId, CharSequenceUtil.format("FBA发货单【{}】发生了仓库或数量改变，ERP加工单【{}】已作废，ERP加工单不同步FBA发货单数据", entity.getDeliveryNo(), machineInfoEntity.getCode() ));
            } else {
                // 暂时改成只处理待提交的修改
                if(Objects.equals(approveStatusEnum, ApproveStatusEnum.WAIT_SUBMIT) ) {
                    this.updateMachineFromFbaDelivery(entity, machineInfoEntity, warehouseEntity, sourceType, syncTaskId);
                } else if (Objects.equals(approveStatusEnum, ApproveStatusEnum.APPROVE) || Objects.equals(approveStatusEnum, ApproveStatusEnum.APPROVE_ING) || Objects.equals(approveStatusEnum, ApproveStatusEnum.REJECT) ) {
                    log.warn("FBA发货单【{}】发生了改变，ERP加工单【{}】状态已经为【{}】，不修改ERP加工单信息", entity.getDeliveryNo(), machineInfoEntity.getCode(), approveStatusEnum.getName() );
                    this.sendNotice(syncTaskId, CharSequenceUtil.format("FBA发货单【{}】发生了仓库或数量改变，ERP加工单【{}】状态已经为【{}】,请人工核实调整数据", entity.getDeliveryNo(), machineInfoEntity.getCode(), approveStatusEnum.getName() ));
                }
            }
        } else if (entity.getDeliveryStatus().intValue() == FbaDeliveryStatusEnum.INVALID.getCode()) {
            // ERP加工单如果审核通过需要先反审核再作废
            if(Objects.equals(approveStatusEnum, ApproveStatusEnum.APPROVE)) {
                // 先反审核
                machineInfoService.disApprove(machineInfoEntity);
            } else if (Objects.equals(approveStatusEnum, ApproveStatusEnum.APPROVE_ING)) {
                // 撤销
                machineInfoService.cancelProcess(Collections.singletonList(machineInfoEntity.getId()));
            }
            if(!Objects.equals(machineInfoEntity.getInvalidStatus(), InvalidStatusEnum.VOIDED.getStatus())) {
                // 作废
                machineInfoService.invalid(Collections.singletonList(machineInfoEntity.getId()), CharSequenceUtil.format("FBA发货单{}作废", entity.getDeliveryNo()));
            } else {
                log.warn("FBA发货单【{}】，ERP加工单【{}】都为作废状态，无需处理", entity.getDeliveryNo(), machineInfoEntity.getCode(), approveStatusEnum.getName() );
            }
        }
    }

    /**
     * 判断更新的FBA发货单和ERP中的加工单是否发生改变
     * @param entity
     * @param machineInfoEntity
     * @param warehouseEntity
     * @return
     */
    private boolean checkFbaDeliveryMachineUpdate(DmpFbaDeliveryEntity entity, MachineInfoEntity machineInfoEntity, WarehouseEntity warehouseEntity) {
        Boolean isKeyUpdate = Boolean.FALSE;
        // 判断仓库是否发生改变
        if(!Objects.equals(warehouseEntity.getKingdeeWarehouseCode(), entity.getWarehouseCode())) {
            log.warn("{}FBA发货单【{}】发货状态【{}】,仓库发生改变同步到加工单【{}】", entity.getPlatformSign(), entity.getDeliveryNo(), entity.getDeliveryStatus(), machineInfoEntity.getCode());
            isKeyUpdate = Boolean.TRUE;
        }
        List<DmpFbaDeliveryDetailEntity> dmpItemList = entity.getItemList();
        List<MachineDetailEntity> machineDetailEntityList = machineDetailService.listByMainId(machineInfoEntity.getId());
        Map<String,List<DmpFbaDeliveryDetailEntity>> dmpSkuMap = dmpItemList.stream().collect(Collectors.groupingBy(DmpFbaDeliveryDetailEntity::getSkuNo));
        Map<String, List<MachineDetailEntity>> machineSkuMap = machineDetailEntityList.stream().collect(Collectors.groupingBy(MachineDetailEntity::getSkuNo));
        // 判断SKU和数量是否发生改变
        for(Map.Entry<String, List<MachineDetailEntity>> machineEntry : machineSkuMap.entrySet()) {
            String machineSkuNo = machineEntry.getKey();
            Integer machineSkuQty = machineEntry.getValue().stream().collect(Collectors.summingInt(MachineDetailEntity::getQty));
            if(!dmpSkuMap.containsKey(machineSkuNo)) {
                log.warn("ERP加工单【{}】,SKU【{}】不存在FBA发货单【{}】中", machineInfoEntity.getCode(), machineSkuNo, entity.getDeliveryNo() );
                isKeyUpdate = Boolean.TRUE;
                break;
            }
            List<DmpFbaDeliveryDetailEntity> dmpSkuList = dmpSkuMap.get(machineSkuNo);
            Integer dmpSkuQty = dmpSkuList.stream().collect(Collectors.summingInt(DmpFbaDeliveryDetailEntity::getDeliveryNum));
            if(machineSkuQty.intValue() != dmpSkuQty.intValue()) {
                log.warn("ERP加工单【{}】,SKU【{}】数量【{}】和FBA发货单【{}】中的数量【{}】不一致", machineInfoEntity.getCode(), machineSkuNo, machineSkuQty, entity.getDeliveryNo(), dmpSkuQty );
                isKeyUpdate = Boolean.TRUE;
                break;
            }
        }
        return isKeyUpdate;
    }

    /**
     * 修改ERP加工单
     * @param entity
     * @param machineInfoEntity
     * @param warehouseEntity
     * @param sourceType
     * @param syncTaskId
     */
    private void updateMachineFromFbaDelivery(DmpFbaDeliveryEntity entity, MachineInfoEntity machineInfoEntity, WarehouseEntity warehouseEntity, String sourceType, String syncTaskId) {
        MachineInfoDTO.UpdateDTO updateDTO = BeanMapperUtils.map(MachineInfoDTO.UpdateDTO.class, machineInfoEntity);
        updateDTO.setId(machineInfoEntity.getId());
        updateDTO.setWarehouseId(warehouseEntity.getId());
        // 事务类型和单据类型不同步
        updateDTO.setWorkType(machineInfoEntity.getWorkType());
        updateDTO.setType(machineInfoEntity.getType());
        updateDTO.setWarehouseId(warehouseEntity.getId());
        updateDTO.setSourceType(sourceType);
        updateDTO.setSourceId(entity.getDeliveryId());
        updateDTO.setSourceCode(entity.getDeliveryNo());

        // 加工单明细信息
        List<MachineDetailDTO.UpdateDTO> detailList = Lists.newArrayList();

        List<String> parentSkuNos = entity.getItemList().stream().map(DmpFbaDeliveryDetailEntity::getSkuNo).distinct().collect(Collectors.toList());

        // 查询BOM信息
        List<BomInfoEntity> skuList = plmTaskFeign.listBomByParentSkuNos(parentSkuNos);

        // TODO 后续会增加限制，同步到WMS中的加工单不允许新增其他BOM或移除现有的BOM
        for(DmpFbaDeliveryDetailEntity dmpFbaDeliveryDetailEntity : entity.getItemList()) {
            MachineDetailDTO.UpdateDTO member = new MachineDetailDTO.UpdateDTO();
            String parentSkuNo = dmpFbaDeliveryDetailEntity.getSkuNo();
            member.setId(null);
            member.setQty(dmpFbaDeliveryDetailEntity.getDeliveryNum());
            member.setSkuNo(parentSkuNo);

            String parentSkuId = skuList.stream().filter(s -> Objects.equals(s.getParentSkuNo(), parentSkuNo)).
                    findFirst().map(BomInfoEntity::getParentSkuId).orElse("");
            if (CharSequenceUtil.isBlank(parentSkuId)) {
                throw new ServiceException(ApiError.ERROR_NOT_FOUND_SKU,parentSkuNo);
            }
            member.setSkuId(parentSkuId);

            //版本
            String version = skuList.stream().filter(obj -> obj.getParentSkuId().equals(member.getSkuId())).map(BomInfoEntity::getBomVersion).findFirst().orElse(StringPool.ZERO);
            member.setReferenceVersion(version);

            // 子件明细
            List<BomChildrenSkuDTO> bomChildrenSkuList = plmTaskFeign.listBomChildBySkuIds(Collections.singletonList(parentSkuId));

            List<BomChildrenSkuDTO> bomList = bomChildrenSkuList.stream().filter(obj -> Objects.equals(obj.getParentSkuId(), parentSkuId)).collect(Collectors.toList());

            List<MachineSubComponentsDTO.UpdateDTO> subComponentsList = Lists.newArrayList();

            for (BomChildrenSkuDTO dmpBomEntity : bomList) {
                MachineSubComponentsDTO.UpdateDTO subDTO = new MachineSubComponentsDTO.UpdateDTO();

                String subSkuId = bomList.stream().filter(s -> Objects.equals(s.getSkuNo(), dmpBomEntity.getSkuNo())).findFirst().map(BomChildrenSkuDTO::getSkuId).orElse("");

                if(StrUtils.isEmpty(subSkuId)) {
                    String errmsg = CharSequenceUtil.format("FBA发货单同步生成加工单子级SKU【{}】在ERP中不存在，对应的父级SKU【{}】", dmpBomEntity.getSkuNo(), parentSkuNo);
                    throw new ServiceException(ApiError.ERROR_NOT_FOUND_SKU.code, errmsg);
                }
                subDTO.setId(null);
                subDTO.setSkuId(subSkuId);
                subDTO.setSkuNo(dmpBomEntity.getSkuNo());
                subDTO.setQty(dmpBomEntity.getQuantity() * member.getQty());
                subDTO.setWarehouseId(warehouseEntity.getId());
                subDTO.setRemark(CharSequenceUtil.format("同步ERP：FBA发货单号{}", updateDTO.getSourceCode()));
                subComponentsList.add(subDTO);
            }
            member.setSubComponentsList(subComponentsList);

            detailList.add(member);
        }
        updateDTO.setDetailList(detailList);
        machineInfoService.update(updateDTO);
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
        List<WarehouseEntity> warehouseEntityList = warehouseService.listByKingdeeCodeList(Collections.singletonList(entity.getWarehouseCode()));
        if(CollUtil.isEmpty(warehouseEntityList)) {
            String errmsg = CharSequenceUtil.format("FBA发货单同步生成ERP加工单仓库【{}】在ERP中不存在", entity.getWarehouseCode());
            throw new ServiceException(ApiError.ERROR_99076.code, errmsg);
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

            String parentSkuId = skuList.stream().filter(s -> Objects.equals(s.getParentSkuNo(), dmpFbaDeliveryDetailEntity.getSkuNo())).
                    findFirst().map(BomInfoEntity::getParentSkuId).orElse("");
            if (CharSequenceUtil.isBlank(parentSkuId)) {
                String errmsg = CharSequenceUtil.format("FBA发货单同步生成ERP加工单父级SKU【{}】在ERP中不存在", parentSkuNo);
                throw new ServiceException(ApiError.ERROR_NOT_FOUND_SKU.code,errmsg);
            }
            member.setSkuId(parentSkuId);

            //版本
            String version = skuList.stream().filter(obj -> obj.getParentSkuId().equals(member.getSkuId())).map(BomInfoEntity::getBomVersion).findFirst().orElse(StringPool.ZERO);
            member.setReferenceVersion(version);

            // 子件明细
            // List<DmpBomEntity> bomList = dmpFbaDeliveryDetailEntity.getBomList();
            // bom信息
            List<BomChildrenSkuDTO> bomChildrenSkuList = plmTaskFeign.listBomChildBySkuIds(Collections.singletonList(parentSkuId));
            if(CollUtil.isEmpty(bomChildrenSkuList)) {
                String errmsg = CharSequenceUtil.format("FBA发货单同步生成ERP加工单父级SKU【{}】在ERP中未查询到BOM信息",parentSkuNo);
                throw new ServiceException(ApiError.ERROR_95173.code,errmsg);
            }

            List<BomChildrenSkuDTO> bomList = bomChildrenSkuList.stream().filter(obj -> Objects.equals(obj.getParentSkuId(), parentSkuId)).collect(Collectors.toList());

            // List<String> subSkuNos = bomList.stream().map(BomChildrenSkuDTO::getSkuNo).distinct().collect(Collectors.toList());
            // List<SkuVO> subSkuList = plmTaskFeign.listBySkuNoList(subSkuNos);

            List<MachineSubComponentsDTO.AddDTO> subComponentsList = Lists.newArrayListWithExpectedSize(bomList.size());

            for (BomChildrenSkuDTO dmpBomEntity : bomList) {
                MachineSubComponentsDTO.AddDTO subDTO = new MachineSubComponentsDTO.AddDTO();

                String subSkuId = bomList.stream().filter(s -> Objects.equals(s.getSkuNo(), dmpBomEntity.getSkuNo())).findFirst().map(BomChildrenSkuDTO::getSkuId).orElse("");
                if(StrUtils.isEmpty(subSkuId)) {
                    String errmsg = CharSequenceUtil.format("FBA发货单同步生成ERP加工单子级SKU【{}】在ERP中不存在，对应的父级SKU【{}】", dmpBomEntity.getSkuNo(), parentSkuNo);
                    throw new ServiceException(ApiError.ERROR_NOT_FOUND_SKU.code,errmsg);
                }
                subDTO.setSkuId(subSkuId);
                subDTO.setSkuNo(dmpBomEntity.getSkuNo());
                subDTO.setQty(dmpBomEntity.getQuantity() * member.getQty());
                subDTO.setWarehouseId(warehouseEntity.getId());
                subDTO.setRemark(CharSequenceUtil.format("同步ERP：FBA发货单号{}", addDTO.getSourceCode()));
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
        warnMsgInfoDTO.setTitle("FBA发货单同步生成ERP加工单异常");
        warnMsgInfoDTO.setErpServerModuleEnum(ErpServerModuleEnum.ERP_SERVER_DMP);
        warnMsgInfoDTO.setBizName("FBA发货单同步生成ERP加工单");
        warnMsgInfoDTO.setTableName("dmp_sync_task");
        warnMsgInfoDTO.setTableId(syncTaskId);
        warnMsgInfoDTO.setKeyInfo(errInfo);
        mqProducerService.sendWarnMsg(warnMsgInfoDTO);
    }

}