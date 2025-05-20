package com.erp.server.wms.service.impl;

import com.common.business.enums.OmsPlatformEnum;
import com.common.business.enums.UnitEnum;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.dto.OverseasProviderDTO;
import com.erp.model.wms.dto.third.*;
import com.erp.server.wms.handler.AbstractThirdWarehouseHandler;
import com.sdk.wms.jifeng.dto.request.JiFengAuthRequest;
import com.sdk.wms.jifeng.dto.request.JiFengCreateInboundRequest;
import com.sdk.wms.jifeng.dto.response.JiFengBaseResp;
import com.sdk.wms.jifeng.dto.response.JiFengCreateInboundResp;
import com.sdk.wms.jifeng.dto.response.JiFengTokenResp;
import com.sdk.wms.jifeng.service.JiFengService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author liuruipeng
 * @date 2023年11月17日 10:51
 */
@Slf4j
@Service
public class JiFengHandlerServiceImpl extends AbstractThirdWarehouseHandler {

    @Resource
    private JiFengService jiFengService;

    @Override
    public OmsPlatformEnum getPlatForm() {
        return OmsPlatformEnum.JIFENG;
    }


    @Override
    protected ApiResult<List<ThirdWarehouseSkuResp>> getSkuList(ThirdWarehouseProductReq productReq) {
        return null;
    }

    @Override
    protected ApiResult<String> createInboundBill(ThirdWarehouseCreateInboundReq createInboundReq) {
        JiFengCreateInboundRequest jiFengCreateInboundRequest = this.buildInboundDto(createInboundReq);
        JiFengBaseResp<JiFengCreateInboundResp> resp = jiFengService.createInbound(ThirdWarehouseContext.getAuthMap(),jiFengCreateInboundRequest);
        if(!isSuccess(resp)){
            return failure(resp.getMessage());
        }
        return success(resp.getData().getInboundNo());
    }

