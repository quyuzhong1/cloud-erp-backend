package com.erp.server.oms.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.dto.PlatformOrderLogisticsDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.constant.SqlConstants;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.enums.CountrySiteEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.common.message.constant.RedisKeyConstant;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.dto.SoB2cLogisticsDTO;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.model.oms.entity.SoB2cReceiverEntity;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.oms.enums.SoB2cErrorTypeEnum;
import com.erp.model.oms.enums.SoB2cLogisticSourceSystemEnum;
import com.erp.model.oms.enums.TransferStatusEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.LogisticsBillDTO;
import com.erp.model.tms.dto.LogisticsBillDetailDTO;
import com.erp.model.tms.dto.LogisticsChannelDTO;
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
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    private OperateLogService operateLogService;
    @Lazy
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

    @Resource
    private SoB2cLabelService soB2cLabelService;

    @Resource
    private SoB2cErrorService soB2cErrorService;
    @Override
    public Boolean add(SoB2cLogisticsDTO.AddDTO logisticsDTO, String mainId) {
        SoB2cLogisticsEntity entity = new SoB2cLogisticsEntity();
        BeanMapperUtils.copy(logisticsDTO, entity);
        entity.setMainId(mainId);
        handleLogisticsData(entity);
        if(StringUtils.isNotBlank(entity.getCode()) ){
            entity.setSourceSystem(SoB2cLogisticSourceSystemEnum.ERP.getCode());
        }
        return this.save(entity);
    }


    @Override
    public Boolean update(SoB2cLogisticsDTO.UpdateDTO logisticsDTO, String mainId) {
        SoB2cLogisticsEntity old = super.getById(logisticsDTO.getId());
        if(null == old){
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "B2C销售订单物流信息表");
        }
        if(StringUtils.isNotBlank(old.getCode()) && old.getSourceSystem().equals(SoB2cLogisticSourceSystemEnum.THIRD.getCode())){
            if(!old.getLogisticsChannelId().equals(logisticsDTO.getLogisticsChannelId()) || !old.getCode().equals(logisticsDTO.getCode())){
                throw new ServiceException("请先取消物流单后修改渠道和单号信息");
            }
        }

        SoB2cLogisticsEntity entity = new SoB2cLogisticsEntity();
        BeanMapperUtils.copy(logisticsDTO, entity);
        if(!old.getLogisticsChannelId().equals(logisticsDTO.getLogisticsChannelId()) || !old.getCode().equals(logisticsDTO.getCode())){
            entity.setSourceSystem(SoB2cLogisticSourceSystemEnum.ERP.getCode());
        }
        entity.setMainId(mainId);
        handleLogisticsData(entity);

        boolean update = this.updateById(entity);

        //主表信息
        SoB2cEntity soB2cEntity = soB2cService.getById(old.getMainId());
        // 记录主单操作日志
        log.info("编辑 开始记录B2C销售订单表日志数据，单号：【{}】", soB2cEntity.getCode());
        String msg =  CharSequenceUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), soB2cEntity.getCode(), "B2C销售订单表");
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
            List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(Arrays.asList(entity.getAccessoriesSkuId()));
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
        return lambdaQuery().eq(SoB2cLogisticsEntity::getMainId, mainId).last(" limit 1 ").one();
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
    public Boolean updateLogisticsCode(String mainId, String transportNo, String trackNo, String iossTaxNo) {
        return lambdaUpdate().eq(SoB2cLogisticsEntity::getMainId, mainId).
                set(SoB2cLogisticsEntity::getCode, transportNo).
                set(SoB2cLogisticsEntity::getTrackNo, trackNo).
                set(SoB2cLogisticsEntity::getIossTaxNo, iossTaxNo).
                set(SoB2cLogisticsEntity::getSourceSystem, SoB2cLogisticSourceSystemEnum.THIRD.getCode()).
                update();
    }

    @Override
    public Boolean updateTransferInfo(List<SoB2cLogisticsEntity> updateLogisticList) {
        if(CollectionUtils.isEmpty(updateLogisticList)){
            return true;
        }
        Map<String,List<SoB2cLogisticsEntity>> updateMap = updateLogisticList.stream().collect(Collectors.groupingBy(SoB2cLogisticsEntity::getTransferLogisticsChannelId));
        updateMap.forEach((key,val)->{
            String transferLogisticsSupplierId = val.get(0).getTransferLogisticsSupplierId();
            List<String> ids = val.stream().map(v->v.getId()).collect(Collectors.toList());
            lambdaUpdate().in(SoB2cLogisticsEntity::getId, ids).
                    set(SoB2cLogisticsEntity::getTransferLogisticsSupplierId, transferLogisticsSupplierId).
                    set(SoB2cLogisticsEntity::getTransferLogisticsChannelId, key).
                    update();
        });
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SoB2cLogisticsEntity saveOrUpdateEntity(PlatformOrderDTO dto, SoB2cEntity mainEntity, BigDecimal allNetWeight,
                                                   BigDecimal maxLength, BigDecimal maxWidth, BigDecimal totalHeight) {
        boolean isShopee = LogisticsPlatformEnum.SHOPEE.getCode().equals(dto.getDictPlatform());
        List<PlatformOrderLogisticsDTO> logisticsList = dto.getLogisticsList();
        //获取主表下物流记录
        SoB2cLogisticsEntity oldEntity = getByMainId(mainEntity.getId());
        if (CollectionUtils.isEmpty(logisticsList)) {
            if (null == oldEntity) {
                SoB2cLogisticsEntity entity = B2cOrderConsumerConverter.INSTANCE.convertNewLogistics(null, mainEntity.getId(), allNetWeight, maxLength, maxWidth, totalHeight);
                handleLogisticsData(entity);
                // 无信息新增空表
                if (!this.save(entity)) {
                    throw new ServiceException("[SoB2cLogisticsEntity] 保存失败");
                }
                return entity;
            } else {
                // 保留历史
                if (null != oldEntity.getWeight() && oldEntity.getWeight().compareTo(BigDecimal.ZERO) > 0){
                    allNetWeight = oldEntity.getWeight();
                }
                if (null != oldEntity.getLength() && oldEntity.getLength().compareTo(BigDecimal.ZERO) > 0){
                    maxLength = oldEntity.getLength();
                }
                if (null != oldEntity.getWidth() && oldEntity.getWidth().compareTo(BigDecimal.ZERO) > 0){
                    maxWidth = oldEntity.getWidth();
                }
                if (null != oldEntity.getHeight() && oldEntity.getHeight().compareTo(BigDecimal.ZERO) > 0){
                    totalHeight  = oldEntity.getHeight();
                }
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
        //获取主表下物流记录
        List<SoB2cLogisticsEntity> listByMainId = getListByMainId(mainEntity.getId());
        //跨店铺拆单需要修改这里
        SoB2cLogisticsEntity entity = CollectionUtils.isNotEmpty(listByMainId) ? listByMainId.get(0) : null;
        //重置物流信息
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
                if (null != entity.getWeight() && entity.getWeight().compareTo(BigDecimal.ZERO) > 0){
                    allNetWeight = entity.getWeight();
                }
                if (null != entity.getLength() && entity.getLength().compareTo(BigDecimal.ZERO) > 0){
                    maxLength = entity.getLength();
                }
                if (null != entity.getWidth() && entity.getWidth().compareTo(BigDecimal.ZERO) > 0){
                    maxWidth = entity.getWidth();
                }
                if (null != entity.getHeight() && entity.getHeight().compareTo(BigDecimal.ZERO) > 0){
                    totalHeight  = entity.getHeight();
                }
                if (StringUtils.isNotBlank(entity.getCode())){
                    entity2.setCode(entity.getCode());
                }
                if (StringUtils.isNotBlank(entity.getTrackNo())){
                    entity2.setTrackNo(entity.getTrackNo());
                }
                //如果美客多平台订单不是平台仓发货，不更新物流单号
                if (PlatformDictEnum.MERCADOLIBRE.getCode().equalsIgnoreCase(dto.getDictPlatform())
                        || PlatformDictEnum.MERCADOLIBRE_LOCAL.getCode().equalsIgnoreCase(dto.getDictPlatform())) {
                    if (!mainEntity.hasPlatformWarehouseOrder()) {
                        entity.setCode(oldEntity.getCode());
                    }
                }
                entity2.setWeight(allNetWeight);
                entity2.setLength(maxLength);
                entity2.setWidth(maxWidth);
                entity2.setHeight(totalHeight);
                entity2.setId(entity.getId());

                if (!this.updateById(entity2)) {
                    throw new ServiceException("[SoB2cLogisticsEntity] 更新失败");
                }
                entity = entity2;
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
    public List<SoB2cLogisticsDTO.TrackNoDTO> listTrackNoEmptyList(SoB2cDTO.QueryDTO queryDTO) {
        return baseMapper.listTrackNoEmptyList(queryDTO);
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
        return lambdaQuery().eq(SoB2cLogisticsEntity::getTrackNo, trackNo).last( SqlConstants.LIMIT_1).one();
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
    public Boolean updateWeight(String soId, String id, BigDecimal weightByG, String operation) {
        String msg =  CharSequenceUtil.format("用户【{}】更新重量为{} ", UserContext.getDefaultLoginUser().getUserName(),weightByG+"g");
        if(!MathUtil.isValidNumber(weightByG,12)){
            throw new ServiceException("重量过大，整数最大值不能超过12位");
        }
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C.getCode(), soId, operation);
        return lambdaUpdate()
                .set(SoB2cLogisticsEntity::getWeight, weightByG)
                .eq(SoB2cLogisticsEntity::getMainId, soId)
                .eq(SoB2cLogisticsEntity::getId, id)
                .update();
    }

    @Override
    public SoB2cLogisticsEntity getByTrackNoOrTransportNo(String logisticsCode) {
        return baseMapper.getByTrackNoOrTransportNo(logisticsCode);
    }

    @Override
    @DistributeLocker(businessType = RedisKeyConstant.SO_B2C_ORDER_KEY,keyName = "id",waiteTime = 60)
    public BatchResultDTO cancelLogistic(String id, List<SoB2cEntity> soB2cEntityList, List<SoB2cLogisticsEntity> soB2cLogisticsEntityList, Boolean checkBillStatus) {
        SoB2cEntity soB2cEntity = soB2cEntityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
        if(Objects.isNull(soB2cEntity)){
            return BatchResultDTO.fail(id,id,"找不到销售订单");
        }
        SoB2cLogisticsEntity soB2cLogisticsEntity = soB2cLogisticsEntityList.stream().filter(v->v.getMainId().equals(id)).findFirst().orElse(null);
        if(Objects.isNull(soB2cLogisticsEntity)){
            return BatchResultDTO.fail(id,soB2cEntity.getCode(),"找不到物流单");
        }
        //已存在的渠道为空
        if (StringUtils.isBlank(soB2cLogisticsEntity.getLogisticsChannelId())) {
            return BatchResultDTO.fail(id,soB2cEntity.getCode(),"物流渠道为空");
        }
        if (checkBillStatus && StringUtils.isBlank(soB2cLogisticsEntity.getCode())) {
            return BatchResultDTO.fail(id,soB2cEntity.getCode(),"未获取跟踪号，无法取消");
        }
        if (!SoB2cBillStatusEnum.ENUM_IN_DISTRIBUTION.getCode().equals(soB2cEntity.getBillStatus()) && checkBillStatus) {
            return BatchResultDTO.fail(id,soB2cEntity.getCode(),"只有配货中的订单可以取消");
        }
        if(TransferStatusEnum.SUCCESS.getCode().equals(soB2cEntity.getTransferStatus()) && checkBillStatus){
            throw new ServiceException( CharSequenceUtil.format("订单信息已预报，请取消订单预报后支持重新获取跟踪号"));
        }
        //取消物流单
        LogisticsBillDTO.CancelBillDTO cancelBillDTO = LogisticsBillDTO.CancelBillDTO.builder().
                channelId(soB2cLogisticsEntity.getLogisticsChannelId())
                .transportNo(soB2cLogisticsEntity.getCode())
                .referenceNumber(soB2cEntity.getCode())
                .platformCode(soB2cEntity.getPlatformCode())
                .shopId(soB2cEntity.getShopId())
                .build();
        String sourceSystem = soB2cLogisticsEntity.getSourceSystem();
        ApiResult cancelResult = ApiResult.success();
        if (SoB2cLogisticSourceSystemEnum.THIRD.getCode().equals(sourceSystem)){
            cancelResult = logisticsBillFeign.cancelBill(cancelBillDTO);
        }
        //取消失败
        if (!cancelResult.isSuccess() && cancelResult.getCode()!=-1) {
            log.error("取消物流单失败,单号:【{}/{}】,{} ", soB2cLogisticsEntity.getCode(),soB2cLogisticsEntity.getTrackNo(),cancelResult.getMsg());
            return BatchResultDTO.fail(id,soB2cEntity.getCode(),cancelResult.getMsg());
        }else{
            String msg =  CharSequenceUtil.format("取消物流单单号成功,单号:【{}/{}】 ", soB2cLogisticsEntity.getCode(),soB2cLogisticsEntity.getTrackNo());
            operateLogService.addModuleOperateLog(msg ,ModuleTypeEnum.SO_B2C.getCode(), soB2cEntity.getId(), "取消物流单");
            soB2cLogisticsEntity.setCode("");
            soB2cLogisticsEntity.setTrackNo("");
            this.updateById(soB2cLogisticsEntity);
            //清空面单信息
            soB2cLabelService.deleteByMainIds(Arrays.asList(id));
            //清空获取面单异常
            soB2cErrorService.removeErrorOrder(id, SoB2cErrorTypeEnum.GET_LOGISTICS_LABEL.getCode());
            return BatchResultDTO.success(id,soB2cEntity.getCode(),"取消成功");
        }

    }

    @Override
    public void updateLogisticsBySoId(String soId, String trackNo) {
        if (CharSequenceUtil.isNotBlank(soId)){
            this.lambdaUpdate().eq(SoB2cLogisticsEntity::getMainId, soId).set(SoB2cLogisticsEntity::getTrackNo, trackNo).update();
        }
    }

    @Override
    public void updateTrackNoByTransportNo(List<LogisticsBillDTO.TrackDTO> trackDTOS) {
        if (CollectionUtils.isEmpty(trackDTOS)){
            return;
        }
        trackDTOS = trackDTOS.stream().filter(e -> Objects.nonNull(e) && StringUtils.isNotBlank(e.getTransportNo()) && StringUtils.isNotBlank(e.getTrackNo()))
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(trackDTOS)){
            baseMapper.updateTrackNoByTransportNo(trackDTOS);
        }
    }

    @Override
    public void updateLogisticsFee(String b2cSoId, BigDecimal totalShippingCost, String currency) {
        if (CharSequenceUtil.isBlank(b2cSoId) || Objects.isNull(totalShippingCost) || CharSequenceUtil.isBlank(currency)){
            return;
        }
        this.lambdaUpdate().eq(SoB2cLogisticsEntity::getMainId, b2cSoId)
                .set(SoB2cLogisticsEntity::getEstimatedShippingCost, totalShippingCost).set(SoB2cLogisticsEntity::getEstimatedShippingCurrency,currency).update();
    }

    @Override
    public List<SoB2cLogisticsDTO.transferOrderDTO> transferOrderView(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)){
            return Collections.EMPTY_LIST;
        }
        return baseMapper.transferOrderView(ids);
    }

    @Override
    public BatchResultDTO transferOrderSave(SoB2cLogisticsDTO.transferOrderDTO dto, LogisticsChannelDTO.BaseDTO channel, SoB2cEntity soB2cEntity) {
        if (CharSequenceUtil.isBlank(dto.getTransportNo())){
            dto.setTransportNo(dto.getTrackNo());
        }
        //获取物流单id
        SoB2cLogisticsEntity logisticsEntity = this.getById(dto.getLogisticsId());
        String msg = CharSequenceUtil.format("物流信息变更 物流渠道由【{}】改为【{}】,物流跟踪号由【{}】改为【{}】，物流运单号由【{}】改为【{}】"
                ,logisticsEntity.getLogisticsChannelName(),channel.getName(),logisticsEntity.getTrackNo(),dto.getTrackNo(),logisticsEntity.getCode(),dto.getTransportNo());
        this.lambdaUpdate().eq(SoB2cLogisticsEntity::getId, dto.getLogisticsId())
               .set(SoB2cLogisticsEntity::getLogisticsChannelId, channel.getId())
               .set(SoB2cLogisticsEntity::getCode, dto.getTransportNo())
               .set(SoB2cLogisticsEntity::getTrackNo, dto.getTrackNo()).update();
         operateLogService.addModuleOperateLog(msg,ModuleTypeEnum.SO_B2C.getCode(), soB2cEntity.getId(), "物流转单");
        return BatchResultDTO.success(dto.getId(),soB2cEntity.getCode(),"转单成功");
    }
}
