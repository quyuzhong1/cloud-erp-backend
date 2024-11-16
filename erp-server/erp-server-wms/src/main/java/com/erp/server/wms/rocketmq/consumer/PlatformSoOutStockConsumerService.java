package com.erp.server.wms.rocketmq.consumer;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncTaskIdDTO;
import com.common.business.dto.PlatformSoOutStockDTO;
import com.common.business.dto.PlatformSoOutStockDetailDTO;
import com.common.business.enums.*;
import com.common.core.controller.vo.ApiResult;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.message.constant.RocketMqTopic;
import com.common.message.handler.AbstractPlatformConsumerHandler;
import com.erp.model.dmp.dto.MongoDBUpdateDTO;
import com.erp.model.oms.dto.SoB2cErrorDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.enums.SoB2cErrorTypeEnum;
import com.erp.model.wms.dto.SoOutstockDTO;
import com.erp.model.wms.dto.SoOutstockDetailDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.inventory.InventoryClosedRecordDTO;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.rpc.dmp.feign.DmpMongoDbFeign;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.server.wms.service.InventoryClosedRecordService;
import com.erp.server.wms.service.SoOutstockService;
import com.erp.server.wms.service.WarehouseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.checkerframework.checker.units.qual.A;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.lang.reflect.Array;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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
        consumeMode = ConsumeMode.CONCURRENTLY
)
public class PlatformSoOutStockConsumerService<T extends DmpSyncTaskIdDTO> extends AbstractPlatformConsumerHandler<T> {

    @Resource
    private DmpTaskFeign dmpTaskFeign;
    @Resource
    private DmpMongoDbFeign dmpMongoDbFeign;
    @Resource
    private SoOutstockService soOutstockService;
    @Resource
    private SoB2cFeign soB2cFeign;
    @Resource
    private ShopInfoFeign shopInfoFeign;
    @Resource
    private InventoryClosedRecordService inventoryClosedRecordService;


