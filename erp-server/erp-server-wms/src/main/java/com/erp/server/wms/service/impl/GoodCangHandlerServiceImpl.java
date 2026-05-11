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
import com.erp.model.wms.enums.B2bThirdWarehouseCancelResultEnum;
import com.erp.model.wms.enums.ThirdWarehouseCancelResultEnum;
import com.erp.server.wms.convert.OverseasWarehouseInboundConverter;
import com.erp.server.wms.convert.ThirdWarehouseConverter;
import com.erp.server.wms.handler.AbstractThirdWarehouseHandler;
import com.sdk.wms.goodcang.dto.request.*;
import com.sdk.wms.goodcang.dto.response.*;
import com.sdk.wms.goodcang.enums.GoodCangEnums;
import com.sdk.wms.goodcang.service.GoodCangService;
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
public class GoodCangHandlerServiceImpl extends AbstractThirdWarehouseHandler {
    private static final String GOOD_CANG_ORDER_ATTACHMENT = "ORDER_ATTACHMENT";

    @Resource
    private GoodCangService goodCangService;

    @Override
    public OmsPlatformEnum getPlatForm() {
        return OmsPlatformEnum.OMS_GOOD_CANG;
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

    @Override
    public ApiResult<String> createInboundBill(ThirdWarehouseCreateInboundReq createInboundReq) {

        GoodCangCreateInboundReq goodCangCreateInboundReq = this.buildInboundDto(createInboundReq);
        // 创建入库单
        GoodCangResponse<String> goodCangResponse = goodCangService.createInboundBill(goodCangCreateInboundReq);

        return isSuccess(goodCangResponse.getAsk(), "") ? success(goodCangResponse.getData()) : failure(goodCangResponse.getMessage());
    }

    @Override
    protected ApiResult<String> editInboundBill(ThirdWarehouseCreateInboundReq createInboundReq) {
        GoodCangCreateInboundReq goodCangCreateInboundReq = this.buildInboundDto(createInboundReq);
        // 编辑入库单
        GoodCangResponse<String> goodCangResponse = goodCangService.editInboundBill(goodCangCreateInboundReq);

        return isSuccess(goodCangResponse.getAsk(), "") ? success(goodCangResponse.getData()) : failure(goodCangResponse.getMessage());
    }

    @Override
    public ApiResult<String> cancelInboundBill(@Valid ThirdWarehouseCancelInboundReq cancelInboundReq) {
        GoodCangResponse<String> response = goodCangService.cancelInboundBill(cancelInboundReq.getReceivingCode());
        return isSuccess(response.getAsk(), "") ? success(response.getData()) : failure(response.getMessage());
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
    public ApiResult<ThirdWarehouseQueryOutboundResponse> createOutboundBill(ThirdWarehouseCreateOutboundReq createOutboundReq) {
        GoodCangCreateOutboundReq cangCreateOutboundReq = OverseasWarehouseInboundConverter.INSTANCE.outboundDtoToGoodCang(createOutboundReq);
        if(StringUtils.isNotBlank(createOutboundReq.getCarrierType())){
            cangCreateOutboundReq.setDistributorType(Integer.valueOf(createOutboundReq.getCarrierType()));
        }
        log.warn(getPlatForm().getName()+"创建出库单请求:{}", JSONUtil.toJsonStr(cangCreateOutboundReq));
        GoodCangResponse<String> response = goodCangService.createOutboundBill(cangCreateOutboundReq);
        log.warn(getPlatForm().getName()+"创建出库单结果:{}", JSONUtil.toJsonStr(response));
        if(response.getMessage().contains("参考号重复")){
            GoodCangResponse<String> orderCode = goodCangService.getOutboundCode(createOutboundReq.getReferenceNo());
            return success(ThirdWarehouseQueryOutboundResponse.builder().shippingOrderNo(orderCode.getData()).build());
        }
        return isSuccess(response.getAsk(), "") ? success(ThirdWarehouseQueryOutboundResponse.builder().shippingOrderNo(response.getData()).build()) : failure(response.getMessage());
    }

    @Override
    public ApiResult<ThirdWarehouseUploadFileResponse> uploadFile(@Valid ThirdWarehouseUploadFileReq uploadFileReq){
        GoodCangUploadFileReq goodCangUploadFileReq = GOOD_CANG_ORDER_ATTACHMENT.equalsIgnoreCase(uploadFileReq.getFileType())
                ? ThirdWarehouseConverter.INSTANCE.reqToGoodCangB2bAttachmentUploadFileReq(uploadFileReq)
                : ThirdWarehouseConverter.INSTANCE.reqToGoodCangUploadFileReq(uploadFileReq);
        if(CharSequenceUtil.isNotBlank(uploadFileReq.getFileType())){
            goodCangUploadFileReq.setUseFor(uploadFileReq.getFileType());
        }
        if(GOOD_CANG_ORDER_ATTACHMENT.equalsIgnoreCase(uploadFileReq.getFileType())
                && CharSequenceUtil.isNotBlank(uploadFileReq.getFileName())){
            goodCangUploadFileReq.setFileName(uploadFileReq.getFileName());
            goodCangUploadFileReq.setFile(cleanB2bAttachmentBase64(goodCangUploadFileReq.getFile(), uploadFileReq.getFileName()));
        }
        log.warn(getPlatForm().getName() + "上传文件请求: useFor={}, fileName={}", goodCangUploadFileReq.getUseFor(), goodCangUploadFileReq.getFileName());
        GoodCangResponse<GoodCangUploadFileResp> response = goodCangService.uploadFile(goodCangUploadFileReq);
        log.warn(getPlatForm().getName() + "上传文件结果:{}", JSONUtil.toJsonStr(response));
        if (Objects.isNull(response)) {
            return failure("谷仓上传文件响应为空");
        }
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
        GoodCangResponse<String> response = goodCangService.cancelOutboundBill(cancelOutboundReq.getOrderCode(),cancelOutboundReq.getReason());
        if(Objects.isNull(response.getCancelStatus())){
            return failure(response.getMessage());
        }
        if(response.getCancelStatus().equals(3)){
            return success(B2bThirdWarehouseCancelResultEnum.INTERCEPTION_FAILED.getCode());
        }
        return success(B2bThirdWarehouseCancelResultEnum.INTERCEPTION_SUCCESSFUL.getCode());
    }

    @Override
    protected ApiResult<ThirdWarehouseQueryOutboundResponse> queryOutboundBill(@Valid ThirdWarehouseQueryOutboundReq queryOutboundReq){
        GoodCangResponse<String> response = goodCangService.getOutboundCode(queryOutboundReq.getErpOrderCode());
        return CharSequenceUtil.isNotBlank(response.getData()) ? success(ThirdWarehouseQueryOutboundResponse.builder().shippingOrderNo(response.getData()).build()) : failure(response.getMessage());
    }

    @Override
    protected ApiResult<List<ThirdWarehouseQueryFbaOutboundResponse>> queryFbaOutboundBill(ThirdWarehouseQueryFbaOutboundReq req) {
        List<ThirdWarehouseQueryFbaOutboundResponse> responses = new ArrayList<>();
        String failureMsg = null;
        if (CollUtil.isNotEmpty(req.getPlatformOrderCodeList())) {
            GoodCangGetOutBoundReq goodCangGetOutBoundReq = GoodCangGetOutBoundReq.builder()
                    .orderCodeArr(req.getPlatformOrderCodeList())
                    .page(1)
                    .pageSize(20)
                    .build();

            GoodCangResponse<List<GoodCangOutboundResp>> response = goodCangService.getOutboundBatch(goodCangGetOutBoundReq);
            if (Objects.isNull(response)) {
                failureMsg = "谷仓查询订单响应为空";
            } else if(!isSuccess(response.getAsk(), response.getMessage())){
                failureMsg = response.getMessage();
            } else if (CollUtil.isNotEmpty(response.getData())) {
                for (GoodCangOutboundResp goodCangOrderDTO : response.getData()) {
                    responses.add(buildFbaOutboundResponse(goodCangOrderDTO));
                }
                return success(responses);
            } else {
                failureMsg = CharSequenceUtil.blankToDefault(response.getMessage(), "未查询到谷仓订单");
            }
            log.warn(getPlatForm().getName() + "按谷仓订单号未查询到B2B订单, orderCode={}, response={}", req.getPlatformOrderCodeList(), JSONUtil.toJsonStr(response));
        }

        if (CollUtil.isEmpty(req.getErpOrderCodeList())) {
            return failure(CharSequenceUtil.blankToDefault(failureMsg, "未查询到谷仓订单"));
        }
        for (String referenceNo : req.getErpOrderCodeList()) {
            GoodCangResponse<GoodCangOrderDTO> response = goodCangService.getOrderByRefCode(referenceNo);
            if (Objects.isNull(response) || !isSuccess(response.getAsk(), response.getMessage()) || Objects.isNull(response.getData())) {
                log.warn(getPlatForm().getName() + "按参考号未查询到B2B订单, referenceNo={}, response={}", referenceNo, JSONUtil.toJsonStr(response));
                continue;
            }
            responses.add(buildFbaOutboundResponse(response.getData(), referenceNo));
        }
        return CollUtil.isNotEmpty(responses) ? success(responses) : failure(CharSequenceUtil.blankToDefault(failureMsg, "未查询到谷仓订单"));
    }

    private ThirdWarehouseQueryFbaOutboundResponse buildFbaOutboundResponse(GoodCangOutboundResp goodCangOrderDTO) {
        ThirdWarehouseQueryFbaOutboundResponse res = new ThirdWarehouseQueryFbaOutboundResponse();
        res.setCode(goodCangOrderDTO.getReferenceNo());
        res.setPlatformOrderCode(goodCangOrderDTO.getOrderCode());
        res.setTrackNo(goodCangOrderDTO.getTrackNo());
        res.setDeliveryTimeStr(Objects.nonNull(goodCangOrderDTO.getOutBoundTime()) ? goodCangOrderDTO.getOutBoundTime().toString() : null);
        res.setPlatformOriginalStatus(goodCangOrderDTO.getOrderStatus());
        res.setStatus(GoodCangEnums.B2BOrderStatusEnum.getErpOrderStatus(goodCangOrderDTO.getOrderStatus()));
        return res;
    }

    private ThirdWarehouseQueryFbaOutboundResponse buildFbaOutboundResponse(GoodCangOrderDTO goodCangOrderDTO, String referenceNo) {
        ThirdWarehouseQueryFbaOutboundResponse res = new ThirdWarehouseQueryFbaOutboundResponse();
        res.setCode(CharSequenceUtil.blankToDefault(goodCangOrderDTO.getReferenceNo(), referenceNo));
        res.setPlatformOrderCode(goodCangOrderDTO.getOrderCode());
        res.setTrackNo(goodCangOrderDTO.getTrackingNo());
        res.setDeliveryTimeStr(goodCangOrderDTO.getDateShipping());
        res.setPlatformCreateTimeStr(goodCangOrderDTO.getDateCreate());
        res.setPlatformUpdateTimeStr(goodCangOrderDTO.getDateModify());
        res.setPlatformOriginalStatus(goodCangOrderDTO.getOrderStatus());
        res.setStatus(GoodCangEnums.B2BOrderStatusEnum.getErpOrderStatus(goodCangOrderDTO.getOrderStatus()));
        res.setErrorType(goodCangOrderDTO.getAbnormalProblemReason());
        res.setSwOrderNumber(goodCangOrderDTO.getPlatformOrderCode());
        res.setWarehouseCode(goodCangOrderDTO.getWarehouseCode());
        res.setShippingMethod(goodCangOrderDTO.getShippingMethod());
        res.setCarrierName(goodCangOrderDTO.getCarrierName());
        return res;
    }

    @Override
    protected Boolean warehouseAuthorize(OverseasProviderDTO.AuthorizeParamDTO dto) {
        GoodCangResponse<List<GoodCangWarehouseResp>> response = goodCangService.getWarehouse();
        if(!isSuccess(response.getAsk(), "")){
            throw new ServiceException("授权失败,"+response.getMessage());
        }
        return isSuccess(response.getAsk(), "");
    }

    private GoodCangCreateInboundReq buildInboundDto(ThirdWarehouseCreateInboundReq createInboundReq){
        GoodCangCreateInboundReq goodCangCreateInboundReq = OverseasWarehouseInboundConverter.INSTANCE.inboundDtoToGoodCang(createInboundReq);

        //处理揽收数据
        GoodCangCreateInboundReq.CollectingAddress collectingAddress = OverseasWarehouseInboundConverter.INSTANCE.inboundDtoToGoodCangCollect(createInboundReq);
        List<GoodCangCreateInboundReq.CollectingAddress> collectingAddressList = Collections.singletonList(collectingAddress);
        goodCangCreateInboundReq.setCollectingAddressList(collectingAddressList);
        // 处理箱子明细
        List<GoodCangCreateInboundReq.Item> itemList = new ArrayList<>();
        List<ThirdWarehouseCreateInboundReq.Item> requestItemList = createInboundReq.getItems();

        // 检查请求的商品数据是否为空
        if (CollectionUtils.isEmpty(requestItemList)) {
            throw new ServiceException("入库单产品数据为空");
        }

        // 根据箱号对商品进行分组
        Map<Integer, List<ThirdWarehouseCreateInboundReq.Item>> requestItemMap = requestItemList.stream().collect(Collectors.groupingBy(ThirdWarehouseCreateInboundReq.Item::getBoxNo));

        // 遍历分组后的数据，构建 GoodCang 的箱子明细对象
        requestItemMap.forEach((key, val) -> {
            GoodCangCreateInboundReq.Item item = new GoodCangCreateInboundReq.Item();
            item.setBoxNo(String.valueOf(key));

            List<GoodCangCreateInboundReq.Item.BoxDetail> boxDetails = val.stream()
                    .map(requestItem -> {
                        GoodCangCreateInboundReq.Item.BoxDetail boxDetail = new GoodCangCreateInboundReq.Item.BoxDetail();
                        boxDetail.setProductSku(requestItem.getProductSku());
                        boxDetail.setQuantity(requestItem.getQuantity());
                        return boxDetail;
                    })
                    .collect(Collectors.toList());

            item.setBox_detailList(boxDetails);
            itemList.add(item);
        });

        goodCangCreateInboundReq.setItems(itemList);
        return goodCangCreateInboundReq;
    }
    @Override
    protected ApiResult<String> createFbaOutboundBill(ThirdWarehouseCreateFbaOutboundReq createOutboundReq) {
        GoodCangCreateB2bReq cangCreateOutboundReq = this.buildB2bOrderReq(createOutboundReq);
        log.warn(getPlatForm().getName() + "创建B2B订单请求:{}", JSONUtil.toJsonStr(cangCreateOutboundReq));

        GoodCangResponse<String> response = goodCangService.createB2bBill(cangCreateOutboundReq);
        log.warn(getPlatForm().getName() + "创建B2B订单结果:{}", JSONUtil.toJsonStr(response));

        if (!isSuccess(response.getAsk(), "")) {
            return failure(response.getMessage());
        }

        String requestId = response.getRequestId();

        // 最多重试3次查询任务状态
        return queryTaskWithRetry(requestId, response.getData());
    }

    /**
     * 查询任务状态，支持重试机制
     * status: 0=处理中，1=成功，2=失败
     */
    private ApiResult<String> queryTaskWithRetry(String requestId, String originalData) {
        int maxRetries = 20;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            // 休眠等待异步处理（首次也等待，因为接口是异步的）
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                log.error("线程休眠异常", e);
                Thread.currentThread().interrupt();
                return failure("查询任务被中断");
            }

            GoodCangResponse<List<GoodCangTaskResp>> taskResp = goodCangService.taskStatusList(
                    Collections.singletonList(requestId)
            );
            log.warn(getPlatForm().getName() + "查询B2B订单结果(第{}次):{}", attempt, JSONUtil.toJsonStr(taskResp));

            if (Objects.isNull(taskResp)) {
                return failure("查询订单结果响应为空");
            }
            if (!isSuccess(taskResp.getAsk(), "")) {
                return failure(taskResp.getMessage());
            }

            List<GoodCangTaskResp> goodCangTaskResps = taskResp.getData();
            if (CollUtil.isEmpty(goodCangTaskResps)) {
                return failure("查询订单结果为空");
            }
            GoodCangTaskResp goodCangTaskResp = goodCangTaskResps.get(0);
            Integer status = goodCangTaskResp.getStatus();

            // status: 1=成功，直接返回
            if (Objects.equals(status, 1)) {
                return success(originalData);
            }

            // status: 2=失败，直接返回错误
            if (Objects.equals(status, 2)) {
                return failure(goodCangTaskResp.getErrorMessage());
            }

            // status: 0=处理中，继续重试（如果是最后一次，返回处理中状态）
            if (attempt == maxRetries) {
                log.warn("任务处理超时，requestId: {}", requestId);
                return failure("任务处理超时，请稍后查询");
            }

            log.warn("任务处理中，第{}次查询未就绪，继续等待...", attempt);
        }

        // 理论上不会执行到这里
        return failure("查询任务异常");
    }

