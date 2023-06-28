package com.erp.server.dmp.push.consumer.mabang;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.ApiModuleTypeEnum;
import com.erp.model.dmp.dto.mabang.DmpMabangInOutStockMsgDTO;
import com.erp.model.dmp.entity.DmpOutInStockEntity;
import com.erp.model.dmp.entity.PlatformEntity;
import com.erp.model.dmp.enums.ApiSendStatusEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.server.dmp.push.service.mabang.MabangCommonService;
import com.erp.server.dmp.push.service.mabang.MabangInOutStockService;
import com.erp.server.dmp.service.DmpOutInStockService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @CreateTime: 2023-06-28  14:45
 * @Author: zhangchunlin
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.SYNC_DMP_TO_MABANG_TOPIC, selectorExpression = "dmp_mabang_transfer_info_tag", consumerGroup = RocketMqConsumerGroup.SYNC_DMP_TRANSFER_INFO_TO_MABANG)
public class DmpMabangTransferInfoConsume implements RocketMQListener<DmpMabangInOutStockMsgDTO>  {

    @Autowired
    private DmpOutInStockService dmpOutInStockService;

    @Autowired
    private MabangCommonService mabangCommonService;

    @Autowired
    private MabangInOutStockService mabangInOutStockService;

    @Override
    public void onMessage(DmpMabangInOutStockMsgDTO dmpMabangInOutStockMsgDTO) {
        log.info("监听到DMP直接调拨单信息->出入库，内容：{}", JSONObject.toJSONString(dmpMabangInOutStockMsgDTO));

        // 出入库id
        String outInId = dmpMabangInOutStockMsgDTO.getDmpOutInStockId();

        //模块类型
        Integer type = ApiModuleTypeEnum.TRANSFER_INFO.getCode();
        PlatformEntity platformEntity = mabangCommonService.getPlatformEntity(outInId, type);
        if (ObjectUtils.isEmpty(platformEntity)) {
            return;
        }

        DmpOutInStockEntity dmpOutInStockEntity = dmpOutInStockService.getById(outInId);
        if(Objects.isNull(dmpOutInStockEntity)) {
            mabangCommonService.insertLogWriteBackSyncMabangStatus(platformEntity, outInId, "", StrUtil.format("ERP直接调拨单同步到{}未找到出入库数据", PlatformEnum.MABANG.getDesc()), type, ApiSendStatusEnum.FAILURE.getCode());
            return;
        }
        log.info("ERP直接调拨单id：【{}】，同步马帮状态【{}】", dmpOutInStockEntity.getSourceId(), dmpOutInStockEntity.getSyncMbStatus());

        // 同步成功的不处理
        if(Objects.equals(dmpOutInStockEntity.getSyncMbStatus(), "1")) {
            log.info("ERP直接调拨单同步到马帮已经同步，不处理");
            return;
        }
        if(Objects.equals(dmpOutInStockEntity.getType(), "in")) {
            mabangInOutStockService.sendToMabangInStock(dmpOutInStockEntity, dmpMabangInOutStockMsgDTO.getMabangInOutStock(), platformEntity, type, dmpMabangInOutStockMsgDTO.getApproveType() );
        } else if(Objects.equals(dmpOutInStockEntity.getType(), "out")) {
            mabangInOutStockService.sendToMabangOutStock(dmpOutInStockEntity, dmpMabangInOutStockMsgDTO.getMabangInOutStock(), platformEntity, type, dmpMabangInOutStockMsgDTO.getApproveType() );
        }
    }

}