package com.erp.server.tms.rocketmq;


import cn.hutool.core.collection.CollUtil;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.core.enums.ApiError;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.tms.dto.TmsAsyncTaskRecordDTO;
import com.erp.model.tms.entity.*;
import com.erp.model.tms.enums.*;
import com.erp.model.wms.entity.FirstMileDeliveryDetailEntity;
import com.erp.model.wms.entity.FirstMileDeliveryEntity;
import com.erp.rpc.wms.feign.WmsFirstMileDeliveryFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.tms.mapper.ReportPeriodMonthMapper;
import com.erp.server.tms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.concurrent.CountDownLatch;

/**
 *
 */
@Slf4j
@Service
@RocketMQMessageListener(topic = RocketMqTopic.TMS_PUSH_ALLOCATION_COST_TOPIC,
        selectorExpression = RocketMqNewTag.TMS_PUSH_ALLOCATION_COST_TAG,
        consumerGroup = RocketMqConsumerGroup.TMS_PUSH_ALLOCATION_COST_CONSUMER)
public class PushAllocationCostConsumerService implements RocketMQListener<TmsAsyncTaskRecordDTO.PushParamsDTO> {

    @Resource
    private FirstMileCostAllocationService firstMileCostAllocationService;

    @Resource
    private LogisticsBillCostService logisticsBillCostService;

    @Override
    public void onMessage(TmsAsyncTaskRecordDTO.PushParamsDTO dto) {
        String businessType = dto.getBusinessType();
        if(Objects.equals(businessType,SourceTypeEnum.FIRST_MILE_COST_ALLOCATION.getCode())){
            //下推费用分摊
            firstMileCostAllocationService.pushFirstMileCostAllocation(dto);
        }else {
            //下推小包费用分摊
            logisticsBillCostService.pushSmallBagCostAllocation(dto);
        }
    }



}
