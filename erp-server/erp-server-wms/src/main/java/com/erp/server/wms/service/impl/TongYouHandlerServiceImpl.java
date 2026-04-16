package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.enums.OmsPlatformEnum;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.dto.OverseasProviderDTO;
import com.erp.model.wms.dto.third.*;
import com.erp.model.wms.enums.OverseasInstockTypeEnum;
import com.erp.model.wms.enums.ThirdWarehouseCancelResultEnum;
import com.erp.server.wms.convert.TongYouCreateInboundConverter;
import com.erp.server.wms.convert.TongYouCreateOutboundConverter;
import com.erp.server.wms.handler.AbstractThirdWarehouseHandler;
import com.sdk.wms.tongyou.dto.request.TongYouCreateInboundReq;
import com.sdk.wms.tongyou.dto.request.TongYouCreateOutboundReq;
import com.sdk.wms.tongyou.dto.response.TongYouBaseResp;
import com.sdk.wms.tongyou.dto.response.TongYouInboundResp;
import com.sdk.wms.tongyou.dto.response.TongYouOutboundResp;
import com.sdk.wms.tongyou.service.TongYouService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通邮处理服务实现类
 * @author will
 * @date 2025/11/11 15:21
 */
@Slf4j
@Service
@Validated
public class TongYouHandlerServiceImpl extends AbstractThirdWarehouseHandler {

    @Resource
    private TongYouService tongYouService;


    @Override
    public OmsPlatformEnum getPlatForm() {
        return OmsPlatformEnum.TONG_YOU;
    }

    @Override
    protected ApiResult<List<ThirdWarehouseSkuResp>> getSkuList(ThirdWarehouseProductReq productReq) {
        return success();
    }

    @Override
    public ApiResult<String> createInboundBill(ThirdWarehouseCreateInboundReq createInboundReq) {
        // 通邮推送需要默认ERP的头程发货单号-HH+MM+SS
        String timeFormatter = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss"));
        createInboundReq.setReceivingCode(CharSequenceUtil.format("{}_{}",createInboundReq.getReferenceNo(),timeFormatter));

        TongYouCreateInboundReq tongYouCreateInboundReq =  this.buildInboundDto(createInboundReq);
        TongYouBaseResp<TongYouInboundResp> TongYouInboundRespTongYouBaseResp = tongYouService.createInboundBill(tongYouCreateInboundReq);
        if(!isSuccess(TongYouInboundRespTongYouBaseResp.getError())){
            return failure(TongYouInboundRespTongYouBaseResp.getContent());
        }
        //通邮无单号返回直接给空字符串
        return success(createInboundReq.getReceivingCode());
    }


    /**
     * 处理request信息
     * @author will
     * @date 2025/11/17 17:35
     * @param createInboundReq
     * @return TongYouCreateInboundReq
     */
    private TongYouCreateInboundReq buildInboundDto(ThirdWarehouseCreateInboundReq createInboundReq) {
        TongYouCreateInboundReq request = new TongYouCreateInboundReq();
        //主表信息
        TongYouCreateInboundReq.AddDTO addDTO = TongYouCreateInboundConverter.INSTANCE.inboundToThird(createInboundReq);
        if (CharSequenceUtil.equals(createInboundReq.getReceivingType(), OverseasInstockTypeEnum.SELF_HEADWAY.getCode())) {
            addDTO.setJhfs("693");
            addDTO.setOrder_types("718");
        } else if (CharSequenceUtil.equals(createInboundReq.getReceivingType(), OverseasInstockTypeEnum.TRANSFER_AGENT.getCode())) {
            addDTO.setJhfs("692");
            addDTO.setOrder_types("717");
            addDTO.setTcck(createInboundReq.getTransitWarehouseCode());
            addDTO.setChqd(createInboundReq.getLogisticsChannel());
        }
        addDTO.setWaybill(createInboundReq.getReceivingCode());

        //明细信息
        List<TongYouCreateInboundReq.AddDetailDTO> addDetailDTOList = TongYouCreateInboundConverter.INSTANCE.inboundDetailToThird(createInboundReq.getItems());
        addDTO.setOrder_products(addDetailDTOList);
        request.setOrder_list(Collections.singletonList(addDTO));
        return request;
    }

