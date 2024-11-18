package com.erp.server.tms.schedule;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.constant.ThirdConstants;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.sys.entity.SysPostUserEntity;
import com.erp.model.sys.vo.FsBatchSendMessageDTO;
import com.erp.model.sys.vo.ThirdUnionDTO;
import com.erp.model.tms.dto.CfgSettingValueDTO;
import com.erp.model.tms.dto.TmsFirstMileLogisticDTO;
import com.erp.model.tms.entity.CfgSettingEntity;
import com.erp.model.tms.enums.CfgSettingEnum;
import com.erp.model.tms.enums.FmLogisticTrackStatusEnum;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.sys.feign.SysPostFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.sdk.fs.service.FsService;
import com.erp.server.tms.service.CfgSettingService;
import com.erp.server.tms.service.TmsFirstMileLogisticService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 头程物流单预警推送飞书
 */
@Component
@Slf4j
@EnableScheduling
public class FmLogisticWarnJob {

    @Resource
    private TmsFirstMileLogisticService firstMileLogisticService;

    @Resource
    private CfgSettingService cfgSettingService;

    @Resource
    private ShopInfoFeign shopInfoFeign;

    @Resource
    private SysPostFeign sysPostFeign;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private FsService fsService;

    @Value("${third.fs.appUrl}")
    private String fsAppUrl;

    /**
     * 报关设置自动生成-定时器调用
     * @return
     */
    @XxlJob("sendFmLogisticWarnJob")
    public void sendFmLogisticWarnJob() {
        //查询全部预警
        List<TmsFirstMileLogisticDTO.PagingVO> pagingVOS = firstMileLogisticService.hasWarnPaging(new TmsFirstMileLogisticDTO.PagingParamDTO());
        pagingVOS = pagingVOS.stream().filter(v-> Objects.nonNull(v.getWarnHour()) && v.getWarnHour() < 0 && !FmLogisticTrackStatusEnum.SIGN.getCode().equals(v.getLogisticsStatus())).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(pagingVOS)){
            return;
        }
        //今日超期
        List<TmsFirstMileLogisticDTO.PagingVO> todayPagingVOS = pagingVOS.stream().filter(v-> v.getWarnHour() > -24).collect(Collectors.toList());
        //预警发送人员分为两部分，一部分是销售店铺负责人，一部分是抄送人
        //处理抄送人消息发送
        int totalWarnCount = pagingVOS.size();
        int todayCount = todayPagingVOS.size();
        String titleContent = CharSequenceUtil.format("总计{}票货物出现异常，今天新增{}异常，请即时跟进", totalWarnCount,todayCount);
        String msgContent = CharSequenceUtil.format("通知类型：在途异常通知");
        this.sendMsgWhenOverdue(new ArrayList<>(),titleContent,msgContent);

