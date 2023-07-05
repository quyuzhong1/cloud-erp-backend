package com.erp.server.dmp.push.consumer.mabang;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.enums.ErpServerModuleEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncKingdeeOperateEnum;
import com.common.core.utils.StrUtils;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.ApiModuleTypeEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.dto.mabang.MabangInOutStockDTO;
import com.erp.model.dmp.entity.DmpWarehouseMappingEntity;
import com.erp.model.dmp.entity.PlatformEntity;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.msg.dto.WarnMsgInfoDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.wms.dto.sync.MabangMachineInfoDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.WorkTypeEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.dmp.push.service.common.DmpSyncCommonService;
import com.erp.server.dmp.push.service.mabang.MabangInOutStockService;
import com.erp.server.dmp.service.CfgSettingService;
import com.erp.server.dmp.service.DmpWarehouseMappingService;
import com.erp.server.dmp.utils.MabangUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 加工单推送到马帮
 * @CreateTime: 2023-07-03  14:54
 * @Author: zhangchunlin
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.SYNC_WMS_TO_DMP_TOPIC, selectorExpression = "erp_dmp_machine_info_tag", consumerGroup = RocketMqConsumerGroup.SYNC_ERP_MACHINE_INFO_TO_DMP)
public class ErpMabangMachineInfoConsume implements RocketMQListener<MabangMachineInfoDTO> {

    @Autowired
    private DmpSyncCommonService dmpSyncCommonService;

    @Autowired
    private CfgSettingService cfgSettingService;

    @Autowired
    private DmpWarehouseMappingService dmpWarehouseMappingService;

    @Autowired
    private MabangInOutStockService mabangInOutStockService;

    @Autowired
    private PlmTaskFeign plmTaskFeign;

