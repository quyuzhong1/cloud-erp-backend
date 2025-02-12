package com.erp.server.wms.schedule;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.JSONObject;
import com.common.core.utils.MathUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.msg.constant.NoticeMsgConstant;
import com.erp.model.msg.dto.NoticeMsgInfoDTO;
import com.erp.model.msg.enums.NoticeTypeEnum;
import com.erp.model.sys.entity.SysPostUserEntity;
import com.erp.model.wms.dto.CfgSettingValueDTO;
import com.erp.model.wms.dto.QcEffectivenessDTO;
import com.erp.model.wms.entity.CfgSettingEntity;
import com.erp.model.wms.enums.CfgSettingEnum;
import com.erp.model.wms.enums.QcBillStatusEnum;
import com.erp.rpc.sys.feign.SysPostFeign;
import com.erp.server.wms.service.CfgSettingService;
import com.erp.server.wms.service.QcEffectivenessService;
import com.erp.server.wms.service.QcInfoService;
import com.erp.server.wms.service.WarehouseLocationReplenishService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import io.seata.common.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @description: 飞书通知定时任务
 * @author Will
 * @date: 2025/2/12 16:16
 */
@Component
@Slf4j
public class CfgFsNoticeJob {

    @Resource
    private CfgSettingService cfgSettingService;

    @Resource
    private SysPostFeign sysPostFeign;

    @Resource
    private MQProducerService<NoticeMsgInfoDTO> mqProducerService;

    @Resource
    private QcEffectivenessService qcEffectivenessService;

    @Resource
    private QcInfoService qcInfoService;
    @Resource
    private WarehouseLocationReplenishService warehouseLocationReplenishService;

    /**
     * 飞书通知定时任务
     * @author Will
     * @date: 2025/2/12 16:16
     * @return ReturnT<String>
     */
    @XxlJob("fsQcNotice")
    public ReturnT<String> fsQcNotice() {
        XxlJobHelper.log("====开始发送飞书通知=====");
        //查询系统配置


        NoticeMsgInfoDTO noticeMsgInfoDTO = new NoticeMsgInfoDTO();
        noticeMsgInfoDTO.setReceiverUserIds(noticeUserIdList);
        //消息头
        noticeMsgInfoDTO.setTitle(title);
        //消息体
        String msgContent = CharSequenceUtil.format(NoticeMsgConstant.FS_QC_SETTING_CONTENT,"质检通知", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        noticeMsgInfoDTO.setContent(msgContent);
        noticeMsgInfoDTO.setNoticeTypeEnum(NoticeTypeEnum.WMS_TASK);
        SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.NOTICE_MSG_TOPIC, tagName,
                noticeMsgInfoDTO, IdUtil.simpleUUID());
        if (!SendStatus.SEND_OK.equals(result.getSendStatus())) {
            log.error("消息发送结果失败：{}", JSONObject.toJSONString(result));
        }
        return ReturnT.SUCCESS;
    }
}