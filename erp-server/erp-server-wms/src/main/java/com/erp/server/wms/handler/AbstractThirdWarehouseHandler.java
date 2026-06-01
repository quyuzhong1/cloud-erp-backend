package com.erp.server.wms.handler;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.enums.ErpServerModuleEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncStatusEnum;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.msg.dto.WarnMsgInfoDTO;
import com.erp.model.msg.enums.WarnMsgTypeEnum;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.wms.dto.OverseasProviderDTO;
import com.erp.model.wms.dto.third.*;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.sdk.oms.amz.spapi.client.StringUtil;
import com.erp.server.wms.service.OverseasProviderService;
import com.erp.server.wms.service.ThirdWarehouseService;
import io.seata.common.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public abstract class AbstractThirdWarehouseHandler extends BaseController implements ThirdWarehouseService {

    private static final String THIRD_WAREHOUSE_EMPTY_RESPONSE = "第三方仓接口返回为空";
    private static final String THIRD_WAREHOUSE_TIMEOUT_TEST_REMARK = "测试三方仓超时";

    @Resource
    private OverseasProviderService overseasProviderService;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private MQProducerService<T> mqProducerService;

    public void handleAuthInfo(String id) {
        OverseasProviderEntity authEntity = getAuthEntity(id);
        if(Objects.isNull(authEntity)){
            throw new ServiceException(ApiError.WH_OVERSEAS_PROVIDER_NOT_FOUND);
        }
        if(!authEntity.getAuthStatus().equals(AuthStatusEnum.ALREADY.getCode())){
            throw new ServiceException(ApiError.WH_OVERSEAS_PROVIDER_NOT_AUTH);
        }
        ThirdWarehouseContext.setAuthMap(authEntity.getAuthJson());
    }

    private OverseasProviderEntity getAuthEntity(String id) {
        return overseasProviderService.getById(id);
    }

    @Override
    public Boolean authorize(OverseasProviderDTO.AuthorizeParamDTO dto) {
        try {
            ThirdWarehouseContext.setAuthMap(dto.getAuthJson());
            return warehouseAuthorize(dto);
        } finally {
            ThirdWarehouseContext.remove();
        }
    }

    @Override
    public ApiResult<List<ThirdWarehouseSkuResp>> getSkuList(ThirdWarehouseProductReq productReq, String authId) {
        return handleAndRemoveContext(() -> getSkuList(productReq), authId,null,null);
    }

    @Override
    public ApiResult<String> createInboundBill(ThirdWarehouseCreateInboundReq createInboundReq, String authId) {
        return handleAndRemoveContext(() -> createInboundBill(createInboundReq), authId,SourceTypeEnum.THIRD_WAREHOUSE_CREATE_INBOUND_BILL,createInboundReq.getReferenceNo());
    }

    @Override
    public ApiResult<String> editInboundBill(ThirdWarehouseCreateInboundReq createInboundReq, String authId) {
        if(CharSequenceUtil.isBlank(createInboundReq.getReceivingCode())){
            return failure("第三方入库单号不能为空");
        }
        return handleAndRemoveContext(() -> editInboundBill(createInboundReq), authId,SourceTypeEnum.THIRD_WAREHOUSE_EDIT_INBOUND_BILL,createInboundReq.getReceivingCode());
    }
    @Override
    public ApiResult<String> cancelInboundBill(ThirdWarehouseCancelInboundReq cancelInboundReq, String authId) {
        return handleAndRemoveContext(() -> cancelInboundBill(cancelInboundReq), authId,SourceTypeEnum.THIRD_WAREHOUSE_CANCEL_INBOUND_BILL,cancelInboundReq.getReceivingCode());
    }

    @Override
    public ApiResult<ThirdWarehouseQueryOutboundResponse> createOutboundBill(ThirdWarehouseCreateOutboundReq createOutboundReq, String authId) {
        log.error("createOutboundBill authId:{} request:{}", authId, JSONUtil.toJsonStr(createOutboundReq));
        ThirdWarehouseContext.setTimeoutTest(isTimeoutTestRemark(createOutboundReq.getRemark()));
        //相同sku合并数量
        if(CollectionUtils.isNotEmpty(createOutboundReq.getItems())){
            Map<String,Integer> mergeSkuMap = createOutboundReq.getItems().stream().collect(Collectors.toMap(ThirdWarehouseCreateOutboundReq.Item::getProductSku, ThirdWarehouseCreateOutboundReq.Item::getQuantity, Integer::sum));
            //将map转成List<Item>
            createOutboundReq.setItems(mergeSkuMap.entrySet().stream().map(v->{
                    ThirdWarehouseCreateOutboundReq.Item item = createOutboundReq.getItems().stream().filter(i->i.getProductSku().equals(v.getKey())).findFirst().orElse(new ThirdWarehouseCreateOutboundReq.Item());
               return new ThirdWarehouseCreateOutboundReq.Item(item.getThirdBarcode(),v.getKey(),v.getValue(),item.getHsCode(),item.getProductSkuId());
            }).collect(Collectors.toList()));
        }
        return handleAndRemoveContext(() -> createOutboundBill(createOutboundReq), authId,SourceTypeEnum.THIRD_WAREHOUSE_CREATE_OUTBOUND_BILL,createOutboundReq.getReferenceNo());
    }

    private boolean isTimeoutTestRemark(String remark) {
        return THIRD_WAREHOUSE_TIMEOUT_TEST_REMARK.equals(CharSequenceUtil.trim(remark));
    }
    @Override
    public ApiResult<String> createFbaOutboundBill(ThirdWarehouseCreateFbaOutboundReq createOutboundReq, String authId) {
        log.error("createFbaOutboundBill authId:{} request:{}", authId, JSONUtil.toJsonStr(createOutboundReq));
        return handleAndRemoveContext(() -> createFbaOutboundBill(createOutboundReq), authId,SourceTypeEnum.THIRD_WAREHOUSE_CREATE_FBA_OUTBOUND_BILL,createOutboundReq.getReferenceNo());
    }

    @Override
    public ApiResult<String> cancelOutboundBill(ThirdWarehouseCancelOutboundReq cancelOutboundReq, String authId) {
        return handleAndRemoveContext(() -> cancelOutboundBill(cancelOutboundReq), authId,SourceTypeEnum.THIRD_WAREHOUSE_CANCEL_OUTBOUND_BILL,cancelOutboundReq.getOrderCode());
    }

    @Override
    public ApiResult<String> cancelFbaOutboundBill(ThirdWarehouseCancelFbaOutboundReq cancelOutboundReq, String authId) {
        return handleAndRemoveContext(() -> cancelFbaOutboundBill(cancelOutboundReq), authId,SourceTypeEnum.THIRD_WAREHOUSE_CANCEL_OUTBOUND_BILL,cancelOutboundReq.getOrderCode());
    }

    @Override
    public ApiResult<ThirdWarehouseQueryOutboundResponse> queryOutboundBill(ThirdWarehouseQueryOutboundReq queryOutboundReq, String authId) {
        return handleAndRemoveContext(() -> queryOutboundBill(queryOutboundReq), authId,SourceTypeEnum.THIRD_WAREHOUSE_QUERY_OUTBOUND_BILL,queryOutboundReq.getErpOrderCode());
    }
    @Override
    public ApiResult<List<ThirdWarehouseQueryFbaOutboundResponse>> queryFbaOutboundBill(ThirdWarehouseQueryFbaOutboundReq queryOutboundReq, String authId) {
        return handleAndRemoveContext(() -> queryFbaOutboundBill(queryOutboundReq), authId,SourceTypeEnum.THIRD_WAREHOUSE_QUERY_OUTBOUND_BILL,String.join(",", queryOutboundReq.getErpOrderCodeList()));
    }

    @Override
    public ApiResult<ThirdWarehouseQueryFbaOutboundPageResponse> queryFbaOutboundBillPage(ThirdWarehouseQueryFbaOutboundPageReq queryOutboundReq, String authId) {
        String businessCode = CharSequenceUtil.blankToDefault(queryOutboundReq.getStartUpdateTime(), "") + "~"
                + CharSequenceUtil.blankToDefault(queryOutboundReq.getEndUpdateTime(), "");
        return handleAndRemoveContext(() -> queryFbaOutboundBillPage(queryOutboundReq), authId, SourceTypeEnum.THIRD_WAREHOUSE_QUERY_OUTBOUND_BILL, businessCode);
    }

    @Override
    public ApiResult<List<ThirdWarehouseCalculateFeeResponse>> getCalculateFeeBatch(ThirdWarehouseCalculateFeeReq calculateFeeReq, String authId) {
        return handleAndRemoveContext(() -> getCalculateFeeBatch(calculateFeeReq), authId,SourceTypeEnum.THIRD_WAREHOUSE_CALCULATE_FEE,calculateFeeReq.getCountryCode());
    }

    @Override
    public ApiResult<ThirdWarehouseUploadFileResponse> uploadFile(ThirdWarehouseUploadFileReq uploadFileReq, String authId) {
        return handleAndRemoveContext(() -> uploadFile(uploadFileReq), authId,SourceTypeEnum.THIRD_WAREHOUSE_UPLOAD_FILE,uploadFileReq.getOrderCode());
    }

    @Override
    public ApiResult<ThirdWarehouseUploadOrderLabelResponse> uploadOrderLabel(ThirdWarehouseUploadOrderLabelReq uploadOrderLabelReq, String authId) {
        return handleAndRemoveContext(() -> uploadOrderLabel(uploadOrderLabelReq), authId,SourceTypeEnum.THIRD_WAREHOUSE_UPLOAD_ORDER_LABEL,uploadOrderLabelReq.getOrderCode());
    }

    @Override
    public ApiResult<ThirdWarehouseUploadHandoverFileResponse> uploadHandoverFile(ThirdWarehouseUploadHandoverFileReq uploadHandoverFileReq, String authId) {
        return handleAndRemoveContext(() -> uploadHandoverFile(uploadHandoverFileReq), authId,SourceTypeEnum.THIRD_WAREHOUSE_UPLOAD_HANDOVER_FILE,uploadHandoverFileReq.getOrderCode());
    }


    @Override
    public ApiResult<String> refreshToken(String authId,Map<String,Object> map) {
        return handleAndRemoveContext(() ->  refreshToken(map), authId,SourceTypeEnum.THIRD_WAREHOUSE_REFRESH_TOKEN,authId);
    }

    protected abstract ApiResult<List<ThirdWarehouseSkuResp>> getSkuList(ThirdWarehouseProductReq productReq);

    protected abstract ApiResult<String> createInboundBill(ThirdWarehouseCreateInboundReq createInboundReq);

    protected abstract ApiResult<String> editInboundBill(ThirdWarehouseCreateInboundReq createInboundReq);

    protected abstract ApiResult<String> cancelInboundBill(@Valid ThirdWarehouseCancelInboundReq cancelInboundReq);
    protected abstract ApiResult<List<ThirdWarehouseCalculateFeeResponse>> getCalculateFeeBatch(@Valid ThirdWarehouseCalculateFeeReq calculateFeeReq);
    protected abstract ApiResult<ThirdWarehouseUploadFileResponse> uploadFile(@Valid ThirdWarehouseUploadFileReq uploadFileReq);
    protected abstract ApiResult<ThirdWarehouseUploadOrderLabelResponse> uploadOrderLabel(@Valid ThirdWarehouseUploadOrderLabelReq uploadFileReq);
    protected abstract ApiResult<ThirdWarehouseUploadHandoverFileResponse> uploadHandoverFile(@Valid ThirdWarehouseUploadHandoverFileReq uploadHandoverFileReq);

    protected abstract ApiResult<ThirdWarehouseQueryOutboundResponse> createOutboundBill(ThirdWarehouseCreateOutboundReq createOutboundReq);
    protected abstract ApiResult<String> createFbaOutboundBill(ThirdWarehouseCreateFbaOutboundReq createOutboundReq);

    protected  ApiResult<String> refreshToken(Map<String,Object> map){
        return success();
    };


    protected abstract ApiResult<String> cancelOutboundBill(@Valid ThirdWarehouseCancelOutboundReq cancelOutboundReq);
    protected abstract ApiResult<String> cancelFbaOutboundBill(@Valid ThirdWarehouseCancelFbaOutboundReq cancelOutboundReq);
    protected abstract ApiResult<ThirdWarehouseQueryOutboundResponse> queryOutboundBill(@Valid ThirdWarehouseQueryOutboundReq queryOutboundReq);
    protected abstract ApiResult<List<ThirdWarehouseQueryFbaOutboundResponse>> queryFbaOutboundBill(@Valid ThirdWarehouseQueryFbaOutboundReq req);
    protected ApiResult<ThirdWarehouseQueryFbaOutboundPageResponse> queryFbaOutboundBillPage(@Valid ThirdWarehouseQueryFbaOutboundPageReq req) {
        return failure("当前平台不支持按时间分页查询B2B出库单");
    }

    protected abstract Boolean warehouseAuthorize(OverseasProviderDTO.AuthorizeParamDTO dto);

    private <T> ApiResult<T> handleAndRemoveContext(Handler<T> handler, String authId,SourceTypeEnum businessType,String erpBusinessCode) {
        try {
            //设置授权信息
            handleAuthInfo(authId);
            //执行逻辑
            ApiResult<T> result = handler.handle();
            if (isRetryableOutboundResult(result, businessType)) {
                result = ApiResult.error(ApiError.WH_OVERSEAS_INTERFACE_EXCEPTION.getCode(), getRetryableOutboundMessage(result.getMsg()));
            }
            ThirdWarehouseContext.setMsg(result.getMsg());
            //记录日志
            pushOperateLog(businessType,result.getCode(),erpBusinessCode, false);
            return result;
        } catch (Exception e){
            log.error(ApiError.WH_OVERSEAS_INTERFACE_EXCEPTION.getMsg(),e);
            ThirdWarehouseContext.setMsg(ExceptionUtil.stacktraceToString(e,2000));
            pushOperateLog(businessType,2000,erpBusinessCode, false);
            return ApiResult.error(ApiError.WH_OVERSEAS_INTERFACE_EXCEPTION.getCode(), getThirdWarehouseExceptionMessage(e, businessType));
        } finally {
            // remove thread-local
            ThirdWarehouseContext.remove();
        }
    }

    private String getThirdWarehouseExceptionMessage(Exception e, SourceTypeEnum businessType) {
        if (isOutboundBusiness(businessType) && isTimeoutException(e)) {
            return CharSequenceUtil.blankToDefault(e.getMessage(), "第三方仓接口请求超时");
        }
        if (isOutboundBusiness(businessType) && isEmptyResponseException(e)) {
            return THIRD_WAREHOUSE_EMPTY_RESPONSE;
        }
        return e.getMessage();
    }

    private boolean isRetryableOutboundResult(ApiResult<?> result, SourceTypeEnum businessType) {
        return isOutboundBusiness(businessType)
                && ObjectUtil.isNotEmpty(result)
                && !result.isSuccess()
                && (isTimeoutMessage(result.getMsg()) || isEmptyResponseMessage(result.getMsg()));
    }

    private String getRetryableOutboundMessage(String message) {
        return isEmptyResponseMessage(message) ? THIRD_WAREHOUSE_EMPTY_RESPONSE : message;
    }

    private boolean isOutboundBusiness(SourceTypeEnum businessType) {
        return businessType == SourceTypeEnum.THIRD_WAREHOUSE_CREATE_OUTBOUND_BILL
                || businessType == SourceTypeEnum.THIRD_WAREHOUSE_QUERY_OUTBOUND_BILL;
    }

    private boolean isEmptyResponseException(Exception e) {
        return e instanceof NullPointerException && CharSequenceUtil.isBlank(e.getMessage())
                || isEmptyResponseMessage(e.getMessage());
    }

    private boolean isTimeoutException(Throwable throwable) {
        Throwable current = throwable;
        while (Objects.nonNull(current)) {
            if (current instanceof java.net.SocketTimeoutException || isTimeoutMessage(current.getMessage())) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private boolean isEmptyResponseMessage(String message) {
        if (CharSequenceUtil.isBlank(message)) {
            return false;
        }
        return message.contains("返回为空")
                || message.contains("响应为空")
                || message.contains("返回数据为空")
                || message.contains("返回结果为空")
                || message.contains("响应结果为空")
                || message.contains("接口返回为空")
                || isBlankOriginalJsonParseError(message);
    }

    private boolean isTimeoutMessage(String message) {
        if (CharSequenceUtil.isBlank(message)) {
            return false;
        }
        String lowerMessage = message.toLowerCase();
        return lowerMessage.contains("timeout")
                || lowerMessage.contains("timed out")
                || lowerMessage.contains("sockettimeoutexception")
                || message.contains("超时");
    }

    private boolean isBlankOriginalJsonParseError(String message) {
        if (!CharSequenceUtil.containsIgnoreCase(message, "json 解析失败") || !message.contains("原始值：")) {
            return false;
        }
        int originalValueStart = message.indexOf("原始值：") + "原始值：".length();
        int originalValueEnd = message.indexOf("，异常", originalValueStart);
        if (originalValueEnd < 0) {
            return false;
        }
        String originalValue = message.substring(originalValueStart, originalValueEnd);
        return CharSequenceUtil.isBlank(originalValue) || "null".equalsIgnoreCase(originalValue.trim());
    }

    @FunctionalInterface
    private interface Handler<T> {
        ApiResult<T> handle();
    }

    private void pushOperateLog(SourceTypeEnum businessType, Integer status, String erpBusinessCode, boolean sendMsg) {
        if(businessType == null){
            return;
        }
        DmpPushTaskEntity dmpPushTaskEntity = buildDmpPushTaskEntity(businessType, status, erpBusinessCode);
        try {
            String id = dmpTaskFeign.saveDmpPushTask(dmpPushTaskEntity);
            //增加异常预警
            if (!ApiResult.success().getCode().equals(status) && sendMsg) {
                dmpPushTaskEntity.setId(id);
                sendPushWarnMsg(dmpPushTaskEntity);
            }
        } catch (Exception e) {
            log.error("saveDmpPushTask:记录操作日志失败",e);
        }
    }

    private DmpPushTaskEntity buildDmpPushTaskEntity(SourceTypeEnum businessType, Integer status, String erpBusinessCode) {
        DmpPushTaskEntity dmpPushTaskEntity = new DmpPushTaskEntity();
        dmpPushTaskEntity.setSourcePlatformName(PlatformEnum.ERP_WMS.getDesc());
        dmpPushTaskEntity.setSourceType(businessType.getCode());
        dmpPushTaskEntity.setSourceId(erpBusinessCode);
        dmpPushTaskEntity.setSourceCode(erpBusinessCode);
        dmpPushTaskEntity.setTargetPlatformName(getPlatForm().getName());
        dmpPushTaskEntity.setStatus(status.equals(ApiResult.success().getCode()) ? SyncStatusEnum.SUCCESS_SYNC.getCode() : SyncStatusEnum.NO_NEED_SYNC.getCode());
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
        warnMsgInfo.setErpServerModuleEnum(ErpServerModuleEnum.ERP_SERVER_WMS);
        warnMsgInfo.setTitle(CharSequenceUtil.format("第三方仓【{}】从{}推送至{}失败", entity.getSourceCode(), entity.getSourcePlatformName(), entity.getTargetPlatformName()));
        warnMsgInfo.setTableName(SourceTypeEnum.getTableName(entity.getSourceType()));
        warnMsgInfo.setTableId(entity.getSourceId());
        warnMsgInfo.setKeyInfo(StringUtil.isEmpty(ThirdWarehouseContext.getMsg())?"":ThirdWarehouseContext.getMsg());
        warnMsgInfo.setWarnMsgTypeEnum(WarnMsgTypeEnum.SYS_EXCEPTION);
        return warnMsgInfo;
    }
}