    @Autowired
    private MQProducerService mqProducerService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onMessage(MabangMachineInfoDTO mabangMachineInfoDTO) {
        log.info("监听到ERP加工单单信息，内容：{}", JSONObject.toJSONString(mabangMachineInfoDTO));

        // 主单
        MachineInfoEntity machineInfoEntity = mabangMachineInfoDTO.getMachineInfoEntity();

        //模块类型
        Integer type = ApiModuleTypeEnum.MACHINE_INFO.getCode();
        PlatformEntity platformEntity = dmpSyncCommonService.getPlatformEntity(machineInfoEntity.getId(), type);
        if (ObjectUtils.isEmpty(platformEntity)) {
            return;
        }

        // 明细
        List<MachineDetailEntity> machineDetailList  = mabangMachineInfoDTO.getMachineDetailEntityList();
        // 子明细
        List<MachineSubComponentsEntity> subMachineList = mabangMachineInfoDTO.getMachineSubComponentsEntityList();
        // 操作项
        String operate = StrUtils.null2EmptyWithTrim(mabangMachineInfoDTO.getOperate());
        // 单据来源类型
        String sourceType = StrUtils.null2EmptyWithTrim(mabangMachineInfoDTO.getSourceType());
        String sourceTypeName = SourceTypeEnum.getName(sourceType);

        List<String> warehouseCodeList = Lists.newArrayList();
        // 父SKU仓
        String parentWarehouseCode =  StrUtils.null2EmptyWithTrim(machineInfoEntity.getWarehouseCode());
        warehouseCodeList.add(parentWarehouseCode);
        // 子SKU仓库
        subMachineList.stream().forEach(subMachine-> warehouseCodeList.add(subMachine.getWarehouseCode()));
        Map<String, DmpWarehouseMappingEntity> warehouseMap = dmpWarehouseMappingService.getByWarehouseCodes(warehouseCodeList);

        // 操作人
        String employeeName = cfgSettingService.getValue(SettingEnum.ERP_TO_MB_EMPLOYEE_NAME);
        if(StrUtils.isEmpty(employeeName)) {
            employeeName = MabangUtil.MABANG_EMPLOYEE_NAME;
        }
        final String opEmployeeName = employeeName;

        List<String> parentSkuIds = machineDetailList.stream().map(MachineDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        // 子SKU产品信息
        List<String> subSkuIds = subMachineList.stream().map(MachineSubComponentsEntity::getSkuId).distinct().collect(Collectors.toList());
        List<String> skuIds = new ArrayList<>();
        skuIds.addAll(parentSkuIds);
        skuIds.addAll(subSkuIds);
        List<ProductDetailEntity> productDetailList = plmTaskFeign.getByIdList(skuIds);

        DmpWarehouseMappingEntity dmpWarehouseMappingEntity = warehouseMap.get(parentWarehouseCode);
        // 判断加工单主单仓库是否存在，不存在发送提醒
        if(Objects.isNull(dmpWarehouseMappingEntity)) {
            sendWarehouseNotice(sourceTypeName, machineInfoEntity.getId(), machineInfoEntity.getCode(), machineInfoEntity.getWarehouseCode());
            return;
        }
        // 判断加工单子件仓库是否存在，不存在发送提醒
        for(MachineSubComponentsEntity subComponentsEntity : subMachineList) {
            if(Objects.isNull(warehouseMap.get(subComponentsEntity.getWarehouseCode()))) {
                sendWarehouseNotice(sourceTypeName, machineInfoEntity.getId(), machineInfoEntity.getCode(), subComponentsEntity.getWarehouseCode());
                return;
            }
        }
        log.info("ERP加工单单号【{}】，事务类型【{}】", machineInfoEntity.getCode(), WorkTypeEnum.getByCode(machineInfoEntity.getWorkType()));

        // 审核【组装】、反审核【拆卸】
        if(  (Objects.equals(SyncKingdeeOperateEnum.OPERATE_APPROVE.getCode(), operate) && Objects.equals(machineInfoEntity.getWorkType(), WorkTypeEnum.ASSEMBLE.getCode()) )
                || (Objects.equals(SyncKingdeeOperateEnum.OPERATE_DISAPPROVE.getCode(), operate) && Objects.equals(machineInfoEntity.getWorkType(), WorkTypeEnum.DISASSEMBLE.getCode()) ) ) {
            // 父SKU手工入库
            MabangInOutStockDTO mabangInStockDTO = MabangUtil.fillMabangInOutStock(dmpWarehouseMappingEntity.getWarehouseCode(), dmpWarehouseMappingEntity.getWarehouseName(), opEmployeeName,
                    productDetailList, machineInfoEntity, machineDetailList, "in");
            mabangInOutStockService.inOutStock(mabangInStockDTO, machineInfoEntity.getId(), machineInfoEntity.getCode(), sourceType, operate);
            // 手工出库（按仓库维度）
            Map<String, List<MachineSubComponentsEntity>> subWarehouseMap = subMachineList.stream().collect(Collectors.groupingBy(MachineSubComponentsEntity::getWarehouseCode));
            subWarehouseMap.forEach((warehouseCode, subWareList)->{
                // 判断仓库存在与否，发送提醒
                DmpWarehouseMappingEntity dmpSubWarehouseMappingEntity = warehouseMap.get(warehouseCode);
                MabangInOutStockDTO mabangOutStockDTO = MabangUtil.fillMabangInOutStockSub(dmpSubWarehouseMappingEntity.getWarehouseCode(), dmpSubWarehouseMappingEntity.getWarehouseName(), opEmployeeName,
                        productDetailList, machineInfoEntity, subWareList, "out");
                mabangInOutStockService.inOutStock(mabangOutStockDTO, machineInfoEntity.getId(), machineInfoEntity.getCode(), sourceType, operate);
            });
        }

        // 审核【拆卸】、反审核【组装】
        if( (Objects.equals(SyncKingdeeOperateEnum.OPERATE_APPROVE.getCode(), operate) && Objects.equals(machineInfoEntity.getWorkType(), WorkTypeEnum.DISASSEMBLE.getCode()) )
                || (Objects.equals(SyncKingdeeOperateEnum.OPERATE_DISAPPROVE.getCode(), operate) &&  Objects.equals(machineInfoEntity.getWorkType(), WorkTypeEnum.ASSEMBLE.getCode()))) {
             // 父SKU手工出库
            MabangInOutStockDTO mabangOutStockDTO = MabangUtil.fillMabangInOutStock(dmpWarehouseMappingEntity.getWarehouseCode(), dmpWarehouseMappingEntity.getWarehouseName(), opEmployeeName,
                    productDetailList, machineInfoEntity, machineDetailList, "out");
            mabangInOutStockService.inOutStock(mabangOutStockDTO, machineInfoEntity.getId(), machineInfoEntity.getCode(), sourceType, operate);
            // 子SKU手工入库（按仓库维度）
            Map<String, List<MachineSubComponentsEntity>> subWarehouseMap = subMachineList.stream().collect(Collectors.groupingBy(MachineSubComponentsEntity::getWarehouseCode));
            subWarehouseMap.forEach((warehouseCode, subWareList)->{
                DmpWarehouseMappingEntity dmpSubWarehouseMappingEntity = warehouseMap.get(warehouseCode);
                MabangInOutStockDTO mabangInStockDTO = MabangUtil.fillMabangInOutStockSub(dmpSubWarehouseMappingEntity.getWarehouseCode(), dmpSubWarehouseMappingEntity.getWarehouseName(), opEmployeeName,
                        productDetailList, machineInfoEntity, subWareList, "in");
                mabangInOutStockService.inOutStock(mabangInStockDTO, machineInfoEntity.getId(), machineInfoEntity.getCode(), sourceType, operate);
            });
        }

    }

    /**
     * 仓库不存在消息通知
     * @param sourceTypeName
     * @param machineId
     * @param machineCode
     * @param warehouseCode
     */
    private void sendWarehouseNotice(String sourceTypeName, String machineId, String machineCode, String warehouseCode) {
        String errMsg = StrUtil.format("ERP{}，单据编号: {}，推送马帮手工入库仓库{}在马帮中不存在", sourceTypeName, machineCode, warehouseCode);
        log.info(errMsg);
        WarnMsgInfoDTO warnMsgInfoDTO = new WarnMsgInfoDTO();
        warnMsgInfoDTO.setTitle(StrUtil.format("ERP{}推送马帮手工入库异常",sourceTypeName));
        warnMsgInfoDTO.setErpServerModuleEnum(ErpServerModuleEnum.ERP_SERVER_WMS);
        warnMsgInfoDTO.setBizName(StrUtil.format("ERP{}推送马帮手工入库", sourceTypeName));
        warnMsgInfoDTO.setTableName("machine_info");
        warnMsgInfoDTO.setTableId(machineId);
        warnMsgInfoDTO.setKeyInfo(errMsg);
        mqProducerService.sendWarnMsg(warnMsgInfoDTO);
    }


}