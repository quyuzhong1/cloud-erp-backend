package com.erp.server.tms.service.logistics;


import com.common.business.annotation.LogisticsPlatformType;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.business.utils.PdfUtil;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.enums.DeliveryTypeEnum;
import com.erp.model.tms.vo.request.ChanelQueryVO;
import com.erp.model.tms.vo.request.LogisticsGetLabelVO;
import com.erp.model.tms.vo.request.LogisticsOrderVO;
import com.erp.model.tms.vo.request.LogisticsProductVO;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;
import com.erp.model.tms.vo.response.LogisticsPrintLabelResponse;
import com.erp.model.tms.vo.response.LogisticsServiceResponseVO;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.server.tms.handler.AbstractLogisticsHandler;
import com.sdk.oms.tiktok.dto.TikTokShopInfoDTO;
import com.sdk.oms.tiktok.dto.tiktok.fully.TikTokFullyDeliveryReq;
import com.sdk.oms.tiktok.dto.tiktok.fully.TikTokFullyDeliveryResp;
import com.sdk.oms.tiktok.dto.tiktok.packages.PackageDetailDTO;
import com.sdk.oms.tiktok.dto.tiktok.packages.PackageDocumentDTO;
import com.sdk.oms.tiktok.dto.tiktok.ship.ShipOrderOther;
import com.sdk.oms.tiktok.dto.tiktok.ship.ShipOrderOtherParam;
import com.sdk.oms.tiktok.service.TikTokFullService;
import com.sdk.oms.tiktok.service.TikTokSdkClientService;
import com.sdk.tms.tiktok.channel.provider.ShippingProvidersBean;
import com.sdk.tms.tiktok.service.TikTokShipperService;
import io.seata.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
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
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private ShopInfoFeign shopInfoFeign;

    @Resource
    private TikTokFullService tikTokFullService;

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
            TikTokFullyDeliveryReq.PackagesDTO.ItemsDTO itemsDTO = new TikTokFullyDeliveryReq.PackagesDTO.ItemsDTO(logisticsProductVO.getPlatformSkuId(),logisticsProductVO.getQuantity());
            itemsDTOS.add(itemsDTO);
        }
        TikTokFullyDeliveryReq.PackagesDTO packagesDTO = new TikTokFullyDeliveryReq.PackagesDTO();
        packagesDTO.setItems(itemsDTOS);
        packagesDTOS.add(packagesDTO);
        tikTokFullyDeliveryReq.setPackages(packagesDTOS);
        TikTokFullyDeliveryResp tikTokFullyDeliveryResp = tikTokFullService.createDelivery(shopId,tikTokFullyDeliveryReq);
        if(tikTokFullyDeliveryResp.getCode() != 0){
            return failure(tikTokFullyDeliveryResp.getCode(),tikTokFullyDeliveryResp.getMessage(),null);
        }
        return success(LogisticsOrderResponseVO.builder()
                .transportNo(tikTokFullyDeliveryResp.getData().getDeliveryOrderCode())
                .deliveryNo(logisticsOrderVO.getDeliveryNo())
                .trackNo(tikTokFullyDeliveryResp.getData().getDeliveryOrderCode())
                .build());

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
        return null;
    }

    @Override
    public ApiResult<List<LogisticsServiceResponseVO>> listLogisticsService(Map<String, String> authMap) {
        return ApiResult.error(-1, "功能未开放");
    }
}