    @Override
    public void updateSyncTaskStatus(DmpSyncMqDTO.ParamDTO paramDTO) {
        dmpTaskFeign.updateSyncInfo(paramDTO);
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
        return CharSequenceUtil.format("{}_{}_{}", PlatformCategoryEnum.THIRD_SYSTEM.getCode(),
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
        // 查询销售订单是否存在?
        // 忽略店铺
        List<SoB2cEntity> soB2cEntityList = soB2cFeign.getByPlatformCode(
                Collections.singletonList(dto.getPlatformCode()),
                dto.getDictPlatform(),
                "",
                SourceTypeEnum.SO_B2C.getCode()
        );
        if (CollectionUtils.isEmpty(soB2cEntityList)){
            log.warn("[销售出库单物消费服务]:B2C销售单不存在：单号={}", dto.getPlatformCode());
            // 恢复待清洗
            MongoDBUpdateDTO mongoDBUpdateDTO = MongoDBUpdateDTO.builder()
                    .tableName(getTableName(dto.getDictPlatform()))
                    .uniqueId(dto.getUniqueId())
                    .isClean(-10)
                    .build();
            dmpMongoDbFeign.updateMongoDbData(mongoDBUpdateDTO);
            return ApiResult.success();
        }
        SoB2cEntity soB2cEntity  = soB2cEntityList.stream()
                    .filter(e->e.getShopId().equalsIgnoreCase(dto.getShopId()))
                    .findFirst()
                    .orElse(null);
        if (null == soB2cEntity){
            // 查询相同账号的店铺ID
            List<ShopInfoEntity> sameAccountShopList = shopInfoFeign.getRelatedByShopId(dto.getShopId());
            List<String> shopIdList = sameAccountShopList.stream().map(BaseEntity::getId).collect(Collectors.toList());
            soB2cEntity = soB2cEntityList.stream()
                    .filter(e-> shopIdList.contains(e.getShopId()))
                    .findFirst()
                    .orElse(null);
        }

        if (null == soB2cEntity){
            log.warn("[销售出库销售消费服务]:配置的B2C销售单不存在：单号={}", dto.getPlatformCode());
            // 恢复待清洗
            MongoDBUpdateDTO mongoDBUpdateDTO = MongoDBUpdateDTO.builder()
                    .tableName(getTableName(dto.getDictPlatform()))
                    .uniqueId(dto.getUniqueId())
                    .isClean(-10)
                    .build();
            dmpMongoDbFeign.updateMongoDbData(mongoDBUpdateDTO);
//            throw new ServiceException("配置的B2C销售单不存在：单号=" + dto.getPlatformCode());
            return ApiResult.success();
        }

        // 记录订单数据（独立事务）
        soB2cFeign.checkAndFillBySoOutStock(dto);

        // B2C销售订单添加整个销售出库单的基础信息
        SoOutstockDTO.GenerateB2cDTO generateB2cDTO;
        try {
            PlatformSoOutStockDetailDTO detailDTO = dto.getDetailList().get(0);
            if (CharSequenceUtil.isBlank(detailDTO.getWarehouseId())){
                String msg = CharSequenceUtil.format("未找到对应仓库, 仓库【{}】, 仓库中心【{}】", detailDTO.getWarehouseName(), detailDTO.getFulfillmentCenterId());
                throw new ServiceException(msg);
            }

            generateB2cDTO = soB2cFeign.getSoOutStockByIdAndWarehouseId(soB2cEntity.getId(), detailDTO.getWarehouseId());
            if (PlatformDictEnum.AMAZON.getCode().equalsIgnoreCase(dto.getDictPlatform())){
                if (CharSequenceUtil.isBlank(detailDTO.getWarehouseId()) ||
                    CharSequenceUtil.isBlank(detailDTO.getWarehouseName()) ||
                    CharSequenceUtil.isBlank(detailDTO.getWarehouseOrgId()) ||
                    CharSequenceUtil.isBlank(detailDTO.getWarehouseOrgName())
                ){
                    ServiceException.runError(CharSequenceUtil.format("仓库信息缺失缺失:未找到仓储中心【{}】对应仓库", detailDTO.getFulfillmentCenterId()));
                }
                // 按仓库中心对应仓库
                generateB2cDTO.setWarehouseId(detailDTO.getWarehouseId());
                generateB2cDTO.setWarehouseName(detailDTO.getWarehouseName());
                generateB2cDTO.setWarehouseOrgId(detailDTO.getWarehouseOrgId());
                generateB2cDTO.setWarehouseOrgName(detailDTO.getWarehouseOrgName());
            }

        } catch (Exception e) {
            log.error("[销售出库销售消费服务]:查询销售出库单的基础信息异常：单号={}, error={}", dto.getPlatformCode(), ExceptionUtil.stacktraceToString(e));
            // 生成明细异常记录
            List<SoB2cDetailEntity> detailList = soB2cFeign.listDetailByMainIds(Collections.singletonList(soB2cEntity.getId()));
            if (CollectionUtils.isEmpty(detailList)){
                throw new ServiceException("明细ID为空, 订单ID=" + soB2cEntity.getId());
            }
            String platformOrderDetailId = dto.getDetailList().get(0).getPlatformOrderDetailId();
            SoB2cDetailEntity detailEntity = detailList.stream().filter(d -> d.getSourceDetailId().equals(platformOrderDetailId))
                    .findFirst()
                    .orElseThrow(() -> new ServiceException("未找到明细"));
            SoB2cErrorDTO.AddDTO addError = new SoB2cErrorDTO.AddDTO();
            addError.setType(SoB2cErrorTypeEnum.GENERATE_OUTSTOCK.getCode());
            addError.setParamJson(JSONUtil.toJsonStr(ext));
            addError.setReturnJson("");
            addError.setDetailId(detailEntity.getId());
            addError.setMainId(soB2cEntity.getId());
            addError.setMessage(CharSequenceUtil.format("自动生成销售出库单失败：{}", e.getMessage()));
            soB2cFeign.addSoB2cError(addError);
            return ApiResult.success();
        }

        // 检查允许生成销售出库单的日期
        LocalDate stopDate = soOutstockService.getStopSoOutStockDate();
        if (null != stopDate && !generateB2cDTO.getBillDate().isAfter(stopDate)){
            log.warn("[销售出库销售消费服务]:当前销售出库单日期【{}】因配置日期【{}】停止生成：单号={}", generateB2cDTO.getBillDate(),stopDate, dto.getPlatformCode());
            return ApiResult.success();
        }
        // 关账时间
        LocalDate closedLocalDate = inventoryClosedRecordService.checkClosed(generateB2cDTO.getWarehouseOrgId(), generateB2cDTO.getBillDate());
        if (null != closedLocalDate){
            // 已关账
            log.warn("[销售出库销售消费服务]:当前销售出库单日期【{}】因关账【{}】停止生成：单号={}", generateB2cDTO.getBillDate(), stopDate, dto.getPlatformCode());
            return ApiResult.success();
        }


        // 校验sku映射关系
        if (generateB2cDTO.getDetailList().stream().anyMatch(e-> CharSequenceUtil.isBlank(e.getSkuId()))){
            SoB2cErrorDTO.AddDTO addError = new SoB2cErrorDTO.AddDTO();
            addError.setType(SoB2cErrorTypeEnum.GENERATE_OUTSTOCK.getCode());
            addError.setParamJson(JSONUtil.toJsonStr(ext));
            addError.setReturnJson("");
            addError.setDetailId(generateB2cDTO.getDetailList().stream().map(SoOutstockDetailDTO.AddDTO::getSoDetailId).findFirst().orElse(""));
            addError.setMainId(soB2cEntity.getId());
            addError.setMessage(CharSequenceUtil.format("自动生成销售出库单失败：订单未匹配Sku映射关系"));
            soB2cFeign.addSoB2cError(addError);
            return ApiResult.success();
        }

        // 检查和生成销售出库单
        soOutstockService.checkAndGenerate(generateB2cDTO, dto, soB2cEntity);
        return ApiResult.success();
    }
}
