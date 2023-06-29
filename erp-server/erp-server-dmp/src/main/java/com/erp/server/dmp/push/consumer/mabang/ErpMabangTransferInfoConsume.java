package com.erp.server.dmp.push.consumer.mabang;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.enums.SyncKingdeeOperateEnum;
import com.common.core.utils.StrUtils;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.ApiModuleTypeEnum;
import com.erp.model.dmp.dto.mabang.MabangInOutStockDTO;
import com.erp.model.dmp.entity.DmpWarehouseMappingEntity;
import com.erp.model.dmp.entity.PlatformEntity;
import com.erp.model.dmp.enums.ApiSendStatusEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.wms.dto.sync.MabangTransferInfoDTO;
import com.erp.model.wms.entity.TransferInfoDetailEntity;
import com.erp.model.wms.entity.TransferInfoEntity;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.dmp.push.service.common.DmpSyncCommonService;
import com.erp.server.dmp.push.service.mabang.MabangInOutStockService;
import com.erp.server.dmp.service.CfgSettingService;
import com.erp.server.dmp.service.DmpWarehouseMappingService;
import com.erp.server.dmp.utils.MabangUtil;
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
    private DmpSyncCommonService dmpSyncCommonService;

    @Autowired
    private CfgSettingService cfgSettingService;

    @Autowired
    private DmpWarehouseMappingService dmpWarehouseMappingService;

    @Autowired
    private MabangInOutStockService mabangInOutStockService;

    @Autowired
    private PlmTaskFeign plmTaskFeign;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onMessage(MabangTransferInfoDTO mabangTransferInfoDTO) {
        log.info("监听到ERP直接调拨单信息，内容：{}", JSONObject.toJSONString(mabangTransferInfoDTO));

        // 主单
        TransferInfoEntity transferInfo = mabangTransferInfoDTO.getTransferInfo();

        //模块类型
        Integer type = ApiModuleTypeEnum.TRANSFER_INFO.getCode();
        PlatformEntity platformEntity = dmpSyncCommonService.getPlatformEntity(transferInfo.getId(), type);
        if (ObjectUtils.isEmpty(platformEntity)) {
            return;
        }

        // 明细
        List<TransferInfoDetailEntity> transferDetailList  = mabangTransferInfoDTO.getTransferList();
        // 操作项
        String operate = StrUtils.null2EmptyWithTrim(mabangTransferInfoDTO.getOperate());
        // 单据来源类型
        String sourceType = StrUtils.null2EmptyWithTrim(mabangTransferInfoDTO.getSourceType());

        // 数据过滤处理
        String value = cfgSettingService.getValue(SettingEnum.ERP_TO_MB_WAREHOUSE_NAME);
        if(StrUtils.isEmpty(value)) {
            log.error("ERP直接调拨单同步到马帮出入库未配置监控仓库");
            dmpSyncCommonService.insertLogWriteBackSyncMabangStatus(platformEntity, transferInfo.getId(), "", StrUtil.format("ERP直接调拨单同步到{}出入库未配置监控仓库", PlatformEnum.MABANG.getDesc()), type, ApiSendStatusEnum.FAILURE.getCode());
            return;
        }
        List<String> warehouseCodeList = Arrays.asList(value.split(","));
        log.info("ERP直接调拨单同步到马帮出入库配置的监控仓库为：【{}】", JSONObject.toJSONString(warehouseCodeList));
        // 调入仓
        String inWarehouseCode =  StrUtils.null2EmptyWithTrim(transferInfo.getInWarehouseCode());
        // 调出仓
        String outWarehouseCode = StrUtils.null2EmptyWithTrim(transferInfo.getOutWarehouseCode());
        Map<String, DmpWarehouseMappingEntity> warehouseMap = dmpWarehouseMappingService.getByWarehouseCodes(Arrays.asList(inWarehouseCode, outWarehouseCode));
        Map<String, Object> warehouseCheckMap = MabangUtil.checkTransferInOutWarehouse(inWarehouseCode, outWarehouseCode, warehouseMap, warehouseCodeList);
        boolean warehouseStop = (boolean) warehouseCheckMap.get("stop");
        if(warehouseStop) {
            return;
        }
        String inWarehouseName = StrUtils.null2EmptyWithTrim(warehouseCheckMap.get("inWarehouseCode"));
        String outWarehouseName =  StrUtils.null2EmptyWithTrim(warehouseCheckMap.get("outWarehouseCode"));

        // 操作人
        String employeeName = cfgSettingService.getValue(SettingEnum.ERP_TO_MB_EMPLOYEE_NAME);
        if(StrUtils.isEmpty(employeeName)) {
            employeeName = MabangUtil.MABANG_EMPLOYEE_NAME;
        }

        List<String> skuIds = transferDetailList.stream().map(TransferInfoDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> productDetailList = plmTaskFeign.getByIdList(skuIds);

        // 审核
        if (Objects.equals(SyncKingdeeOperateEnum.OPERATE_APPROVE.getCode(), operate)) {
            if(warehouseCodeList.contains(inWarehouseCode)) {
                // 手工入库
                MabangInOutStockDTO mabangInOutStockDTO = MabangUtil.fillMabangInOutStock(inWarehouseCode, inWarehouseName, employeeName, productDetailList, transferInfo, transferDetailList, "in", operate);
                mabangInOutStockService.inOutStock(mabangInOutStockDTO, transferInfo, sourceType, operate);
            }
            if(warehouseCodeList.contains(outWarehouseCode)) {
                // 手工出库
                MabangInOutStockDTO mabangInOutStockDTO = MabangUtil.fillMabangInOutStock(outWarehouseCode, outWarehouseName, employeeName, productDetailList, transferInfo, transferDetailList, "out", operate);
                mabangInOutStockService.inOutStock(mabangInOutStockDTO, transferInfo, sourceType, operate);
            }
        }
        // 反审核
        if (Objects.equals(SyncKingdeeOperateEnum.OPERATE_DISAPPROVE.getCode(), operate)) {
            // 操作跟审核相反，审核的入库为出库，审核的出库为入库
            if(warehouseCodeList.contains(inWarehouseCode)) {
                // 手工出库
                MabangInOutStockDTO mabangInOutStockDTO = MabangUtil.fillMabangInOutStock(inWarehouseCode, inWarehouseName, employeeName, productDetailList, transferInfo, transferDetailList, "out", operate);
                mabangInOutStockService.inOutStock(mabangInOutStockDTO, transferInfo,  sourceType, operate);
            }
            if(warehouseCodeList.contains(outWarehouseCode)) {
                // 手工入库
                MabangInOutStockDTO mabangInOutStockDTO = MabangUtil.fillMabangInOutStock(outWarehouseCode, outWarehouseName, employeeName, productDetailList, transferInfo, transferDetailList, "in", operate);
                mabangInOutStockService.inOutStock(mabangInOutStockDTO, transferInfo, sourceType, operate);
            }
        }

    }


}