    private String cleanB2bAttachmentBase64(String fileData, String fileName) {
        if (CharSequenceUtil.isBlank(fileData) || CharSequenceUtil.isBlank(fileName)) {
            return fileData;
        }
        String extension = StringUtils.substringAfterLast(fileName, ".");
        byte[] magic = getAttachmentMagic(extension);
        if (Objects.isNull(magic)) {
            return fileData;
        }
        try {
            byte[] bytes = Base64.getDecoder().decode(fileData);
            int index = indexOf(bytes, magic);
            if (index <= 0) {
                return fileData;
            }
            log.warn(getPlatForm().getName() + "B2B附件存在前置脏字节，上传前已裁剪, fileName={}, offset={}", fileName, index);
            return Base64.getEncoder().encodeToString(Arrays.copyOfRange(bytes, index, bytes.length));
        } catch (IllegalArgumentException e) {
            log.warn(getPlatForm().getName() + "B2B附件base64解析失败，保持原始内容上传, fileName={}", fileName, e);
            return fileData;
        }
    }

    private byte[] getAttachmentMagic(String extension) {
        if ("pdf".equalsIgnoreCase(extension)) {
            return new byte[]{'%', 'P', 'D', 'F', '-'};
        }
        if ("xlsx".equalsIgnoreCase(extension) || "docx".equalsIgnoreCase(extension)) {
            return new byte[]{'P', 'K'};
        }
        return null;
    }

