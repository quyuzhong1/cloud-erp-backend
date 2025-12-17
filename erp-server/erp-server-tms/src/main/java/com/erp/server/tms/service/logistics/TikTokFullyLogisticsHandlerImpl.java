package com.erp.server.tms.service.logistics;


import cn.hutool.json.JSONUtil;
import com.common.business.annotation.LogisticsPlatformType;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.business.utils.PdfUtil;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.enums.BusinessTypeEnum;
import com.erp.model.tms.enums.RequestStatusEnums;
import com.erp.model.tms.vo.request.ChanelQueryVO;
import com.common.core.exception.ServiceException;
import com.erp.model.tms.vo.request.LogisticsGetLabelVO;
import com.erp.model.tms.vo.request.LogisticsOrderVO;
import com.erp.model.tms.vo.request.LogisticsProductVO;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;
import com.erp.model.tms.vo.response.LogisticsPrintLabelResponse;
import com.erp.model.tms.vo.response.LogisticsServiceResponseVO;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.server.tms.handler.AbstractLogisticsHandler;
import com.erp.server.tms.service.LogisticsOperateService;
import com.sdk.oms.tiktok.dto.tiktok.channel.provider.ShippingProviderDTO;
import com.sdk.oms.tiktok.dto.tiktok.fully.TikTokFullyDeliveryReq;
import com.sdk.oms.tiktok.dto.tiktok.fully.TikTokFullyDeliveryResp;
import com.sdk.oms.tiktok.dto.tiktok.fully.TikTokFullyPrintDeliveryResp;
import com.sdk.oms.tiktok.service.TikTokFullService;
import com.sdk.tms.tiktok.channel.provider.ShippingProvidersBean;
import com.sdk.tms.tiktok.service.TikTokShipperService;
import io.seata.common.util.CollectionUtils;
import io.seata.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;

/**
 * TikTok全托管物流
 */
@Slf4j
@Component
@LogisticsPlatformType(LogisticsPlatformEnum.TIK_TOK_FULLY)
public class TikTokFullyLogisticsHandlerImpl extends AbstractLogisticsHandler {

    @Resource
    private TikTokFullService tikTokFullService;
    @Resource
    private TikTokShipperService tikTokShipperService;

    @Resource
    private ShopInfoFeign shopInfoFeign;
    @Resource
    private LogisticsOperateService logisticsOperateService;

    /**
     * 查询店铺
     * @param shopId
     * @return
     */
    @Override
    public Map<String, String> getLogisticsAuthConfigByShopId(String shopId) {
        //获取商铺配置信息
        Map<String, String> map = new HashMap<>();
        map.put("shopId", shopId);
        return map;
    }

