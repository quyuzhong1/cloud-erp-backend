package com.erp.server.dmp.push.service.mabang.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.core.utils.StrUtils;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.dto.mabang.DmpMabangInOutStockMsgDTO;
import com.erp.model.dmp.dto.mabang.MabangInOutStockDTO;
import com.erp.model.dmp.entity.DmpOutInStockDetailEntity;
import com.erp.model.dmp.entity.DmpOutInStockEntity;
import com.erp.model.dmp.entity.PlatformEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.wms.entity.TransferInfoEntity;
import com.erp.server.dmp.push.service.mabang.MabangInOutStockService;
import com.erp.server.dmp.service.DmpOutInStockDetailService;
import com.erp.server.dmp.service.DmpOutInStockService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


/**
 * 马帮手工出入库实现类
 * @CreateTime: 2023-06-27  19:51
 * @Author: zhangchunlin
 */
@Slf4j
@Service
public class MabangInOutStockServiceImpl implements MabangInOutStockService {

    @Autowired
    private DmpOutInStockService dmpOutInStockService;

    @Autowired
    private DmpOutInStockDetailService dmpOutInStockDetailService;

    @Autowired
    private MQProducerService mqProducerService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void inStock(PlatformEntity platformEntity, MabangInOutStockDTO mabangInOutStock, TransferInfoEntity transferInfo, String sourceType) {
        // 新增出入库数据
        DmpOutInStockEntity dmpOutInStockEntity = new DmpOutInStockEntity();
        dmpOutInStockEntity.setWarehouseCode(mabangInOutStock.getWarehouseCode());
        dmpOutInStockEntity.setWarehouseName(mabangInOutStock.getWarehouseName());
        dmpOutInStockEntity.setType("in");
        dmpOutInStockEntity.setTypeName("");
        dmpOutInStockEntity.setChargeUserName(mabangInOutStock.getEmployeeName());
        dmpOutInStockEntity.setRemark(mabangInOutStock.getRemark());
        dmpOutInStockEntity.setSourceType(StrUtils.null2EmptyWithTrim(sourceType));
        dmpOutInStockEntity.setSourceId(StrUtils.null2EmptyWithTrim(transferInfo.getId()));
        dmpOutInStockEntity.setPlatformSign(PlatformEnum.ERP.getDesc());
        // 未同步
        dmpOutInStockEntity.setSyncMbStatus("0");
        dmpOutInStockEntity.setLastSyncMbTime(LocalDateTime.now());
        dmpOutInStockEntity.setTargetPlatformSign(PlatformEnum.MABANG.getDesc());

        dmpOutInStockEntity.setTargetOrderCode("");

        // 出入库记录主单保存
        dmpOutInStockService.save(dmpOutInStockEntity);

        // 出入库记录明细保存
        List<DmpOutInStockDetailEntity> dmpOutInStockDetailEntityList = Lists.newArrayListWithExpectedSize(mabangInOutStock.getData().size());
        mabangInOutStock.getData().stream().forEach(skuItem -> {
            DmpOutInStockDetailEntity dmpOutInStockDetailEntity = new DmpOutInStockDetailEntity();
            dmpOutInStockDetailEntity.setMainId(dmpOutInStockEntity.getId());
            dmpOutInStockDetailEntity.setSkuNo(skuItem.getStockSku());
            dmpOutInStockDetailEntity.setProductName(skuItem.getProductName());
            dmpOutInStockDetailEntity.setSourceDetailId(skuItem.getSourceDetailId());
            dmpOutInStockDetailEntity.setPrice(BigDecimal.ZERO);
            dmpOutInStockDetailEntity.setQty(Integer.parseInt(skuItem.getQuantity()));
            dmpOutInStockDetailEntity.setWarehouseLocation(skuItem.getGridCode());
            dmpOutInStockDetailEntityList.add(dmpOutInStockDetailEntity);
        });
        dmpOutInStockDetailService.saveBatch(dmpOutInStockDetailEntityList);

        // 发送MQ消息处理出入库信息然后发送到马帮
        DmpMabangInOutStockMsgDTO dmpMabangInOutStockMsgDTO = new DmpMabangInOutStockMsgDTO();
        dmpMabangInOutStockMsgDTO.setDmpOutInStockId(dmpOutInStockEntity.getId());
        dmpMabangInOutStockMsgDTO.setMabangInOutStock(mabangInOutStock);

        SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.SYNC_DMP_TO_MABANG_TOPIC, RocketMqTagEnum.DMP_MABANG_TRANSFER_INFO_TAG.getName(),
                dmpMabangInOutStockMsgDTO, dmpOutInStockEntity.getId());
        if (!SendStatus.SEND_OK.equals(result.getSendStatus())) {
            throw new RuntimeException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
        }

    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void outStock(PlatformEntity platformEntity, MabangInOutStockDTO mabangInOutStock, TransferInfoEntity transferInfo, String sourceType) {
        // 新增出入库数据
        DmpOutInStockEntity dmpOutInStockEntity = new DmpOutInStockEntity();
        dmpOutInStockEntity.setWarehouseCode(mabangInOutStock.getWarehouseCode());
        dmpOutInStockEntity.setWarehouseName(mabangInOutStock.getWarehouseName());
        dmpOutInStockEntity.setType("out");
        dmpOutInStockEntity.setTypeName("");
        dmpOutInStockEntity.setChargeUserName(mabangInOutStock.getEmployeeName());
        dmpOutInStockEntity.setRemark(mabangInOutStock.getRemark());
        dmpOutInStockEntity.setSourceType(StrUtils.null2EmptyWithTrim(sourceType));
        dmpOutInStockEntity.setSourceId(StrUtils.null2EmptyWithTrim(transferInfo.getId()));
        dmpOutInStockEntity.setPlatformSign(PlatformEnum.ERP.getDesc());
        // 未同步
        dmpOutInStockEntity.setSyncMbStatus("0");
        dmpOutInStockEntity.setLastSyncMbTime(LocalDateTime.now());
        dmpOutInStockEntity.setTargetPlatformSign(PlatformEnum.MABANG.getDesc());

        dmpOutInStockEntity.setTargetOrderCode("");

        // 出入库记录主单保存
        dmpOutInStockService.save(dmpOutInStockEntity);

        // 出入库记录明细保存
        List<DmpOutInStockDetailEntity> dmpOutInStockDetailEntityList = Lists.newArrayListWithExpectedSize(mabangInOutStock.getData().size());
        mabangInOutStock.getData().stream().forEach(skuItem -> {
            DmpOutInStockDetailEntity dmpOutInStockDetailEntity = new DmpOutInStockDetailEntity();
            dmpOutInStockDetailEntity.setMainId(dmpOutInStockEntity.getId());
            dmpOutInStockDetailEntity.setSkuNo(skuItem.getStockSku());
            dmpOutInStockDetailEntity.setProductName(skuItem.getProductName());
            dmpOutInStockDetailEntity.setSourceDetailId(skuItem.getSourceDetailId());
            dmpOutInStockDetailEntity.setPrice(BigDecimal.ZERO);
            dmpOutInStockDetailEntity.setQty(Integer.parseInt(skuItem.getQuantity()));
            dmpOutInStockDetailEntity.setWarehouseLocation(skuItem.getGridCode());
            dmpOutInStockDetailEntityList.add(dmpOutInStockDetailEntity);
        });
        dmpOutInStockDetailService.saveBatch(dmpOutInStockDetailEntityList);
    }

}