package com.erp.server.tms.handler;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.enums.ErpServerModuleEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncStatusEnum;
import com.common.business.threadlocal.TransferLogisticsContext;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.entity.DmpPullTaskEntity;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.msg.dto.WarnMsgInfoDTO;
import com.erp.model.msg.enums.WarnMsgTypeEnum;
import com.erp.model.tms.dto.transfer.*;
import com.erp.model.tms.entity.ProductRegistrationEntity;
import com.erp.model.tms.entity.TransferLogisticsAuthEntity;
import com.erp.model.tms.entity.TransferLogisticsChannelEntity;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.server.tms.service.TransferLogisticsAuthService;
import com.erp.server.tms.service.TransferLogisticsService;
import io.seata.common.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@Validated
public abstract class AbstractTransferLogisticsHandler extends BaseController implements TransferLogisticsService {

    @Resource
    private TransferLogisticsAuthService transferLogisticsAuthService;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private MQProducerService<WarnMsgInfoDTO> mqProducerService;

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
    public ApiResult<Object>authorization(Map<String, String> authConfig) {
        try {
            TransferLogisticsContext.setAuthMap(authConfig);
            ApiResult<List<TransferLogisticsChannelEntity>>  result = this.getShippingMethodList();
            return result.isSuccess()?ApiResult.success():ApiResult.error(ApiError.ERROR_AUTHORIZE_FAIL.code,"授权失败:"+result.getMsg());
        } finally {
            TransferLogisticsContext.remove();
        }
    }

    @Override
    public ApiResult<String> createProduct(TransferLogisticsCreateProductReq createProductReq, String authId)  {
        return handleAndRemoveContext(() -> createProduct(createProductReq), authId, SourceTypeEnum.TRANSFER_LOGISTICS_CREATE_PRODUCT,createProductReq.getSku());
    }
    @Override
    public ApiResult<List<TransferLogisticsChannelEntity>> getShippingMethodList(String authId) {
        return handleAndRemoveContext(this::getShippingMethodList, authId, SourceTypeEnum.TRANSFER_LOGISTICS_GET_SHIPPING,"");
    }

