package com.erp.server.wms.schedule;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import com.common.business.wrapper.FeignQuery;
import com.common.core.utils.MathUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.msg.constant.NoticeMsgConstant;
import com.erp.model.msg.dto.NoticeMsgInfoDTO;
import com.erp.model.msg.enums.NoticeTypeEnum;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.sys.entity.SysPostEntity;
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
 * @description: 店铺过期通知
 */
@Component
@Slf4j
public class CfgShopExpireNoticeJob {

    @Resource
    private CfgSettingService cfgSettingService;

    @Resource
    private SysPostFeign sysPostFeign;

    @Resource
    private MQProducerService<NoticeMsgInfoDTO> mqProducerService;

    /**
     * 店铺过期通知
     */
    @XxlJob("CfgShopExpireNoticeJob")
    public ReturnT<String> cfgShopExpireNoticeJob() {
        XxlJobHelper.log("====开始店铺过期通知通知=====");
        //查询系统配置
        CfgSettingEntity cfgSettingEntity = cfgSettingService.getByKey(CfgSettingEnum.SHOP_EXPIRE_NOTICE.getCode());
        if (ObjectUtil.isEmpty(cfgSettingEntity) || ObjectUtil.isEmpty(cfgSettingEntity.getDataJson())) {
            XxlJobHelper.log("未设置店铺过期通知配置，无需发送通知");
            return ReturnT.SUCCESS;
        }
        CfgSettingValueDTO.ShopAuthExpireNoticeDTO dto = BeanUtil.toBean(cfgSettingEntity.getDataJson(), CfgSettingValueDTO.ShopAuthExpireNoticeDTO.class);
        if (ObjectUtil.isEmpty(dto.getSendTime())) {
            XxlJobHelper.log("未设置发送时间，无需发送通知");
            return ReturnT.SUCCESS;
        }
        if (!(dto.getSendTime().getHour() == LocalTime.now().getHour() && dto.getSendTime().getMinute() == LocalTime.now().getMinute())) {
            XxlJobHelper.log("未到设置发送时间，无需发送通知");
            return ReturnT.SUCCESS;
        }
        if(CollectionUtils.isEmpty(dto.getRoleIdList())){
            XxlJobHelper.log("未设置角色id，无需发送通知");
            return ReturnT.SUCCESS;
        }
        List<String> noticeUserIdList = dto.getUserIdList();
        List<SysPostEntity> sysPostEntities = sysPostFeign.listById(dto.getRoleIdList());
        List<String> postNameList = sysPostEntities.stream().map(SysPostEntity::getPostName).distinct().collect(Collectors.toList());
        if(CollectionUtils.isEmpty(postNameList) && CollectionUtils.isEmpty(noticeUserIdList)){
            XxlJobHelper.log("未找到通知人员，无需发送通知");
            return ReturnT.SUCCESS;
        }
        //查询授权过期时间小于当前时间加7天，已授权的店铺
        LocalDate datePlusSeven = LocalDate.now().plusDays(7);
        List<ShopInfoEntity> shopInfoEntityList = FeignQuery.create(ShopInfoEntity.class)
                .eq(ShopInfoEntity::getAuthStatus, AuthStatusEnum.ALREADY.getCode())
                .le(ShopInfoEntity::getAuthExpireDate, datePlusSeven)
                .list();
        if(CollectionUtils.isEmpty(shopInfoEntityList)) {
            XxlJobHelper.log("未找到授权过期的店铺，无需发送通知");
            return ReturnT.SUCCESS;
        }
        for (ShopInfoEntity shopInfoEntity : shopInfoEntityList) {
            NoticeMsgInfoDTO noticeMsgInfoDTO = new NoticeMsgInfoDTO();
            List<String> currentNoticeUserIdList = new ArrayList<>();
            if(CollectionUtils.isNotEmpty(noticeUserIdList)){
                currentNoticeUserIdList.addAll(noticeUserIdList);
            }
            if(postNameList.contains("创建人")){
                currentNoticeUserIdList.add(shopInfoEntity.getCreateUserId());
            }
            if(postNameList.contains("负责人")){
                currentNoticeUserIdList.add(shopInfoEntity.getChargeId());
            }
            noticeMsgInfoDTO.setReceiverUserIds(currentNoticeUserIdList);
            String tagName = RocketMqTagEnum.MSG_NOTICE_TAG.getName();
            //消息头
            String title = "店铺到期预警";
            noticeMsgInfoDTO.setTitle(title);
            //消息体
            String msgContent = CharSequenceUtil.format(NoticeMsgConstant.SHOP_EXPIRE_NOTICE,shopInfoEntity.getName(),shopInfoEntity.getAuthExpireDate());
            noticeMsgInfoDTO.setContent(msgContent);
            noticeMsgInfoDTO.setNoticeTypeEnum(NoticeTypeEnum.OMS_TASK);
            SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.NOTICE_MSG_TOPIC, tagName,
                    noticeMsgInfoDTO, IdUtil.simpleUUID());
            if (!SendStatus.SEND_OK.equals(result.getSendStatus())) {
                log.error("消息发送结果失败：{}", JSONObject.toJSONString(result));
            }
        }
        return ReturnT.SUCCESS;
    }
}