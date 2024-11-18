package com.erp.server.wms.rocketmq.consumer;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.dto.PlatformSoOutStockDTO;
import com.common.business.dto.PlatformSoOutStockDetailDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.core.entity.BaseEntity;
import com.common.core.exception.ServiceException;
import com.common.message.constant.RocketMqNewConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.common.message.handler.AbstractNewPlatformConsumerHandler;
import com.erp.model.oms.dto.SoB2cErrorDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.enums.SoB2cErrorTypeEnum;
import com.erp.model.wms.dto.SoOutstockDTO;
import com.erp.model.wms.dto.SoOutstockDetailDTO;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.server.wms.service.InventoryClosedRecordService;
import com.erp.server.wms.service.SoOutstockService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 新销售出库单数据消费服务
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqNewTopic.DMP_AMZ_SO_OUT_STOCK_TO_WMS_TOPIC,
        selectorExpression = RocketMqNewTag.DMP_AMZ_SO_OUT_STOCK_TO_WMS_TAG,
        consumerGroup = RocketMqNewConsumerGroup.DMP_AMZ_SO_OUT_STOCK_TO_WMS_GROUP,
        consumeMode = ConsumeMode.CONCURRENTLY)
public class PlatformNewSoOutStockConsumerService extends AbstractNewPlatformConsumerHandler {
    @Resource
    private SoB2cFeign soB2cFeign;
    @Resource
    private ShopInfoFeign shopInfoFeign;
    @Resource
    private SoOutstockService soOutstockService;
    @Resource
    private InventoryClosedRecordService inventoryClosedRecordService;

    @Override
    public String getBizName() {
        return "平台销售出库单";
    }

