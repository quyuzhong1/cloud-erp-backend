package com.erp.server.oms.rocketmq;

import cn.hutool.json.JSONUtil;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncTaskIdDTO;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.dto.PlatformOrderDetailDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncStatusEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.common.message.constant.RocketMqTopic;
import com.common.message.handler.AbstractPlatformConsumerHandler;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.wms.feign.SoOutstockFeign;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.server.oms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 下载平台订单消费服务
 * @author Cloud
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.PLATFORM_PULL_DATA_TOPIC,
        selectorExpression = "third_system_order_tag",
        consumerGroup = "${spring.cloud.nacos.discovery.namespace}-platform_pull_order_consumer",
        consumeMode = ConsumeMode.ORDERLY)
public class PlatformOrderConsumerService<T extends DmpSyncTaskIdDTO> extends AbstractPlatformConsumerHandler<T> {

    @Resource
    private DmpTaskFeign dmpTaskFeign;
    @Resource
    private PlatformOrderConsumerHandleService platformOrderConsumerHandleService;


    @Override
    public void updateSyncTaskStatus(String id, SyncStatusEnum code, String msg) {
        dmpTaskFeign.updateSyncInfo(new DmpSyncMqDTO.ParamDTO(id, code.getCode(), msg));
    }

    @Override
    public void sendWarnMsg(String syncTaskId, String msg) {
//        dmpTaskFeign.sendWarnMsg(syncTaskId);
    }

    @Override
//    @Transactional(rollbackFor = Exception.class)
    public ApiResult<?> handle(Object ext) {
        log.info("[B2C订单消费] 消费:dto={}", JSONUtil.toJsonStr(ext));
        PlatformOrderDTO dto = JSONUtil.toBean(ext.toString(), PlatformOrderDTO.class);
        platformOrderConsumerHandleService.handleAll(dto);
        return ApiResult.success();
    }
}
