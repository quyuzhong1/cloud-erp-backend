package com.erp.server.wms.schedule;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.fastjson2.JSONObject;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ErpServerModuleEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.msg.dto.NoticeMsgCardButtonDTO;
import com.erp.model.msg.dto.NoticeMsgInfoDTO;
import com.erp.model.msg.dto.WarnMsgInfoDTO;
import com.erp.model.msg.enums.NoticeTypeEnum;
import com.erp.model.msg.enums.WarnMsgTypeEnum;
import com.erp.model.sys.dto.CfgNoticeDTO;
import com.erp.model.sys.entity.CfgNoticeDetailEntity;
import com.erp.model.sys.entity.CfgNoticeEntity;
import com.erp.model.sys.entity.DictBasicEntity;
import com.erp.model.sys.enums.DictBasicEnum;
import com.erp.model.wms.dto.VirtualInventoryAgeDTO;
import com.erp.model.wms.dto.VirtualInventoryDiffDTO;
import com.erp.model.wms.dto.VirtualTransFlowDetailDTO;
import com.erp.model.wms.entity.QcNoticeEntity;
import com.erp.model.wms.enums.*;
import com.erp.server.wms.service.QcNoticeService;
import com.erp.server.wms.service.VirtualInventoryDetailService;
import com.erp.server.wms.service.VirtualInventoryDiffService;
import com.erp.server.wms.service.VirtualTransFlowDetailService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @description:
 * @author jack
 * @date: 2025-04-23
 */
@Component
@Slf4j
public class QcNoticeStatusJob {

    @Resource
    private QcNoticeService qcNoticeService;

    /**
     * @description: 间隔半个小时执行一次 , 更新质检通知单的时效
     * 已质检时：
     * 明细最新质检时间-审核时间
     * 未质检&部分质检时：
     * 当前时间-审核时间
     * 精确到小时，不足30分钟时舍弃，大于等于30时进1
     * @author jack
     * @date: 2025-04-23
     */
    @XxlJob("QcNoticeStatusJob")
    public ReturnT<String> virtualNotice() {
        XxlJobHelper.log("====开始更新质检通知单的时效=====");
        //当前时间
        LocalDateTime nowTime = LocalDateTime.now();
        //查询审核已通过的； 状态为待质检或部分质检的数据进行更新时效
        List<String> statusList = Arrays.asList(QcNoticeStatusEnum.PART.getCode(), QcNoticeStatusEnum.WAIT.getCode());
        List<QcNoticeEntity> list = qcNoticeService.lambdaQuery()
                .in(QcNoticeEntity::getQcStatus, statusList)
                .eq(QcNoticeEntity::getApproveStatus, ApproveStatusEnum.APPROVE.getCode())
                .list();
        if(CollUtil.isNotEmpty(list)){
            for (QcNoticeEntity qcNoticeEntity : list) {
                LocalDateTime approveTime = qcNoticeEntity.getApproveTime();
                int hoursDiff = qcNoticeService.getHoursDiff(approveTime, nowTime);

                qcNoticeService.lambdaUpdate()
                        .eq(QcNoticeEntity::getId, qcNoticeEntity.getId())
                        .set(QcNoticeEntity::getQcTImeliness, hoursDiff)
                        .update();

            }
        }
        XxlJobHelper.log("====开始更新质检通知单的时效=====");
        return ReturnT.SUCCESS;
    }
}