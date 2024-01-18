package com.erp.server.wms.service.impl;

import com.common.business.enums.OmsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.dto.third.request.ThirdWarehouseCancelInboundReq;
import com.erp.model.wms.dto.third.request.ThirdWarehouseCancelOutboundReq;
import com.erp.model.wms.dto.third.request.ThirdWarehouseCreateInboundReq;
import com.erp.model.wms.dto.third.request.ThirdWarehouseCreateOutboundReq;
import com.erp.model.wms.enums.ThirdWarehouseCancelResultEnum;
import com.erp.server.wms.convert.OverseasWarehouseInboundConverter;
import com.erp.server.wms.handler.AbstractThirdWarehouseHandler;
import com.sdk.wms.goodcang.dto.request.GoodCangCreateInboundReq;
import com.sdk.wms.goodcang.dto.request.GoodCangCreateOutboundReq;
import com.sdk.wms.goodcang.dto.request.GoodCangGetOutBoundReq;
import com.sdk.wms.goodcang.dto.response.GoodCangOutboundResp;
import com.sdk.wms.goodcang.dto.response.GoodCangResponse;
import com.sdk.wms.goodcang.dto.response.GoodCangWarehouseResp;
import com.sdk.wms.goodcang.service.GoodCangService;
import io.seata.common.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author liuruipeng
 * @date 2023年11月17日 10:51
 */
@Slf4j
@Service
public class GoodCangHandlerServiceImpl extends AbstractThirdWarehouseHandler {

    @Resource
    private GoodCangService goodCangService;

    @Override
    public OmsPlatformEnum getPlatForm() {
        return OmsPlatformEnum.OMS_GOOD_CANG;
    }

    @Override
    public ApiResult<String> createInboundBill(ThirdWarehouseCreateInboundReq createInboundReq) {

        GoodCangCreateInboundReq goodCangCreateInboundReq = this.buildInboundDto(createInboundReq);
        // 创建入库单
        GoodCangResponse<String> goodCangResponse = goodCangService.createInboundBill(goodCangCreateInboundReq);

        return isSuccess(goodCangResponse.getAsk()) ? success(goodCangResponse.getData()) : failure(goodCangResponse.getMessage());
    }

    @Override
    protected ApiResult<String> editInboundBill(ThirdWarehouseCreateInboundReq createInboundReq) {
        GoodCangCreateInboundReq goodCangCreateInboundReq = this.buildInboundDto(createInboundReq);
        // 编辑入库单
        GoodCangResponse<String> goodCangResponse = goodCangService.editInboundBill(goodCangCreateInboundReq);

        return isSuccess(goodCangResponse.getAsk()) ? success(goodCangResponse.getData()) : failure(goodCangResponse.getMessage());
    }

    @Override
    public ApiResult<String> cancelInboundBill(@Valid ThirdWarehouseCancelInboundReq cancelInboundReq) {
        GoodCangResponse<String> response = goodCangService.cancelInboundBill(cancelInboundReq.getReceivingCode());
        return isSuccess(response.getAsk()) ? success(response.getData()) : failure(response.getMessage());
    }

    @Override
    public ApiResult<String> createOutboundBill(ThirdWarehouseCreateOutboundReq createOutboundReq) {
        GoodCangCreateOutboundReq cangCreateOutboundReq = OverseasWarehouseInboundConverter.INSTANCE.outboundDtoToGoodCang(createOutboundReq);
        GoodCangResponse<String> response = goodCangService.createOutboundBill(cangCreateOutboundReq);
        if(response.getMessage().contains("参考号重复")){
            return ApiResult.success();
        }
        return isSuccess(response.getAsk()) ? success(response.getData()) : failure(response.getMessage());
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
    protected Boolean hasWarehouse() {
        GoodCangResponse<List<GoodCangWarehouseResp>> response = goodCangService.getWarehouse();
        if(!isSuccess(response.getAsk())){
            throw new ServiceException("授权失败,"+response.getMessage());
        }
        return isSuccess(response.getAsk());
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
    public boolean isSuccess(String ask){
        return "Success".equals(ask);
    }
}
