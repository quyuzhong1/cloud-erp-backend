package com.erp.server.wms.rocketmq.consumer;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.dto.*;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.PlatformCategoryEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.SyncStatusEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.common.message.constant.RocketMqTopic;
import com.common.message.handler.AbstractPlatformConsumerHandler;
import com.erp.model.dmp.dto.MongoDBUpdateDTO;
import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.oms.dto.ShopInfoDTO;
import com.erp.model.oms.dto.SoB2cErrorDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.enums.RuleTypeEnum;
import com.erp.model.oms.enums.SoB2cErrorTypeEnum;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.wms.dto.SoOutstockDTO;
import com.erp.model.wms.entity.FbaShipmentEntity;
import com.erp.rpc.dmp.feign.DmpMongoDbFeign;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.oms.feign.OmsTaskFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.oms.feign.SkuMappingFeign;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.SoOutstockFeign;
import com.erp.server.wms.convert.FbaShipmentConsumerConverter;
import com.erp.server.wms.service.CfgAmzFulfillmentCenterService;
import com.erp.server.wms.service.FbaShipmentService;
import com.erp.server.wms.service.SoOutstockService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.checkerframework.checker.units.qual.A;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 销售出库单消费服务
 *
 * @author Jim
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.PLATFORM_PULL_DATA_TOPIC,
        selectorExpression = "third_system_so_out_stock_tag",
        consumerGroup = "${spring.cloud.nacos.discovery.namespace}-platform_pull_so_out_stock_consumer",
        consumeMode = ConsumeMode.ORDERLY)
public class PlatformSoOutStockConsumerService<T extends DmpSyncTaskIdDTO> extends AbstractPlatformConsumerHandler<T> {

    @Resource
    private DmpTaskFeign dmpTaskFeign;
    @Resource
    private DmpMongoDbFeign dmpMongoDbFeign;
    @Resource
    private SoOutstockService soOutstockService;
    @Resource
    private SoB2cFeign soB2cFeign;


    @Override
    public void updateSyncTaskStatus(String id, SyncStatusEnum code, String msg) {
        dmpTaskFeign.updateSyncInfo(new DmpSyncMqDTO.ParamDTO(id, code.getCode(), msg));
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

    /**
     * 根据平台组装表名
     */
    private String getTableName(String platform){
        return StrUtil.format("{}_{}_{}", PlatformCategoryEnum.THIRD_SYSTEM.getCode(),
                platform, BusinessTypeEnum.SO_OUT_STOCK.getCode());
    }

    @Override
    public void sendWarnMsg(String syncTaskId, String msg) {
        dmpTaskFeign.sendWarnMsg(syncTaskId);
    }

    @Override
    public ApiResult<?> handle(Object ext) {
        // 亚马逊物流销售消费服务
        log.info("[销售出库单] 消费:dto={}", JSONUtil.toJsonStr(ext));
        PlatformSoOutStockDTO dto = JSONUtil.toBean(ext.toString(), PlatformSoOutStockDTO.class);
        // TODO 本次移除
        if (!dto.getPlatformCode().equalsIgnoreCase(dto.getDetailList().get(0).getPlatformCode())){
            // 跳过历史脏数据
            return ApiResult.success();
        }
        if (dto.getDetailList().stream().anyMatch(e-> null == dto.getShopId())){
            // 跳过历史脏数据
            return ApiResult.success();
        }

        // 已有出库详情/不保存订单
        Boolean hasDeliveryDetail = Boolean.FALSE;
        if (StringUtils.isNotEmpty(dto.getPlatformCode()) && StringUtils.isNotEmpty(dto.getDictPlatform())){
            hasDeliveryDetail = dmpMongoDbFeign.checkHasDeliveryDetail(dto.getPlatformCode(), dto.getDictPlatform());
        }
        if (hasDeliveryDetail){
            log.warn("[亚马逊物流销售消费服务]:已存在对应销售出库单不新增：单号={}", dto.getPlatformCode());
            return ApiResult.success();
        }
        // 查询销售出库单是否存在?
        List<SoB2cEntity> soB2cEntityList = soB2cFeign.getByPlatformCode(
                Collections.singletonList(dto.getPlatformCode()),
                dto.getDictPlatform(),
                dto.getShopId()
        );
        if (CollectionUtils.isEmpty(soB2cEntityList)){
            log.warn("[亚马逊物流销售消费服务]:B2C销售单不存在：单号={}", dto.getPlatformCode());
            throw new ServiceException("B2C销售单不存在：单号=" + dto.getPlatformCode());
        }
        SoB2cEntity soB2cEntity = soB2cEntityList.get(0);
        // B2C销售订单添加整个销售出库单的基础信息
        SoOutstockDTO.GenerateB2cDTO generateB2cDTO = soB2cFeign.getSoOutstockInfoById(soB2cEntity.getId());

        // 校验sku映射关系
        if (generateB2cDTO.getDetailList().stream().anyMatch(e-> StringUtils.isBlank(e.getSkuId()))){
            SoB2cErrorDTO.AddDTO addError = new SoB2cErrorDTO.AddDTO();
            addError.setType(SoB2cErrorTypeEnum.GENERATE_OUTSTOCK.getCode());
            addError.setParamJson(JSONUtil.toJsonStr(ext));
            addError.setReturnJson("");
            addError.setMainId(soB2cEntity.getId());
            addError.setMessage(StrUtil.format("自动生成销售出库单失败：订单未匹配Sku映射关系"));
            soB2cFeign.addSoB2cError(addError);
            return ApiResult.success();
        }

        // 检查和生成销售出库单
        soOutstockService.checkAndGenerate(generateB2cDTO, dto, soB2cEntity);
        return ApiResult.success();
    }
}
