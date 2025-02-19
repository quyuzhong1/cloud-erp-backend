package com.erp.server.wms.schedule;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import com.common.business.wrapper.FeignQuery;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.msg.constant.NoticeMsgConstant;
import com.erp.model.msg.dto.NoticeMsgCardButtonDTO;
import com.erp.model.msg.dto.NoticeMsgInfoDTO;
import com.erp.model.msg.enums.NoticeTypeEnum;
import com.erp.model.sys.dto.CfgNoticeDTO;
import com.erp.model.sys.entity.CfgNoticeDetailEntity;
import com.erp.model.sys.entity.CfgNoticeEntity;
import com.erp.model.wms.enums.CfgVirtualNoticeObjectTypeEnum;
import com.erp.model.wms.enums.CfgVirtualNoticeWeekOptionEnum;
import com.erp.rpc.sys.feign.SysPostFeign;
import com.erp.server.wms.service.CfgSettingService;
import com.erp.server.wms.service.QcEffectivenessService;
import com.erp.server.wms.service.QcInfoService;
import com.erp.server.wms.service.WarehouseLocationReplenishService;
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
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @description: 飞书通知定时任务
 * @author Will
 * @date: 2025/2/12 16:16
 */
@Component
@Slf4j
public class CfgNoticeJob {

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
    @XxlJob("virtualNotice")
    public ReturnT<String> virtualNotice() {
        XxlJobHelper.log("====开始发送飞书通知=====");
        //当前时间
        LocalDateTime now = LocalDateTime.now();
        LocalTime localTime = now.toLocalTime();
        //查询系统配置
        List<CfgNoticeEntity> list = FeignQuery.create(CfgNoticeEntity.class).eq(CfgNoticeEntity::getDisabled, Boolean.FALSE).list();
        if (CollUtil.isEmpty(list)) {
            XxlJobHelper.log("系统配置为空");
            return ReturnT.SUCCESS;
        }
        List<String> idList = list.stream().map(CfgNoticeEntity::getId).distinct().collect(Collectors.toList());
        List<CfgNoticeDetailEntity> detailList = FeignQuery.create(CfgNoticeDetailEntity.class).in(CfgNoticeDetailEntity::getMainId, idList).list();
        if (CollUtil.isEmpty(detailList)) {
            XxlJobHelper.log("系统配置明细为空");
            return ReturnT.SUCCESS;
        }

        list.parallelStream().forEach(obj -> {
            //是否发送通知
            AtomicReference<Boolean> isNotice = new AtomicReference<>(Boolean.FALSE);
            //需要发送通知的用户
            List<String> userIdList = new ArrayList<>();
            //需要发送通知的飞书群
            List<String> fsGroupList = new ArrayList<>();

            List<CfgNoticeDetailEntity> cfgDetailList = detailList.stream().filter(e -> StrUtil.equals(e.getMainId(), obj.getId())).collect(Collectors.toList());
            if (CollUtil.isEmpty(cfgDetailList)) {
                XxlJobHelper.log("系统配置明细为空,配置id:{}", obj.getId());
                return;
            }
            Map<String, List<CfgNoticeDetailEntity>> map = cfgDetailList.stream().collect(Collectors.groupingBy(CfgNoticeDetailEntity::getNoticeType));
            //按天
            List<CfgNoticeDetailEntity> cfgNoticeDayDetailList = map.get(CfgVirtualNoticeObjectTypeEnum.NOTICE_DAY.getCode());
            if (CollUtil.isNotEmpty(cfgNoticeDayDetailList)) {
                cfgNoticeDayDetailList.stream().filter(e -> BeanUtil.toBean(e.getNoticeValueJson(), CfgNoticeDTO.NoticeTimeDTO.class).getTime().format(DateTimeFormatter.ofPattern("HHmm")).equals(localTime.format(DateTimeFormatter.ofPattern("HHmm")))).forEach(e -> {
                    isNotice.set(Boolean.TRUE);
                });
            }
            //按周
            List<CfgNoticeDetailEntity> cfgNoticeWeekDetailList = map.get(CfgVirtualNoticeObjectTypeEnum.NOTICE_WEEK.getCode());
            if (CollUtil.isNotEmpty(cfgNoticeWeekDetailList)) {
                cfgNoticeWeekDetailList.forEach(e -> {
                    CfgNoticeDTO.NoticeTimeDTO noticeTimeDTO = BeanUtil.toBean(e.getNoticeValueJson(), CfgNoticeDTO.NoticeTimeDTO.class);
                    //周选项是否相同
                    boolean equalsWeek = Objects.requireNonNull(CfgVirtualNoticeWeekOptionEnum.getEnum(noticeTimeDTO.getWeekOption())).name().equals(now.getDayOfWeek().name());
                    //时间是否相同
                    boolean equalsTime = noticeTimeDTO.getTime().format(DateTimeFormatter.ofPattern("HHmm")).equals(localTime.format(DateTimeFormatter.ofPattern("HHmm")));
                    //周选项和时间相同则发送通知
                    if (equalsWeek && equalsTime) {
                        isNotice.set(Boolean.TRUE);
                    }
                });
            }
            if (!isNotice.get()) {
                XxlJobHelper.log("当前时间不发送通知");
                return;
            }
            //按人员
            List<CfgNoticeDetailEntity> userDetailList = map.get(CfgVirtualNoticeObjectTypeEnum.NOTICE_USER.getCode());
            if (CollUtil.isNotEmpty(userDetailList)) {
                userDetailList.forEach(e -> {
                    userIdList.addAll(BeanUtil.toBean(e.getNoticeValueJson(), CfgNoticeDTO.NoticeObjectDTO.class).getNoticeObjectList());
                });
            }
            //按飞书群
            List<CfgNoticeDetailEntity> fsGroupDetailList = map.get(CfgVirtualNoticeObjectTypeEnum.NOTICE_USER.getCode());
            if (CollUtil.isNotEmpty(fsGroupDetailList)) {
                fsGroupDetailList.forEach(e -> {
                    fsGroupList.addAll(BeanUtil.toBean(e.getNoticeValueJson(), CfgNoticeDTO.NoticeObjectDTO.class).getNoticeObjectList());
                });
            }

            //发送通知
            sendNotice(obj, userIdList, fsGroupList);
        });
        return ReturnT.SUCCESS;
    }

