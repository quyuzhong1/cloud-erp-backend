package com.erp.server.dmp.push.consumer.mabang;

import com.alibaba.fastjson.JSONObject;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncKingdeeOperateEnum;
import com.common.core.utils.StrUtils;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.dto.mabang.MabangInOutStockDTO;
import com.erp.model.dmp.entity.DmpWarehouseMappingEntity;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.wms.dto.sync.MabangMachineInfoDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.WorkTypeEnum;
import com.erp.model.wms.enums.inventory.InventoryInOutEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
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

        // 明细
        List<MachineDetailEntity> machineDetailList  = mabangMachineInfoDTO.getMachineDetailEntityList();
        // 子明细
        List<MachineSubComponentsEntity> subMachineList = mabangMachineInfoDTO.getMachineSubComponentsEntityList();
        // 操作项
        String operate = StrUtils.null2EmptyWithTrim(mabangMachineInfoDTO.getOperate());
        // 单据来源类型
        String sourceType = StrUtils.null2EmptyWithTrim(mabangMachineInfoDTO.getSourceType());
        String sourceTypeName = SourceTypeEnum.getName(sourceType);

        // 数据过滤处理
        String value = cfgSettingService.getValue(SettingEnum.ERP_TO_MB_WAREHOUSE_NAME);
        if(StrUtils.isEmpty(value)) {
            mabangInOutStockService.sendNoticeNoMonitorWarehouse(sourceTypeName);
            return;
        }
        List<String> permitWarehouseCodeList = Arrays.asList(value.split(MabangUtil.SEPARATOR));
        log.warn("ERP加工单同步到马帮出入库配置的监控仓库为：【{}】", JSONObject.toJSONString(permitWarehouseCodeList));

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

        // 父SKU产品信息
        List<String> parentSkuIds = machineDetailList.stream().map(MachineDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        // 子SKU产品信息
        List<String> subSkuIds = subMachineList.stream().map(MachineSubComponentsEntity::getSkuId).distinct().collect(Collectors.toList());
        List<String> skuIds = new ArrayList<>();
        skuIds.addAll(parentSkuIds);
        skuIds.addAll(subSkuIds);
        List<ProductDetailEntity> productDetailList = plmTaskFeign.getByIdList(skuIds);

        DmpWarehouseMappingEntity dmpWarehouseMappingEntity = warehouseMap.get(parentWarehouseCode);
        log.warn("ERP加工单单号【{}】，事务类型【{}】", machineInfoEntity.getCode(), WorkTypeEnum.getByCode(machineInfoEntity.getWorkType()));

        // 审核【组装】、反审核【拆卸】
        if(  (Objects.equals(SyncKingdeeOperateEnum.OPERATE_APPROVE.getCode(), operate) && Objects.equals(machineInfoEntity.getWorkType(), WorkTypeEnum.ASSEMBLE.getCode()) )
                || (Objects.equals(SyncKingdeeOperateEnum.OPERATE_DISAPPROVE.getCode(), operate) && Objects.equals(machineInfoEntity.getWorkType(), WorkTypeEnum.DISASSEMBLE.getCode()) ) ) {
            // 父SKU手工入库
            if(Objects.nonNull(dmpWarehouseMappingEntity) && permitWarehouseCodeList.contains(dmpWarehouseMappingEntity.getWarehouseCode())) {
                MabangInOutStockDTO mabangInStockDTO = MabangUtil.fillMabangInOutStock(dmpWarehouseMappingEntity.getWarehouseCode(), dmpWarehouseMappingEntity.getWarehouseName(), opEmployeeName,
                        productDetailList, machineInfoEntity, machineDetailList, InventoryInOutEnum.IN_STOCK.getCode());
                mabangInOutStockService.inOutStock(mabangInStockDTO, machineInfoEntity.getId(), machineInfoEntity.getCode(), sourceType, operate);
            }

            // 手工出库（按仓库维度）
            Map<String, List<MachineSubComponentsEntity>> subWarehouseMap = subMachineList.stream().collect(Collectors.groupingBy(MachineSubComponentsEntity::getWarehouseCode));
            subWarehouseMap.forEach((warehouseCode, subWareList)->{
                DmpWarehouseMappingEntity dmpSubWarehouseMappingEntity = warehouseMap.get(warehouseCode);
                if(Objects.nonNull(dmpSubWarehouseMappingEntity) && permitWarehouseCodeList.contains(warehouseCode)) {
                    MabangInOutStockDTO mabangOutStockDTO = MabangUtil.fillMabangInOutStockSub(dmpSubWarehouseMappingEntity.getWarehouseCode(), dmpSubWarehouseMappingEntity.getWarehouseName(), opEmployeeName,
                            productDetailList, machineInfoEntity, subWareList, InventoryInOutEnum.OUT_STOCK.getCode());
                    mabangInOutStockService.inOutStock(mabangOutStockDTO, machineInfoEntity.getId(), machineInfoEntity.getCode(), sourceType, operate);
                }
            });
        }

        // 审核【拆卸】、反审核【组装】
        if( (Objects.equals(SyncKingdeeOperateEnum.OPERATE_APPROVE.getCode(), operate) && Objects.equals(machineInfoEntity.getWorkType(), WorkTypeEnum.DISASSEMBLE.getCode()) )
                || (Objects.equals(SyncKingdeeOperateEnum.OPERATE_DISAPPROVE.getCode(), operate) &&  Objects.equals(machineInfoEntity.getWorkType(), WorkTypeEnum.ASSEMBLE.getCode()))) {
             // 父SKU手工出库
            if(Objects.nonNull(dmpWarehouseMappingEntity) && permitWarehouseCodeList.contains(dmpWarehouseMappingEntity.getWarehouseCode())) {
                MabangInOutStockDTO mabangOutStockDTO = MabangUtil.fillMabangInOutStock(dmpWarehouseMappingEntity.getWarehouseCode(), dmpWarehouseMappingEntity.getWarehouseName(), opEmployeeName,
                        productDetailList, machineInfoEntity, machineDetailList, InventoryInOutEnum.OUT_STOCK.getCode());
                mabangInOutStockService.inOutStock(mabangOutStockDTO, machineInfoEntity.getId(), machineInfoEntity.getCode(), sourceType, operate);
            }

            // 子SKU手工入库（按仓库维度）
            Map<String, List<MachineSubComponentsEntity>> subWarehouseMap = subMachineList.stream().collect(Collectors.groupingBy(MachineSubComponentsEntity::getWarehouseCode));
            subWarehouseMap.forEach((warehouseCode, subWareList)->{
                DmpWarehouseMappingEntity dmpSubWarehouseMappingEntity = warehouseMap.get(warehouseCode);
                if(Objects.nonNull(dmpSubWarehouseMappingEntity) && permitWarehouseCodeList.contains(warehouseCode)) {
                    MabangInOutStockDTO mabangInStockDTO = MabangUtil.fillMabangInOutStockSub(dmpSubWarehouseMappingEntity.getWarehouseCode(), dmpSubWarehouseMappingEntity.getWarehouseName(), opEmployeeName,
                            productDetailList, machineInfoEntity, subWareList, InventoryInOutEnum.IN_STOCK.getCode());
                    mabangInOutStockService.inOutStock(mabangInStockDTO, machineInfoEntity.getId(), machineInfoEntity.getCode(), sourceType, operate);
                }
            });
        }

    }

}