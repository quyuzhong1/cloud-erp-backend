package com.erp.server.wms.service.impl;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.PlatformDeliveryInterceptDTO;
import com.common.business.dto.PlatformShipOrderDTO;
import com.common.business.enums.UnitEnum;
import com.common.business.handler.PlatformSaveHandler;
import com.common.core.constant.EnumMessage;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.oms.enums.TransferStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.entity.TransferDeclareDetailEntity;
import com.erp.model.tms.enums.TransferDeclareUploadStatusEnum;
import com.erp.model.wms.dto.WeightingOutboundDTO;
import com.erp.model.wms.entity.SoB2cDeliveryEntity;
import com.erp.model.wms.enums.SoB2cDeliveryStatusEnum;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.tms.feign.TransferDeclareFeign;
import com.erp.server.wms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author liuruipeng
 * @date 2023年12月13日 20:03
 */
@Slf4j
@Service
public class WeightingOutboundServiceImpl implements WeightingOutboundService {

    @Resource
    private SoB2cDeliveryService soB2cDeliveryService;

    @Resource
    private SoOutstockService soOutstockService;

    @Resource
    private SoB2cFeign soB2cFeign;

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private CommonService commonService;

    @Resource
    private TransferDeclareFeign transferDeclareFeign;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WeightingOutboundDTO.ViewDTO scan(WeightingOutboundDTO.ScanDTO dto) {
        SoB2cDeliveryEntity entity = soB2cDeliveryService.getByBusinessCode(dto.getBusinessCode());
        if (Objects.isNull(entity)) {
            throw new ServiceException("单号在系统不存在");
        }

        //是否自动发货
        Boolean isAutoDelivery = dto.getIsAutoDelivery();
        String sourceId = entity.getSourceId();

        TransferDeclareDetailEntity  declareDetailEntity = transferDeclareFeign.getBySoId(entity.getSourceId());
        if (ObjectUtil.isEmpty(declareDetailEntity)) {
            declareDetailEntity = new TransferDeclareDetailEntity();
        }
        SoB2cEntity soB2cEntity = soB2cFeign.getById(sourceId);
        if(ObjectUtil.isEmpty(soB2cEntity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }

        //验证订单平台是否取消
        if (soB2cEntity.getIsCancel()) {
            //订单拦截
            soB2cFeign.deliveryIntercept(new SoB2cDTO.RemarkDTO(soB2cEntity.getId(), "平台取消"));
            return null;
        } else {
            if (soB2cFeign.checkPlatformShipOrder(soB2cEntity.getId())) {
                //如果订单原始状态非取消，这里需要再次调用平台接口查询，是否已取消
                PlatformDeliveryInterceptDTO deliveryInterceptDTO = new PlatformDeliveryInterceptDTO();
                deliveryInterceptDTO.setSoB2cId(soB2cEntity.getId());
                deliveryInterceptDTO.setDictPlatform(soB2cEntity.getDictPlatform());
                deliveryInterceptDTO.setOldIsCancel(soB2cEntity.getIsCancel());
                deliveryInterceptDTO.setPlatformCode(soB2cEntity.getPlatformCode());
                deliveryInterceptDTO.setShopId(soB2cEntity.getShopId());
                Boolean flag = PlatformSaveHandler.deliveryIntercept(deliveryInterceptDTO);
                if (flag) {
                    soB2cFeign.deliveryIntercept(new SoB2cDTO.RemarkDTO(soB2cEntity.getId(), "平台取消"));
                    return null;
                }
            }
        }

        if (soB2cEntity.getIsIntercept()) {
            throw new ServiceException(ApiError.LOGISTICS_INTERCEPT_NOT_PACKAGE);
        }
        if (soB2cEntity.getInvalidStatus()) {
            throw new ServiceException(ApiError.INVALID_NOT_PACKAGE);
        }

        //查询订单物流信息获取跟踪号
        List<SoB2cLogisticsEntity> soB2cLogisticsEntities = soB2cFeign.listSoB2cLogisticsByMainIdList(Arrays.asList(soB2cEntity.getId()));
        //设置物流跟踪单号
        String trackNo = soB2cLogisticsEntities.stream().filter(req -> req.getMainId().equals(soB2cEntity.getId())).map(req -> req.getTrackNo()).findFirst().orElse("");

        if (Objects.nonNull(dto.getWeight())) {
            if (Objects.isNull(dto.getWeightUnit())) {
                throw new ServiceException("称重单位不能为空");
            }
            if (Objects.isNull(EnumMessage.getNameByCode(UnitEnum.WeightUnitEnum.class, dto.getWeightUnit()))) {
                throw new ServiceException("非法称重单位");
            }

            entity.setWeight(dto.getWeight());
            entity.setWeightUnit(dto.getWeightUnit());
            entity.setWeighingTime(LocalDateTime.now());
            entity.setIsWeigh(true);
            if (!soB2cDeliveryService.updateById(entity)) {
                throw new ServiceException("发货单更新失败");
            }
            String msg = StrUtil.format("用户【{}】更新【{}】单据单号为【{}】称重出库完成", commonService.getUserInfo().getUserName(), "b2c发货单", entity.getCode());
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C_DELIVERY.getCode(), entity.getId(), "称重出库");

            //转成g
            BigDecimal weightByG = dto.getWeight();
            if(UnitEnum.WeightUnitEnum.KG.getCode().equals(dto.getWeightUnit())){
                weightByG = dto.getWeight().multiply(BigDecimal.valueOf(1000));
            }

            //更新B2c物流订单重量
            List<SoB2cLogisticsEntity> soB2cLogisticsEntityList = soB2cFeign.listSoB2cLogisticsByMainIdList(Arrays.asList(soB2cEntity.getId()));
            for (SoB2cLogisticsEntity v : soB2cLogisticsEntityList) {
                v.setWeight(weightByG);
            }
            soB2cFeign.batchUpdateLogistics(soB2cLogisticsEntityList);
        }
        //自动发货
        if (isAutoDelivery && entity.getIsWeigh()) {

            //如果是待上传或上传失败则直接返回
            if (StrUtil.equals(soB2cEntity.getTransferStatus(),TransferStatusEnum.WAIT.getCode()) || StrUtil.equals(declareDetailEntity.getOrderUploadStatus(),TransferDeclareUploadStatusEnum.WAIT_UPLOAD.getCode()) ||
                    StrUtil.equals(declareDetailEntity.getOrderUploadStatus(),TransferDeclareUploadStatusEnum.UPLOAD_FAILURE.getCode())) {
                return this.buildViewDTO(entity,soB2cEntity.getTransferStatus(),declareDetailEntity.getOrderUploadStatus(), trackNo);
            }
            //调用第三方平台SDK发货
            try {
                if (soB2cFeign.checkPlatformShipOrder(entity.getSourceId())) {
                    //调用第三方平台SDK发货
                    PlatformShipOrderDTO platformShipOrderDTO = new PlatformShipOrderDTO();
                    platformShipOrderDTO.setSoB2cId(entity.getSourceId());
                    platformShipOrderDTO.setDictPlatform(entity.getDictPlatform());
                    PlatformSaveHandler.shipOrder(platformShipOrderDTO);
                }
            } catch (Exception e) {
                log.error("【称重出库】销售单【{}】 标记发货失败 >>>错误信息{}", entity.getCode(), ExceptionUtil.stacktraceToString(e));
                throw new ServiceException(ApiError.PLATFORM_SHIP_ORDER_ERROR, entity.getDictPlatform(), e.getMessage());
            }

            //获取一个当前时间当作发货时间
            LocalDateTime deliveryTime = LocalDateTime.now();

            //将发货状态更新为已发货
            entity.setStatus(SoB2cDeliveryStatusEnum.SHIPPED.getCode());
            entity.setDeliveryTime(deliveryTime);
            if (!soB2cDeliveryService.updateById(entity)) {
                throw new ServiceException("发货单更新失败");
            }
            //修改订单状态待发货
            SoB2cDTO.UpdateDeliveryTimeDTO updateDeliveryTimeDTO = new SoB2cDTO.UpdateDeliveryTimeDTO();
            updateDeliveryTimeDTO.setSoB2cIds(Arrays.asList(entity.getSourceId()));
            updateDeliveryTimeDTO.setStatus(SoB2cBillStatusEnum.ENUM_SHIPPED.getCode());
            updateDeliveryTimeDTO.setDeliveryTime(deliveryTime);
            soB2cFeign.updateSoB2cStatusAndDeliveryTime(updateDeliveryTimeDTO);

            soB2cDeliveryService.generateB2cSoOutstock(entity);

            String msg = StrUtil.format("用户【{}】通过【{}】触发单据编号【{}】的自动发货功能", commonService.getUserInfo().getUserName(), "称重出库", entity.getCode());
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C_DELIVERY.getCode(), entity.getId(), "称重出库");

        }
        return this.buildViewDTO(entity,soB2cEntity.getTransferStatus(),declareDetailEntity.getOrderUploadStatus(), trackNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reset(String id) {
        SoB2cDeliveryEntity entity = soB2cDeliveryService.getById(id);
        if (Objects.isNull(entity)) {
            throw new ServiceException("查询的发货单为空");
        }
        SoB2cEntity soB2cEntity = soB2cFeign.getById(entity.getSourceId());
        if(ObjectUtil.isEmpty(soB2cEntity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }
        entity.setIsWeigh(false);
        entity.setWeightUnit(UnitEnum.WeightUnitEnum.G.getCode());
        entity.setWeight(BigDecimal.ZERO);
        entity.setWeighingTime(null);
        if (!soB2cDeliveryService.updateById(entity)) {
            throw new ServiceException("发货单更新失败");
        }

        //更新B2c物流订单重量
        List<SoB2cLogisticsEntity> soB2cLogisticsEntityList = soB2cFeign.listSoB2cLogisticsByMainIdList(Arrays.asList(soB2cEntity.getId()));
        for (SoB2cLogisticsEntity v : soB2cLogisticsEntityList) {
            v.setWeight(BigDecimal.ZERO);
        }
        soB2cFeign.batchUpdateLogistics(soB2cLogisticsEntityList);

    }

    private WeightingOutboundDTO.ViewDTO buildViewDTO(SoB2cDeliveryEntity entity,String transferStatus,String orderUploadStatus, String trackNo) {
        return WeightingOutboundDTO.ViewDTO.builder()
                .id(entity.getId())
                .code(entity.getSoCode())
                .transportNo(entity.getTransportNo())
                .trackNo(trackNo)
                .weight(entity.getWeight())
                .weightUnit(entity.getWeightUnit())
                .status(entity.getIsWeigh())
                .transferStatus(transferStatus)
                .orderUploadStatus(orderUploadStatus)
                .build();
    }
}