    @Override
    protected ApiResult<String> editInboundBill(ThirdWarehouseCreateInboundReq createInboundReq) {
        throw new ServiceException("该仓库入库单不允许修改，请取消入库单后重新创建");
    }

    @Override
    public ApiResult<String> cancelInboundBill(@Valid ThirdWarehouseCancelInboundReq cancelInboundReq) {
        //通邮不支持直接取消，需要判断通邮入库单是否已取消状态，是则允许取消，否则不允许取消
        Map<String, Object> authJson = new HashMap<>();
        //密钥
        Object object = ThirdWarehouseContext.getAuthMap().get("appToken");
        authJson.put("token",ObjectUtil.isEmpty(object) ? "" : object.toString());
        authJson.put("waybill",cancelInboundReq.getReceivingCode());

        TongYouBaseResp<List<TongYouInboundResp>> resp = tongYouService.getInboundBill(authJson);
        if(!isSuccess(resp.getError())){
            //没有数据默认已取消
            if (CharSequenceUtil.equals("没有数据",resp.getContent())) {
                log.warn("通邮入库单{}未查询到数据，默认已取消",cancelInboundReq.getReceivingCode());
                return success();
            }
            throw new ServiceException("查询通邮入库单失败,"+resp.getContent());
        }
        if (CollUtil.isEmpty(resp.getData())) {
            return failure("未查询到对应通邮入库单信息");
        }
        TongYouInboundResp tongYouInboundResp = resp.getData().get(0);
        if (!CharSequenceUtil.equals(tongYouInboundResp.getStatus(),"7")) {
            throw new ServiceException("三方仓单据未取消，ERP不允许取消");
        }
        return success();
    }

    @Override
    protected ApiResult<List<ThirdWarehouseCalculateFeeResponse>> getCalculateFeeBatch(ThirdWarehouseCalculateFeeReq calculateFeeReq) {
        return success(Collections.singletonList(new ThirdWarehouseCalculateFeeResponse()));
    }

    @Override
    protected ApiResult<ThirdWarehouseUploadFileResponse> uploadFile(ThirdWarehouseUploadFileReq uploadFileReq) {
        return success(new ThirdWarehouseUploadFileResponse());
    }

    @Override
    protected ApiResult<ThirdWarehouseUploadOrderLabelResponse> uploadOrderLabel(ThirdWarehouseUploadOrderLabelReq uploadFileReq) {
        return success(new ThirdWarehouseUploadOrderLabelResponse());
    }

    @Override
    protected ApiResult<ThirdWarehouseUploadHandoverFileResponse> uploadHandoverFile(ThirdWarehouseUploadHandoverFileReq uploadHandoverFileReq) {
        return null;
    }

    @Override
    public ApiResult<ThirdWarehouseQueryOutboundResponse> createOutboundBill(ThirdWarehouseCreateOutboundReq createOutboundReq) {
        TongYouCreateOutboundReq TongYouCreateOutboundReq =  this.buildOutboundDto(createOutboundReq);
        log.warn(getPlatForm().getName()+"创建出库单请求:{}", JSONUtil.toJsonStr(TongYouCreateOutboundReq));
        TongYouBaseResp<TongYouOutboundResp> tongYouBaseResp = tongYouService.createOutboundBill(TongYouCreateOutboundReq);
        log.warn(getPlatForm().getName()+"创建出库单结果:{}", JSONUtil.toJsonStr(tongYouBaseResp));
        if(!isSuccess(tongYouBaseResp.getError())){
            return failure(tongYouBaseResp.getContent());
        }
        return success(ThirdWarehouseQueryOutboundResponse.builder().build());
    }

    @Override
    protected ApiResult<String> createFbaOutboundBill(ThirdWarehouseCreateFbaOutboundReq createOutboundReq) {
        return failure("ERP功能暂不支持");
    }

