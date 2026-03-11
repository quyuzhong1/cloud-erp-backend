package com.erp.server.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.enums.OmsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.wms.dto.OverseasProviderDTO;
import com.erp.model.wms.dto.third.*;
import com.erp.model.wms.enums.ThirdWarehouseCancelResultEnum;
import com.erp.server.wms.convert.OverseasWarehouseInboundConverter;
import com.erp.server.wms.convert.ThirdWarehouseConverter;
import com.erp.server.wms.handler.AbstractThirdWarehouseHandler;
import com.sdk.wms.goodcang.dto.request.*;
import com.sdk.wms.goodcang.dto.response.*;
import com.sdk.wms.goodcang.service.GoodCangService;
import com.sdk.wms.zhongbao.dto.request.OverseasInboundCancelRequest;
import com.sdk.wms.zhongbao.dto.request.OverseasInboundCreateRequest;
import com.sdk.wms.zhongbao.dto.response.*;
import com.sdk.wms.zhongbao.service.ZhongbaoService;
import io.seata.common.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author liuruipeng
 * @date 2023年11月17日 10:51
 */
@Slf4j
@Service
public class ZhongBaoHandlerServiceImpl extends AbstractThirdWarehouseHandler {

    @Resource
    private GoodCangService goodCangService;

    @Resource
    private ZhongbaoService zhongbaoService;
    @Override
    public OmsPlatformEnum getPlatForm() {
        return OmsPlatformEnum.ZHONG_BAO;
    }

    @Override
    protected ApiResult<ThirdWarehouseUploadHandoverFileResponse> uploadHandoverFile(ThirdWarehouseUploadHandoverFileReq uploadHandoverFileReq) {
        return null;
    }

    @Override
    protected ApiResult<List<ThirdWarehouseSkuResp>> getSkuList(ThirdWarehouseProductReq productReq) {
        GoodCangGetSkuReq goodCangGetSkuReq = GoodCangGetSkuReq.builder()
                .page(1)
                .pageSize(100)
                .productSkuArr(productReq.getSkuNoList())
                .build();
        List<GoodCangSkuResp> respList = new ArrayList<>();
        int page = 1;
        while (true) {
            goodCangGetSkuReq.setPage(page);
            GoodCangResponse<List<GoodCangSkuResp>> goodCangResponse = goodCangService.getSkuList(goodCangGetSkuReq);
            if (!isSuccess(goodCangResponse.getAsk(), "")) {
                log.error("谷仓查询产品信息异常" + goodCangResponse);
                return failure(goodCangResponse.getMessage());
            }
            respList.addAll(goodCangResponse.getData());
            if (goodCangResponse.getCount() <= page * 100) {
                break;
            }
            page++;
        }
        List<ThirdWarehouseSkuResp> thirdWarehouseSkuRespList = BeanUtil.copyToList(respList,ThirdWarehouseSkuResp.class);
        return success(thirdWarehouseSkuRespList);
    }

    /***
     * B2C发货单下推海外仓
     * @param createInboundReq
     * @return
     */
    @Override
    public ApiResult<String> createInboundBill(ThirdWarehouseCreateInboundReq createInboundReq) {
        OverseasInboundCreateRequest overseasInboundCreateRequest = this.buildInboundDto(createInboundReq);
        // 创建入库单
        BaseResponse<OverseasInboundCreateResponse> responseBaseResponse = zhongbaoService.overseasInboundCreate(overseasInboundCreateRequest);
        return responseBaseResponse.getSuccess() ? success(responseBaseResponse.getData().getOrderNo()) : failure(responseBaseResponse.getMessage() +":"+ responseBaseResponse.getErrors().stream().collect(Collectors.joining(", ")));
    }

