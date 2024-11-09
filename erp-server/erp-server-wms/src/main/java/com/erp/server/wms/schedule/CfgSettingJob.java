package com.erp.server.wms.schedule;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
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
import com.erp.model.wms.dto.WarehouseLocationReplenishDTO;
import com.erp.model.wms.entity.CfgSettingEntity;
import com.erp.model.wms.enums.CfgSettingEnum;
import com.erp.model.wms.enums.QcBillStatusEnum;
import com.erp.model.wms.enums.ReplenishBillStatusEnum;
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
 * @description: 系统配置定时任务
 * @author Will
 * @date: 2024/4/10 16:16
 */
@Component
@Slf4j
public class CfgSettingJob {

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
     * 质检超时飞书通知
     * @author Will
     * @date: 2024/4/10 16:19
     * @return ReturnT<String>
     */
    @XxlJob("fsQcNotice")
    public ReturnT<String> fsQcNotice() {
        XxlJobHelper.log("====开始发送质检飞书通知=====");
        //查询系统配置
        CfgSettingEntity cfgSettingEntity = cfgSettingService.getByKey(CfgSettingEnum.FS_QC_NOTICE.getCode());
        if (ObjectUtil.isEmpty(cfgSettingEntity) || ObjectUtil.isEmpty(cfgSettingEntity.getDataJson())) {
            XxlJobHelper.log("未设置质检飞书通知配置，无需发送通知");
            return ReturnT.SUCCESS;
        }
        CfgSettingValueDTO.FsQcNoticeDTO dto = BeanUtil.toBean(cfgSettingEntity.getDataJson(), CfgSettingValueDTO.FsQcNoticeDTO.class);
        if (ObjectUtil.isEmpty(dto.getSendTime())) {
            XxlJobHelper.log("未设置发送时间，无需发送通知");
            return ReturnT.SUCCESS;
        }
        if (!(dto.getSendTime().getHour() == LocalTime.now().getHour() && dto.getSendTime().getMinute() == LocalTime.now().getMinute())) {
            XxlJobHelper.log("未到设置发送时间，无需发送通知");
            return ReturnT.SUCCESS;
        }

        List<String> noticeUserIdList = new ArrayList<>();
        //岗位处理
        if (CollectionUtils.isNotEmpty(dto.getPostIdList())) {
            List<SysPostUserEntity> sysPostList = sysPostFeign.listPostUserByPostIdList(dto.getPostIdList());
            //岗位下用户
            List<String> postUserIdList = sysPostList.stream().map(SysPostUserEntity::getUserId).distinct().collect(Collectors.toList());
            noticeUserIdList.addAll(postUserIdList);
        }
        //抄送人员
        if (CollectionUtils.isNotEmpty(dto.getUserIdList())) {
            noticeUserIdList.addAll(dto.getUserIdList());
            noticeUserIdList = noticeUserIdList.stream().distinct().collect(Collectors.toList());
        }
        if (CollectionUtils.isEmpty(noticeUserIdList)) {
            XxlJobHelper.log("未找到通知人员，无需发送通知");
            return ReturnT.SUCCESS;
        }
        NoticeMsgInfoDTO noticeMsgInfoDTO = new NoticeMsgInfoDTO();
        noticeMsgInfoDTO.setReceiverUserIds(noticeUserIdList);
        String tagName = RocketMqTagEnum.MSG_NOTICE_TAG.getName();

        QcEffectivenessDTO.CommonSearchParamDTO paramDTO = new  QcEffectivenessDTO.CommonSearchParamDTO();
        LocalDate now = LocalDate.now();
        paramDTO.setDateList(Arrays.asList(now, now));
        List<QcEffectivenessDTO.ViewQcOverviewDetailDTO> list = qcEffectivenessService.listQcBillGroupQcStatus(paramDTO);
        //质检单总计
        Integer totalCount = list.stream().map(QcEffectivenessDTO.ViewQcOverviewDetailDTO::getCount).reduce(MathUtil.ZERO,Integer::sum);
        //已质检数量
        Integer hasQcCount = list.stream().filter(obj -> CharSequenceUtil.equals(obj.getType(), QcBillStatusEnum.FINISH_QC.getCode())
                || CharSequenceUtil.equals(obj.getType(), QcBillStatusEnum.EXEMPTION.getCode()))
                .map(QcEffectivenessDTO.ViewQcOverviewDetailDTO::getCount)
                .reduce(MathUtil.ZERO,Integer::sum);
        //未质检数量
        Integer notQcCount = list.stream().filter(obj -> CharSequenceUtil.equals(obj.getType(), QcBillStatusEnum.DRAFT.getCode())
                ||  CharSequenceUtil.equals(obj.getType(), QcBillStatusEnum.WAIT_QC.getCode())
                ||  CharSequenceUtil.equals(obj.getType(), QcBillStatusEnum.WAIT_RE_QC.getCode()))
                .map(QcEffectivenessDTO.ViewQcOverviewDetailDTO::getCount)
                .reduce(MathUtil.ZERO,Integer::sum);
        //累计未质检
        QcEffectivenessDTO.CountQcParamDTO qcParamDTO = new QcEffectivenessDTO.CountQcParamDTO();
        qcParamDTO.setQcStatusList(Arrays.asList(QcBillStatusEnum.WAIT_QC.getCode(),QcBillStatusEnum.DRAFT.getCode(),QcBillStatusEnum.WAIT_RE_QC.getCode()));
        Integer notQcTotalCount = qcInfoService.countTotalNotQc(qcParamDTO);

        //超时未质检
        qcParamDTO.setIsTimeOut(Boolean.TRUE);
        Integer timeOutTotalCount = qcInfoService.countTotalNotQc(qcParamDTO);

        //已质检比例
        String rate = MathUtil.divide(new BigDecimal(hasQcCount), new BigDecimal(totalCount)).multiply(MathUtil.BigDecimal_100).stripTrailingZeros().toPlainString() + "%";

        //消息头
        String title = CharSequenceUtil.format(NoticeMsgConstant.FS_QC_SETTING_HEAD,totalCount,hasQcCount,rate,notQcCount,notQcTotalCount,timeOutTotalCount);
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


    /**
     * 仓位补货定时飞书提醒
     * @author Will
     * @date: 2024/4/10 16:19
     * @return ReturnT<String>
     */
    @XxlJob("fsWlrNotice")
    public ReturnT<String> fsWlrNotice() {
        XxlJobHelper.log("====开始发送仓位补货飞书通知=====");
        //查询系统配置
        CfgSettingEntity cfgSettingEntity = cfgSettingService.getByKey(CfgSettingEnum.FS_WAREHOUSE_LOCATION_REPLENISH_NOTICE.getCode());
        if (ObjectUtil.isEmpty(cfgSettingEntity) || ObjectUtil.isEmpty(cfgSettingEntity.getDataJson())) {
            XxlJobHelper.log("未设置仓位补货飞书通知配置，无需发送通知");
            return ReturnT.SUCCESS;
        }
        CfgSettingValueDTO.FsQcNoticeDTO dto = BeanUtil.toBean(cfgSettingEntity.getDataJson(), CfgSettingValueDTO.FsQcNoticeDTO.class);
        if (ObjectUtil.isEmpty(dto.getSendTime())) {
            XxlJobHelper.log("未设置发送时间，无需发送通知");
            return ReturnT.SUCCESS;
        }
        if (!(dto.getSendTime().getHour() == LocalTime.now().getHour() && dto.getSendTime().getMinute() == LocalTime.now().getMinute())) {
            XxlJobHelper.log("未到设置发送时间，无需发送通知");
            return ReturnT.SUCCESS;
        }

        List<String> noticeUserIdList = new ArrayList<>();
        //岗位处理
        if (CollectionUtils.isNotEmpty(dto.getPostIdList())) {
            List<SysPostUserEntity> sysPostList = sysPostFeign.listPostUserByPostIdList(dto.getPostIdList());
            //岗位下用户
            List<String> postUserIdList = sysPostList.stream().map(SysPostUserEntity::getUserId).distinct().collect(Collectors.toList());
            noticeUserIdList.addAll(postUserIdList);
        }
        //抄送人员
        if (CollectionUtils.isNotEmpty(dto.getUserIdList())) {
            noticeUserIdList.addAll(dto.getUserIdList());
            noticeUserIdList = noticeUserIdList.stream().distinct().collect(Collectors.toList());
        }
        if (CollectionUtils.isEmpty(noticeUserIdList)) {
            XxlJobHelper.log("未找到通知人员，无需发送通知");
            return ReturnT.SUCCESS;
        }
        NoticeMsgInfoDTO noticeMsgInfoDTO = new NoticeMsgInfoDTO();
        noticeMsgInfoDTO.setReceiverUserIds(noticeUserIdList);
        String tagName = RocketMqTagEnum.MSG_NOTICE_TAG.getName();

        //统计仓位补货的各个类型的数量
        String waitHandle = "0";
        String handleIng = "0";
        List<WarehouseLocationReplenishDTO.TabDTO> tabList = warehouseLocationReplenishService.listTabInfo();
        for (WarehouseLocationReplenishDTO.TabDTO tab : tabList) {
            String tabFlag = tab.getTabFlag();
            if(tabFlag.equals(ReplenishBillStatusEnum.WAIT_HANDLE.getCode())){
                waitHandle = tab.getCount()+"";
            }
            if(tabFlag.equals(ReplenishBillStatusEnum.HANDLE_ING.getCode())){
                handleIng = tab.getCount()+"";
            }
        }
        //消息头
        String title = NoticeMsgConstant.FS_FINISH_PACKING_HEAD;
        noticeMsgInfoDTO.setTitle(title);
        //消息体
        String msgContent = StrUtil.format(NoticeMsgConstant.FS_WLR_SETTING_CONTENT,waitHandle,handleIng, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        noticeMsgInfoDTO.setContent(msgContent);
        noticeMsgInfoDTO.setNoticeTypeEnum(NoticeTypeEnum.WMS_TASK);
        SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.NOTICE_MSG_TOPIC, tagName,
                noticeMsgInfoDTO, IdUtil.simpleUUID());
        if (!SendStatus.SEND_OK.equals(result.getSendStatus())) {
            log.error("消息发送结果失败：{}", JSONObject.toJSONString(result));
        }
        return ReturnT.SUCCESS;
    }


    /**
     * 仓位补货定时飞书提醒
     * @author Will
     * @date: 2024/4/10 16:19
     * @return ReturnT<String>
     */
    @XxlJob("fsWlrNotice")
    public ReturnT<String> fsWlrNotice() {
        XxlJobHelper.log("====开始发送仓位补货飞书通知=====");
        //查询系统配置
        CfgSettingEntity cfgSettingEntity = cfgSettingService.getByKey(CfgSettingEnum.FS_WAREHOUSE_LOCATION_REPLENISH_NOTICE.getCode());
        if (ObjectUtil.isEmpty(cfgSettingEntity) || ObjectUtil.isEmpty(cfgSettingEntity.getDataJson())) {
            XxlJobHelper.log("未设置仓位补货飞书通知配置，无需发送通知");
            return ReturnT.SUCCESS;
        }
        CfgSettingValueDTO.FsQcNoticeDTO dto = BeanUtil.toBean(cfgSettingEntity.getDataJson(), CfgSettingValueDTO.FsQcNoticeDTO.class);
        if (ObjectUtil.isEmpty(dto.getSendTime())) {
            XxlJobHelper.log("未设置发送时间，无需发送通知");
            return ReturnT.SUCCESS;
        }
        if (!(dto.getSendTime().getHour() == LocalTime.now().getHour() && dto.getSendTime().getMinute() == LocalTime.now().getMinute())) {
            XxlJobHelper.log("未到设置发送时间，无需发送通知");
            return ReturnT.SUCCESS;
        }

        List<String> noticeUserIdList = new ArrayList<>();
        //岗位处理
        if (CollectionUtils.isNotEmpty(dto.getPostIdList())) {
            List<SysPostUserEntity> sysPostList = sysPostFeign.listPostUserByPostIdList(dto.getPostIdList());
            //岗位下用户
            List<String> postUserIdList = sysPostList.stream().map(SysPostUserEntity::getUserId).distinct().collect(Collectors.toList());
            noticeUserIdList.addAll(postUserIdList);
        }
        //抄送人员
        if (CollectionUtils.isNotEmpty(dto.getUserIdList())) {
            noticeUserIdList.addAll(dto.getUserIdList());
            noticeUserIdList = noticeUserIdList.stream().distinct().collect(Collectors.toList());
        }
        if (CollectionUtils.isEmpty(noticeUserIdList)) {
            XxlJobHelper.log("未找到通知人员，无需发送通知");
            return ReturnT.SUCCESS;
        }
        NoticeMsgInfoDTO noticeMsgInfoDTO = new NoticeMsgInfoDTO();
        noticeMsgInfoDTO.setReceiverUserIds(noticeUserIdList);
        String tagName = RocketMqTagEnum.MSG_NOTICE_TAG.getName();

        //统计仓位补货的各个类型的数量
        String waitHandle = "0";
        String handleIng = "0";
        List<WarehouseLocationReplenishDTO.TabDTO> tabList = warehouseLocationReplenishService.listTabInfo();
        for (WarehouseLocationReplenishDTO.TabDTO tab : tabList) {
            String tabFlag = tab.getTabFlag();
            if(tabFlag.equals(ReplenishBillStatusEnum.WAIT_HANDLE.getCode())){
                waitHandle = tab.getCount()+"";
            }
            if(tabFlag.equals(ReplenishBillStatusEnum.HANDLE_ING.getCode())){
                handleIng = tab.getCount()+"";
            }
        }
        //消息头
        String title = NoticeMsgConstant.FS_FINISH_PACKING_HEAD;
        noticeMsgInfoDTO.setTitle(title);
        //消息体
        String msgContent = StrUtil.format(NoticeMsgConstant.FS_WLR_SETTING_CONTENT,waitHandle,handleIng, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
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