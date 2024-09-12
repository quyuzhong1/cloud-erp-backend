
package com.erp.server.wms.rocketmq.consumer;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.constant.DictKindgeeConstant;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncTaskIdDTO;
import com.common.business.dto.PlatformOtherOutStockDTO;
import com.common.business.dto.PlatformOtherOutStockDetailDTO;
import com.common.business.enums.*;
import com.common.business.wrapper.FeignQuery;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.common.core.utils.date.DateUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.handler.AbstractPlatformConsumerHandler;
import com.erp.model.dmp.dto.MongoDBUpdateDTO;
import com.erp.model.oms.dto.SoB2cErrorDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.SoB2cErrorTypeEnum;
import com.erp.model.sys.dto.DictKingdeeDTO;
import com.erp.model.sys.dto.SysDepartmentUserNumberDTO;
import com.erp.model.wms.dto.OtherOutstockCustomerDTO;
import com.erp.model.wms.dto.OtherOutstockDTO;
import com.erp.model.wms.dto.OtherOutstockDetailDTO;
import com.erp.model.wms.dto.SoOutstockDetailDTO;
import com.erp.model.wms.enums.InventoryDirectionEnum;
import com.erp.model.wms.enums.OutstockTypeEnum;
import com.erp.rpc.dmp.feign.DmpMongoDbFeign;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.service.OtherOutstockService;
import com.erp.server.wms.service.SoOutstockService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.validation.constraints.*;
import java.lang.reflect.Array;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 其他出库单消费服务
 *
 * @author Jim
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.PLATFORM_PULL_DATA_TOPIC,
        selectorExpression = "third_system_other_out_stock_tag",
        consumerGroup = "${spring.cloud.nacos.discovery.namespace}-platform_pull_other_out_stock_consumer",
        consumeMode = ConsumeMode.ORDERLY)
public class PlatformOtherOutStockConsumerService<T extends DmpSyncTaskIdDTO> extends AbstractPlatformConsumerHandler<T> {

