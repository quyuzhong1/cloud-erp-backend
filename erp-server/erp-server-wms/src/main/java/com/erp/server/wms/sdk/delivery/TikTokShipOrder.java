package com.erp.server.wms.sdk.delivery;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.annotation.PlatformShipOrderAnno;
import com.common.business.dto.PlatformShipOrderDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.service.IPlatformService;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.dto.SoB2cDetailDTO;
import com.erp.model.tms.dto.LogisticsChannelDTO;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.sdk.oms.tiktok.dto.TikTokShopInfoDTO;
import com.sdk.oms.tiktok.dto.tiktok.ship.SelfShipmentBean;
import com.sdk.oms.tiktok.dto.tiktok.ship.ShipOrderOtherParam;
import com.sdk.oms.tiktok.dto.tiktok.ship.ShipOrderUS;
import com.sdk.oms.tiktok.dto.tiktok.ship.ShipOrderUSParam;
import com.sdk.oms.tiktok.service.TikTokSdkClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@PlatformShipOrderAnno(method = PlatformDictEnum.TIK_TOK)
public class TikTokShipOrder implements IPlatformService {

    @Resource
    private SoB2cFeign soB2cFeign;

    @Resource
    private ShopInfoFeign shopInfoFeign;

    @Resource
    private LogisticsFeign logisticsFeign;

    @Resource
    private TikTokSdkClientService tikTokSdkClientService;

    @Override
    public void shipOrder(PlatformShipOrderDTO dto) {
        SoB2cDTO.ViewDTO view = soB2cFeign.view(dto.getSoB2cId());
        if (ObjectUtil.isEmpty(view)) {
            throw new ServiceException(ApiError.ERROR_92003);
        }

        TikTokShopInfoDTO tikTokShopInfoDTO = tikTokSdkClientService.getShopInfoByShopId(view.getShopId());


        List<SoB2cDetailDTO.ViewDTO> detailList = view.getDetailList();
        List<String> sourceDetailIds = detailList.stream()
                .filter(req -> StringUtils.isBlank(req.getSourcePlatform())
                        && StringUtils.isNotBlank(req.getSourceDetailId())
                ).map(req -> req.getSourceDetailId())
                .distinct()
                .collect(Collectors.toList());
        //获取销售渠道信息
        LogisticsChannelDTO.SignShipDTO tmsScaleChannelShipDTO = logisticsFeign.getScaleChannelByChannelById(
                view.getLogisticsDTO().getLogisticsChannelId(),
                PlatformDictEnum.TIK_TOK.getCode()
        );
        if (null == tmsScaleChannelShipDTO){
            throw new ServiceException("找不到渠道信息");
        }
        if ("US".equalsIgnoreCase(tikTokShopInfoDTO.getSite())) {
            ShipOrderUSParam paramDTO = new ShipOrderUSParam();
            paramDTO.setTrackingNumber(view.getLogisticsDTO().getCode());
            paramDTO.setOrderLineItemIds(sourceDetailIds);
            paramDTO.setShippingProviderId(tmsScaleChannelShipDTO.getPlatformChannelId());
            ShipOrderUS shipOrderUS = tikTokSdkClientService.sendTikTokShipOrderUS(tikTokShopInfoDTO, view.getPlatformCode(), paramDTO);
            if (shipOrderUS.getCode() != 0) {
                throw new ServiceException("TikTok标记发货失败");
            }
        } else {
            ShipOrderOtherParam paramDTO = new ShipOrderOtherParam();
            SelfShipmentBean selfShipmentBean = new SelfShipmentBean();
            selfShipmentBean.setTrackingNumber(view.getLogisticsDTO().getCode());
            selfShipmentBean.setShippingProviderId(tmsScaleChannelShipDTO.getPlatformChannelId());
            paramDTO.setSelfShipment(selfShipmentBean);
            tikTokSdkClientService.sendTikTokShipOrderOther(tikTokShopInfoDTO, view.getLogisticsDTO().getPlatformPackageId(), paramDTO);
        }

    }
}
