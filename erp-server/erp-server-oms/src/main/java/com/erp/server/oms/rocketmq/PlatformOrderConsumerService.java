package com.erp.server.oms.rocketmq;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncTaskIdDTO;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.dto.PlatformOrderDetailDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncStatusEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MapUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.handler.AbstractPlatformConsumerHandler;
import com.erp.model.dmp.dto.OrderMongoDTO;
import com.erp.model.oms.constant.PlatformTypeConstants;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.oms.dto.OmsMongoDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.wms.feign.SoOutstockFeign;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.server.oms.enums.CleanDataTableEnum;
import com.erp.server.oms.mongo.MongoService;
import com.erp.server.oms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 下载平台订单消费服务
 * @author Cloud
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.PLATFORM_PULL_DATA_TOPIC,
        selectorExpression = "third_system_order_tag",
        consumerGroup = "${spring.cloud.nacos.discovery.namespace}-platform_pull_order_consumer",
        consumeMode = ConsumeMode.ORDERLY)
public class PlatformOrderConsumerService<T extends DmpSyncTaskIdDTO> extends AbstractPlatformConsumerHandler<T> {

    @Resource
    private DmpTaskFeign dmpTaskFeign;
    @Resource
    private MongoService mongoService;
    @Resource
    private PlatformOrderConsumerHandleService platformOrderConsumerHandleService;


    @Override
    public void updateSyncTaskStatus(String id, SyncStatusEnum code, String msg) {
        dmpTaskFeign.updateSyncInfo(new DmpSyncMqDTO.ParamDTO(id, code.getCode(), msg));
    }

    @Override
    public void sendWarnMsg(String syncTaskId, String msg) {
//        dmpTaskFeign.sendWarnMsg(syncTaskId);
    }

    @Override
    public ApiResult<?> handle(Object ext) {
        log.info("[B2C订单消费] 消费:dto={}", JSONUtil.toJsonStr(ext));
        PlatformOrderDTO dto = JSONUtil.toBean(ext.toString(), PlatformOrderDTO.class);
        platformOrderConsumerHandleService.handleAll(dto);
        return ApiResult.success();
    }

    @Override
    public void updateMongodbData(String platform,String uniqueId, Integer isClean){
        if (StringUtils.isEmpty(uniqueId) || StringUtils.isEmpty(platform) || Objects.isNull(isClean)){
            return;
        }
        String tableName = getTableName(platform);
        if (StringUtils.isEmpty(tableName)) return;
        Class tClass = CleanDataTableEnum.getByName(tableName).getTClass();
        OmsMongoDTO updateDto = new OmsMongoDTO();
        updateDto.setUniqueId(uniqueId);
        MapUtil mapUtil = new MapUtil();
        mapUtil.put("isClean", isClean);
        mongoService.updateMongoData(updateDto, mapUtil, tableName, tClass);
    }

    /**
     * 根据平台组装表名
     * @param platform
     * @return
     */
    private String getTableName(String platform){
        switch (platform){
            case PlatformTypeConstants.SHOPEE:
               return StrUtil.format("{}_{}_{}", CleanDataTableEnum.SHOPEE_ORDER.getCategory(),
                       platform, CleanDataTableEnum.SHOPEE_ORDER.getBusiness());
            case PlatformTypeConstants.ALIEXPRESS:
                return StrUtil.format("{}_{}_{}", CleanDataTableEnum.ALI_EXPRESS_ORDER.getCategory(),
                        platform, CleanDataTableEnum.ALI_EXPRESS_ORDER.getBusiness());
            case PlatformTypeConstants.SHOPIFY:
                return StrUtil.format("{}_{}_{}", CleanDataTableEnum.SHOPIFY_ORDER.getCategory(),
                        platform, CleanDataTableEnum.SHOPIFY_ORDER.getBusiness());
            case PlatformTypeConstants.WALMART:
                return StrUtil.format("{}_{}_{}", CleanDataTableEnum.WALMART_ORDER.getCategory(),
                        platform, CleanDataTableEnum.WALMART_ORDER.getBusiness());
            default:
                return null;
        }
    }
}
