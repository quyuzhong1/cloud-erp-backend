package com.erp.server.dmp.push.consumer.mabang;

import cn.hutool.core.collection.ListUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.utils.RedisUtil;
import com.common.core.utils.StrUtils;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.dmp.dto.mabang.MabangInOutStockDTO;
import com.erp.model.dmp.entity.DmpWarehouseMappingEntity;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.wms.dto.sync.MabangTransferInfoDTO;
import com.erp.model.wms.entity.TransferInfoDetailEntity;
import com.erp.model.wms.entity.TransferInfoEntity;
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

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 直接调拨单推送到马帮
 * @CreateTime: 2023-06-27  14:54
 * @Author: zhangchunlin
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.SYNC_WMS_TO_DMP_TOPIC, selectorExpression = "erp_dmp_transfer_info_tag", consumerGroup = RocketMqConsumerGroup.SYNC_ERP_TRANSFER_INFO_TO_DMP)
public class ErpMabangTransferInfoConsume implements RocketMQListener<MabangTransferInfoDTO> {

    @Autowired
    private CfgSettingService cfgSettingService;

    @Autowired
    private DmpWarehouseMappingService dmpWarehouseMappingService;

    @Autowired
    private MabangInOutStockService mabangInOutStockService;

    @Autowired
    private PlmTaskFeign plmTaskFeign;
    @Autowired
    private RedisUtil redisUtil;
    @Resource
    private ErpMabangTransferInfoConsume erpMabangTransferInfoConsume;

