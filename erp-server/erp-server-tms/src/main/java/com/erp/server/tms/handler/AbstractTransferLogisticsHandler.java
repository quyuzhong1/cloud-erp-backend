package com.erp.server.tms.handler;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.enums.ErpServerModuleEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncStatusEnum;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.common.business.threadlocal.TransferLogisticsContext;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.msg.dto.WarnMsgInfoDTO;
import com.erp.model.msg.enums.WarnMsgTypeEnum;
import com.erp.model.tms.dto.transfer.TransferLogisticsCreateInboundReq;
import com.erp.model.tms.dto.transfer.TransferLogisticsCreateOrderReq;
import com.erp.model.tms.dto.transfer.TransferLogisticsOrderDTO;
import com.erp.model.tms.dto.transfer.TransferLogisticsProductDTO;
import com.erp.model.tms.entity.ProductRegistrationEntity;
import com.erp.model.tms.entity.TransferLogisticsAuthEntity;
import com.erp.model.tms.entity.TransferLogisticsChannelEntity;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.server.tms.service.TransferLogisticsAuthService;
import com.erp.server.tms.service.TransferLogisticsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@Validated
public abstract class AbstractTransferLogisticsHandler extends BaseController implements TransferLogisticsService {

    @Resource
    private TransferLogisticsAuthService transferLogisticsAuthService;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private MQProducerService mqProducerService;

    public void handleAuthInfo(String id) {
        TransferLogisticsAuthEntity authEntity = getAuthEntity(id);
        if(Objects.isNull(authEntity)){
            throw new ServiceException(ApiError.NOT_FOUND_OVERSEAS_PROVIDE);
        }
        TransferLogisticsContext.setAuthMap(transferLogisticsAuthService.getTransferLogisticsAuthConfig(id,getPlatForm().getCode()));
    }

    private TransferLogisticsAuthEntity getAuthEntity(String id) {
        return transferLogisticsAuthService.getById(id);
    }

    @Override
    public ApiResult authorization(Map<String, String> authConfig) {
        try {
            TransferLogisticsContext.setAuthMap(authConfig);
            ApiResult<List<TransferLogisticsChannelEntity>>  result = this.getShippingMethodList();
            return result.isSuccess()?ApiResult.success():ApiResult.error(ApiError.ERROR_AUTHORIZE_FAIL.code,"授权失败:"+result.getMsg());
        } finally {
            TransferLogisticsContext.remove();
        }
    }

    @Override
    public ApiResult<List<TransferLogisticsChannelEntity>> getShippingMethodList(String authId) {
        return handleAndRemoveContext(this::getShippingMethodList, authId, SourceTypeEnum.TRANSFER_LOGISTICS_GET_SHIPPING,"");
    }

    @Override
    public ApiResult<String> createOrder(TransferLogisticsCreateOrderReq createOrderReq, String authId) {
        return handleAndRemoveContext(() -> createOrder(createOrderReq), authId, SourceTypeEnum.TRANSFER_LOGISTICS_CREATE_ORDER,createOrderReq.getReferenceNo());
    }

    @Override
    public ApiResult<TransferLogisticsOrderDTO> getOrderByCode(String orderCode, String authId) {
        return handleAndRemoveContext(() -> getOrderByCode(orderCode), authId, SourceTypeEnum.TRANSFER_LOGISTICS_GET_ORDER_BY_CODE,orderCode);
    }

    @Override
    public ApiResult<List<ProductRegistrationEntity>> getAllProductInfo(String authId) {
        return handleAndRemoveContext(this::getAllProductInfo, authId, SourceTypeEnum.TRANSFER_LOGISTICS_GET_ALL_PRODUCT_INFO,"");
    }

    @Override
    public ApiResult<String> createInbound(TransferLogisticsCreateInboundReq createInboundReq, String authId) {
        return handleAndRemoveContext(() -> createInbound(createInboundReq), authId, SourceTypeEnum.TRANSFER_LOGISTICS_CREATE_INBOUND,createInboundReq.getReferenceCode());
    }

    @Override
    public ApiResult<String> printLabel(String orderCode, String authId) {
        return handleAndRemoveContext(() -> printLabel(orderCode), authId, SourceTypeEnum.TRANSFER_LOGISTICS_PRINT_LABEL,orderCode);
    }

    protected abstract ApiResult<List<TransferLogisticsChannelEntity>> getShippingMethodList();

    protected abstract ApiResult<String> createOrder(@Valid TransferLogisticsCreateOrderReq createOrderReq);

    protected abstract ApiResult<TransferLogisticsOrderDTO> getOrderByCode(String orderCode);

