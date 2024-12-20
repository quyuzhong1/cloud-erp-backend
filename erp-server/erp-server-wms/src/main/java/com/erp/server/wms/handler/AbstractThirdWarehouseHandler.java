package com.erp.server.wms.handler;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.enums.ErpServerModuleEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncStatusEnum;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.dto.ThirdWarehouseTaskDTO;
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
import com.sdk.wms.antu.dto.request.AntuCalculateFeeReq;
import com.sdk.wms.antu.dto.response.AntuCalculateFeeResp;
import com.sdk.wms.antu.dto.response.AntuResponse;
import io.seata.common.util.CollectionUtils;
import io.seata.common.util.StringUtils;
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

    @Resource
    private OverseasProviderService overseasProviderService;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private MQProducerService<T> mqProducerService;

    public void handleAuthInfo(String id) {
        OverseasProviderEntity authEntity = getAuthEntity(id);
        if(Objects.isNull(authEntity)){
            throw new ServiceException(ApiError.NOT_FOUND_OVERSEAS_PROVIDE);
        }
        if(!authEntity.getAuthStatus().equals(AuthStatusEnum.ALREADY.getCode())){
            throw new ServiceException(ApiError.OVERSEAS_PROVIDE_NOT_AUTH);
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
            boolean result = hasWarehouse();
            if (result) {
                dmpTaskFeign.createThirdWarehouseTask(new ThirdWarehouseTaskDTO.AddDTO(dto.getId(), dto.getAuthJson(), getPlatForm().getCode()));
            }
            return result;
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
    public ApiResult<String> createOutboundBill(ThirdWarehouseCreateOutboundReq createOutboundReq, String authId) {
        //相同sku合并数量
        if(CollectionUtils.isNotEmpty(createOutboundReq.getItems())){
            Map<String,Integer> mergeSkuMap = createOutboundReq.getItems().stream().collect(Collectors.toMap(ThirdWarehouseCreateOutboundReq.Item::getProductSku, ThirdWarehouseCreateOutboundReq.Item::getQuantity, Integer::sum));
            //将map转成List<Item>
            createOutboundReq.setItems(mergeSkuMap.entrySet().stream().map(v->new ThirdWarehouseCreateOutboundReq.Item(v.getKey(),v.getValue())).collect(Collectors.toList()));
        }
        return handleAndRemoveContext(() -> createOutboundBill(createOutboundReq), authId,SourceTypeEnum.THIRD_WAREHOUSE_CREATE_OUTBOUND_BILL,createOutboundReq.getReferenceNo());
    }

    @Override
    public ApiResult<String> cancelOutboundBill(ThirdWarehouseCancelOutboundReq cancelOutboundReq, String authId) {
        return handleAndRemoveContext(() -> cancelOutboundBill(cancelOutboundReq), authId,SourceTypeEnum.THIRD_WAREHOUSE_CANCEL_OUTBOUND_BILL,cancelOutboundReq.getOrderCode());
    }

    @Override
    public ApiResult<List<ThirdWarehouseCalculateFeeResponse>> getCalculateFeeBatch(ThirdWarehouseCalculateFeeReq calculateFeeReq, String authId) {
        return handleAndRemoveContext(() -> getCalculateFeeBatch(calculateFeeReq), authId,SourceTypeEnum.THIRD_WAREHOUSE_CALCULATE_FEE,calculateFeeReq.getCountryCode());
    }

    protected abstract ApiResult<List<ThirdWarehouseSkuResp>> getSkuList(ThirdWarehouseProductReq productReq);

    protected abstract ApiResult<String> createInboundBill(ThirdWarehouseCreateInboundReq createInboundReq);

    protected abstract ApiResult<String> editInboundBill(ThirdWarehouseCreateInboundReq createInboundReq);

    protected abstract ApiResult<String> cancelInboundBill(@Valid ThirdWarehouseCancelInboundReq cancelInboundReq);
    protected abstract ApiResult<List<ThirdWarehouseCalculateFeeResponse>> getCalculateFeeBatch(@Valid ThirdWarehouseCalculateFeeReq calculateFeeReq);

    protected abstract ApiResult<String> createOutboundBill(ThirdWarehouseCreateOutboundReq createOutboundReq);

    protected abstract ApiResult<String> cancelOutboundBill(@Valid ThirdWarehouseCancelOutboundReq cancelOutboundReq);

    protected abstract Boolean hasWarehouse();

    private <T> ApiResult<T> handleAndRemoveContext(Handler<T> handler, String authId,SourceTypeEnum businessType,String erpBusinessCode) {
        try {
            //设置授权信息
            handleAuthInfo(authId);
            //执行逻辑
            ApiResult<T> result = handler.handle();
            ThirdWarehouseContext.setMsg(result.getMsg());
            //记录日志
            pushOperateLog(businessType,result.getCode(),erpBusinessCode, false);
            return result;
        } catch (Exception e){
            log.error(ApiError.THIRD_WAREHOUSE_INTERFACE_EXCEPTION.msg,e);
            ThirdWarehouseContext.setMsg(ExceptionUtil.stacktraceToString(e,2000));
            pushOperateLog(businessType,2000,erpBusinessCode, true);
            return ApiResult.error(ApiError.THIRD_WAREHOUSE_INTERFACE_EXCEPTION.code,e.getMessage());
        } finally {
            // remove thread-local
            ThirdWarehouseContext.remove();
        }
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
            String id = dmpTaskFeign.saveOrUpdateDmpPushTask(dmpPushTaskEntity);
            //增加异常预警
            if (!ApiResult.success().getCode().equals(status) && sendMsg) {
                dmpPushTaskEntity.setId(id);
                sendPushWarnMsg(dmpPushTaskEntity);
            }
        } catch (Exception e) {
            log.error("saveOrUpdateDmpPushTask:记录操作日志失败",e);
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