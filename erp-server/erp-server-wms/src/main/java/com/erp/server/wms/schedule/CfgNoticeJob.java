package com.erp.server.wms.schedule;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.common.business.enums.ErpServerModuleEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.entity.DmpBomEntity;
import com.erp.model.dmp.mabang.ComboSkuInfoEntity;
import com.erp.model.msg.dto.NoticeMsgCardButtonDTO;
import com.erp.model.msg.dto.NoticeMsgInfoDTO;
import com.erp.model.msg.dto.WarnMsgInfoDTO;
import com.erp.model.msg.enums.NoticeTypeEnum;
import com.erp.model.msg.enums.WarnMsgTypeEnum;
import com.erp.model.sys.dto.CfgNoticeDTO;
import com.erp.model.sys.entity.CfgNoticeDetailEntity;
import com.erp.model.sys.entity.CfgNoticeEntity;
import com.erp.model.wms.dto.VirtualInventoryAgeDTO;
import com.erp.model.wms.dto.VirtualInventoryDiffDTO;
import com.erp.model.wms.dto.VirtualTransFlowDetailDTO;
import com.erp.model.wms.enums.CfgVirtualNoticeNodeTypeEnum;
import com.erp.model.wms.enums.CfgVirtualNoticeObjectTypeEnum;
import com.erp.model.wms.enums.CfgVirtualNoticeWeekOptionEnum;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    private MQProducerService<NoticeMsgInfoDTO> mqProducerService;

    @Resource
    private VirtualInventoryDiffService virtualInventoryDiffService;

    @Resource
    private VirtualInventoryDetailService virtualInventoryDetailService;


    @Resource
    private VirtualTransFlowDetailService virtualTransFlowDetailService;
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
        String noticeNode = entity.getNoticeNode();
        String title = "";
        String content = "";

        switch (Objects.requireNonNull(CfgVirtualNoticeNodeTypeEnum.getEnum(noticeNode))) {
            case INVENTORY_DIFF:
                content = getNoticeContentByDistribution(entity.getNoticeRule());
                title = "库存分配差异通知";
                break;
            case INVENTORY_DETAIL_DIFF:
                content = getNoticeContentByInventoryAge(entity.getNoticeRule());
                title = "库龄差异通知";
                break;
            case FROZEN_INVENTORY_DIFF:
                content = getNoticeContentByBill(entity.getNoticeRule());
                title = "冻结库存差异通知";
                break;
            default:
                break;
        }

        //按人员发送
        NoticeMsgInfoDTO noticeMsgInfoDTO = getNoticeMsgInfoDTO(userIdList, title, content);
        String tagName = RocketMqTagEnum.MSG_NOTICE_TAG.getName();
        SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.NOTICE_MSG_TOPIC, tagName,
                noticeMsgInfoDTO, IdUtil.simpleUUID());
        if (!SendStatus.SEND_OK.equals(result.getSendStatus())) {
            log.error("消息发送结果失败：{}", JSONObject.toJSONString(result));
        }

        //按群发送
        mqProducerService.sendWarnMsg(warnMsgInfo);
    }

    /**
     * 数据组装
     * @author will
     * @date 2025/2/19 19:44
     * @param userIdList
     * @param title
     * @param content
     * @return com.erp.model.msg.dto.NoticeMsgInfoDTO
     */
    private static NoticeMsgInfoDTO getNoticeMsgInfoDTO(List<String> userIdList, String title, String content) {
        NoticeMsgInfoDTO noticeMsgInfoDTO = new NoticeMsgInfoDTO();
        noticeMsgInfoDTO.setReceiverUserIds(userIdList);
        //消息头
        noticeMsgInfoDTO.setTitle(title);
        noticeMsgInfoDTO.setContent(content);
        noticeMsgInfoDTO.setNoticeTypeEnum(NoticeTypeEnum.WMS_TASK);

        //按钮
        NoticeMsgCardButtonDTO noticeMsgCardButtonDTO = new NoticeMsgCardButtonDTO();
        noticeMsgCardButtonDTO.setName("查看详情");
        noticeMsgCardButtonDTO.setUrl("https://erp.ulanzi.cn:8060/");
        noticeMsgInfoDTO.setNoticeMsgCardButtonDTO(noticeMsgCardButtonDTO);
        return noticeMsgInfoDTO;
    }

    private static WarnMsgInfoDTO getWarnMsgInfoDTO(String title,String content) {
        WarnMsgInfoDTO warnMsgInfo = new WarnMsgInfoDTO();
        warnMsgInfo.setTitle(StrUtil.format("加工SKU变更:sku:【{}】", ext.getComboSku()));
        warnMsgInfo.setBizName(StrUtil.format("加工SKU{}:sku:【{}】", CollectionUtil.isNotEmpty(bomList)? "更新" : "新增", ext.getComboSku()));
        warnMsgInfo.setErpServerModuleEnum(ErpServerModuleEnum.ERP_SERVER_DMP);
        warnMsgInfo.setTableName(SqlHelper.table(DmpBomEntity.class).getTableName());
        warnMsgInfo.setTableId("");
        warnMsgInfo.setKeyInfo(StrUtil.format("【{}】平台加工SKU【{}】发生变更，请及时更新plm BOM信息系统", ext.getPlatformSign(), ext.getComboSku()));
        warnMsgInfo.setWarnMsgTypeEnum(WarnMsgTypeEnum.MACHINING_SKU_NOTICE);
        return warnMsgInfo;
    }

    /**
     *获取通知内容(库存分配差异)
     */
    private String getNoticeContentByDistribution(String noticeRule) {
        //库存分配差异通知（按sku+仓库）
        List<VirtualInventoryDiffDTO.SendNoticeSkuDTO> sendNoticeSkuList = virtualInventoryDiffService.listDiffSkuSendNotice();
        //库存分配差异通知（按汇总）
        List<VirtualInventoryDiffDTO.SendNoticeTotalDTO> sendNoticeTotalList = virtualInventoryDiffService.listDiffTotalSendNotice();
        return "";
    }

    /**
     * 获取通知内容(库龄差异)
     */
    private String getNoticeContentByInventoryAge(String noticeRule) {
        //库龄差异通知（按sku+仓库）
        List<VirtualInventoryAgeDTO.SendNoticeSkuDTO> sendNoticeSkuList = virtualInventoryDetailService.listDiffSkuSendNotice();
        //库龄差异通知（按汇总）
        List<VirtualInventoryAgeDTO.SendNoticeTotalDTO> sendNoticeTotalList = virtualInventoryDetailService.listDiffTotalSendNotice();
        return "";
    }

    /**
     * 获取通知内容(冻结库存差异)
     * @return
     */
    private String getNoticeContentByBill(String noticeRule) {
        //冻结库存差异通知（按sku+仓库）
        List<VirtualTransFlowDetailDTO.SendNoticeSkuDTO> sendNoticeSkuList = virtualTransFlowDetailService.listDiffSkuSendNotice();
        //冻结库存差异通知（按汇总）
        List<VirtualTransFlowDetailDTO.SendNoticeTotalDTO> sendNoticeTotalList = virtualTransFlowDetailService.listDiffTotalSendNotice();
        return "";
    }
}