    @Override
    public void handle(String ext) {
        // 亚马逊物流销售消费服务
        log.info("[新中台销售出库单消费] 消费:dto={}", JSONUtil.toJsonStr(ext));
        PlatformSoOutStockDTO dto = JSONObject.parseObject(ext, PlatformSoOutStockDTO.class);
        // 查询销售订单是否存在?
        // 忽略店铺
        List<SoB2cEntity> soB2cEntityList = soB2cFeign.getByPlatformCode(
                Collections.singletonList(dto.getPlatformCode()),
                dto.getDictPlatform(),
                "",
                SourceTypeEnum.SO_B2C.getCode()
        );
        if (CollectionUtils.isEmpty(soB2cEntityList)) {
            log.warn("[新中台销售出库单消费]:B2C销售单不存在：单号={}", dto.getPlatformCode());
            // 恢复待清洗
            // 发送预警
            return;
        }
        SoB2cEntity soB2cEntity = soB2cEntityList.stream()
                .filter(e -> e.getShopId().equalsIgnoreCase(dto.getShopId()))
                .findFirst()
                .orElse(null);
        if (null == soB2cEntity) {
            // 查询相同账号的店铺ID
            List<ShopInfoEntity> sameAccountShopList = shopInfoFeign.getRelatedByShopId(dto.getShopId());
            List<String> shopIdList = sameAccountShopList.stream().map(BaseEntity::getId).collect(Collectors.toList());
            soB2cEntity = soB2cEntityList.stream()
                    .filter(e -> shopIdList.contains(e.getShopId()))
                    .findFirst()
                    .orElse(null);
        }

        if (null == soB2cEntity) {
            log.warn("[新中台销售出库单消费]:配置的B2C销售单不存在：单号={}", dto.getPlatformCode());
            // 发送预警
            return;
        }

        // 记录订单数据（独立事务）
        soB2cFeign.checkAndFillBySoOutStock(dto);

        // B2C销售订单添加整个销售出库单的基础信息
        SoOutstockDTO.GenerateB2cDTO generateB2cDTO;
        try {
            PlatformSoOutStockDetailDTO detailDTO = dto.getDetailList().get(0);
            if (CharSequenceUtil.isBlank(detailDTO.getWarehouseId())) {
                String msg = CharSequenceUtil.format("未找到对应仓库, 仓库【{}】, 仓库中心【{}】", detailDTO.getWarehouseName(), detailDTO.getFulfillmentCenterId());
                throw new ServiceException(msg);
            }

            generateB2cDTO = soB2cFeign.getSoOutStockByIdAndWarehouseId(soB2cEntity.getId(), detailDTO.getWarehouseId());
            if (PlatformDictEnum.AMAZON.getCode().equalsIgnoreCase(dto.getDictPlatform())) {
                if (CharSequenceUtil.isBlank(detailDTO.getWarehouseId()) ||
                        CharSequenceUtil.isBlank(detailDTO.getWarehouseName()) ||
                        CharSequenceUtil.isBlank(detailDTO.getWarehouseOrgId()) ||
                        CharSequenceUtil.isBlank(detailDTO.getWarehouseOrgName())
                ) {
                    ServiceException.runError(CharSequenceUtil.format("仓库信息缺失缺失:未找到仓储中心【{}】对应仓库", detailDTO.getFulfillmentCenterId()));
                }
                // 按仓库中心对应仓库
                generateB2cDTO.setWarehouseId(detailDTO.getWarehouseId());
                generateB2cDTO.setWarehouseName(detailDTO.getWarehouseName());
                generateB2cDTO.setWarehouseOrgId(detailDTO.getWarehouseOrgId());
                generateB2cDTO.setWarehouseOrgName(detailDTO.getWarehouseOrgName());
            }

        } catch (Exception e) {
            log.error("[新中台销售出库单消费]:查询销售出库单的基础信息异常：单号={}, error={}", dto.getPlatformCode(), ExceptionUtil.stacktraceToString(e));
            // 生成明细异常记录
            List<SoB2cDetailEntity> detailList = soB2cFeign.listDetailByMainIds(Collections.singletonList(soB2cEntity.getId()));
            if (CollectionUtils.isEmpty(detailList)) {
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
            return;
        }

        // 检查允许生成销售出库单的日期
        LocalDate stopDate = soOutstockService.getStopSoOutStockDate();
        if (null != stopDate && !generateB2cDTO.getBillDate().isAfter(stopDate)) {
            log.warn("[新中台销售出库单消费]:当前销售出库单日期【{}】因配置日期【{}】停止生成：单号={}", generateB2cDTO.getBillDate(), stopDate, dto.getPlatformCode());
            return;
        }
        // 关账时间
        LocalDate closedLocalDate = inventoryClosedRecordService.checkClosed(generateB2cDTO.getWarehouseOrgId(), generateB2cDTO.getBillDate());
        if (null != closedLocalDate) {
            // 已关账
            log.warn("[新中台销售出库单消费]:当前销售出库单日期【{}】因关账【{}】停止生成：单号={}", generateB2cDTO.getBillDate(), stopDate, dto.getPlatformCode());
            return;
        }

        // 校验sku映射关系
        if (generateB2cDTO.getDetailList().stream().anyMatch(e -> CharSequenceUtil.isBlank(e.getSkuId()))) {
            SoB2cErrorDTO.AddDTO addError = new SoB2cErrorDTO.AddDTO();
            addError.setType(SoB2cErrorTypeEnum.GENERATE_OUTSTOCK.getCode());
            addError.setParamJson(JSONUtil.toJsonStr(ext));
            addError.setReturnJson("");
            addError.setDetailId(generateB2cDTO.getDetailList().stream().map(SoOutstockDetailDTO.AddDTO::getSoDetailId).findFirst().orElse(""));
            addError.setMainId(soB2cEntity.getId());
            addError.setMessage(CharSequenceUtil.format("自动生成销售出库单失败：订单未匹配Sku映射关系"));
            soB2cFeign.addSoB2cError(addError);
            return;
        }

        // 检查和生成销售出库单
        soOutstockService.checkAndGenerate(generateB2cDTO, dto, soB2cEntity);
    }

}