    @Override
    protected ApiResult<String> editInboundBill(ThirdWarehouseCreateInboundReq createInboundReq) {
        OverseasInboundCreateRequest overseasInboundCreateRequest = this.buildInboundDto(createInboundReq);
        // 编辑入库单
        BaseResponse<OverseasInboundUpdateResponse> responseBaseResponse = zhongbaoService.overseasInboundUpdate(overseasInboundCreateRequest);

        return responseBaseResponse.getSuccess() ? success(responseBaseResponse.getData().getOrderNo()) : failure(responseBaseResponse.getMessage());
    }

    @Override
    public ApiResult<String> cancelInboundBill(@Valid ThirdWarehouseCancelInboundReq cancelInboundReq) {
        OverseasInboundCancelRequest overseasInboundCancelRequest = OverseasInboundCancelRequest.builder()
                .orderNos(Collections.singletonList(cancelInboundReq.getReceivingCode()))
                .cancelRemark(cancelInboundReq.getRemark())
                .build();
        BaseResponse<OverseasInboundCancelResponse> responseBaseResponse = zhongbaoService.overseasInboundCancel(overseasInboundCancelRequest);
        return responseBaseResponse.getSuccess() ? success(responseBaseResponse.getData().getSuccessList().get(0).getOrderNo()) : failure(responseBaseResponse.getData().getFailList().get(0).getMessage());
    }

    /***
     * B2C发货单下推海外仓
     * @param createInboundReq
     * @return
     */
    @Override
    public ApiResult<String> approveInboundBill(ThirdWarehouseCreateInboundReq createInboundReq) {
        OverseasInboundCreateRequest overseasInboundCreateRequest = this.buildInboundDto(createInboundReq);
        // 创建入库单
        BaseResponse<OverseasInboundApproveResponse> responseBaseResponse = zhongbaoService.overseasInboundApprove(overseasInboundCreateRequest);
        return responseBaseResponse.getSuccess() ? success(responseBaseResponse.getData().getOrderNo()) : failure(responseBaseResponse.getMessage());
    }
    @Override
    public ApiResult<List<ThirdWarehouseCalculateFeeResponse>> getCalculateFeeBatch(@Valid ThirdWarehouseCalculateFeeReq calculateFeeReq) {
        GoodCangCalculateDeliveryFeeReq goodCangCalculateDeliveryFeeReq = ThirdWarehouseConverter.INSTANCE.reqToGucangCalculateFeeReq(calculateFeeReq);
        GoodCangResponse<List<GoodCangCalculateDeliveryFeeResp>> response = goodCangService.getCalculateDeliveryFee(goodCangCalculateDeliveryFeeReq);
        List<GoodCangCalculateDeliveryFeeResp> goodCangCalculateDeliveryFeeRespList = response.getData();
        String currency = response.getCurrency();
        List<ThirdWarehouseCalculateFeeResponse> dataList = convertCalculateDeliveryFeeResp(currency, goodCangCalculateDeliveryFeeRespList);
        return isSuccess(response.getAsk(), "") ? success(dataList) : failure(response.getMessage());
    }

    private List<ThirdWarehouseCalculateFeeResponse> convertCalculateDeliveryFeeResp(String currency, List<GoodCangCalculateDeliveryFeeResp> goodCangCalculateDeliveryFeeRespList) {
        if (CollUtil.isEmpty(goodCangCalculateDeliveryFeeRespList)){
            return Collections.emptyList();
        }
        List<ThirdWarehouseCalculateFeeResponse> list = new ArrayList<>();
        for (GoodCangCalculateDeliveryFeeResp resp : goodCangCalculateDeliveryFeeRespList){
            ThirdWarehouseCalculateFeeResponse response = ThirdWarehouseConverter.INSTANCE.gucangResToThirdWarehouseResponse(resp);
            response.setCurrency(currency);
            List<GoodCangCalculateDeliveryFeeResp.Income> income = resp.getIncome();
            //设置其他费用
            setOtherCostByIncome(response, income);
            list.add(response);
        }
        return list;
    }

