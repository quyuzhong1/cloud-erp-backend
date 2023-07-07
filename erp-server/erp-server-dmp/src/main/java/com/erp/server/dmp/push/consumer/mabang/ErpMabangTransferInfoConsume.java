package com.erp.server.dmp.push.consumer.mabang;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.enums.ErpServerModuleEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncKingdeeOperateEnum;
import com.common.core.utils.StrUtils;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.dto.mabang.MabangInOutStockDTO;
import com.erp.model.dmp.entity.DmpWarehouseMappingEntity;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.msg.dto.WarnMsgInfoDTO;
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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
    private MQProducerService mqProducerService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onMessage(MabangTransferInfoDTO mabangTransferInfoDTO) {
        log.info("监听到ERP直接调拨单信息，内容：{}", JSONObject.toJSONString(mabangTransferInfoDTO));

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
            sendNotice(sourceTypeName);
            return;
        }
        List<String> warehouseCodeList = Arrays.asList(value.split(MabangUtil.SEPARATOR));
        log.info("ERP直接调拨单同步到马帮出入库配置的监控仓库为：【{}】", JSONObject.toJSONString(warehouseCodeList));
        // 调入仓
        String inWarehouseCode =  StrUtils.null2EmptyWithTrim(transferInfo.getInWarehouseCode());
        // 调出仓
        String outWarehouseCode = StrUtils.null2EmptyWithTrim(transferInfo.getOutWarehouseCode());
        Map<String, DmpWarehouseMappingEntity> warehouseMap = dmpWarehouseMappingService.getByWarehouseCodes(Arrays.asList(inWarehouseCode, outWarehouseCode));
        Map<String, Object> warehouseCheckMap = MabangUtil.checkTransferInOutWarehouse(inWarehouseCode, outWarehouseCode, warehouseMap, warehouseCodeList);
        boolean warehouseStop = (boolean) warehouseCheckMap.get(MabangUtil.SEND_STOP);
        if(warehouseStop) {
            return;
        }
        String inWarehouseName = StrUtils.null2EmptyWithTrim(warehouseCheckMap.get(MabangUtil.IN_WAREHOUSE_CODE));
        String outWarehouseName =  StrUtils.null2EmptyWithTrim(warehouseCheckMap.get(MabangUtil.OUT_WAREHOUSE_CODE));

        // 操作人
        String employeeName = cfgSettingService.getValue(SettingEnum.ERP_TO_MB_EMPLOYEE_NAME);
        if(StrUtils.isEmpty(employeeName)) {
            employeeName = MabangUtil.MABANG_EMPLOYEE_NAME;
        }

        // SKU ID集合，获取SKU信息
        List<String> skuIds = transferDetailList.stream().map(TransferInfoDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> productDetailList = plmTaskFeign.getByIdList(skuIds);

        List<MabangInOutStockDTO> mabangInOutStockDTOList = null;
        // 审核
        if (Objects.equals(SyncKingdeeOperateEnum.OPERATE_APPROVE.getCode(), operate)) {
            mabangInOutStockDTOList = Lists.newArrayList();
            if(warehouseCodeList.contains(inWarehouseCode)) {
                // 手工入库
                log.info("ERP直接调拨单【{}】审核同步到马帮出入库：入库仓库【{}】", transferInfo.getCode(), inWarehouseCode);
                MabangInOutStockDTO mabangInOutStockDTO = MabangUtil.fillMabangInOutStock(inWarehouseCode, inWarehouseName, employeeName, productDetailList, transferInfo, transferDetailList, InventoryInOutEnum.IN_STOCK.getCode(), operate);
                mabangInOutStockDTOList.add(mabangInOutStockDTO);
            }
            if(warehouseCodeList.contains(outWarehouseCode)) {
                // 手工出库
                log.info("ERP直接调拨单【{}】审核同步到马帮出入库：出库仓库【{}】", transferInfo.getCode(), outWarehouseCode);
                MabangInOutStockDTO mabangInOutStockDTO = MabangUtil.fillMabangInOutStock(outWarehouseCode, outWarehouseName, employeeName, productDetailList, transferInfo, transferDetailList, InventoryInOutEnum.OUT_STOCK.getCode(), operate);
                mabangInOutStockDTOList.add(mabangInOutStockDTO);
            }
        }
        // 反审核
        if (Objects.equals(SyncKingdeeOperateEnum.OPERATE_DISAPPROVE.getCode(), operate)) {
            mabangInOutStockDTOList = Lists.newArrayList();
            // 操作跟审核相反，审核的入库为出库，审核的出库为入库
            if(warehouseCodeList.contains(inWarehouseCode)) {
                // 手工出库
                log.info("ERP直接调拨单【{}】反审核同步到马帮出入库：出库仓库【{}】", transferInfo.getCode(), inWarehouseCode);
                MabangInOutStockDTO mabangInOutStockDTO = MabangUtil.fillMabangInOutStock(inWarehouseCode, inWarehouseName, employeeName, productDetailList, transferInfo, transferDetailList, InventoryInOutEnum.OUT_STOCK.getCode(), operate);
                mabangInOutStockDTOList.add(mabangInOutStockDTO);
            }
            if(warehouseCodeList.contains(outWarehouseCode)) {
                // 手工入库
                log.info("ERP直接调拨单【{}】反审核同步到马帮出入库：入库仓库【{}】", transferInfo.getCode(), outWarehouseCode);
                MabangInOutStockDTO mabangInOutStockDTO = MabangUtil.fillMabangInOutStock(outWarehouseCode, outWarehouseName, employeeName, productDetailList, transferInfo, transferDetailList, InventoryInOutEnum.IN_STOCK.getCode(), operate);
                mabangInOutStockDTOList.add(mabangInOutStockDTO);
            }
        }
        mabangInOutStockService.batchInOutStock(mabangInOutStockDTOList, transferInfo.getId(),  transferInfo.getCode(), sourceType, operate);

    }

    /**
     * 未配置监控仓库发送提醒
     * @param sourceTypeName
     */
    private void sendNotice(String sourceTypeName) {
        log.error("ERP直接调拨单同步到马帮出入库未配置监控仓库");
        WarnMsgInfoDTO warnMsgInfoDTO = new WarnMsgInfoDTO();
        warnMsgInfoDTO.setTitle(StrUtil.format("ERP{}推送马帮出入库未配置监控仓库", sourceTypeName));
        warnMsgInfoDTO.setErpServerModuleEnum(ErpServerModuleEnum.ERP_SERVER_DMP);
        warnMsgInfoDTO.setBizName(StrUtil.format("ERP{}推送马帮手工入库", sourceTypeName));
        warnMsgInfoDTO.setTableName("");
        warnMsgInfoDTO.setTableId("");
        warnMsgInfoDTO.setKeyInfo("ERP直接调拨单同步到马帮出入库未配置监控仓库，请在cfg_setting表erp_to_mb_direct_transfer配置");
        mqProducerService.sendWarnMsg(warnMsgInfoDTO);
    }


}