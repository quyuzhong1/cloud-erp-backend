package com.erp.server.mrp.schedule;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.wrapper.FeignQuery;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.mrp.dto.CfgNoticeDTO;
import com.erp.model.mrp.entity.CfgNoticeDetailEntity;
import com.erp.model.mrp.entity.CfgNoticeEntity;
import com.erp.model.mrp.entity.DeliverySuggestEntity;
import com.erp.model.mrp.enums.CfgNoticeNodeEnum;
import com.erp.model.mrp.enums.CfgNoticeObjectTypeEnum;
import com.erp.model.mrp.enums.SuggestStatusEnum;
import com.erp.model.msg.dto.NoticeMsgCardButtonDTO;
import com.erp.model.msg.dto.NoticeMsgInfoDTO;
import com.erp.model.msg.enums.NoticeTypeEnum;
import com.erp.model.oms.entity.DictBasicEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.wms.dto.WmsDeliveryPlanDetailDTO;
import com.erp.model.wms.entity.WmsDeliveryPlanDetailEntity;
import com.erp.model.wms.enums.CfgVirtualNoticeWeekOptionEnum;
import com.erp.model.wms.enums.VitualWarehouseChannelTypeEnum;
import com.erp.server.mrp.service.CfgNoticeDetailService;
import com.erp.server.mrp.service.CfgNoticeService;
import com.erp.server.mrp.service.DeliverySuggestService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.erp.model.mrp.enums.CfgNoticeNodeEnum.CONFIRM;
import static com.erp.model.mrp.enums.CfgNoticeNodeEnum.GENERATE;

@Component
@Slf4j
@EnableScheduling
public class CfgNoticeJob {

    @Resource
    private MQProducerService<NoticeMsgInfoDTO> mqProducerService;

    @Resource
    private CfgNoticeService cfgNoticeService;
    @Resource
    private CfgNoticeDetailService cfgNoticeDetailService;

    @Resource
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Resource
    private DeliverySuggestService deliverySuggestService;

    @XxlJob("mrpNotice")
    public ReturnT<String> mrpNotice() {
        //当前时间
        LocalDateTime now = LocalDateTime.now();
        LocalTime localTime = now.toLocalTime();
        //查询系统配置
        List<CfgNoticeEntity> list = cfgNoticeService.list(Wrappers.<CfgNoticeEntity>lambdaQuery().eq(CfgNoticeEntity::getDisabled, false));
        if (CollUtil.isEmpty(list)) {
            XxlJobHelper.log("系统配置为空");
            return ReturnT.SUCCESS;
        }
        List<String> idList = list.stream().map(CfgNoticeEntity::getId).distinct().collect(Collectors.toList());
        List<CfgNoticeDetailEntity> detailList = cfgNoticeDetailService.listByMainIdList(idList);
        if (CollUtil.isEmpty(detailList)) {
            XxlJobHelper.log("系统配置明细为空");
            return ReturnT.SUCCESS;
        }
        List<ShopInfoEntity> shopInfoList = FeignQuery.list(ShopInfoEntity.class);
        List<DictBasicEntity> salesPlatformList = FeignQuery.create(DictBasicEntity.class)
                .eq(DictBasicEntity::getType, DictBasicTypeEnum.SALES_PLATFORM.getType())
                .eq(DictBasicEntity::getStatus, Boolean.TRUE)
                .eq(DictBasicEntity::getIsDeleted, Boolean.FALSE)
                .list();
        for (CfgNoticeEntity cfgNotice : list) {
            CompletableFuture.runAsync(() -> sendMrpNotice(now, localTime, cfgNotice, detailList, shopInfoList, salesPlatformList), threadPoolTaskExecutor);
        }

        return ReturnT.SUCCESS;
    }