    private void setOtherCostByIncome(ThirdWarehouseCalculateFeeResponse response, List<GoodCangCalculateDeliveryFeeResp.Income> income) {
        if(CollUtil.isEmpty(income)){
            response.setShippingCost(BigDecimal.ZERO);
            response.setDeclareCost(BigDecimal.ZERO);
            response.setOtherCost(BigDecimal.ZERO);
            response.setRegistrationCost(BigDecimal.ZERO);
            response.setOperatingCost(BigDecimal.ZERO);
        }else {
            for (GoodCangCalculateDeliveryFeeResp.Income cost : income){
                if (cost.getName().contains("运输费")){
                    response.setShippingCost(new BigDecimal(cost.getAmount()));
                }else if (cost.getName().contains("关税") || cost.getName().contains("报关费") || cost.getName().contains("偏远住宅费") || cost.getName().contains("附加费")){
                    BigDecimal amount = new BigDecimal(cost.getAmount());
                    BigDecimal declareCost = Objects.nonNull(response.getDeclareCost()) ? response.getDeclareCost() : BigDecimal.ZERO;
                    response.setDeclareCost(MathUtil.add(amount, declareCost));
                }else if (cost.getName().contains("操作费")){
                    response.setOperatingCost(new BigDecimal(cost.getAmount()));
                }
            }
        }
    }

    @Override
    public ApiResult<String> createOutboundBill(ThirdWarehouseCreateOutboundReq createOutboundReq) {
        GoodCangCreateOutboundReq cangCreateOutboundReq = OverseasWarehouseInboundConverter.INSTANCE.outboundDtoToGoodCang(createOutboundReq);
        if(StringUtils.isNotBlank(createOutboundReq.getCarrierType())){
            cangCreateOutboundReq.setDistributorType(Integer.valueOf(createOutboundReq.getCarrierType()));
        }
        log.warn(getPlatForm().getName()+"创建出库单请求:{}", JSONUtil.toJsonStr(cangCreateOutboundReq));
        GoodCangResponse<String> response = goodCangService.createOutboundBill(cangCreateOutboundReq);
        log.warn(getPlatForm().getName()+"创建出库单结果:{}", JSONUtil.toJsonStr(response));
        if(response.getMessage().contains("参考号重复")){
            GoodCangResponse<String> orderCode = goodCangService.getOutboundCode(createOutboundReq.getReferenceNo());
            return success(orderCode.getData());
        }
        return isSuccess(response.getAsk(), "") ? success(response.getData()) : failure(response.getMessage());
    }

    @Override
    public ApiResult<ThirdWarehouseUploadFileResponse> uploadFile(@Valid ThirdWarehouseUploadFileReq uploadFileReq){
        GoodCangUploadFileReq goodCangUploadFileReq = ThirdWarehouseConverter.INSTANCE.reqToGoodCangUploadFileReq(uploadFileReq);
        if(CharSequenceUtil.isNotBlank(uploadFileReq.getFileType())){
            goodCangUploadFileReq.setUseFor(uploadFileReq.getFileType());
        }
        GoodCangResponse<GoodCangUploadFileResp> response = goodCangService.uploadFile(goodCangUploadFileReq);
        GoodCangUploadFileResp goodCangUploadFileResp = response.getData();
        ThirdWarehouseUploadFileResponse resToThirdWarehouseResponse = ThirdWarehouseConverter.INSTANCE.goodCangResToThirdWarehouseUploadFileResponse(goodCangUploadFileResp);
        return isSuccess(response.getAsk(), response.getMessage()) ? success(resToThirdWarehouseResponse) : failure(response.getMessage());

    }
    @Override
    public ApiResult<ThirdWarehouseUploadOrderLabelResponse> uploadOrderLabel(@Valid ThirdWarehouseUploadOrderLabelReq uploadFileReq){
        GoodCangUploadOrderLabelReq goodCangUploadFileReq = ThirdWarehouseConverter.INSTANCE.reqToGoodCangUploadOrderLabelReq(uploadFileReq);
        GoodCangResponse<GoodCangUploadOrderLabelResp> response = goodCangService.uploadOrderLabel(goodCangUploadFileReq);
        GoodCangUploadOrderLabelResp resp = response.getData();
        ThirdWarehouseUploadOrderLabelResponse uploadOrderLabelResponse = ThirdWarehouseConverter.INSTANCE.googCangResToThirdWarehouseUploadOrderLabelResponse(resp);
        if(response.getMessage().contains("订单状态已确认")){
            return success(new ThirdWarehouseUploadOrderLabelResponse(uploadFileReq.getOrderCode()));
        }
        return isSuccess(response.getAsk(), response.getMessage()) ? success(uploadOrderLabelResponse) : failure(response.getMessage());

    }

