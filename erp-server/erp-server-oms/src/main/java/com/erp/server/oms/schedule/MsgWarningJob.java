package com.erp.server.oms.schedule;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.msg.dto.NoticeMsgInfoDTO;
import com.erp.model.msg.enums.NoticeTypeEnum;
import com.erp.model.oms.dto.CfgSettingDTO;
import com.erp.model.oms.dto.FullyManagedDTO;
import com.erp.model.oms.entity.CfgSettingEntity;
import com.erp.model.oms.enums.CfgSettingEnum;
import com.erp.server.oms.service.CfgSettingService;
import com.erp.server.oms.service.SoB2cExtendService;
import com.erp.server.oms.service.SoB2cService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * @author zdy
 * @ClassName MsgWarningJob
 * @description: 消息预警任务
 * @date 2025年03月26日
 * @version: 1.0
 */
@Component
@Slf4j
public class MsgWarningJob {
    @Resource
    private CfgSettingService cfgSettingService;
    @Resource
    private SoB2cExtendService soB2cExtendService;
    @Resource
    private MQProducerService mqProducerService;
    /**
     * 全托管预警信息推送
     */
    @XxlJob("fullyManagedOrderMsgWarning")
    public void fullyManagedOrderMsgWarning() {
        XxlJobHelper.log("全托管预警信息推送开始执行");
        String jobParam = XxlJobHelper.getJobParam();
        //获取预警配置
        CfgSettingEntity setting = cfgSettingService.getSettingByKey(CfgSettingEnum.TIME_OUT_CONFIG.getCode());
        if (Objects.isNull(setting)) {
            XxlJobHelper.log("全托管预警信息推送执行完成 没有配置预警配置");
            return; // 没有配置则不执行
        }
        CfgSettingDTO.TimeOutSettingDTO timeOutSettingDTO = BeanUtil.toBean(setting.getValue(), CfgSettingDTO.TimeOutSettingDTO.class);
        if (Objects.isNull(timeOutSettingDTO) || Objects.isNull(timeOutSettingDTO.getWarningTime()) || CollUtil.isEmpty(timeOutSettingDTO.getUserIdList())) {
            XxlJobHelper.log("全托管预警信息推送执行完成 没有配置预警时间或预警人");
            return; // 没有配置则不执行
        }
        BigDecimal warningTime = timeOutSettingDTO.getWarningTime();
        //获取预警时间在两个小时内的全托管超时订单
        List<FullyManagedDTO.WarningDTO> warningDTOList = soB2cExtendService.fullyManagedOrderMsgWarning(120);
        if (CollUtil.isEmpty(warningDTOList)) {
            XxlJobHelper.log("全托管预警信息推送执行完成 没有需要预警的订单");
            return; // 没有配置则不执行
        }
        //发送预警信息
        for (FullyManagedDTO.WarningDTO warningDTO : warningDTOList) {
            //发送预警信息
            NoticeMsgInfoDTO msgInfoDTO = new NoticeMsgInfoDTO();
            msgInfoDTO.setReceiverUserIds(timeOutSettingDTO.getUserIdList());
            msgInfoDTO.setContent("订单单号将于【" + warningTime + "】小时后超时,请及时发货。" + "</br>" + "通知单号：【" + warningDTO.getCode() + "/" + warningDTO.getPlatformCode() + "】");
            msgInfoDTO.setTitle("全托管订单发货提醒");
            msgInfoDTO.setDelayTime(warningDTO.getDeliveryWarningTime());
            msgInfoDTO.setNoticeTypeEnum(NoticeTypeEnum.OMS_TASK);
            mqProducerService.sendNoticeMsg(msgInfoDTO);
        }
        XxlJobHelper.log("全托管预警信息推送执行完成");
    }

}