    /**
     * 根据配置发送通知
     *
     * @param now               当前时间
     * @param localTime         时间
     * @param cfgNotice         配置
     * @param detailList        明细配置
     * @param shopInfoList      店铺
     * @param salesPlatformList 平台
     */
    private void sendMrpNotice(LocalDateTime now, LocalTime localTime, CfgNoticeEntity cfgNotice, List<CfgNoticeDetailEntity> detailList, List<ShopInfoEntity> shopInfoList, List<DictBasicEntity> salesPlatformList) {
        //是否发送通知
        AtomicReference<Boolean> isNotice = new AtomicReference<>(Boolean.FALSE);
        //需要发送通知的用户
        List<String> userIdList = new ArrayList<>();

        List<CfgNoticeDetailEntity> cfgDetailList = detailList.stream().filter(e -> CharSequenceUtil.equals(e.getMainId(), cfgNotice.getId())).collect(Collectors.toList());
        if (CollUtil.isEmpty(cfgDetailList)) {
            XxlJobHelper.log("系统配置明细为空,配置id:{}", cfgNotice.getId());
            return;
        }
        Map<String, List<CfgNoticeDetailEntity>> map = cfgDetailList.stream().collect(Collectors.groupingBy(CfgNoticeDetailEntity::getNoticeType));
        //按天
        List<CfgNoticeDetailEntity> cfgNoticeDayDetailList = map.get(CfgNoticeObjectTypeEnum.NOTICE_DAY.getCode());
        if (CollUtil.isNotEmpty(cfgNoticeDayDetailList)) {
            cfgNoticeDayDetailList.stream()
                    .filter(e -> BeanUtil.toBean(e.getNoticeValueJson(), CfgNoticeDTO.NoticeTimeDTO.class).getTime().format(DateTimeFormatter.ofPattern("HHmm")).equals(localTime.format(DateTimeFormatter.ofPattern("HHmm"))))
                    .forEach(e -> isNotice.set(Boolean.TRUE));
        }
        //按周
        List<CfgNoticeDetailEntity> cfgNoticeWeekDetailList = map.get(CfgNoticeObjectTypeEnum.NOTICE_WEEK.getCode());
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
        if (Boolean.FALSE.equals(isNotice.get())) {
            XxlJobHelper.log("当前时间不发送通知");
            return;
        }
        //按人员
        List<CfgNoticeDetailEntity> userDetailList = map.get(CfgNoticeObjectTypeEnum.NOTICE_USER.getCode());
        if (CollUtil.isNotEmpty(userDetailList)) {
            userDetailList.forEach(e -> userIdList.addAll(BeanUtil.toBean(e.getNoticeValueJson(), CfgNoticeDTO.NoticeObjectDTO.class).getNoticeObjectList()));
        }
        CfgNoticeDTO.NoticeRuleDTO noticeRuleDTO = JSONUtil.toBean(cfgNotice.getNoticeRule(), CfgNoticeDTO.NoticeRuleDTO.class);
        if (VitualWarehouseChannelTypeEnum.SHOP.getCode().equals(noticeRuleDTO.getRuleType())) {
            List<String> chargeId = shopInfoList.stream()
                    .filter(v -> noticeRuleDTO.getChannelId().contains(v.getId()))
                    .map(ShopInfoEntity::getChargeId)
                    .distinct()
                    .collect(Collectors.toList());
            userIdList.addAll(chargeId);
        }
        getNoticeContent(cfgNotice.getNoticeNode(), noticeRuleDTO, userIdList, shopInfoList, salesPlatformList);
    }

