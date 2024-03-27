package com.erp.server.tms.schedule;

import cn.hutool.core.util.StrUtil;
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
        pagingVOS = pagingVOS.stream().filter(v-> Objects.nonNull(v.getWarnHour()) && v.getWarnHour() < 0).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(pagingVOS)){
            return;
        }
        List<String> shopIdList = pagingVOS.stream().map(TmsFirstMileLogisticDTO.PagingVO::getShopId).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        List<ShopInfoEntity> shopInfoEntityList = shopInfoFeign.listShopInfoByIds(shopIdList);

        List<TmsFirstMileLogisticDTO.MsgDTO> msgDTOList = new ArrayList<>();
        for (TmsFirstMileLogisticDTO.PagingVO pagingVO : pagingVOS) {
            TmsFirstMileLogisticDTO.MsgDTO msgDTO = new TmsFirstMileLogisticDTO.MsgDTO();
            ShopInfoEntity shopInfoEntity = shopInfoEntityList.stream().filter(v->v.getId().equals(pagingVO.getShopId())).findFirst().orElse(null);
            if(Objects.nonNull(shopInfoEntity) && StringUtils.isNotBlank(shopInfoEntity.getChargeId())){
                msgDTO.setShopChargeIdList(Arrays.asList(shopInfoEntity.getChargeId()));
            }
            String titleContent = StrUtil.format("总计{}票货物出现异常，今天新增{}异常，请即时跟进", "","");
            String msgContent = StrUtil.format("通知类型：在途异常通知");
            msgDTO.setTitleContent(titleContent);
            msgDTO.setMessageContent(msgContent);
            msgDTOList.add(msgDTO);
        }
        msgDTOList.forEach(v->{
            this.sendMsgWhenOverdue(v.getShopChargeIdList(),v.getTitleContent(),v.getMessageContent());
        });
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
        Map contentMap = fsService.getCardMessageMap(titleContent , messageContent, fsAppUrl);
        sendMessage.setContentMap(contentMap);
        //发送消息
        fsService.sendMessage(sendMessage);
    }
}
