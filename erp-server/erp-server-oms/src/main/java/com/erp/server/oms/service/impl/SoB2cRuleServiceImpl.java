package com.erp.server.oms.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.OmsPlatformEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.dto.SoB2cErrorDTO;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.oms.enums.SoB2cErrorTypeEnum;
import com.erp.model.oms.enums.TransferStatusEnum;
import com.erp.model.oms.enums.SoB2cNfeStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.LogisticsMappingDTO;
import com.erp.model.tms.dto.LogisticsSupplierDTO;
import com.erp.model.tms.enums.LogisticsMappingTypeEnum;
import com.erp.model.wms.dto.OverseasProviderWarehouseDTO;
import com.erp.rpc.tms.feign.LogisticsAuthFeign;
import com.erp.rpc.tms.feign.LogisticsMappingFeign;
import com.erp.rpc.wms.feign.WmsOverseasWarehouseFeign;
import com.erp.server.oms.mapper.SoB2cMapper;
import com.erp.server.oms.service.*;
import io.seata.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * B2C销售订单表 服务实现类
 * </p>
 *
 * @author Will
 * @since 2023-08-18
 */
@Slf4j
@Service
public class SoB2cRuleServiceImpl extends SuperServiceImpl<SoB2cMapper, SoB2cEntity> implements SoB2cRuleService {

    @Resource
    private SoB2cLogisticsService soB2cLogisticsService;

    @Resource
    private SoB2cDetailService soB2cDetailService;

    @Resource
    private LogisticsAuthFeign logisticsAuthFeign;

    @Resource
    private WmsOverseasWarehouseFeign wmsOverseasWarehouseFeign;

    @Resource
    @Lazy
    private SoB2cService soB2cService;

    @Resource
    private LogisticsMappingFeign logisticsMappingFeign;

    @Resource
    private SoB2cErrorService soB2cErrorService;

    @Resource
    private OperateLogService operateLogService;