    private void getNoticeContent(String noticeNode, CfgNoticeDTO.NoticeRuleDTO noticeRuleDTO, List<String> userIdList,
                                  List<ShopInfoEntity> shopInfoList, List<DictBasicEntity> salesPlatformList) {
        CfgNoticeNodeEnum noticeNodeEnum = CfgNoticeNodeEnum.getEnum(noticeNode);
        String title = null;
        String content = null;
        switch (Objects.requireNonNull(noticeNodeEnum)) {
            case CONFIRM:
                title = "发货建议已完成确认，请尽快下推发货计划";
                content = getContent(noticeRuleDTO, shopInfoList, salesPlatformList, CONFIRM);
                break;
            case GENERATE:
                title = "发货建议已生成，请尽快确认";
                content = getContent(noticeRuleDTO, shopInfoList, salesPlatformList, GENERATE);
                break;
        }
        if (CollectionUtils.isEmpty(userIdList)) {
            XxlJobHelper.log("未找到通知人员");
            return;
        }
        if (StringUtils.isEmpty(content)) {
            XxlJobHelper.log("无通知内容");
            return;
        }
        //按人员发送
        NoticeMsgInfoDTO noticeMsgInfoDTO = getNoticeMsgInfoDTO(userIdList, title, content);
        String tagName = RocketMqTagEnum.MSG_NOTICE_TAG.getName();
        SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.NOTICE_MSG_TOPIC, tagName,
                noticeMsgInfoDTO, IdUtil.simpleUUID());
        if (!SendStatus.SEND_OK.equals(result.getSendStatus())) {
            log.error("消息发送结果失败：{}", JSONObject.toJSONString(result));
        }
    }

    private String getContent(CfgNoticeDTO.NoticeRuleDTO noticeRuleDTO, List<ShopInfoEntity> shopInfoList, List<DictBasicEntity> salesPlatformList, CfgNoticeNodeEnum noticeNodeEnum) {
        StringBuilder sb = new StringBuilder();
        sb.append("平台：");
        List<String> shopIdList;
        if (VitualWarehouseChannelTypeEnum.PLATFORM.getCode().equals(noticeRuleDTO.getRuleType())) {
            if (CollectionUtils.isEmpty(noticeRuleDTO.getChannelId()) || "".equals(noticeRuleDTO.getChannelId().get(0))) {
                sb.append("全部平台\n");
                shopIdList = shopInfoList.stream().map(ShopInfoEntity::getId).collect(Collectors.toList());
            } else {
                String platformName = salesPlatformList.stream()
                        .filter(v -> noticeRuleDTO.getChannelId().contains(v.getValue()))
                        .map(DictBasicEntity::getName)
                        .collect(Collectors.joining(","));
                sb.append(platformName)
                        .append("平台\n");
                shopIdList = shopInfoList.stream().filter(v -> noticeRuleDTO.getChannelId().contains(v.getDictPlatform()))
                        .map(ShopInfoEntity::getId).collect(Collectors.toList());
            }
            sb.append("店铺：--\n");
        } else {
            if (CollectionUtils.isEmpty(noticeRuleDTO.getChannelId()) || "".equals(noticeRuleDTO.getChannelId().get(0))) {
                sb.append("全部平台\n");
                sb.append("店铺：全部店铺\n");
                shopIdList = shopInfoList.stream().map(ShopInfoEntity::getId).collect(Collectors.toList());
            } else {
                List<ShopInfoEntity> shopInfoEntityList = shopInfoList.stream()
                        .filter(v -> noticeRuleDTO.getChannelId().contains(v.getId()))
                        .collect(Collectors.toList());
                List<String> platformList = shopInfoEntityList.stream()
                        .map(ShopInfoEntity::getDictPlatform)
                        .distinct()
                        .collect(Collectors.toList());
                String platformName = salesPlatformList.stream()
                        .filter(v -> platformList.contains(v.getValue()))
                        .map(DictBasicEntity::getName)
                        .collect(Collectors.joining(","));
                List<String> shopName = shopInfoEntityList.stream().map(ShopInfoEntity::getName)
                        .collect(Collectors.toList());
                sb.append(platformName)
                        .append("平台\n");
                shopIdList = noticeRuleDTO.getChannelId();
                sb.append("店铺：");
                sb.append(shopName);
                sb.append("\n");
            }
        }
        sb.append("发货建议：");
        List<DeliverySuggestEntity> list = deliverySuggestService.list(Wrappers.<DeliverySuggestEntity>lambdaQuery().in(DeliverySuggestEntity::getShopId, shopIdList));
        if (CONFIRM.getCode().equals(noticeNodeEnum.getCode())) {

            List<WmsDeliveryPlanDetailEntity> deliveryPlanDetailList = FeignQuery.list(WmsDeliveryPlanDetailEntity.class);
            List<String> ids = deliveryPlanDetailList.stream()
                    .map(WmsDeliveryPlanDetailEntity::getSourceJson)
                    .map(v -> BeanUtil.copyToList(JSONUtil.parseArray(v), WmsDeliveryPlanDetailDTO.SourceJsonDTO.class))
                    .flatMap(v -> v.stream().map(WmsDeliveryPlanDetailDTO.SourceJsonDTO::getSourceId))
                    .collect(Collectors.toList());
            List<String> codes = list.stream()
                    .filter(v -> Boolean.FALSE.equals(v.getInvalidStatus()))
                    .filter(v -> SuggestStatusEnum.FINISH.getCode().equals(v.getStatus()))
                    .filter(v -> !ids.contains(v.getId()))
                    .map(DeliverySuggestEntity::getCode)
                    .collect(Collectors.toList());
            sb.append(codes.size())
                    .append("条\n");
            for (String code : codes.subList(0, Math.min(100, codes.size()))) {
                sb.append(code)
                        .append("\n");
            }
        } else {
            List<String> codes = list.stream()
                    .filter(v -> Boolean.FALSE.equals(v.getInvalidStatus()))
                    .filter(v -> SuggestStatusEnum.DRAFT.getCode().equals(v.getStatus()))
                    .map(DeliverySuggestEntity::getCode)
                    .collect(Collectors.toList());
            sb.append(codes.size())
                    .append("条\n");
            for (String code : codes.subList(0, Math.min(100, codes.size()))) {
                sb.append(code)
                        .append("\n");
            }
        }
        return sb.toString();
    }


    private NoticeMsgInfoDTO getNoticeMsgInfoDTO(List<String> userIdList, String title, String content) {
        NoticeMsgInfoDTO noticeMsgInfoDTO = new NoticeMsgInfoDTO();
        noticeMsgInfoDTO.setReceiverUserIds(userIdList);
        //消息头
        noticeMsgInfoDTO.setTitle(title);
        noticeMsgInfoDTO.setContent(content);
        noticeMsgInfoDTO.setNoticeTypeEnum(NoticeTypeEnum.MRP_TASK);

        //按钮
        NoticeMsgCardButtonDTO noticeMsgCardButtonDTO = new NoticeMsgCardButtonDTO();
        noticeMsgCardButtonDTO.setName("查看详情");
        noticeMsgCardButtonDTO.setUrl("https://erp.ulanzi.cn:8060/");
        noticeMsgInfoDTO.setNoticeMsgCardButtonDTO(noticeMsgCardButtonDTO);
        return noticeMsgInfoDTO;
    }

}
