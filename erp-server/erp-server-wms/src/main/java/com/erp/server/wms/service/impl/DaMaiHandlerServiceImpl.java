package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.enums.OmsPlatformEnum;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.dto.OverseasProviderDTO;
import com.erp.model.wms.dto.third.*;
import com.erp.server.wms.handler.AbstractThirdWarehouseHandler;
import com.sdk.wms.damai.dto.request.DaMaiCalculateFeeRequest;
import com.sdk.wms.damai.dto.request.DaMaiCancelInboundRequest;
import com.sdk.wms.damai.dto.request.DaMaiCreateInboundRequest;
import com.sdk.wms.damai.dto.response.*;
import com.sdk.wms.damai.service.DaMaiService;
import com.sdk.wms.jifeng.dto.response.JiFengBaseResp;
import com.sdk.wms.jifeng.dto.response.JiFengCreateInboundResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @author liuruipeng
 * @date 2023年11月17日 10:51
 */
@Slf4j
@Service
public class DaMaiHandlerServiceImpl extends AbstractThirdWarehouseHandler {

    @Resource
    private DaMaiService daMaiService;

    @Override
    public OmsPlatformEnum getPlatForm() {
        return OmsPlatformEnum.DA_MAI;
    }


    @Override
    protected ApiResult<List<ThirdWarehouseSkuResp>> getSkuList(ThirdWarehouseProductReq productReq) {
        return null;
    }

    @Override
    protected ApiResult<String> createInboundBill(ThirdWarehouseCreateInboundReq createInboundReq) {
        DaMaiCreateInboundRequest daMaiCreateInboundRequest = this.buildInboundDto(createInboundReq);
        DaMaiBaseResp<DaMaiCreateInboundResp> resp = daMaiService.createInbound(ThirdWarehouseContext.getAuthMap(), daMaiCreateInboundRequest);
        if(!isSuccess(resp)){
            return failure(resp.getMsg());
        }
        return success(resp.getData().getAsnAnNo());
    }

    private DaMaiCreateInboundRequest buildInboundDto(ThirdWarehouseCreateInboundReq createInboundReq) {
        List<DaMaiCreateInboundRequest.AsnAnSkuListDTO> asnAnSkuList = new ArrayList<>();
        //相同sku相同数量的sku 的箱子合并
        List<ThirdWarehouseCreateInboundReq.Item> items = createInboundReq.getItems();
        //根据箱号排序
        items.sort(Comparator.comparing(ThirdWarehouseCreateInboundReq.Item::getBoxNo));
        // 使用 LinkedHashMap 保证分组后的 Key 顺序
        Map<Integer, List<ThirdWarehouseCreateInboundReq.Item>> originItemMap = items.stream()
                .collect(Collectors.groupingBy(
                        ThirdWarehouseCreateInboundReq.Item::getBoxNo,
                        LinkedHashMap::new,  // 指定有序 Map 实现
                        Collectors.toList()
                ));
        originItemMap.forEach((boxNo,itemList) -> {
            String batchNo = "";
            //查询是否有相同规格的箱子
            for (Map.Entry<Integer, List<ThirdWarehouseCreateInboundReq.Item>> entry : originItemMap.entrySet()) {
                Integer tempBoxNo = entry.getKey();
                List<ThirdWarehouseCreateInboundReq.Item> tempItemList = entry.getValue();
                if(boxNo >= tempBoxNo){
                    continue;
                }
                if(itemList.size() != tempItemList.size()){
                    continue;
                }
                for (ThirdWarehouseCreateInboundReq.Item tempItem : tempItemList) {
                    for (ThirdWarehouseCreateInboundReq.Item item : itemList) {
                        if(!tempItem.getProductSku().equals(item.getProductSku()) || !tempItem.getQuantity().equals(item.getQuantity())){
                            batchNo = item.getBatchNo();
                            break;
                        }
                    }
                }
            }
            Map<String,Integer> skuMap = itemList.stream()
                    .collect(Collectors.toMap(ThirdWarehouseCreateInboundReq.Item::getProductSku, ThirdWarehouseCreateInboundReq.Item::getQuantity, Integer::sum));
            if(StringUtils.isNotBlank(batchNo)){
                String finalBatchNo = batchNo;
                List<DaMaiCreateInboundRequest.AsnAnSkuListDTO> sameBatchList = asnAnSkuList.stream()
                        .filter(v -> v.getCustPackageNo().equals(finalBatchNo)).collect(Collectors.toList());
                itemList.get(0).setBatchNo(batchNo);
                //箱数+1，重新计算单箱sku数量
                for (DaMaiCreateInboundRequest.AsnAnSkuListDTO asnAnSkuListDTO : sameBatchList) {
                    Integer packQty = asnAnSkuListDTO.getPackQty();
                    packQty = packQty + 1;
                    asnAnSkuListDTO.setPackQty(packQty);
                    asnAnSkuListDTO.setTotalSkuQty((asnAnSkuListDTO.getTotalSkuQty() + skuMap.get(asnAnSkuListDTO.getCustSkuCode()))/packQty);
                }
            }else{
                //批次号，当前日期
                String custLotNo = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                String custPackageNo = "";
                //计算客户箱唛前缀ZXGG+三位流水号
                if(CollectionUtils.isEmpty(asnAnSkuList)){
                    custPackageNo = "ZXGG001";
                }else{
                    //获取上一个箱子的箱唛 +1
                    String lastCustPackageNo = asnAnSkuList.get(asnAnSkuList.size() - 1).getCustPackageNo();
                    int lastNumber = Integer.parseInt(lastCustPackageNo.replace("ZXGG", ""));
                    lastNumber = lastNumber + 1;
                    custPackageNo = "ZXGG" + String.format("%03d", lastNumber);
                }
                itemList.get(0).setBatchNo(custPackageNo);
                for (Map.Entry<String, Integer> entry : skuMap.entrySet()) {
                    DaMaiCreateInboundRequest.AsnAnSkuListDTO asnAnSkuListDTO = DaMaiCreateInboundRequest.AsnAnSkuListDTO.builder()
                            .custSkuCode(entry.getKey())
                            .custLotNo(custLotNo)
                            .custPackageNo(custPackageNo)
                            .totalSkuQty(entry.getValue())
                            .packQty(1)
                            .build();
                    asnAnSkuList.add(asnAnSkuListDTO);
                }
            }
        });
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return DaMaiCreateInboundRequest.builder()
                .whCode(createInboundReq.getWarehouseCode())
                .custReferenceNo(createInboundReq.getReferenceNo())
                .arrivalTime(createInboundReq.getEtaDate().format(formatter))
                .logisticsTrackingNo(createInboundReq.getTrackingNumber())
                .asnAnSkuList(asnAnSkuList)
                .build();
    }