        //处理店铺负责人消息推送
        List<String> shopIdList = pagingVOS.stream().map(TmsFirstMileLogisticDTO.PagingVO::getShopId).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        if(CollectionUtils.isEmpty(shopIdList)){
            return;
        }
        //封装负责人id
        List<ShopInfoEntity> shopInfoEntityList = shopInfoFeign.listShopInfoByIds(shopIdList);
        for (TmsFirstMileLogisticDTO.PagingVO pagingVO : pagingVOS) {
            TmsFirstMileLogisticDTO.MsgDTO msgDTO = new TmsFirstMileLogisticDTO.MsgDTO();
            ShopInfoEntity shopInfoEntity = shopInfoEntityList.stream().filter(v->v.getId().equals(pagingVO.getShopId())).findFirst().orElse(null);
            if(Objects.nonNull(shopInfoEntity) && StringUtils.isNotBlank(shopInfoEntity.getChargeId())){
                pagingVO.setChargeId(shopInfoEntity.getChargeId());
            }
        }
        pagingVOS = pagingVOS.stream().filter(v->StringUtils.isNotBlank(v.getChargeId())).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(pagingVOS)){
            return;
        }
        Map<String,List<TmsFirstMileLogisticDTO.PagingVO>> pagingMap = pagingVOS.stream().collect(Collectors.groupingBy(TmsFirstMileLogisticDTO.PagingVO::getChargeId));
        pagingMap.forEach((key,value)->{
            List<TmsFirstMileLogisticDTO.PagingVO> todayWarnByCharge = value.stream().filter(v-> v.getWarnHour() > -24).collect(Collectors.toList());
            int totalWarnCountByCharge = value.size();
            int todayCountByCharge = todayWarnByCharge.size();
            String titleContentByCharge = CharSequenceUtil.format("总计{}票货物出现异常，今天新增{}异常，请即时跟进", totalWarnCountByCharge,todayCountByCharge);
            String msgContentByCharge = CharSequenceUtil.format("通知类型：在途异常通知");
            this.sendMsgWhenOverdueByCharge(Arrays.asList(key),titleContentByCharge,msgContentByCharge);
        });
    }
    private void sendMsgWhenOverdueByCharge(List<String> shopChargeIdList,String titleContent,String messageContent){
        if(CollectionUtils.isEmpty(shopChargeIdList)){
            return;
        }
        CfgSettingEntity cfgSettingEntity = cfgSettingService.getByKey(CfgSettingEnum.NOTIC.getCode());
        if(Objects.isNull(cfgSettingEntity)){
            return;
        }

        CfgSettingValueDTO.NoticeDTO noticeDTO = JSONUtil.toBean(cfgSettingEntity.getDataJson(),CfgSettingValueDTO.NoticeDTO.class);
        if(Objects.isNull(noticeDTO)){
            return;
        }
        if(!noticeDTO.getIsInTransitShopCharge()){
           return;
        }
        //获取飞书的unionid 与用户关系
        List<ThirdUnionDTO> unionIdList = sysUserFeign.getThirdUnionId(ThirdConstants.FS_PLATFORM);
        FsBatchSendMessageDTO sendMessage = new FsBatchSendMessageDTO();
        //过滤出有飞书配置的用户
        List<String> finalSendUserIds = shopChargeIdList;
        unionIdList =  unionIdList.stream().filter(u -> finalSendUserIds.contains(u.getUserId())).collect(Collectors.toList());
        List<String> unionIds = unionIdList.stream().map(ThirdUnionDTO::getThirdUnionId).distinct().collect(Collectors.toList());
        if(CollectionUtils.isEmpty(unionIds)){
            return;
        }
        sendMessage.setUnionIds(unionIds);
        Map contentMap = fsService.getCardMessageMap(titleContent , messageContent, fsAppUrl,false);
        sendMessage.setContentMap(contentMap);
        //发送消息
        fsService.sendMessage(sendMessage);
    }

    private void sendMsgWhenOverdue(List<String> shopChargeIdList,String titleContent,String messageContent){
        CfgSettingEntity cfgSettingEntity = cfgSettingService.getByKey(CfgSettingEnum.NOTIC.getCode());
        if(Objects.isNull(cfgSettingEntity)){
            return;
        }

        CfgSettingValueDTO.NoticeDTO noticeDTO = JSONUtil.toBean(cfgSettingEntity.getDataJson(),CfgSettingValueDTO.NoticeDTO.class);
        if(Objects.isNull(noticeDTO)){
            return;
        }
        List<String> sendUserIds = new ArrayList<>();
        if(noticeDTO.getIsInTransitShopCharge() && CollectionUtils.isNotEmpty(shopChargeIdList)){
            sendUserIds.addAll(shopChargeIdList);
        }
        if(CollectionUtils.isNotEmpty(noticeDTO.getInTransitUserIdList())){
            sendUserIds.addAll(noticeDTO.getInTransitUserIdList());
        }
        //处理岗位，获取岗位下全部人
        if(CollectionUtils.isNotEmpty(noticeDTO.getInTransitPostIdList())){
            //岗位id
            List<String> postIdList = noticeDTO.getInTransitPostIdList();
            List<SysPostUserEntity> userEntityList = sysPostFeign.getUserIdByPostIds(postIdList);
            if(CollectionUtils.isNotEmpty(userEntityList)){
                sendUserIds.addAll(userEntityList.stream().map(SysPostUserEntity::getUserId).distinct().collect(Collectors.toList()));
            }
        }
//        sendUserIds.add("1730158964328370178");
        //没有需要发送的人员
        if(CollectionUtils.isEmpty(sendUserIds)){
            return;
        }
        sendUserIds = sendUserIds.stream().distinct().collect(Collectors.toList());
        //获取飞书的unionid 与用户关系
        List<ThirdUnionDTO> unionIdList = sysUserFeign.getThirdUnionId(ThirdConstants.FS_PLATFORM);
        FsBatchSendMessageDTO sendMessage = new FsBatchSendMessageDTO();
        //过滤出有飞书配置的用户
        List<String> finalSendUserIds = sendUserIds;
        unionIdList =  unionIdList.stream().filter(u -> finalSendUserIds.contains(u.getUserId())).collect(Collectors.toList());
        List<String> unionIds = unionIdList.stream().map(ThirdUnionDTO::getThirdUnionId).distinct().collect(Collectors.toList());
        if(CollectionUtils.isEmpty(unionIds)){
            return;
        }
        sendMessage.setUnionIds(unionIds);
        Map contentMap = fsService.getCardMessageMap(titleContent , messageContent, fsAppUrl,false);
        sendMessage.setContentMap(contentMap);
        //发送消息
        fsService.sendMessage(sendMessage);
    }
}
