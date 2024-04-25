package com.erp.server.oms.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.dto.PlatformOrderLogisticsDTO;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.UnitEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.enums.CountrySiteEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.dto.SoB2cLogisticsDTO;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.model.oms.entity.SoB2cReceiverEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.LogisticsBillDTO;
import com.erp.model.tms.dto.LogisticsBillDetailDTO;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.tms.feign.LogisticsBillFeign;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.erp.server.oms.convert.B2cOrderConsumerConverter;
import com.erp.server.oms.mapper.SoB2cLogisticsMapper;
import com.erp.server.oms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * <p>
 * B2C销售订单物流信息表 服务实现类
 * </p>
 *
 * @author Will
 * @since 2023-08-18
 */
@Slf4j
@Service
public class SoB2cLogisticsServiceImpl extends SuperServiceImpl<SoB2cLogisticsMapper, SoB2cLogisticsEntity> implements SoB2cLogisticsService {

    @Resource
    private CommonService commonService;

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private SoB2cService soB2cService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private LogisticsBillFeign logisticsBillFeign;

    @Resource
    private LogisticsFeign logisticsFeign;

    @Resource
    private SoB2cReceiverService soB2cReceiverService;

    @Override
    public Boolean add(SoB2cLogisticsDTO.AddDTO logisticsDTO, String mainId) {
        SoB2cLogisticsEntity entity = new SoB2cLogisticsEntity();
        BeanMapperUtils.copy(logisticsDTO, entity);
        entity.setMainId(mainId);
        handleLogisticsData(entity);
        return this.save(entity);
    }


    @Override
    public Boolean update(SoB2cLogisticsDTO.UpdateDTO logisticsDTO, String mainId) {
        SoB2cLogisticsEntity old = super.getById(logisticsDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "B2C销售订单物流信息表"));
        SoB2cLogisticsEntity entity = new SoB2cLogisticsEntity();
        BeanMapperUtils.copy(logisticsDTO, entity);
        entity.setMainId(mainId);
        handleLogisticsData(entity);

        boolean update = this.updateById(entity);