    @Override
    protected ApiResult<String> editInboundBill(ThirdWarehouseCreateInboundReq createInboundReq) {
        throw new ServiceException("该仓库入库单不允许修改，请取消入库单后重新创建");
    }

    @Override
    protected ApiResult<String> cancelInboundBill(ThirdWarehouseCancelInboundReq cancelInboundReq) {
        DaMaiBaseResp<String> resp = daMaiService.cancelInbound(ThirdWarehouseContext.getAuthMap(),new DaMaiCancelInboundRequest(cancelInboundReq.getReceivingCode()));
        if(!isSuccess(resp)){
            return failure(resp.getMsg());
        }
        return success();
    }

    @Override
    protected ApiResult<List<ThirdWarehouseCalculateFeeResponse>> getCalculateFeeBatch(ThirdWarehouseCalculateFeeReq calculateFeeReq) {
        DaMaiCalculateFeeRequest daMaiCalculateFeeRequest = DaMaiCalculateFeeRequest.builder()
                .consigneeCountryCode(calculateFeeReq.getCountryCode())
                .consigneePostalCode(calculateFeeReq.getPostCode())
                .whCode(calculateFeeReq.getWarehouseCode())
                .grossWeight(calculateFeeReq.getWeight().toString())
                .length(calculateFeeReq.getLength().toString())
                .width(calculateFeeReq.getWidth().toString())
                .height(calculateFeeReq.getHeight().toString())
                .build();
        DaMaiPageBaseResp<List<DaMaiCalculateFeeResp>> resp = daMaiService.calculateFee(ThirdWarehouseContext.getAuthMap(), daMaiCalculateFeeRequest);
        if(StringUtils.isNotBlank(resp.getMsg())){
            return failure(resp.getMsg());
        }
        List<DaMaiCalculateFeeResp> daMaiCalculateFeeResps = resp.getData();
        if(CollectionUtils.isEmpty(daMaiCalculateFeeResps)){
            return success(Collections.emptyList());
        }
        daMaiCalculateFeeResps = daMaiCalculateFeeResps.stream().filter(v->StringUtils.isNotBlank(v.getErrMsg())).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(daMaiCalculateFeeResps)){
            return success(Collections.emptyList());
        }
        List<ThirdWarehouseCalculateFeeResponse> responseList = new ArrayList<>();
        for (DaMaiCalculateFeeResp daMaiCalculateFeeResp : daMaiCalculateFeeResps) {
            ThirdWarehouseCalculateFeeResponse response = new ThirdWarehouseCalculateFeeResponse();
            response.setChannelCode(daMaiCalculateFeeResp.getProductCode());
            response.setCurrency(daMaiCalculateFeeResp.getCurrencyCode());
            response.setOtherCost(daMaiCalculateFeeResp.getOtherAmount());
            response.setShippingCost(daMaiCalculateFeeResp.getBaseFreightAmount());
            response.setTotalShippingCost(daMaiCalculateFeeResp.getTotalBillableWeight());
            responseList.add(response);
        }
        return success(responseList);
    }

    @Override
    protected ApiResult<ThirdWarehouseUploadFileResponse> uploadFile(ThirdWarehouseUploadFileReq uploadFileReq) {
        return success(new ThirdWarehouseUploadFileResponse());
    }

    @Override
    protected ApiResult<ThirdWarehouseUploadOrderLabelResponse> uploadOrderLabel(ThirdWarehouseUploadOrderLabelReq uploadFileReq) {
        return success();
    }

    @Override
    protected ApiResult<String> createOutboundBill(ThirdWarehouseCreateOutboundReq createOutboundReq) {
        return null;
    }

    @Override
    protected ApiResult<String> cancelOutboundBill(ThirdWarehouseCancelOutboundReq cancelOutboundReq) {
        return null;
    }

    @Override
    protected Boolean warehouseAuthorize(OverseasProviderDTO.AuthorizeParamDTO dto) {
        Map<String, Object> authJson = dto.getAuthJson();
        DaMaiBaseResp<List<DaMaiWarehouseResp>> authResp = daMaiService.getWarehouseList(authJson);
        if(!isSuccess(authResp)){
            throw new ServiceException("授权失败,"+authResp.getMsg());
        }
        return true;
    }

    public <T> boolean isSuccess(DaMaiBaseResp<T> resp){
        return resp.getStatus() != null && resp.getStatus().equals("success");
    }

}
