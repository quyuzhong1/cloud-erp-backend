package com.erp.server.wms.rocketmq.consumer;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncTaskIdDTO;
import com.common.business.dto.PlatformOutboundDTO;
import com.common.business.enums.*;
import com.common.core.controller.vo.ApiResult;
import com.common.message.constant.RocketMqTopic;
import com.common.message.handler.AbstractPlatformConsumerHandler;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.dto.MongoDBUpdateDTO;
import com.erp.model.dmp.entity.DmpPullTaskEntity;
import com.erp.model.msg.dto.WarnMsgInfoDTO;
import com.erp.model.msg.enums.WarnMsgTypeEnum;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.dto.SoB2cErrorDTO;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.oms.enums.SoB2cErrorTypeEnum;
import com.erp.model.wms.dto.SoOutstockDTO;
import com.erp.rpc.dmp.feign.DmpMongoDbFeign;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.server.wms.service.AsyncService;
import com.erp.server.wms.service.SoOutstockService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.json.JsonObject;
import java.util.Objects;

/**
 * 下载平台入库数据消费服务
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.PLATFORM_PULL_DATA_TOPIC,
        selectorExpression = "third_system_outbound_tag",
        consumerGroup = "${spring.cloud.nacos.discovery.namespace}-platform_pull_outbound_consumer",
        consumeMode = ConsumeMode.ORDERLY)
public class PlatformOutboundConsumerService<T extends DmpSyncTaskIdDTO> extends AbstractPlatformConsumerHandler<T> {

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private MQProducerService mqProducerService;

    @Resource
    private SoOutstockService soOutstockService;

    @Resource
    private SoB2cFeign soB2cFeign;

    @Resource
    private DmpMongoDbFeign dmpMongoDbFeign;
    @Lazy
    @Resource
    private AsyncService asyncService;

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
                platform, BusinessTypeEnum.OUTBOUND.getCode());
    }

    @Override
    public void updateSyncTaskStatus(DmpSyncMqDTO.ParamDTO paramDTO) {
        dmpTaskFeign.updateSyncInfo(paramDTO);
    }

    @Override
    public void sendWarnMsg(String syncTaskId, String msg) {
        DmpPullTaskEntity dmpPullTaskEntity = dmpTaskFeign.getPullTaskById(syncTaskId);
        WarnMsgInfoDTO msgInfoDTO = this.buildWarnMsgInfoDTO(dmpPullTaskEntity, msg);
        mqProducerService.sendWarnMsg(msgInfoDTO);
    }

    @Override
    public ApiResult<?> handle(Object ext) {
        PlatformOutboundDTO dto = JSONUtil.toBean(ext.toString(), PlatformOutboundDTO.class);
        log.warn("第三方出库单参数>>>>>>>{}",JSONUtil.toJsonStr(dto));
        //这个是B2c销售订单code
        String soB2cCode = dto.getReferenceNo();
        String billStatus = dto.getOrderStatus();
        // 查询已有订单
        SoB2cEntity mainEntity = soB2cFeign.getSoCode(soB2cCode);
        if(null == mainEntity){
            if (CharSequenceUtil.isBlank(soB2cCode)){
                return ApiResult.success();
            }
            // 非ERP单号前缀
            if (!soB2cCode.startsWith(BusinessNoConstant.XSDS) && !soB2cCode.startsWith(BusinessNoConstant.XSDD)){
                return ApiResult.success();
            }
            log.error("第三方出库单: 未找到B2C销售订单 >>>>>>>{}",JSONUtil.toJsonStr(dto));
            return ApiResult.success();
        }
        // 当前单据状态
        String curBillStatus = mainEntity.getBillStatus();

        SoB2cDTO.UpdateStatusDTO updateStatus = new SoB2cDTO.UpdateStatusDTO();
        updateStatus.setSoCode(soB2cCode);
        updateStatus.setSoId(mainEntity.getId());
        if (SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(dto.getOrderStatus())){
            updateStatus.setBillStatus(billStatus);
            updateStatus.setAddOperationLog(true);
        }
        updateStatus.setTrackNo(dto.getTrackNo());
        soB2cFeign.updateSoB2cStatusByParams(updateStatus);
        if (SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(dto.getOrderStatus())) {

            // 主单待发货首次变成已发货才触发标记
            if (SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED.getCode().equalsIgnoreCase(curBillStatus)){
                // 校验平台来源明细
                if (soB2cFeign.checkPlatformShipOrder(mainEntity.getId())) {
                    // 调用第三方平台SDK标记发货(独立事务)
                    String businessDesc = "第三方仓出库";
                    asyncService.asyncShipOrder(mainEntity.getId(),
                            mainEntity.getCode(),
                            mainEntity.getDictPlatform(),
                            mainEntity.convertSubmitPlatformUniqueKey(),
                            JSONUtil.toJsonStr(dto),
                            businessDesc, false);
                }
            }
            //清除三方仓异常
            if(SoB2cErrorTypeEnum.THIRD_WAREHOUSE_OUT_EXCEPTION.getCode().equals(mainEntity.getSignOrderError())){
                String type = SoB2cErrorTypeEnum.THIRD_WAREHOUSE_OUT_EXCEPTION.getCode();
                SoB2cErrorDTO.DeleteDTO deleteDTO = new SoB2cErrorDTO.DeleteDTO();
                deleteDTO.setMainId(mainEntity.getId());
                deleteDTO.setType(type);
                soB2cFeign.deleteError(deleteDTO);
            }

            // 校验是否已生成销售出库单
            boolean exist = soOutstockService.checkExist(soB2cCode, SourceTypeEnum.THIRD_WAREHOUSE_CREATE_OUTBOUND_BILL.getCode(), OrderTypeEnum.B2C.getCode());
            if (exist) {
                log.warn("销售订单{} 已生成销售出库单, 忽略生成", soB2cCode );
                return ApiResult.success();
            }
            SoOutstockDTO.GenerateB2cDTO generateB2cDTO = soB2cFeign.getSoOutstockInfoByCode(soB2cCode);
            // 第三方仓出库生成销售出库单（独立事务）
            soOutstockService.thirdWarehouseCheckAndGenerate(generateB2cDTO, dto);
        }

        if (SoB2cBillStatusEnum.ENUM_EXCEPTION.getCode().equals(dto.getOrderStatus())) {
            //更新异常订单信息
            SoB2cErrorDTO.AddDTO addError = new SoB2cErrorDTO.AddDTO(
                    mainEntity.getId(),
                    SoB2cErrorTypeEnum.THIRD_WAREHOUSE_OUT_EXCEPTION.getCode(),
                    null,
                    dto.getAbnormalProblemReason(),
                    JSONUtil.toJsonStr(dto),
                    ""
            );
            soB2cFeign.addSoB2cError(addError);
            //异步取消海外仓订单
            asyncService.asyncCancelThirdWarehouseOrder(mainEntity);
        }
        return ApiResult.success();
    }

    private WarnMsgInfoDTO buildWarnMsgInfoDTO(DmpPullTaskEntity dmpPullTaskEntity, String msg) {
        WarnMsgInfoDTO warnMsgInfo = new WarnMsgInfoDTO();
        warnMsgInfo.setBizName(SourceTypeEnum.getName(dmpPullTaskEntity.getSourceType()));
        warnMsgInfo.setErpServerModuleEnum(ErpServerModuleEnum.ERP_SERVER_WMS);
        warnMsgInfo.setTitle(CharSequenceUtil.format("平台出库消息消费失败，来源平台:{},目标平台:{}", dmpPullTaskEntity.getSourcePlatformName(), dmpPullTaskEntity.getTargetPlatformName()));
        warnMsgInfo.setTableName(SourceTypeEnum.getTableName(dmpPullTaskEntity.getSourceType()));
        warnMsgInfo.setTableId(dmpPullTaskEntity.getId());
        warnMsgInfo.setKeyInfo(CharSequenceUtil.isBlank(msg) ? "" : msg);
        warnMsgInfo.setWarnMsgTypeEnum(WarnMsgTypeEnum.SYS_EXCEPTION);
        return warnMsgInfo;
    }
}