    private JiFengCreateInboundRequest buildInboundDto(ThirdWarehouseCreateInboundReq createInboundReq) {
        JiFengCreateInboundRequest jiFengCreateInboundRequest = JiFengCreateInboundRequest.builder()
                .erpNo(createInboundReq.getReferenceNo())
                .trackingNo(createInboundReq.getTrackingNumber())
                .expectedTime(createInboundReq.getEtaDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .warehouse(createInboundReq.getWarehouseCode())
                .remark(createInboundReq.getRemark())
                .build();
        //封装箱子明细
        List<JiFengCreateInboundRequest.SkuListDTO> skuListDTOS = new ArrayList<>();
        //汇总的sku
        Map<String,Integer> skuTotalMap = new HashMap<>();
        List<ThirdWarehouseCreateInboundReq.Item> items = createInboundReq.getItems();
        //根据箱号排序
        items.sort(Comparator.comparing(ThirdWarehouseCreateInboundReq.Item::getBoxNo));
        // 使用 LinkedHashMap 保证分组后的 Key 顺序
        Map<Integer, List<ThirdWarehouseCreateInboundReq.Item>> itemMap = items.stream()
                .collect(Collectors.groupingBy(
                        ThirdWarehouseCreateInboundReq.Item::getBoxNo,
                        LinkedHashMap::new,  // 指定有序 Map 实现
                        Collectors.toList()
                ));
        List<JiFengCreateInboundRequest.BoxListDTO> boxList = new ArrayList<>();
        itemMap.forEach((boxNo,itemList) -> {
            JiFengCreateInboundRequest.BoxListDTO boxListDTO = new JiFengCreateInboundRequest.BoxListDTO();
            ThirdWarehouseCreateInboundReq.Item firstItem = itemList.get(0);
            if(firstItem.getWeightUnit().equals(UnitEnum.WeightUnitEnum.KG.code)){
                boxListDTO.setWeight(firstItem.getPackageWeight().multiply(new BigDecimal(1000)).intValue());
            }else{
                boxListDTO.setWeight(firstItem.getPackageWeight().intValue());
            }
            boxListDTO.setLength(firstItem.getBoxLength().intValue());
            boxListDTO.setWidth(firstItem.getBoxWidth().intValue());
            boxListDTO.setHeight(firstItem.getBoxHeight().intValue());
            List<JiFengCreateInboundRequest.BoxListDTO.SkuVosDTO> skuVosDTOS = new ArrayList<>();
            for (ThirdWarehouseCreateInboundReq.Item item : itemList) {
                JiFengCreateInboundRequest.BoxListDTO.SkuVosDTO skuVosDTO = new JiFengCreateInboundRequest.BoxListDTO.SkuVosDTO();
                skuVosDTO.setSku(item.getProductSku());
                skuVosDTO.setCount(item.getQuantity());
                skuVosDTOS.add(skuVosDTO);
                if(skuTotalMap.containsKey(item.getProductSku())) {
                    skuTotalMap.put(item.getProductSku(), skuTotalMap.get(item.getProductSku()) + item.getQuantity());
                }else{
                    skuTotalMap.put(item.getProductSku(), item.getQuantity());
                }
            }
            boxListDTO.setSkuVos(skuVosDTOS);
            boxList.add(boxListDTO);
        });
        //skuTotalMap 转 skuListDTOS
        skuTotalMap.forEach((sku,count) -> {
            JiFengCreateInboundRequest.SkuListDTO skuListDTO = new JiFengCreateInboundRequest.SkuListDTO();
            skuListDTO.setSku(sku);
            skuListDTO.setCount(count);
            skuListDTOS.add(skuListDTO);
        });
        jiFengCreateInboundRequest.setSkuList(skuListDTOS);
        jiFengCreateInboundRequest.setBoxList(boxList);
        return jiFengCreateInboundRequest;
    }

    @Override
    protected ApiResult<String> editInboundBill(ThirdWarehouseCreateInboundReq createInboundReq) {
        throw new ServiceException("该仓库入库单不允许修改，请取消入库单后重新创建");
    }

    @Override
    protected ApiResult<String> cancelInboundBill(ThirdWarehouseCancelInboundReq cancelInboundReq) {
        JiFengBaseResp<JiFengCreateInboundResp>  resp = jiFengService.cancelInbound(ThirdWarehouseContext.getAuthMap(), cancelInboundReq.getReceivingCode(),cancelInboundReq.getSourceCode());
        if(!isSuccess(resp)){
            return failure(resp.getMessage());
        }
        return success();
    }

    @Override
    protected ApiResult<List<ThirdWarehouseCalculateFeeResponse>> getCalculateFeeBatch(ThirdWarehouseCalculateFeeReq calculateFeeReq) {
        return null;
    }

    @Override
    protected ApiResult<ThirdWarehouseUploadFileResponse> uploadFile(ThirdWarehouseUploadFileReq uploadFileReq) {
        return null;
    }

    @Override
    protected ApiResult<ThirdWarehouseUploadOrderLabelResponse> uploadOrderLabel(ThirdWarehouseUploadOrderLabelReq uploadFileReq) {
        return null;
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
        JiFengAuthRequest jiFengAuthRequest = JiFengAuthRequest.builder()
                .email(authJson.get("email").toString())
                .token(authJson.get("token").toString())
                .domain(authJson.get("domain").toString())
                .clientId(authJson.get("appKey").toString())
                .clientSecret(authJson.get("appToken").toString())
                .build();
        JiFengBaseResp<String> authResp = jiFengService.authorize(jiFengAuthRequest);
        if(!isSuccess(authResp)){
            throw new ServiceException("授权失败,"+authResp.getMessage());
        }
        jiFengAuthRequest.setKey(authResp.getData());
        JiFengBaseResp<JiFengTokenResp> tokenResp = jiFengService.accessToken(jiFengAuthRequest);
        if(!isSuccess(tokenResp)){
            throw new ServiceException("授权失败,"+tokenResp.getMessage());
        }
        JiFengTokenResp jiFengTokenResp = tokenResp.getData();
        authJson.put("accessToken",jiFengTokenResp.getAccessToken());
        authJson.put("refreshToken",jiFengTokenResp.getRefreshToken());
        authJson.put("userId",jiFengTokenResp.getUserId());
        LocalDateTime expireIn = Instant.ofEpochMilli(jiFengTokenResp.getExpireIn())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        authJson.put("expireIn",expireIn.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        LocalDateTime refreshExpireIn = Instant.ofEpochMilli(jiFengTokenResp.getRefreshExpireIn())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        authJson.put("refreshExpireIn",refreshExpireIn.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        dto.setAuthJson(authJson);
        return true;
    }

    @Override
    protected  ApiResult<String> refreshToken(Map<String,Object> map){
        JiFengAuthRequest jiFengAuthRequest = JiFengAuthRequest.builder()
                .domain(map.get("domain").toString())
                .clientId(map.get("appKey").toString())
                .clientSecret(map.get("appToken").toString())
                .refreshToken(map.get("refreshToken").toString())
                .userId(Integer.valueOf(map.get("userId").toString()))
                .build();
        JiFengBaseResp<JiFengTokenResp> resp = jiFengService.refreshToken(jiFengAuthRequest);
        if(!isSuccess(resp)){
            return failure(resp.getMessage());
        }
        JiFengTokenResp jiFengTokenResp = resp.getData();
        map.put("accessToken",jiFengTokenResp.getAccessToken());
        LocalDateTime expireIn = Instant.ofEpochMilli(jiFengTokenResp.getExpireIn())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        map.put("expireIn",expireIn.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return success();
    };
    public <T> boolean isSuccess(JiFengBaseResp<T> resp){
        return resp.getCode()==0;
    }

    public static void main(String[] args) {
        LocalDateTime localDateTime = Instant.ofEpochMilli(1778310710517l)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        System.out.println(localDateTime);
    }
}