    protected abstract ApiResult<List<ProductRegistrationEntity>> getAllProductInfo();

    protected abstract ApiResult<String> createInbound(TransferLogisticsCreateInboundReq createInboundReq);

    protected abstract ApiResult<String> printLabel(String orderCode);

    private <T> ApiResult<T> handleAndRemoveContext(Handler<T> handler, String authId,SourceTypeEnum businessType,String erpBusinessCode) {
        try {
            //设置授权信息
            handleAuthInfo(authId);
            //执行逻辑
            ApiResult<T> result = handler.handle();
            TransferLogisticsContext.setMsg(result.getMsg());
            //记录日志
            pushOperateLog(businessType,result.getCode(),erpBusinessCode);
            return result;
        } catch (Exception e){
            log.error(ApiError.THIRD_WAREHOUSE_INTERFACE_EXCEPTION.msg,e);
            TransferLogisticsContext.setMsg(ExceptionUtil.stacktraceToString(e,2000));
            pushOperateLog(businessType,2000,erpBusinessCode);
            return ApiResult.error(ApiError.THIRD_WAREHOUSE_INTERFACE_EXCEPTION.code,e.getMessage());
        } finally {
            // remove thread-local
            TransferLogisticsContext.remove();
        }
    }

    @FunctionalInterface
    private interface Handler<T> {
        ApiResult<T> handle();
    }

    private void pushOperateLog(SourceTypeEnum businessType, Integer status, String erpBusinessCode) {
        DmpPushTaskEntity dmpPushTaskEntity = buildDmpPushTaskEntity(businessType, status, erpBusinessCode);
        try {
            String id = dmpTaskFeign.saveOrUpdateDmpPushTask(dmpPushTaskEntity);
            //增加异常预警
            if (!ApiResult.success().getCode().equals(status)) {
                dmpPushTaskEntity.setId(id);
                sendPushWarnMsg(dmpPushTaskEntity);
            }
        } catch (Exception e) {
            log.error("saveOrUpdateDmpPushTask:记录操作日志失败",e);
        }
    }

    private DmpPushTaskEntity buildDmpPushTaskEntity(SourceTypeEnum businessType, Integer status, String erpBusinessCode) {
        DmpPushTaskEntity dmpPushTaskEntity = new DmpPushTaskEntity();
        dmpPushTaskEntity.setSourcePlatformName(PlatformEnum.ERP_TMS.getDesc());
        dmpPushTaskEntity.setSourceType(businessType.getCode());
        dmpPushTaskEntity.setSourceId(erpBusinessCode);
        dmpPushTaskEntity.setSourceCode(erpBusinessCode);
        dmpPushTaskEntity.setTargetPlatformName(getPlatForm().getName());
        dmpPushTaskEntity.setStatus(status.equals(ApiResult.success().getCode()) ? SyncStatusEnum.SUCCESS_SYNC.getCode() : SyncStatusEnum.FAILED_SYNC.getCode());
        dmpPushTaskEntity.setMqTopic("");
        dmpPushTaskEntity.setMqTag("");
        dmpPushTaskEntity.setMqData(ThirdWarehouseContext.getRequestJson());
        dmpPushTaskEntity.setReturnMsg(ThirdWarehouseContext.getResponseJson());
        return dmpPushTaskEntity;
    }

    private void sendPushWarnMsg(DmpPushTaskEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return;
        }
        WarnMsgInfoDTO warnMsgInfo = buildWarnMsgInfoDTO(entity);
        mqProducerService.sendWarnMsg(warnMsgInfo);
    }

    private WarnMsgInfoDTO buildWarnMsgInfoDTO(DmpPushTaskEntity entity) {
        WarnMsgInfoDTO warnMsgInfo = new WarnMsgInfoDTO();
        warnMsgInfo.setBizName(SourceTypeEnum.getName(entity.getSourceType()));
        warnMsgInfo.setErpServerModuleEnum(ErpServerModuleEnum.ERP_SERVER_TMS);
        warnMsgInfo.setTitle(StrUtil.format("物流报关商【{}】从{}推送至{}失败", entity.getSourceCode(), entity.getSourcePlatformName(), entity.getTargetPlatformName()));
        warnMsgInfo.setTableName(SourceTypeEnum.getTableName(entity.getSourceType()));
        warnMsgInfo.setTableId(entity.getSourceId());
        warnMsgInfo.setKeyInfo(StringUtils.isEmpty(ThirdWarehouseContext.getMsg())?"":ThirdWarehouseContext.getMsg());
        warnMsgInfo.setWarnMsgTypeEnum(WarnMsgTypeEnum.SYS_EXCEPTION);
        return warnMsgInfo;
    }
}