    @Override
    public boolean handleAutoSubmitDelivery(String soId, String name) {
        SoB2cEntity entity = this.getByIdOpt(soId).orElseThrow(() -> new ServiceException("销售订单不存在，soId: " + soId));
//
//        // 1. 首先判断保宏预报状态
//        String transferStatus = entity.getTransferStatus();
//        if (TransferStatusEnum.WAIT.getCode().equals(transferStatus)) {
//            log.info("订单【{}】中转预报状态为待中转，等待预报状态变更为预报成功后再执行自动提交发货", entity.getCode());
//            return false;
//        }
//
//        // 2. 预报状态通过后判断开票状态
//        String nfeInvoiceStatus = entity.getNfeInvoiceStatus();
//
//        // 检查是否有开票标识且不等于以下状态：无需开票、待上传、上传失败、已上传、无需上传
//        boolean hasNfeInvoice = StringUtils.isNotBlank(nfeInvoiceStatus) &&
//            !SoB2cNfeStatusEnum.NOT_NEED_INVOICE.getCode().equals(nfeInvoiceStatus) &&
//            !SoB2cNfeStatusEnum.WAIT_UPLOAD.getCode().equals(nfeInvoiceStatus) &&
//            !SoB2cNfeStatusEnum.UPLOAD_FAILURE.getCode().equals(nfeInvoiceStatus) &&
//            !SoB2cNfeStatusEnum.UPLOAD_SUCCESS.getCode().equals(nfeInvoiceStatus) &&
//            !SoB2cNfeStatusEnum.NOT_NEED_UPLOAD.getCode().equals(nfeInvoiceStatus);
//
//        if (hasNfeInvoice) {
//            log.info("订单【{}】有开票标识且状态不为无需开票、待上传、上传失败、已上传、无需上传，等待状态变更为允许状态后触发自动提交发货", entity.getCode());
//            return false;
//        }
//
        SoB2cLogisticsEntity soB2cLogisticsEntity = soB2cLogisticsService.getByMainId(soId);
        if (soB2cLogisticsEntity == null || StringUtils.isBlank(soB2cLogisticsEntity.getLogisticsChannelId())) {
            log.warn("销售订单物流信息为空，soId: {}", soId);
            return false;
        }
        List<SoB2cDetailEntity> detailEntityList = soB2cDetailService.listByMainId(soId);
        if (detailEntityList.isEmpty()) {
            log.warn("销售订单明细信息为空，soId: {}", soId);
            return false;
        }
        String channelId = soB2cLogisticsEntity.getLogisticsChannelId();
        String warehouseId = detailEntityList.get(0).getWarehouseId();
        LogisticsSupplierDTO.AuthDTO authDTO = logisticsAuthFeign.getAuthByChannelId(channelId);
        String logisticsPlatform = authDTO.getLogisticsPlatform();
        boolean isWarehouseLogistic = OmsPlatformEnum.isThirdWarehouse(logisticsPlatform);
        boolean isOverseasWarehouse;
        if(StringUtils.isBlank(warehouseId)){
            isOverseasWarehouse = false;
        }else{
            List<OverseasProviderWarehouseDTO.ViewDTO> overseasWarehouseList = wmsOverseasWarehouseFeign.listByWarehouseIdList(Collections.singletonList(warehouseId));
            isOverseasWarehouse = CollectionUtils.isNotEmpty(overseasWarehouseList);
        }
        // 非海外仓物流+非海外仓仓库 获取跟踪号提交发货
        if( !isWarehouseLogistic && !isOverseasWarehouse) {
            // 获取跟踪号
            soB2cService.getLogisticsCode(entity.getId(),  Boolean.TRUE);
            //重新查询主表判断是否提交发货成功
            entity = this.getById(soId);
            if(!entity.getSignOrderError().equals(SoB2cErrorTypeEnum.GET_LOGISTICS_CODE.getCode())){
                operateLogService.addModuleOperateLog(CharSequenceUtil.format("订单自动提交发货，物流规则【{}】",name),ModuleTypeEnum.SO_B2C.getCode(),entity.getId(),"自动提交发货" );
            }
        }else if(!isWarehouseLogistic) {
            // 非海外仓物流+海外仓仓库 获取跟踪号 然后查询配置的海外仓物流后提交发货
            // 获取跟踪号
            BatchResultDTO batchResultDTO = soB2cService.getLogisticsCode(entity.getId(),  Boolean.FALSE);
            if(!batchResultDTO.getSuccess()){
                return false;
            }
            // 查询配置的海外仓物流
            List<LogisticsMappingDTO.ViewDTO> viewDTOS = logisticsMappingFeign.listByChannelIdAndType(channelId, LogisticsMappingTypeEnum.WAREHOUSE.getCode());
            LogisticsMappingDTO.ViewDTO viewDTO = viewDTOS.stream().filter(v->v.getWarehouseId().equals(warehouseId)).findFirst().orElse(null);
            if(Objects.isNull(viewDTO) || StringUtils.isBlank(viewDTO.getPlatformLogisticsChannelId())) {
                // 没有配置海外仓物流，记录订单异常并返回
                SoB2cErrorDTO.AddDTO addError = new SoB2cErrorDTO.AddDTO();
                addError.setType(SoB2cErrorTypeEnum.SUBMIT_DELIVERY.getCode());
                addError.setMainId(soId);
                addError.setMessage("海外仓自动提交发货异常，未配置海外仓物流");
                soB2cErrorService.add(addError);
                return false;
            }
            if (!submitDelivery(soId, viewDTO,name)){
                return false;
            }
        }else if(isOverseasWarehouse) {
            // 海外仓物流+海外仓仓库 直接提交发货
            if (!submitDelivery(soId, null,name)){
                return false;
            }
        }
        return true;
    }

    public boolean submitDelivery(String soId, LogisticsMappingDTO.ViewDTO viewDTO,String ruleName) {
        // 提交发货
        try {
            String warehouseLogisticsChannelId = Objects.nonNull(viewDTO)?viewDTO.getPlatformLogisticsChannelId():"";
            BatchResultDTO submitDelivery = soB2cService.submitDelivery(soId, warehouseLogisticsChannelId);
            operateLogService.addModuleOperateLog(CharSequenceUtil.format("订单自动提交发货，物流规则【{}】",ruleName),ModuleTypeEnum.SO_B2C.getCode(),soId,"自动提交发货" );
            if(!submitDelivery.getSuccess()){
                if(StringUtils.isNotBlank(submitDelivery.getMsg())){
                    SoB2cErrorDTO.AddDTO addError = new SoB2cErrorDTO.AddDTO();
                    addError.setType(SoB2cErrorTypeEnum.SUBMIT_DELIVERY.getCode());
                    addError.setMainId(soId);
                    addError.setMessage(StrUtil.format("海外仓自动提交发货异常,{}",submitDelivery.getMsg()));
                    soB2cErrorService.add(addError);
                }
                return false;
            }
        }catch (Exception e){
            log.error("[海外仓自动提交发货异常]:soId={},", soId, e);
            SoB2cErrorDTO.AddDTO addError = new SoB2cErrorDTO.AddDTO();
            addError.setType(SoB2cErrorTypeEnum.SUBMIT_DELIVERY.getCode());
            addError.setMainId(soId);
            addError.setMessage(StrUtil.format("海外仓自动提交发货异常,{}",e.getMessage()));
            soB2cErrorService.add(addError);
            return false;
        }
        return true;
    }
}