        //主表信息
        SoB2cEntity soB2cEntity = soB2cService.getById(old.getMainId());
        // 记录主单操作日志
        log.info("编辑 开始记录B2C销售订单表日志数据，单号：【{}】", soB2cEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), soB2cEntity.getCode(), "B2C销售订单表");
        operateLogService.addModuleOperateLogByObj(old, entity, ModuleTypeEnum.SO_B2C.getCode(), soB2cEntity.getId(), msg);
        return update;
    }

    /**
     * @param entity
     * @description: 数据处理
     * @author Will
     * @date: 2023/11/10 15:59
     */
    private void handleLogisticsData(SoB2cLogisticsEntity entity) {
        String accessoriesSkuId = entity.getAccessoriesSkuId();
        String accessoriesSkuNo = "";
        if (StringUtils.isNotBlank(accessoriesSkuId)) {
            List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(Arrays.asList(entity.getAccessoriesSkuId()));
            if (CollectionUtils.isNotEmpty(skuList)) {
                accessoriesSkuNo = skuList.get(0).getSkuNo();
            }
        }
        entity.setAccessoriesSkuNo(accessoriesSkuNo);
        //渠道id
        String logisticsChannelId = entity.getLogisticsChannelId();
        String logisticsChannelName = "";
        if (StringUtils.isNotBlank(logisticsChannelId)) {
            LogisticsChannelEntity channelEntity = logisticsFeign.getChannelById(logisticsChannelId);
            if (Objects.nonNull(channelEntity)) {
                logisticsChannelName = channelEntity.getName();
            }

        }
        entity.setLogisticsChannelName(logisticsChannelName);
        entity.setEstimatedShippingCost(ObjectUtil.isEmpty(entity.getEstimatedShippingCost()) ? BigDecimal.ZERO : entity.getEstimatedShippingCost());
        entity.setAccessoriesCost(ObjectUtil.isEmpty(entity.getAccessoriesCost()) ? BigDecimal.ZERO : entity.getAccessoriesCost());
        entity.setActualShippingCost(ObjectUtil.isEmpty(entity.getActualShippingCost()) ? BigDecimal.ZERO : entity.getActualShippingCost());
        entity.setAccessoriesQty(ObjectUtil.isEmpty(entity.getAccessoriesQty()) ? MathUtil.ZERO : entity.getAccessoriesQty());
        entity.setAccessoriesNw(ObjectUtil.isEmpty(entity.getAccessoriesNw()) ? BigDecimal.ZERO : entity.getAccessoriesNw());
        entity.setWeight(ObjectUtil.isEmpty(entity.getWeight()) ? BigDecimal.ZERO : entity.getWeight());
        entity.setHeight(ObjectUtil.isEmpty(entity.getHeight()) ? BigDecimal.ZERO : entity.getHeight());
        entity.setWidth(ObjectUtil.isEmpty(entity.getWidth()) ? BigDecimal.ZERO : entity.getWidth());
        entity.setLength(ObjectUtil.isEmpty(entity.getLength()) ? BigDecimal.ZERO : entity.getLength());
        entity.setAccessoriesSkuId(StrUtil.isBlank(entity.getAccessoriesSkuId()) ? "" : entity.getAccessoriesSkuId());
    }

    @Override
    public SoB2cLogisticsEntity getByMainId(String mainId) {
        return lambdaQuery().eq(SoB2cLogisticsEntity::getMainId, mainId).one();
    }

    private List<SoB2cLogisticsEntity> getListByMainId(String mainId) {
        return lambdaQuery().eq(SoB2cLogisticsEntity::getMainId, mainId).list();
    }

    @Override
    public List<SoB2cLogisticsEntity> listByMainIds(List<String> mainIds) {
        if (CollectionUtils.isEmpty(mainIds)) {
            return Collections.EMPTY_LIST;
        }
        return lambdaQuery().in(SoB2cLogisticsEntity::getMainId, mainIds).list();
    }

    @Override
    public Boolean deleteByMainIds(List<String> mainIds) {
        return lambdaUpdate().in(SoB2cLogisticsEntity::getMainId, mainIds).remove();
    }

    @Override
    public Boolean updateLogisticsCode(String mainId, String transportNo, String trackNo) {
        return lambdaUpdate().eq(SoB2cLogisticsEntity::getMainId, mainId).
                set(SoB2cLogisticsEntity::getCode, transportNo).
                set(SoB2cLogisticsEntity::getTrackNo, trackNo).
                update(new SoB2cLogisticsEntity());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SoB2cLogisticsEntity saveOrUpdateEntity(PlatformOrderDTO dto, SoB2cEntity mainEntity, BigDecimal allNetWeight,
                                                   BigDecimal maxLength, BigDecimal maxWidth, BigDecimal totalHeight) {
//        if (Objects.isNull(mainEntity) || StrUtil.isBlank(mainEntity.getId())) return;
        boolean isShopee = LogisticsPlatformEnum.SHOPEE.getCode().equals(dto.getDictPlatform());
        List<PlatformOrderLogisticsDTO> logisticsList = dto.getLogisticsList();
        //获取主表下物流记录
        SoB2cLogisticsEntity oldEntity = getByMainId(mainEntity.getId());
        if (CollectionUtils.isEmpty(logisticsList)) {
            if (null == oldEntity) {
                SoB2cLogisticsEntity entity = B2cOrderConsumerConverter.INSTANCE.convertNewLogistics(null, mainEntity.getId(), allNetWeight, maxLength, maxWidth, totalHeight);
                entity.setMainId(mainEntity.getId());
                entity.setWeight(allNetWeight);
                entity.setLength(maxLength);
                entity.setWidth(maxWidth);
                entity.setHeight(totalHeight);
                handleLogisticsData(entity);
                // 无信息新增空表
                if (!this.save(entity)) {
                    throw new ServiceException("[SoB2cLogisticsEntity] 保存失败");
                }
                return entity;
            } else {
                oldEntity.setWeight(allNetWeight);
                oldEntity.setLength(maxLength);
                oldEntity.setWidth(maxWidth);
                oldEntity.setHeight(totalHeight);
                if (!this.updateById(oldEntity)) {
                    throw new ServiceException("[SoB2cLogisticsEntity] 保存失败");
                }
                return oldEntity;
            }
        }
        // 暂时使用第一个
        PlatformOrderLogisticsDTO platformOrderLogisticsDTO = logisticsList.get(0);

        List<LogisticsBillDTO.AddDTO> addDTOList = new ArrayList<>();


        //获取主表下物流记录
        List<SoB2cLogisticsEntity> listByMainId = getListByMainId(mainEntity.getId());
        //转map 比较是否存在记录 不存在则删除 存在则更新
//        Map<String, SoB2cLogisticsEntity> map = listByMainId.stream().collect(Collectors.toMap(SoB2cLogisticsEntity::getCode, Function.identity()));
        SoB2cLogisticsEntity entity = null;
        if (CollectionUtils.isNotEmpty(listByMainId)) {
            entity = listByMainId.get(0);//跨店铺拆单需要修改这里
        }
//            SoB2cLogisticsEntity entity = map.get(platformOrderLogisticsDTO.getCode());
            if (Objects.isNull(entity)) {
                entity = B2cOrderConsumerConverter.INSTANCE.convertNewLogistics(platformOrderLogisticsDTO, mainEntity.getId(), allNetWeight,maxLength,maxWidth,totalHeight);
                entity.setMainId(mainEntity.getId());
                handleLogisticsData(entity);
                if (isShopee && StringUtils.isNotEmpty(entity.getLogisticsChannelName())){
                    //虾皮存在渠道名称不存在渠道id 特殊处理
                    List<LogisticsChannelEntity> channelByNames = logisticsFeign.getChannelByName(entity.getLogisticsChannelName());
                    if (CollectionUtils.isNotEmpty(channelByNames)){
                        entity.setLogisticsChannelId(channelByNames.get(0).getId());
                    }
                }
                if (!this.save(entity)) {
                    throw new ServiceException("[SoB2cLogisticsEntity] 保存失败");
                }
                if (isShopee) {
                    addDTOList.add(buildLogisticsBill(entity, mainEntity));
                }
            } else {
                SoB2cLogisticsEntity entity2 = new SoB2cLogisticsEntity();
                BeanMapperUtils.copy(platformOrderLogisticsDTO, entity2);
                if (isShopee && StringUtils.isNotEmpty(entity2.getLogisticsChannelName())){
                    //虾皮存在渠道名称不存在渠道id 特殊处理
                    List<LogisticsChannelEntity> channelByNames = logisticsFeign.getChannelByName(entity2.getLogisticsChannelName());
                    if (CollectionUtils.isNotEmpty(channelByNames)){
                        entity2.setLogisticsChannelId(channelByNames.get(0).getId());
                    }
                }
                // 保留历史
//                if (null != entity.getWeight() && entity.getWeight().compareTo(BigDecimal.ZERO) > 0){
//                    allNetWeight = entity.getWeight();
//                }
//                if (null != entity.getLength() && entity.getLength().compareTo(BigDecimal.ZERO) > 0){
//                    maxLength = entity.getLength();
//                }
//                if (null != entity.getWidth() && entity.getWidth().compareTo(BigDecimal.ZERO) > 0){
//                    maxWidth = entity.getWidth();
//                }
//                if (null != entity.getHeight() && entity.getHeight().compareTo(BigDecimal.ZERO) > 0){
//                    totalHeight  = entity.getHeight();
//                }
                entity2.setWeight(allNetWeight);
                entity2.setLength(maxLength);
                entity2.setWidth(maxWidth);
                entity2.setHeight(totalHeight);
                entity2.setId(entity.getId());
                //如果美客多平台订单不是平台仓发货，不更新物流单号
                if (PlatformDictEnum.MERCADOLIBRE.getCode().equalsIgnoreCase(dto.getDictPlatform())) {
                    if (!mainEntity.hasPlatformWarehouseOrder()) {
                        entity.setCode(oldEntity.getCode());
                    }
                }
                if (!this.updateById(entity2)) {
                    throw new ServiceException("[SoB2cLogisticsEntity] 更新失败");
                }
                entity = entity2;
                if (isShopee) {
                    addDTOList.add(buildLogisticsBill(entity, mainEntity));
                }
            }
        //虾皮物流订单新增 TMS物流单号记录
        if (isShopee) {
            try {
                logisticsBillFeign.logisticsBillBatchSave(addDTOList);
            } catch (Exception e) {
                log.error("同步物流单异常：{}", addDTOList);
            }

        }
        return entity;
    }

    @Override
    public SoB2cDTO.ShippingCalculationDTO getShippingCalculationByOrderId(String orderId) {
        //买家信息
        SoB2cReceiverEntity receiverEntity = soB2cReceiverService.getByMainId(orderId);
        if (Objects.isNull(receiverEntity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_RECEIVER_NOT_NULL);
        }
        SoB2cLogisticsEntity entity = this.getByMainId(orderId);
        if (Objects.isNull(entity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_NOT_EXIST);
        }
        SoB2cDTO.ShippingCalculationDTO shippingCalculationDTO = new SoB2cDTO.ShippingCalculationDTO();
        shippingCalculationDTO.setWeight(entity.getWeight());
        shippingCalculationDTO.setLength(entity.getLength());
        shippingCalculationDTO.setWidth(entity.getWidth());
        shippingCalculationDTO.setHeight(entity.getHeight());
        shippingCalculationDTO.setToCountry(receiverEntity.getCountry());
        shippingCalculationDTO.setToCountryName(receiverEntity.getCountryName());
        shippingCalculationDTO.setFromCountry(CountrySiteEnum.CHINA.getSite());
        shippingCalculationDTO.setWeightUnit(UnitEnum.WeightUnitEnum.G.getCode());
        return shippingCalculationDTO;
    }

    @Override
    public List<SoB2cLogisticsEntity> listByChannelId(String channelId) {
        return this.lambdaQuery().eq(SoB2cLogisticsEntity::getLogisticsChannelId, channelId).list();
    }

    @Override
    public List<SoB2cLogisticsDTO.TrackNoDTO> listTrackNoEmptyList() {
        return baseMapper.listTrackNoEmptyList();
    }

    @Override
    public Boolean updateDeliveryTimeByMainIds(List<String> mainIds, LocalDateTime deliveryTime) {
        if (CollectionUtils.isEmpty(mainIds)) {
            return Boolean.FALSE;
        }
        return lambdaUpdate()
                .set(SoB2cLogisticsEntity::getDeliveryTime, deliveryTime)
                .in(SoB2cLogisticsEntity::getMainId, mainIds)
                .update();
    }

    @Override
    public SoB2cLogisticsEntity getSoB2cLogisticsByTrackNo(String trackNo) {
        if (StringUtils.isBlank(trackNo)) {
            return null;
        }
        return lambdaQuery().eq(SoB2cLogisticsEntity::getTrackNo, trackNo).last("LIMIT 1").one();
    }

    @Override
    public Boolean clearB2cLogisticsCode(List<String> soIdList) {
        if (CollectionUtils.isEmpty(soIdList)) {
            return Boolean.FALSE;
        }
        //清空物流单号
        lambdaUpdate()
                .set(SoB2cLogisticsEntity::getCode, "")
                .set(SoB2cLogisticsEntity::getTrackNo, "")
                .in(SoB2cLogisticsEntity::getMainId, soIdList)
                .update();

        //删除物流单
        return logisticsBillFeign.removeLogisticsBillBySourceId(soIdList);
    }

    @Override
    public Boolean updateWeight(String soId,String id, BigDecimal weightByG) {
        String msg = StrUtil.format("用户【{}】更新重量为{} ", commonService.getUserInfo().getUserName(),weightByG+"g");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C.getCode(), soId, msg);
        return lambdaUpdate()
                .set(SoB2cLogisticsEntity::getWeight, weightByG)
                .eq(SoB2cLogisticsEntity::getId, id)
                .update();
    }

    private LogisticsBillDTO.AddDTO buildLogisticsBill(SoB2cLogisticsEntity entity, SoB2cEntity mainEntity) {
        LogisticsBillDTO.AddDTO addDTO = new LogisticsBillDTO.AddDTO();
        addDTO.setShopId(mainEntity.getShopId());
        addDTO.setShopName(mainEntity.getShopName());
        addDTO.setSalesPlatform(LogisticsPlatformEnum.SHOPEE.getCode());
        addDTO.setDeliveryTime(entity.getDeliveryTime());
        addDTO.setOrderTime(mainEntity.getPayTime());
        addDTO.setTransportNo(entity.getCode());
        addDTO.setDetailList(buildDetailList(entity));
        addDTO.setSourceId(mainEntity.getId());
        addDTO.setSourceCode(mainEntity.getPlatformCode());
        addDTO.setSourceType(SourceTypeEnum.SO_INFO.getCode());
        addDTO.setOutstockId("");
        addDTO.setOutstockCode("");
        addDTO.setChannelId(entity.getLogisticsChannelId());
        return addDTO;
    }

    private List<LogisticsBillDetailDTO.AddDTO> buildDetailList(SoB2cLogisticsEntity entity) {
        List<LogisticsBillDetailDTO.AddDTO> addDTOList = new ArrayList<>();
        LogisticsBillDetailDTO.AddDTO addDTO = new LogisticsBillDetailDTO.AddDTO();
        addDTO.setTrackNo(entity.getCode());
        addDTO.setTrackStatus("0");
        addDTOList.add(addDTO);
        return addDTOList;
    }
}