    @Override
    public ApiResult<String> cancelOutboundBill(@Valid ThirdWarehouseCancelOutboundReq cancelOutboundReq) {
        GoodCangResponse<String> response = goodCangService.cancelOutboundBill(cancelOutboundReq.getOrderCode(),cancelOutboundReq.getReason());
        if(Objects.isNull(response.getCancelStatus())){
            return failure(response.getMessage());
        }
        if(response.getCancelStatus().equals(3)){
            return success(ThirdWarehouseCancelResultEnum.INTERCEPTION_FAILED.getCode());
        }
        return success(ThirdWarehouseCancelResultEnum.INTERCEPTION_SUCCESSFUL.getCode());
    }

    @Override
    protected ApiResult<String> cancelFbaOutboundBill(ThirdWarehouseCancelFbaOutboundReq cancelOutboundReq) {
        return failure("ERP功能暂不支持");
    }

    @Override
    protected ApiResult<String> queryOutboundBill(@Valid ThirdWarehouseQueryOutboundReq queryOutboundReq){
        GoodCangResponse<String> response = goodCangService.getOutboundCode(queryOutboundReq.getErpOrderCode());
        return CharSequenceUtil.isNotBlank(response.getData()) ? success(response.getData()) : failure(response.getMessage());
    }

    @Override
    protected ApiResult<List<ThirdWarehouseQueryFbaOutboundResponse>> queryFbaOutboundBill(ThirdWarehouseQueryFbaOutboundReq req) {
        return failure("ERP功能暂不支持");
    }

    @Override
    protected Boolean warehouseAuthorize(OverseasProviderDTO.AuthorizeParamDTO dto) {
        Map<String, Object> authJson = dto.getAuthJson();
        BaseResponse open = zhongbaoService.open(zhongbaoService.getToken(authJson));
        if (open.getSuccess()){
            return true;
        }else {
            throw new ServiceException("授权失败,"+open.getMessage());
        }
    }

    private OverseasInboundCreateRequest buildInboundDto(ThirdWarehouseCreateInboundReq createInboundReq){
        OverseasInboundCreateRequest inboundCreateRequest = OverseasWarehouseInboundConverter.INSTANCE.inboundDtoToZhongbao(createInboundReq);
        List<OverseasInboundCreateRequest.Attachment> attachmentOpenDTOs = new ArrayList<>();
        attachmentOpenDTOs.add(OverseasInboundCreateRequest.Attachment.builder().base64(createInboundReq.getFileBase64()).fileName(createInboundReq.getFileName()).build());
        inboundCreateRequest.setAttachmentOpenDTOs(attachmentOpenDTOs);
        return inboundCreateRequest;
    }
    @Override
    protected ApiResult<String> createFbaOutboundBill(ThirdWarehouseCreateFbaOutboundReq createOutboundReq) {
        return failure("ERP功能暂不支持");
    }
    public boolean isSuccess(String ask, String message){
        return "Success".equals(ask) ||"success".equals(message);
    }
}
