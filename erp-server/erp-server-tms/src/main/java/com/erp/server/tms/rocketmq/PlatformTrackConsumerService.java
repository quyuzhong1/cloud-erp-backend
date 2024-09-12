package com.erp.server.tms.rocketmq;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncTaskIdDTO;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.PlatformCategoryEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.common.message.constant.RocketMqTopic;
import com.common.message.handler.AbstractPlatformConsumerHandler;
import com.erp.model.dmp.dto.MongoDBUpdateDTO;
import com.erp.model.tms.entity.LogisticsTrackEntity;
import com.erp.rpc.dmp.feign.DmpMongoDbFeign;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.server.tms.convert.TrackDataConverter;
import com.erp.server.tms.service.LogisticsTrackService;
import com.sdk.tms.track123.dto.PlatformTrackDTO;
import io.seata.common.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * 下载FBA货件消费服务
 *
 * @author Jim
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.PLATFORM_PULL_DATA_TOPIC,
        selectorExpression = "third_system_getTrack_tag",
        consumerGroup = "${spring.cloud.nacos.discovery.namespace}-platform_pull_tms_track_consumer",
        consumeMode = ConsumeMode.ORDERLY)
public class PlatformTrackConsumerService<T extends DmpSyncTaskIdDTO> extends AbstractPlatformConsumerHandler<T> {

    @Resource
    private DmpTaskFeign dmpTaskFeign;
    @Resource
    private DmpMongoDbFeign dmpMongoDbFeign;
    @Resource
    private LogisticsTrackService logisticsTrackService;

    @Override
    public void updateSyncTaskStatus(DmpSyncMqDTO.ParamDTO paramDTO) {
        try {
            dmpTaskFeign.updateSyncInfo(paramDTO);
        }catch (Exception e){
            throw new ServiceException("erp-dmp更新dmp_pull_task异常："+ e.getMessage());
        }
    }
    @Override
    public void updateMongodbData(String platform, String uniqueId, Integer isClean) {
        if (StringUtils.isEmpty(uniqueId) || StringUtils.isEmpty(platform) || Objects.isNull(isClean)){
            return;
        }
        MongoDBUpdateDTO dto = MongoDBUpdateDTO.builder()
                .tableName(getTableName(platform))
                .uniqueId(uniqueId)
                .isClean(isClean)
                .build();
        dmpMongoDbFeign.updateMongoDbData(dto);
    }
    @Override
    public void sendWarnMsg(String syncTaskId, String msg) {

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<?> handle(Object ext) {
        PlatformTrackDTO dto = JSONUtil.toBean(ext.toString(), PlatformTrackDTO.class);
        //根据trackNo拉取轨迹数据
        if (Objects.isNull(dto) || CollectionUtils.isEmpty(dto.getDetails())) return ApiResult.success();
        List<LogisticsTrackEntity> logisticsTrackEntities = TrackDataConverter.INSTANCE.platformToTrack(dto.getDetails());
        //先物理删除  再新增
        if (CollectionUtils.isNotEmpty(logisticsTrackEntities)) {
            //删除
            logisticsTrackService.deleteByTrackNo(dto.getTrackNo());
            //新增
            logisticsTrackService.saveBatch(logisticsTrackEntities);
            //TODO 根据记录最新状态修改订单状态
            //Student latest = Collections.max(studentList,
            //                                 Comparator.comparing(s -> s.getDate()));
            LogisticsTrackEntity max = Collections.max(logisticsTrackEntities, Comparator.comparing(LogisticsTrackEntity::getTrackTime));
            logisticsTrackService.checkTrackStatus(max);
        }
        System.out.println(dto);
        return ApiResult.success();
    }


    /**
     * 根据平台组装表名
     * @param platform
     * @return
     */
    private String getTableName(String platform){
        return StrUtil.format("{}_{}_{}", PlatformCategoryEnum.THIRD_SYSTEM.getCode(),
                platform, BusinessTypeEnum.GET_TRACK.getCode());
    }
}