    @Override
    public ApiResult<LogisticsOrderResponseVO> createOrder(LogisticsOrderVO logisticsOrderVO) {
        Map<String, String> authMap = logisticsOrderVO.getAuthMap();
        String shopId = authMap.get("shopId");
        TikTokFullyDeliveryReq tikTokFullyDeliveryReq = new TikTokFullyDeliveryReq();
        tikTokFullyDeliveryReq.setPackageQuantity(1);
        tikTokFullyDeliveryReq.setStockupOrderCode(logisticsOrderVO.getPlatformCode());
        List<TikTokFullyDeliveryReq.PackagesDTO> packagesDTOS = new ArrayList<>();
        List<TikTokFullyDeliveryReq.PackagesDTO.ItemsDTO> itemsDTOS = new ArrayList<>();
        for (LogisticsProductVO logisticsProductVO : logisticsOrderVO.getLogisticsProductVOList()) {
            TikTokFullyDeliveryReq.PackagesDTO.ItemsDTO itemsDTO = new TikTokFullyDeliveryReq.PackagesDTO.ItemsDTO(logisticsProductVO.getPlatformLineNumber(),logisticsProductVO.getDeliveryQty());
            itemsDTOS.add(itemsDTO);
        }
        TikTokFullyDeliveryReq.PackagesDTO packagesDTO = new TikTokFullyDeliveryReq.PackagesDTO();
        packagesDTO.setItems(itemsDTOS);
        packagesDTOS.add(packagesDTO);
        tikTokFullyDeliveryReq.setPackages(packagesDTOS);
        try {
            TikTokFullyDeliveryResp tikTokFullyDeliveryResp = tikTokFullService.createDelivery(shopId,tikTokFullyDeliveryReq);
            if(tikTokFullyDeliveryResp.getCode() != 0){
                logisticsOperateService.pushOperateLog(logisticsOrderVO.getSourceId(),
                        logisticsOrderVO.getPlatformCode(), BusinessTypeEnum.CREATE_ORDER.getCode(), LogisticsPlatformEnum.TIK_TOK_FULLY.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsOrderVO), JSONUtil.toJsonStr(tikTokFullyDeliveryResp), false);
                return failure(tikTokFullyDeliveryResp.getCode(),tikTokFullyDeliveryResp.getMessage(),null);
            }
            logisticsOperateService.pushOperateLog(logisticsOrderVO.getSourceId(),
                    logisticsOrderVO.getPlatformCode(), BusinessTypeEnum.CREATE_ORDER.getCode(), LogisticsPlatformEnum.TIK_TOK_FULLY.getCode(),
                    RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsOrderVO), JSONUtil.toJsonStr(tikTokFullyDeliveryResp), false);
            return success(LogisticsOrderResponseVO.builder()
                    .transportNo(tikTokFullyDeliveryResp.getData().getDeliveryOrderCode())
                    .deliveryNo(logisticsOrderVO.getDeliveryNo())
                    .trackNo(tikTokFullyDeliveryResp.getData().getDeliveryOrderCode())
                    .build());
        }catch (Exception e){
            logisticsOperateService.pushOperateLog(logisticsOrderVO.getSourceId(),
                    logisticsOrderVO.getPlatformCode(), BusinessTypeEnum.CREATE_ORDER.getCode(), LogisticsPlatformEnum.TIK_TOK_FULLY.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsOrderVO), JSONUtil.toJsonStr(e.getMessage()), false);
            return failure(-1,e.getMessage(),null);
        }
    }
    @Override
    public ApiResult<List<LogisticsSaleChannelEntity>> getChannel(ChanelQueryVO chanelQueryVO) {
        Map<String, String> authMap = chanelQueryVO.getAuthMap();
        List<LogisticsSaleChannelEntity> resuletList = new ArrayList<>();
        List<ShippingProvidersBean> providersBeanList = tikTokShipperService.sendTikTokLogisticsChannel(authMap.get("shopId"));
        for (ShippingProvidersBean providerDTO : providersBeanList) {
            LogisticsSaleChannelEntity logisticsSaleChannelEntity = new LogisticsSaleChannelEntity()
                    .setCode(providerDTO.getId())
                    .setPlatformChannelId(providerDTO.getId())
                    .setCnName(providerDTO.getName())
                    .setLogisticsPlatform(LogisticsPlatformEnum.TIK_TOK.getCode());
            resuletList.add(logisticsSaleChannelEntity);
        }
        return success(resuletList);
    }
    @Override
    public ApiResult<Object>authorization(Map<String, String> authMap) {
        return success("授权成功");
    }
    @Override
    public LogisticsPlatformEnum getPlatForm() {
        return LogisticsPlatformEnum.TIK_TOK_FULLY;
    }


    /**
     * 获取标签
     *
     * @return
     */

    @Override
    public ApiResult<List<LogisticsPrintLabelResponse>> getLabelList(List<LogisticsGetLabelVO> logisticsGetLabelVOList) throws IOException {
        LogisticsGetLabelVO logisticsGetLabelVO = logisticsGetLabelVOList.stream().filter(e -> Objects.nonNull(e.getAuthMap())).findFirst().orElse(null);
        assert logisticsGetLabelVO != null;
        Map<String, String> authMap = logisticsGetLabelVO.getAuthMap();
        String shopId = authMap.get("shopId");
        List<LogisticsPrintLabelResponse> resultList = new ArrayList<>();
        for (LogisticsGetLabelVO vo : logisticsGetLabelVOList) {
            TikTokFullyPrintDeliveryResp tikTokFullyPrintDeliveryResp = tikTokFullService.printDelivery(vo.getTransportNo(), shopId);
            if(tikTokFullyPrintDeliveryResp.getCode()!=0){
                return failure("获取标签失败,"+ tikTokFullyPrintDeliveryResp.getMessage());
            }
            if(StringUtils.isBlank(tikTokFullyPrintDeliveryResp.getData().getDocumentUrl())){
                throw new ServiceException("获取标签失败");
            }else{
                String base64 = PdfUtil.convertPdfUrlToBase64(tikTokFullyPrintDeliveryResp.getData().getDocumentUrl(),true);
                String prefix = "data:application/pdf;base64,";
                LogisticsPrintLabelResponse response = LogisticsPrintLabelResponse.builder()
                        .deliveryNoList(Collections.singletonList(vo.getDeliveryNo()))
                        .transportNoList(Collections.singletonList(vo.getTransportNo()))
                        .trackNoList(Collections.singletonList(vo.getTrackNo()))
                        .base64(prefix + base64).build();
                resultList.add(response);
            }
        }
        return success(resultList);
    }

    @Override
    public ApiResult<List<LogisticsServiceResponseVO>> listLogisticsService(Map<String, String> authMap) {
        return ApiResult.error(-1, "功能未开放");
    }

    @Override
    public List<Map<String, String>> getLogisticsAuthConfigByPlatform(String platform) {
        ApiResult<List<ShopAuthEntity>> authShops = shopInfoFeign.getAuthShopByPlatformType(getPlatForm().getCode());
        if (!authShops.isSuccess() || CollectionUtils.isEmpty(authShops.getData())) return Collections.emptyList();

        List<Map<String, String>> mapList = new ArrayList<>(authShops.getData().size());
        for (ShopAuthEntity shopAuth : authShops.getData()) {
            Map<String, String> map = new HashMap<>();
            map.put("shopId", shopAuth.getShopId());
            map.put("shopName", shopAuth.getShopName());
            mapList.add(map);
        }
        return mapList;
    }
}