    /**
     * 发送通知
     * @author will
     * @date 2025/2/19 17:27
     * @param entity
     * @param userIdList
     * @param fsGroupList
     */
    private void sendNotice (CfgNoticeEntity entity,List<String> userIdList,List<String> fsGroupList) {

        //根据异常类型查询异常信息


        NoticeMsgInfoDTO noticeMsgInfoDTO = new NoticeMsgInfoDTO();
        noticeMsgInfoDTO.setReceiverUserIds(Arrays.asList("117"));
        //消息头
        noticeMsgInfoDTO.setTitle("2343");
        //消息体
        String msgContent = CharSequenceUtil.format(NoticeMsgConstant.FS_QC_SETTING_CONTENT,"质检通知", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        noticeMsgInfoDTO.setContent(msgContent);
        noticeMsgInfoDTO.setNoticeTypeEnum(NoticeTypeEnum.WMS_TASK);

        //按钮
        NoticeMsgCardButtonDTO noticeMsgCardButtonDTO = new NoticeMsgCardButtonDTO();
        noticeMsgCardButtonDTO.setName("查看详情");
        noticeMsgCardButtonDTO.setUrl("https://www.baidu.com");
        noticeMsgInfoDTO.setNoticeMsgCardButtonDTO(noticeMsgCardButtonDTO);
        String tagName = RocketMqTagEnum.MSG_NOTICE_TAG.getName();
        SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.NOTICE_MSG_TOPIC, tagName,
                noticeMsgInfoDTO, IdUtil.simpleUUID());
        if (!SendStatus.SEND_OK.equals(result.getSendStatus())) {
            log.error("消息发送结果失败：{}", JSONObject.toJSONString(result));
        }
    }



}