    @Override
    public ApiResult<String> createOrder(TransferLogisticsCreateOrderReq createOrderReq, String authId) {
        //相同sku合并数量
        if(CollectionUtils.isNotEmpty(createOrderReq.getProductDetailList())){
            createOrderReq.setProductDetailList(createOrderReq.getProductDetailList().stream()
                    .collect(Collectors.groupingBy(
                            TransferLogisticsCreateOrderReq.ProductDetail::getSkuNo,  // 以 skuNo 分组
                            Collectors.reducing((pd1, pd2) -> {
                                // 合并qty
                                pd1.setQty(pd1.getQty() + pd2.getQty());
                                return pd1;
                            })
                    ))
                    .values()
                    .stream()
                    .map(Optional::get)
                    .collect(Collectors.toList()));
        }
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
    public ApiResult<ProductRegistrationEntity> getProductBySku(String skuNo,String authId) {
        return handleAndRemoveContext(() -> getProductBySku(skuNo), authId, SourceTypeEnum.TRANSFER_LOGISTICS_GET_ALL_PRODUCT_INFO,skuNo);
    }
    @Override
    public ApiResult<String> createInbound(TransferLogisticsCreateInboundReq createInboundReq, String authId) {
        return handleAndRemoveContext(() -> createInbound(createInboundReq), authId, SourceTypeEnum.TRANSFER_LOGISTICS_CREATE_INBOUND,createInboundReq.getReferenceCode());
    }

    @Override
    public ApiResult<String> printLabel(String orderCode, String authId) {
        return handleAndRemoveContext(() -> printLabel(orderCode), authId, SourceTypeEnum.TRANSFER_LOGISTICS_PRINT_LABEL,orderCode);
    }

    @Override
    public ApiResult<String> cancelOrder(TransferCancelOrderReq cancelOrderReq, String authId)  {
        return handleAndRemoveContext(() -> cancelOrder(cancelOrderReq), authId, SourceTypeEnum.TRANSFER_LOGISTICS_CANCEL_ORDER,cancelOrderReq.getThirdPlatformCode());
    }

    protected abstract ApiResult<String> cancelOrder(@Valid TransferCancelOrderReq cancelOrderReq);

    protected abstract ApiResult<List<TransferLogisticsChannelEntity>> getShippingMethodList();

    protected abstract ApiResult<String> createProduct(@Valid TransferLogisticsCreateProductReq createProductReq);

    protected abstract ApiResult<String> createOrder(@Valid TransferLogisticsCreateOrderReq createOrderReq);

    protected abstract ApiResult<TransferLogisticsOrderDTO> getOrderByCode(String orderCode);

    protected abstract ApiResult<List<ProductRegistrationEntity>> getAllProductInfo();

    protected abstract ApiResult<ProductRegistrationEntity> getProductBySku(String skuNo);

    protected abstract ApiResult<String> createInbound(TransferLogisticsCreateInboundReq createInboundReq);

    protected abstract ApiResult<String> printLabel(String orderCode);

    private <T> ApiResult<T> handleAndRemoveContext(Handler<T> handler, String authId,SourceTypeEnum businessType,String erpBusinessCode) {
        try {
            //设置授权信息
            handleAuthInfo(authId);
            //执行逻辑
            ApiResult<T> result = handler.handle();
            TransferLogisticsContext.setMsg(JSONUtil.toJsonStr(result));
            //记录日志
            pushOperateLog(businessType,result.getCode(),erpBusinessCode,false);
            return result;
        } catch (Exception e){
            log.error(ApiError.THIRD_WAREHOUSE_INTERFACE_EXCEPTION.msg,e);
            TransferLogisticsContext.setMsg(ExceptionUtil.stacktraceToString(e,2000));
            pushOperateLog(businessType,2000,erpBusinessCode,false);
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

    private void pushOperateLog(SourceTypeEnum businessType, Integer status, String erpBusinessCode,boolean isPush) {
        if("dmp_pull_task".equals(businessType.getTableName())){
            DmpPullTaskEntity dmpPullTaskEntity = buildDmpPullTaskEntity(businessType, status, erpBusinessCode);
            try {
                String id = dmpTaskFeign.saveOrUpdateDmpPullTask(dmpPullTaskEntity);
                //增加异常预警
                if (!ApiResult.success().getCode().equals(status) && isPush) {
                    dmpPullTaskEntity.setId(id);
                    sendPullWarnMsg(dmpPullTaskEntity);
                }
            } catch (Exception e) {
                log.error("saveOrUpdateDmpPullTask:记录操作日志失败",e);
            }
        }else if ("dmp_push_task".equals(businessType.getTableName())){
            DmpPushTaskEntity dmpPushTaskEntity = buildDmpPushTaskEntity(businessType, status, erpBusinessCode);
            try {
                String id = dmpTaskFeign.saveDmpPushTask(dmpPushTaskEntity);
                //增加异常预警
                if (!ApiResult.success().getCode().equals(status) && isPush) {
                    dmpPushTaskEntity.setId(id);
                    sendPushWarnMsg(dmpPushTaskEntity);
                }
            } catch (Exception e) {
                log.error("saveDmpPushTask:记录操作日志失败",e);
            }
        }
    }

    private DmpPullTaskEntity buildDmpPullTaskEntity(SourceTypeEnum businessType, Integer status, String erpBusinessCode) {
        DmpPullTaskEntity dmpPullTaskEntity = new DmpPullTaskEntity();
        dmpPullTaskEntity.setSourcePlatformName(PlatformEnum.ERP_TMS.getDesc());
        dmpPullTaskEntity.setSourceType(businessType.getCode());
        dmpPullTaskEntity.setSourceId(erpBusinessCode);
        dmpPullTaskEntity.setSourceCode(erpBusinessCode);
        dmpPullTaskEntity.setTargetPlatformName(getPlatForm().getName());
        dmpPullTaskEntity.setStatus(status.equals(ApiResult.success().getCode()) ? SyncStatusEnum.SUCCESS_SYNC.getCode() : SyncStatusEnum.NO_NEED_SYNC.getCode());
        dmpPullTaskEntity.setMqTopic("");
        dmpPullTaskEntity.setMqTag("");
        dmpPullTaskEntity.setMqData(TransferLogisticsContext.getRequestJson());
        dmpPullTaskEntity.setReturnMsg(TransferLogisticsContext.getMsg());
        return dmpPullTaskEntity;
    }

    private DmpPushTaskEntity buildDmpPushTaskEntity(SourceTypeEnum businessType, Integer status, String erpBusinessCode) {
        DmpPushTaskEntity dmpPushTaskEntity = new DmpPushTaskEntity();
        dmpPushTaskEntity.setSourcePlatformName(PlatformEnum.ERP_TMS.getDesc());
        dmpPushTaskEntity.setSourceType(businessType.getCode());
        dmpPushTaskEntity.setSourceId(erpBusinessCode);
        dmpPushTaskEntity.setSourceCode(erpBusinessCode);
        dmpPushTaskEntity.setTargetPlatformName(getPlatForm().getName());
        dmpPushTaskEntity.setStatus(status.equals(ApiResult.success().getCode()) ? SyncStatusEnum.SUCCESS_SYNC.getCode() : SyncStatusEnum.NO_NEED_SYNC.getCode());
        dmpPushTaskEntity.setMqTopic("");
        dmpPushTaskEntity.setMqTag("");
        dmpPushTaskEntity.setMqData(TransferLogisticsContext.getRequestJson());
        dmpPushTaskEntity.setReturnMsg(TransferLogisticsContext.getMsg());
        return dmpPushTaskEntity;
    }

    private void sendPushWarnMsg(DmpPushTaskEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return;
        }
        WarnMsgInfoDTO warnMsgInfo = buildWarnMsgInfoDTO(entity);
        mqProducerService.sendWarnMsg(warnMsgInfo);
    }

    private void sendPullWarnMsg(DmpPullTaskEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return;
        }
        WarnMsgInfoDTO warnMsgInfo = buildPullWarnMsgInfoDTO(entity);
        mqProducerService.sendWarnMsg(warnMsgInfo);
    }
    private WarnMsgInfoDTO buildPullWarnMsgInfoDTO(DmpPullTaskEntity entity) {
        WarnMsgInfoDTO warnMsgInfo = new WarnMsgInfoDTO();
        warnMsgInfo.setBizName(SourceTypeEnum.getName(entity.getSourceType()));
        warnMsgInfo.setErpServerModuleEnum(ErpServerModuleEnum.ERP_SERVER_TMS);
        warnMsgInfo.setTitle(CharSequenceUtil.format("ERP拉取物流报关商【{}】数据从{}拉取至{}失败", entity.getSourceCode(),  entity.getTargetPlatformName(),entity.getSourcePlatformName()));
        warnMsgInfo.setTableName(SourceTypeEnum.getTableName(entity.getSourceType()));
        warnMsgInfo.setTableId(entity.getSourceId());
        warnMsgInfo.setKeyInfo(StringUtils.isEmpty(TransferLogisticsContext.getMsg())?"":TransferLogisticsContext.getMsg());
        warnMsgInfo.setWarnMsgTypeEnum(WarnMsgTypeEnum.SYS_EXCEPTION);
        return warnMsgInfo;
    }

    private WarnMsgInfoDTO buildWarnMsgInfoDTO(DmpPushTaskEntity entity) {
        WarnMsgInfoDTO warnMsgInfo = new WarnMsgInfoDTO();
        warnMsgInfo.setBizName(SourceTypeEnum.getName(entity.getSourceType()));
        warnMsgInfo.setErpServerModuleEnum(ErpServerModuleEnum.ERP_SERVER_TMS);
        warnMsgInfo.setTitle(CharSequenceUtil.format("物流报关商【{}】从{}推送至{}失败", entity.getSourceCode(), entity.getSourcePlatformName(), entity.getTargetPlatformName()));
        warnMsgInfo.setTableName(SourceTypeEnum.getTableName(entity.getSourceType()));
        warnMsgInfo.setTableId(entity.getSourceId());
        warnMsgInfo.setKeyInfo(StringUtils.isEmpty(TransferLogisticsContext.getMsg())?"":TransferLogisticsContext.getMsg());
        warnMsgInfo.setWarnMsgTypeEnum(WarnMsgTypeEnum.SYS_EXCEPTION);
        return warnMsgInfo;
    }
}