    @Override
    public void onMessage(MabangTransferInfoDTO mabangTransferInfoDTO) {
        try {
            erpMabangTransferInfoConsume.extracted(mabangTransferInfoDTO);
        }catch (Exception e){
            log.error("直接调拨单推送到马帮异常：{}", e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void extracted(MabangTransferInfoDTO mabangTransferInfoDTO) {
        log.warn("监听到ERP直接调拨单信息，内容：{}", JSONObject.toJSONString(mabangTransferInfoDTO));

        // 主单
        TransferInfoEntity transferInfo = mabangTransferInfoDTO.getTransferInfo();

        // 明细
        List<TransferInfoDetailEntity> transferDetailList  = mabangTransferInfoDTO.getTransferList();
        // 操作项
        String operate = StrUtils.null2EmptyWithTrim(mabangTransferInfoDTO.getOperate());
        // 单据来源类型
        String sourceType = StrUtils.null2EmptyWithTrim(mabangTransferInfoDTO.getSourceType());
        String sourceTypeName = SourceTypeEnum.getName(sourceType);

        // 数据过滤处理
        String value = cfgSettingService.getValue(SettingEnum.ERP_TO_MB_WAREHOUSE_NAME);
        if(StrUtils.isEmpty(value)) {
            mabangInOutStockService.sendNoticeNoMonitorWarehouse(sourceTypeName);
            return;
        }
        List<String> monitorWarehouseCodeList = Arrays.asList(value.split(MabangUtil.SEPARATOR));
        log.warn("ERP直接调拨单同步到马帮出入库配置的监控仓库为：【{}】", JSONObject.toJSONString(monitorWarehouseCodeList));

        List<String> inWarehouseCodeList = transferDetailList.stream().map(TransferInfoDetailEntity::getInWarehouseCode).distinct().collect(Collectors.toList());
        List<String> outWarehouseCodeList = transferDetailList.stream().map(TransferInfoDetailEntity::getOutWarehouseCode).distinct().collect(Collectors.toList());
        List<String> warehouseCodeList = Stream.concat(inWarehouseCodeList.stream(), outWarehouseCodeList.stream()).collect(Collectors.toList());

        Map<String, DmpWarehouseMappingEntity> warehouseMap = dmpWarehouseMappingService.getByWarehouseCodes(warehouseCodeList);

        // 操作人
        String employeeName = cfgSettingService.getValue(SettingEnum.ERP_TO_MB_EMPLOYEE_NAME);
        if(StrUtils.isEmpty(employeeName)) {
            employeeName = MabangUtil.MABANG_EMPLOYEE_NAME;
        }

        // SKU ID集合，获取SKU信息
        List<String> skuIds = transferDetailList.stream().map(TransferInfoDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> productDetailList = plmTaskFeign.getByIdList(skuIds);

        // 调入仓明细
        Map<String, List<TransferInfoDetailEntity>> inTransferInfoMap = transferDetailList.stream().collect(Collectors.groupingBy(TransferInfoDetailEntity::getInWarehouseCode));
        // 调出仓明细
        Map<String, List<TransferInfoDetailEntity>> outTransferInfoMap = transferDetailList.stream().collect(Collectors.groupingBy(TransferInfoDetailEntity::getOutWarehouseCode));


        List<MabangInOutStockDTO>  mabangInOutStockDTOList = Lists.newArrayList();

        String operateName = SyncOperateEnum.getNameByCode(operate);
        for(Map.Entry<String, List<TransferInfoDetailEntity>> inEntry : inTransferInfoMap.entrySet()){
            String inWarehouseCode = inEntry.getKey();
            if(Objects.nonNull(warehouseMap.get(inWarehouseCode)) && monitorWarehouseCodeList.contains(inWarehouseCode) ) {
                // 审核-手工入库；反审核-手工出库
                InventoryInOutEnum inventoryInOutEnum = Objects.equals(SyncOperateEnum.OPERATE_APPROVE.getCode(), operate) ? InventoryInOutEnum.IN_STOCK : InventoryInOutEnum.OUT_STOCK;
                log.warn("ERP直接调拨单【{}】【{}】同步到马帮【{}】：仓库【{}】", transferInfo.getCode(), operateName, inventoryInOutEnum.getName(), inWarehouseCode);
                String inWarehouseName = warehouseMap.get(inWarehouseCode).getWarehouseName();
                List<TransferInfoDetailEntity> inWarehouseDetailList = inEntry.getValue();
                // 此处优化，由于马帮一次手工出入库只能最大支持500条明细
                if(inWarehouseDetailList.size() <= MabangUtil.MAX_DETAIL_SIZE) {
                    MabangInOutStockDTO mabangInOutStockDTO = MabangUtil.fillMabangInOutStock(inWarehouseCode, inWarehouseName, employeeName, productDetailList, transferInfo, inWarehouseDetailList, inventoryInOutEnum.getCode(), operate, redisUtil);
                    mabangInOutStockDTOList.add(mabangInOutStockDTO);
                } else {
                    List<List<TransferInfoDetailEntity>> partitionList = ListUtil.partition(inWarehouseDetailList, MabangUtil.MAX_DETAIL_SIZE);
                    for(List<TransferInfoDetailEntity> dataList : partitionList) {
                        MabangInOutStockDTO mabangInOutStockDTO = MabangUtil.fillMabangInOutStock(inWarehouseCode, inWarehouseName, employeeName, productDetailList, transferInfo, dataList, inventoryInOutEnum.getCode(), operate, redisUtil);
                        mabangInOutStockDTOList.add(mabangInOutStockDTO);
                    }
                }
            }
        }

        for(Map.Entry<String, List<TransferInfoDetailEntity>> outEntry : outTransferInfoMap.entrySet()) {
            String outWarehouseCode = outEntry.getKey();
            if(Objects.nonNull(warehouseMap.get(outWarehouseCode)) && monitorWarehouseCodeList.contains(outWarehouseCode) ) {
                // 审核-手工出库；反审核-手工入库
                InventoryInOutEnum inventoryInOutEnum = Objects.equals(SyncOperateEnum.OPERATE_APPROVE.getCode(), operate) ? InventoryInOutEnum.OUT_STOCK : InventoryInOutEnum.IN_STOCK;
                log.warn("ERP直接调拨单【{}】【{}】同步到马帮【{}】库：仓库【{}】", transferInfo.getCode(), operateName, inventoryInOutEnum.getName(), outWarehouseCode);
                String outWarehouseName = warehouseMap.get(outWarehouseCode).getWarehouseName();
                List<TransferInfoDetailEntity> outWarehouseDetailList = outEntry.getValue();
                // 此处优化，由于马帮一次手工出入库只能最大支持500条明细
                if(outWarehouseDetailList.size() <= MabangUtil.MAX_DETAIL_SIZE) {
                    MabangInOutStockDTO mabangInOutStockDTO = MabangUtil.fillMabangInOutStock(outWarehouseCode, outWarehouseName, employeeName, productDetailList, transferInfo, outWarehouseDetailList, inventoryInOutEnum.getCode(), operate, redisUtil);
                    mabangInOutStockDTOList.add(mabangInOutStockDTO);
                } else {
                    List<List<TransferInfoDetailEntity>> partitionList = ListUtil.partition(outWarehouseDetailList, MabangUtil.MAX_DETAIL_SIZE);
                    for(List<TransferInfoDetailEntity> dataList : partitionList) {
                        MabangInOutStockDTO mabangInOutStockDTO = MabangUtil.fillMabangInOutStock(outWarehouseCode, outWarehouseName, employeeName, productDetailList, transferInfo, dataList, inventoryInOutEnum.getCode(), operate, redisUtil);
                        mabangInOutStockDTOList.add(mabangInOutStockDTO);
                    }
                }
            }
        }
        mabangInOutStockService.batchInOutStock(mabangInOutStockDTOList, transferInfo.getId(),  transferInfo.getCode(), sourceType, operate);
    }


}