    @Resource
    private DmpTaskFeign dmpTaskFeign;
    @Resource
    private DmpMongoDbFeign dmpMongoDbFeign;
    @Resource
    private OtherOutstockService otherOutstockService;
    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private SysDictFeign sysDictFeign;


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
        // 亚马逊多渠道订单和B2C订单来源一致
        if (PlatformDictEnum.AMAZON.getCode().equalsIgnoreCase(platform)){
            return StrUtil.format("{}_{}_{}", PlatformCategoryEnum.THIRD_SYSTEM.getCode(),
                    platform, BusinessTypeEnum.ORDER.getCode());
        } else {
            return StrUtil.format("{}_{}_{}", PlatformCategoryEnum.THIRD_SYSTEM.getCode(),
                    platform, BusinessTypeEnum.SO_MULTI_CHANNEL.getCode());
        }
    }

    @Override
    public void sendWarnMsg(String syncTaskId, String msg) {
        dmpTaskFeign.sendWarnMsg(syncTaskId);
    }

    @Override
    public ApiResult<?> handle(Object ext) {
        // 亚马逊物流销售消费服务
        log.info("[其他出库单] 消费:dto={}", JSONUtil.toJsonStr(ext));
        PlatformOtherOutStockDTO dto = JSONUtil.toBean(ext.toString(), PlatformOtherOutStockDTO.class);
        // 查询多渠道订单是否存在?
        List<SoMultiChannelEntity> list = FeignQuery.create(SoMultiChannelEntity.class)
                .eq(SoMultiChannelEntity::getPlatformCode, dto.getPlatformCode())
                .eq(SoMultiChannelEntity::getDictPlatform, dto.getPlatform())
                .eq(SoMultiChannelEntity::getShopId, dto.getShopId())
                .list();
        if (CollectionUtils.isEmpty(list)){
            log.warn("[其他出库单物消费服务]:多渠道销售单不存在：单号={}", dto.getPlatformCode());
            // 恢复待清洗
            MongoDBUpdateDTO mongoDBUpdateDTO = MongoDBUpdateDTO.builder()
                    .tableName(getTableName(dto.getPlatform()))
                    .uniqueId(dto.getUniqueId())
                    .isClean(-11)
                    .build();
            dmpMongoDbFeign.updateMongoDbData(mongoDBUpdateDTO);
            return ApiResult.success();
        }
        // 主单信息
        SoMultiChannelEntity mainEntity = list.get(0);

        // 店铺信息
        List<ShopInfoEntity> shopList = FeignQuery.create(ShopInfoEntity.class)
                .eq(ShopInfoEntity::getId, dto.getShopId())
                .list();
        if (CollectionUtils.isEmpty(shopList)){
            log.warn("[其他出库单物消费服务]:店铺不存在：单号={}, 店铺ID={}", dto.getPlatformCode(), dto.getShopId());
            // 恢复待清洗
            MongoDBUpdateDTO mongoDBUpdateDTO = MongoDBUpdateDTO.builder()
                    .tableName(getTableName(dto.getPlatform()))
                    .uniqueId(dto.getUniqueId())
                    .isClean(-11)
                    .build();
            dmpMongoDbFeign.updateMongoDbData(mongoDBUpdateDTO);
            return ApiResult.success();

        }
        ShopInfoEntity shopInfo = shopList.get(0);
        //获取部门信息
        SysDepartmentUserNumberDTO deptDTO = sysUserFeign.getDeptByUserId(shopInfo.getChargeId());
        if (null == deptDTO){
            log.warn("[其他出库单物消费服务]:部门不存在：单号={}, 用户ID={}", dto.getPlatformCode(), shopInfo.getChargeId());
            // 恢复待清洗
            MongoDBUpdateDTO mongoDBUpdateDTO = MongoDBUpdateDTO.builder()
                    .tableName(getTableName(dto.getPlatform()))
                    .uniqueId(dto.getUniqueId())
                    .isClean(-12)
                    .build();
            dmpMongoDbFeign.updateMongoDbData(mongoDBUpdateDTO);
            return ApiResult.success();
        }

        // 其他出库单的基础信息
        OtherOutstockDTO.AddDTO generateDTO = generateAddDTO(dto, mainEntity, shopInfo, deptDTO);

        // 校验sku映射关系
        if (generateDTO.getDetailList().stream().anyMatch(e-> StringUtils.isBlank(e.getSkuId()))){
            log.warn("[其他出库单物消费服务]:SKU映射不存在：单号={}", dto.getPlatformCode());
            // 恢复待清洗
            MongoDBUpdateDTO mongoDBUpdateDTO = MongoDBUpdateDTO.builder()
                    .tableName(getTableName(dto.getPlatform()))
                    .uniqueId(dto.getUniqueId())
                    .isClean(-13)
                    .build();
            dmpMongoDbFeign.updateMongoDbData(mongoDBUpdateDTO);
            return ApiResult.success();
        }
        // 检查和生成其他出库单
        otherOutstockService.checkAndAdd(generateDTO);
        return ApiResult.success();
    }

    private OtherOutstockDTO.AddDTO generateAddDTO(PlatformOtherOutStockDTO dto, SoMultiChannelEntity mainEntity, ShopInfoEntity shopInfo, SysDepartmentUserNumberDTO deptDTO) {
        OtherOutstockDTO.AddDTO addDTO = new OtherOutstockDTO.AddDTO();

        LocalDateTime deliveryLocalDateTime = DateUtil.parseLocalDateTimeWithOffset(dto.getPlatformDeliveryTime());
        if (null == deliveryLocalDateTime){
            throw new ServiceException("未找到出库单日期");
        }
        // 出库日期
        addDTO.setBillDate(deliveryLocalDateTime.toLocalDate());
        // 库存方向
        addDTO.setInventoryDirection(InventoryDirectionEnum.ORDINARY.getCode());
        // 发货仓库id
        addDTO.setWarehouseId(shopInfo.getWarehouseId());
        // 领料组织id
        addDTO.setReceiveOrgId(shopInfo.getSalesOrgId());
        //处理类型
        List<DictKingdeeDTO.ListDTO> typeList = sysDictFeign.listByTypeName(DictKindgeeConstant.OTHER_TYPE_NAME);
        List<DictKingdeeDTO.ListDTO> outTypeList = sysDictFeign.listByTypeName(DictKindgeeConstant.OTHER_OUT_TYPE_NAME);
        DictKingdeeDTO.ListDTO typeDTO = typeList.stream().filter(v->v.getName().equals(DictKindgeeConstant.OTHER_OUT_MATERIAL_PICKING)).findFirst().orElse(new DictKingdeeDTO.ListDTO());
        // 业务类型
        addDTO.setType(typeDTO.getCode());
        addDTO.setTypeName(typeDTO.getName());
        //出库类型
        DictKingdeeDTO.ListDTO outTypeDTO = outTypeList.stream().filter(v->v.getName().equals(DictKindgeeConstant.OTHER_OUT_SO_MULTI_CHANNEL)).findFirst().orElse(new DictKingdeeDTO.ListDTO());
        addDTO.setOutType(outTypeDTO.getCode());
        addDTO.setOutTypeName(outTypeDTO.getName());

        // 部门ID
        addDTO.setDeptId(deptDTO.getDepartmentId());
        // 流程申请单号
        addDTO.setProcessApplyCode(dto.getPlatformCode());
        // 主表备注
        addDTO.setRemark(dto.getRemark());
        // 客户信息
        OtherOutstockCustomerDTO.AddDTO customerDTO =  convertCustomer(shopInfo);
        addDTO.setOtherOutstockCustomer(customerDTO);

        List<OtherOutstockDetailDTO.AddDTO> detailList = dto.getDetailList().stream().map(e -> {
            OtherOutstockDetailDTO.AddDTO detailDTO = new OtherOutstockDetailDTO.AddDTO();
            detailDTO.setSkuId(e.getSkuId());
            detailDTO.setSkuNo(e.getSkuNo());
            detailDTO.setActualQty(e.getActualQty());
            detailDTO.setWarehouseLocation(e.getWarehouseLocation());
            detailDTO.setRemark(dto.getUniqueId());
            return detailDTO;
        }).collect(Collectors.toList());

        // 明细
        addDTO.setDetailList(detailList);
        return addDTO;
    }

    /**
     * 转换客户信息
     */
    private OtherOutstockCustomerDTO.AddDTO convertCustomer(ShopInfoEntity shopInfo) {
        OtherOutstockCustomerDTO.AddDTO addDTO = new OtherOutstockCustomerDTO.AddDTO();
        addDTO.setCustomerId(shopInfo.getCustomerId());
        addDTO.setCustomerCode(shopInfo.getCustomerCode());
        addDTO.setName(shopInfo.getName());
        return addDTO;
    }
}