    private int indexOf(byte[] bytes, byte[] magic) {
        if (Objects.isNull(bytes) || Objects.isNull(magic) || bytes.length < magic.length) {
            return -1;
        }
        for (int i = 0; i <= bytes.length - magic.length; i++) {
            boolean matched = true;
            for (int j = 0; j < magic.length; j++) {
                if (bytes[i + j] != magic[j]) {
                    matched = false;
                    break;
                }
            }
            if (matched) {
                return i;
            }
        }
        return -1;
    }

    private GoodCangCreateB2bReq buildB2bOrderReq(ThirdWarehouseCreateFbaOutboundReq createOutboundReq) {
        GoodCangCreateB2bReq goodCangCreateB2bReq = new GoodCangCreateB2bReq();
        goodCangCreateB2bReq.setReferenceNo(createOutboundReq.getReferenceNo());
        goodCangCreateB2bReq.setPackingType("0");
        goodCangCreateB2bReq.setVerify(1);
        goodCangCreateB2bReq.setWarehouseCode(createOutboundReq.getThirdWarehouseCode());
        goodCangCreateB2bReq.setRecipientInfo(GoodCangCreateB2bReq.RecipientInfo.builder()
                        .address1(createOutboundReq.getAddress1())
                        .city(createOutboundReq.getCity())
                        .countryCode(createOutboundReq.getReceiverCountryCode())
                        .name(createOutboundReq.getReceiverName())
                        .phone(createOutboundReq.getTelNumber())
                        .province(createOutboundReq.getProvince())
                        .zipcode(createOutboundReq.getPostCode()).build());
        goodCangCreateB2bReq.setDeliveryService(GoodCangCreateB2bReq.DeliveryService.builder()
                        .isInsurance(createOutboundReq.getIsInsurance()?1:0)
                        .isSignature(createOutboundReq.getIsSignature()?1:0)
                        .smCode(createOutboundReq.getChannelCode()).build());
        List<GoodCangCreateB2bReq.Item> itemList = new ArrayList<>();
        for (ThirdWarehouseCreateFbaOutboundReq.Item item : createOutboundReq.getItems()){
            GoodCangCreateB2bReq.Item productItem = new GoodCangCreateB2bReq.Item();
            productItem.setProductSku(item.getWarehousePlatformSku());
            Integer quantity = item.getDeliveryQty();
            if (Objects.isNull(quantity)) {
                Integer boxQty = Objects.nonNull(item.getBoxQty()) ? item.getBoxQty() : 0;
                Integer perBoxQty = Objects.nonNull(item.getPerBoxQty()) ? item.getPerBoxQty() : 0;
                quantity = boxQty * perBoxQty;
            }
            productItem.setQuantity(quantity);
            itemList.add(productItem);
        }
        goodCangCreateB2bReq.setWarehouseService(GoodCangCreateB2bReq.WarehouseService.builder()
                        .boxMarkNum(0)
                        .isChangeLabel(0)
                        .itemList(itemList).build());
        goodCangCreateB2bReq.setOtherInfo(GoodCangCreateB2bReq.OtherInfo.builder()
                        .orderDesc(createOutboundReq.getRemark())
                        .packingFileId(StringUtils.isNotBlank(createOutboundReq.getFileId())?Integer.valueOf(createOutboundReq.getFileId()):null)
                        .build());
        return goodCangCreateB2bReq;
    }

    public boolean isSuccess(String ask, String message){
        return "Success".equals(ask) ||"success".equals(message);
    }
}