    /**
     * 海外仓出库数据格式化
     * @author will
     * @date 2025/11/21 16:57
     * @param createOutboundReq
     * @return TongYouCreateOutboundReq
     */
    private TongYouCreateOutboundReq buildOutboundDto(ThirdWarehouseCreateOutboundReq createOutboundReq) {
        //主表信息
        TongYouCreateOutboundReq addDTO = TongYouCreateOutboundConverter.INSTANCE.outboundToThird(createOutboundReq,createOutboundReq.getReceiverInfo());
        if (CharSequenceUtil.equals(createOutboundReq.getIsApiSignName(),"是")) {
            addDTO.setQmfw("AdultSignature");
        } else {
            addDTO.setQmfw("NoSignature");
        }
        addDTO.setZip(createOutboundReq.getReceiverInfo().getZipCode());
        //收货人电话
        String phone = createOutboundReq.getReceiverInfo().getPhone();
        //买家电话
        String buyerNumber = createOutboundReq.getReceiverInfo().getBuyerNumber();
        addDTO.setMobile(StrUtil.isBlank(phone) ? buyerNumber : phone);
        addDTO.setPhone(StrUtil.isBlank(buyerNumber) ? phone : buyerNumber);
        //明细信息
        List<TongYouCreateOutboundReq.AddDetailDTO> addDetailDTOList = TongYouCreateOutboundConverter.INSTANCE.outboundDetailToThird(createOutboundReq.getItems());
        addDTO.setDeliver_products(addDetailDTOList);
        return addDTO;

    }

    @Override
    public ApiResult<String> cancelOutboundBill(@Valid ThirdWarehouseCancelOutboundReq cancelOutboundReq) {
        log.warn(getPlatForm().getName()+"取消出库单请求:{}", JSONUtil.toJsonStr(cancelOutboundReq));
        TongYouBaseResp<String> tongYouBaseResp = tongYouService.cancelOutboundBill(cancelOutboundReq);
        log.warn(getPlatForm().getName()+"取消出库单结果:{}", JSONUtil.toJsonStr(tongYouBaseResp));
        if(!isSuccess(tongYouBaseResp.getError())){
            return failure(tongYouBaseResp.getContent());
        }
        return success(ThirdWarehouseCancelResultEnum.INTERCEPTION_SUCCESSFUL.getCode());

    }

    @Override
    protected ApiResult<String> cancelFbaOutboundBill(ThirdWarehouseCancelFbaOutboundReq cancelOutboundReq) {
        return failure("ERP功能暂不支持");
    }

    @Override
    protected ApiResult<ThirdWarehouseQueryOutboundResponse> queryOutboundBill(@Valid ThirdWarehouseQueryOutboundReq queryOutboundReq){
        Map<String, Object> authJson = new HashMap<>();
        //密钥
        Object object = ThirdWarehouseContext.getAuthMap().get("appToken");
        authJson.put("token",ObjectUtil.isEmpty(object) ? "" : object.toString());
        authJson.put("deliver_no",queryOutboundReq.getErpOrderCode());

        log.warn(getPlatForm().getName()+"查询出库单请求:{}", JSONUtil.toJsonStr(authJson));
        TongYouBaseResp<String> tongYouBaseResp = tongYouService.getOutboundBill(authJson);
        log.warn(getPlatForm().getName()+"查询出库单结果:{}", JSONUtil.toJsonStr(tongYouBaseResp));
        if(!isSuccess(tongYouBaseResp.getError())){
            return failure(tongYouBaseResp.getContent());
        }
        return success(ThirdWarehouseQueryOutboundResponse.builder().shippingOrderNo(tongYouBaseResp.getData()).build());
    }

    @Override
    protected ApiResult<List<ThirdWarehouseQueryFbaOutboundResponse>> queryFbaOutboundBill(ThirdWarehouseQueryFbaOutboundReq req) {
        return failure("ERP功能暂不支持");
    }

    @Override
    protected Boolean warehouseAuthorize(OverseasProviderDTO.AuthorizeParamDTO dto) {
        dto.getAuthJson().put("token",dto.getAuthJson().get("appToken"));
        TongYouBaseResp<String> response = tongYouService.getWarehouse(dto.getAuthJson());
        if(!isSuccess(response.getError())){
            throw new ServiceException("授权失败,"+response.getContent());
        }
        return true;
    }

    public boolean isSuccess(String code){
        return CharSequenceUtil.equals(code,"T");
    }

}
