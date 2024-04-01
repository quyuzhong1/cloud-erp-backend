package com.erp.server.wms.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.enums.UnitEnum;
import com.common.core.constant.EnumMessage;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.oms.enums.TransferStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.entity.TransferDeclareDetailEntity;
import com.erp.model.tms.enums.TransferDeclareUploadStatusEnum;
import com.erp.model.wms.dto.WeightingOutboundDTO;
import com.erp.model.wms.entity.SoB2cDeliveryEntity;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.tms.feign.TransferDeclareFeign;
import com.erp.server.wms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collections;
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
        if (isAutoDelivery) {
            SoB2cEntity soB2cEntity = soB2cFeign.getById(sourceId);
            if (Objects.nonNull(soB2cEntity)) {
                String transferStatus = soB2cEntity.getTransferStatus();
                //表示要中转啊
                if (!TransferStatusEnum.NOT.getCode().equals(transferStatus)) {
                    TransferDeclareDetailEntity transferDeclareDetailEntity = transferDeclareFeign.getBySoId(sourceId);
                    if (Objects.nonNull(transferDeclareDetailEntity)) {
                        String uploadSuccess = TransferDeclareUploadStatusEnum.UPLOAD_SUCCESS.getCode();
                        String uploadStatus = transferDeclareDetailEntity.getOrderUploadStatus();
                        if (!uploadSuccess.equals(uploadStatus)) {
                            throw new ServiceException(ApiError.NOT_TRANSFER_DECLARE);
                        }
                    } else {
                        throw new ServiceException(ApiError.NOT_TRANSFER_DECLARE);
                    }
                }
            }
        }
        if (Objects.nonNull(dto.getWeight())) {
            if (Objects.isNull(dto.getWeightUnit())) {
                throw new ServiceException("称重单位不能为空");
            }
            if (Objects.isNull(EnumMessage.getNameByCode(UnitEnum.WeightUnitEnum.class, dto.getWeightUnit()))) {
                throw new ServiceException("非法称重单位");
            }
            entity.setWeight(dto.getWeight());
            entity.setWeightUnit(dto.getWeightUnit());
            entity.setIsWeigh(true);
            if (!soB2cDeliveryService.updateById(entity)) {
                throw new ServiceException("发货单更新失败");
            }
            String msg = StrUtil.format("用户【{}】更新【{}】单据单号为【{}】称重出库完成", commonService.getUserInfo().getUserName(), "b2c发货单", entity.getCode());
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C_DELIVERY.getCode(), entity.getId(), "称重出库");
        }
        TransferDeclareDetailEntity declareDetailEntity = new TransferDeclareDetailEntity();
        if (isAutoDelivery && entity.getIsWeigh()) {
            declareDetailEntity = transferDeclareFeign.getBySoId(entity.getSourceId());
            if (ObjectUtil.isEmpty(declareDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_TRANSFER_DECLARE_NOT_EXIST);
            }
            //如果是待上传或上传失败则直接返回
            if (StrUtil.equals(declareDetailEntity.getOrderUploadStatus(),TransferDeclareUploadStatusEnum.WAIT_UPLOAD.getCode()) ||
                    StrUtil.equals(declareDetailEntity.getOrderUploadStatus(),TransferDeclareUploadStatusEnum.UPLOAD_FAILURE.getCode())) {
                return this.buildViewDTO(entity,declareDetailEntity.getTransferStatus(),declareDetailEntity.getOrderUploadStatus());
            }

            //将发货状态更新为已发货
            entity.setStatus(SoB2cBillStatusEnum.ENUM_SHIPPED.getCode());
            if (!soB2cDeliveryService.updateById(entity)) {
                throw new ServiceException("发货单更新失败");
            }
            soB2cFeign.updateSoB2cStatus(Collections.singletonList(entity.getSourceId()), SoB2cBillStatusEnum.ENUM_SHIPPED.getCode());

            soB2cDeliveryService.generateB2cSoOutstock(entity);

        }
        return this.buildViewDTO(entity,declareDetailEntity.getTransferStatus(),declareDetailEntity.getOrderUploadStatus());
    }

    @Override
    public void reset(String id) {
        SoB2cDeliveryEntity entity = soB2cDeliveryService.getById(id);
        if (Objects.isNull(entity)) {
            throw new ServiceException("查询的发货单为空");
        }
        entity.setIsWeigh(false);
        entity.setWeightUnit(UnitEnum.WeightUnitEnum.G.getCode());
        entity.setWeight(BigDecimal.ZERO);
        if (!soB2cDeliveryService.updateById(entity)) {
            throw new ServiceException("发货单更新失败");
        }
    }

    private WeightingOutboundDTO.ViewDTO buildViewDTO(SoB2cDeliveryEntity entity,String transferStatus,String orderUploadStatus) {
        return WeightingOutboundDTO.ViewDTO.builder()
                .id(entity.getId())
                .code(entity.getSoCode())
                .transportNo(entity.getTransportNo())
                .weight(entity.getWeight())
                .weightUnit(entity.getWeightUnit())
                .status(entity.getIsWeigh())
                .transferStatus(transferStatus)
                .orderUploadStatus(orderUploadStatus)
                .build();
    }
}
