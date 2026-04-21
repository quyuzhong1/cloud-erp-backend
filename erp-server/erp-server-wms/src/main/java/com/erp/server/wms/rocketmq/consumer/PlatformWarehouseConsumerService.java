package com.erp.server.wms.rocketmq.consumer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.druid.sql.visitor.functions.Char;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncTaskIdDTO;
import com.common.business.dto.PlatformWarehouseDTO;
import com.common.business.enums.*;
import com.common.business.wrapper.FeignQuery;
import com.common.core.controller.vo.ApiResult;
import com.common.core.utils.StrUtils;
import com.common.message.handler.AbstractPlatformConsumerHandler;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.dto.MongoDBUpdateDTO;
import com.erp.model.dmp.entity.DmpPullTaskEntity;
import com.erp.model.msg.dto.WarnMsgInfoDTO;
import com.erp.model.msg.enums.WarnMsgTypeEnum;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.wms.entity.OverseasProviderWarehouseEntity;
import com.erp.rpc.dmp.feign.DmpMongoDbFeign;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.convert.OverseasWarehouseConverter;
import com.erp.server.wms.service.OverseasProviderWarehouseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 下载平台仓库消费服务
 */
@Service
@Slf4j
//@RocketMQMessageListener(topic = RocketMqTopic.PLATFORM_PULL_DATA_TOPIC,
//        selectorExpression = "third_system_warehouse_tag",
//        consumerGroup = "${spring.cloud.nacos.discovery.namespace}-platform_pull_warehouse_consumer",
//        consumeMode = ConsumeMode.ORDERLY)
public class PlatformWarehouseConsumerService<T extends DmpSyncTaskIdDTO> extends AbstractPlatformConsumerHandler<T> {

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private OverseasProviderWarehouseService overseasProviderWarehouseService;

    @Resource
    private MQProducerService mqProducerService;


    @Resource
    private DmpMongoDbFeign dmpMongoDbFeign;

    @Override
    public void updateMongodbData(String platform, String uniqueId, Integer isClean) {
        if (org.apache.commons.lang3.StringUtils.isEmpty(uniqueId) || org.apache.commons.lang3.StringUtils.isEmpty(platform) || Objects.isNull(isClean)){
            return;
        }
        MongoDBUpdateDTO dto = MongoDBUpdateDTO.builder()
                .tableName(getTableName(platform))
                .uniqueId(uniqueId)
                .isClean(isClean)
                .build();
        dmpMongoDbFeign.updateMongoDbData(dto);
    }

    /**
     * 根据平台组装表名
     * @param platform
     * @return
     */
    private String getTableName(String platform){
        return CharSequenceUtil.format("{}_{}_{}", PlatformCategoryEnum.THIRD_SYSTEM.getCode(),
                platform, BusinessTypeEnum.INBOUND.getCode());
    }

    @Override
    public void updateSyncTaskStatus(DmpSyncMqDTO.ParamDTO paramDTO) {
        dmpTaskFeign.updateSyncInfo(paramDTO);
    }

    @Override
    public void sendWarnMsg(String syncTaskId, String msg) {
        DmpPullTaskEntity dmpPullTaskEntity = dmpTaskFeign.getPullTaskById(syncTaskId);
        WarnMsgInfoDTO msgInfoDTO = this.buildWarnMsgInfoDTO(dmpPullTaskEntity,msg);
        mqProducerService.sendWarnMsg(msgInfoDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<?> handle(Object ext) {
        PlatformWarehouseDTO dto = JSONUtil.toBean(ext.toString(), PlatformWarehouseDTO.class);
        //海外仓
        if(WarehousePlatformTypeEnum.OVERSEAS_WAREHOUSE.getCode().equals(dto.getWarehousePlatformType())){
            handleOverseasWarehouse(dto);
        }
        return ApiResult.success();
    }

    private void handleOverseasWarehouse(PlatformWarehouseDTO dto) {
        //查询数据库存在的数据
        OverseasProviderWarehouseEntity dbEntity = overseasProviderWarehouseService.getByPlatform(dto.getProviderErpId(),dto.getWarehouseCode());
        OverseasProviderWarehouseEntity mqEntity = OverseasWarehouseConverter.INSTANCE.warehouseDb(dto);
        //设置国家名称
        if (OmsPlatformEnum.ZHONG_BAO.getCode().equals(dto.getProvider()) && CharSequenceUtil.isBlank(dto.getCountryCode())){
            mqEntity.setCountryName(CharSequenceUtil.EMPTY);
        }else {
            setCountryName(mqEntity);
        }
        if(Objects.isNull(dbEntity)){
            //不存在，插入,默认禁用
            mqEntity.setDisabled(Boolean.TRUE);
            overseasProviderWarehouseService.save(mqEntity);
        }else{
            updateOverseasWarehouse(dbEntity, mqEntity);
        }
    }

    private void updateOverseasWarehouse(OverseasProviderWarehouseEntity dbEntity, OverseasProviderWarehouseEntity mqEntity) {
        dbEntity.setPlatformWarehouseType(mqEntity.getPlatformWarehouseType());
        dbEntity.setPlatformWarehouseName(mqEntity.getPlatformWarehouseName());
        dbEntity.setCountry(mqEntity.getCountry());
        dbEntity.setCountryName(mqEntity.getCountryName());
        overseasProviderWarehouseService.updateById(dbEntity);
    }

    private void setCountryName(OverseasProviderWarehouseEntity mqEntity){
        if(StrUtils.isEmpty(mqEntity.getCountryName())){
            Optional.ofNullable(mqEntity.getCountry())
                    .map(sysUserFeign::getCountryById)
                    .ifPresent(dictCountryEntity -> mqEntity.setCountryName(dictCountryEntity.getNameCn()));
            return;
        }
        List<DictCountryEntity> list = FeignQuery.create(DictCountryEntity.class).eq(DictCountryEntity::getNameCn, mqEntity.getCountryName()).list();
        if (CollUtil.isNotEmpty(list)) {
            mqEntity.setCountry(list.get(0).getId());
        }
    }

    private WarnMsgInfoDTO buildWarnMsgInfoDTO(DmpPullTaskEntity dmpPullTaskEntity, String msg) {
        WarnMsgInfoDTO warnMsgInfo = new WarnMsgInfoDTO();
        warnMsgInfo.setBizName(SourceTypeEnum.getName(dmpPullTaskEntity.getSourceType()));
        warnMsgInfo.setErpServerModuleEnum(ErpServerModuleEnum.ERP_SERVER_WMS);
        warnMsgInfo.setTitle(CharSequenceUtil.format("仓库消息消费失败，来源平台:{},目标平台:{}",dmpPullTaskEntity.getSourcePlatformName(),dmpPullTaskEntity.getTargetPlatformName()));
        warnMsgInfo.setTableName(SourceTypeEnum.getTableName(dmpPullTaskEntity.getSourceType()));
        warnMsgInfo.setTableId(dmpPullTaskEntity.getId());
        warnMsgInfo.setKeyInfo(CharSequenceUtil.isBlank(msg)?"":msg);
        warnMsgInfo.setWarnMsgTypeEnum(WarnMsgTypeEnum.SYS_EXCEPTION);
        return warnMsgInfo;
    }
}
