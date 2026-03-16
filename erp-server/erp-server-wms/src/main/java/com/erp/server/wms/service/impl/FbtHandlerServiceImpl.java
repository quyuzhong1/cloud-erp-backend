package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.enums.OmsPlatformEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.wms.dto.OverseasProviderDTO;
import com.erp.model.wms.dto.third.*;
import com.erp.server.wms.handler.AbstractThirdWarehouseHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * FBT第三方仓处理器。
 * 当前仅实现授权校验与上下文回填，其余标准第三方仓接口暂不适用。
 */
@Slf4j
@Service
public class FbtHandlerServiceImpl extends AbstractThirdWarehouseHandler {

    @Override
    public OmsPlatformEnum getPlatForm() {
        return OmsPlatformEnum.FBT;
    }

    @Override
    protected ApiResult<List<ThirdWarehouseSkuResp>> getSkuList(ThirdWarehouseProductReq productReq) {
        return failure("FBT仓暂不支持该接口");
    }

    @Override
    protected ApiResult<String> createInboundBill(ThirdWarehouseCreateInboundReq createInboundReq) {
        return failure("FBT仓暂不支持该接口");
    }

    @Override
    protected ApiResult<String> editInboundBill(ThirdWarehouseCreateInboundReq createInboundReq) {
        return failure("FBT仓暂不支持该接口");
    }

    @Override
    protected ApiResult<String> cancelInboundBill(@Valid ThirdWarehouseCancelInboundReq cancelInboundReq) {
        return failure("FBT仓暂不支持该接口");
    }


    @Override
    protected ApiResult<List<ThirdWarehouseCalculateFeeResponse>> getCalculateFeeBatch(@Valid ThirdWarehouseCalculateFeeReq calculateFeeReq) {
        return failure("FBT仓暂不支持该接口");
    }

    @Override
    protected ApiResult<ThirdWarehouseUploadFileResponse> uploadFile(@Valid ThirdWarehouseUploadFileReq uploadFileReq) {
        return failure("FBT仓暂不支持该接口");
    }

    @Override
    protected ApiResult<ThirdWarehouseUploadOrderLabelResponse> uploadOrderLabel(@Valid ThirdWarehouseUploadOrderLabelReq uploadFileReq) {
        return failure("FBT仓暂不支持该接口");
    }

    @Override
    protected ApiResult<ThirdWarehouseUploadHandoverFileResponse> uploadHandoverFile(@Valid ThirdWarehouseUploadHandoverFileReq uploadHandoverFileReq) {
        return failure("FBT仓暂不支持该接口");
    }

    @Override
    protected ApiResult<ThirdWarehouseQueryOutboundResponse> createOutboundBill(ThirdWarehouseCreateOutboundReq createOutboundReq) {
        return failure("FBT仓暂不支持该接口");
    }

    @Override
    protected ApiResult<String> createFbaOutboundBill(ThirdWarehouseCreateFbaOutboundReq createOutboundReq) {
        return failure("FBT仓暂不支持该接口");
    }

    @Override
    protected ApiResult<String> cancelOutboundBill(@Valid ThirdWarehouseCancelOutboundReq cancelOutboundReq) {
        return failure("FBT仓暂不支持该接口");
    }

    @Override
    protected ApiResult<String> cancelFbaOutboundBill(@Valid ThirdWarehouseCancelFbaOutboundReq cancelOutboundReq) {
        return failure("FBT仓暂不支持该接口");
    }

    @Override
    protected ApiResult<ThirdWarehouseQueryOutboundResponse> queryOutboundBill(@Valid ThirdWarehouseQueryOutboundReq queryOutboundReq) {
        return failure("FBT仓暂不支持该接口");
    }

    @Override
    protected ApiResult<List<ThirdWarehouseQueryFbaOutboundResponse>> queryFbaOutboundBill(@Valid ThirdWarehouseQueryFbaOutboundReq req) {
        return failure("FBT仓暂不支持该接口");
    }

    @Override
    protected Boolean warehouseAuthorize(OverseasProviderDTO.AuthorizeParamDTO dto) {
        Map<String, Object> authJson = dto.getAuthJson();
        if (authJson == null) {
            throw new ServiceException("授权信息不能为空");
        }
        String shopAccount = firstNotBlank(authJson.get("shopAccount"));
        if (StrUtil.isBlank(shopAccount)) {
            throw new ServiceException("TikTok店铺账号不能为空");
        }
        ShopInfoEntity shopInfo = findAuthorizedShopByAccount(shopAccount);
        if (shopInfo == null || StrUtil.isBlank(shopInfo.getId())) {
            throw new ServiceException("未找到已授权的TikTok店铺");
        }

        authJson.put("shopAccount", shopAccount);
        authJson.put("shopId", shopInfo.getId());
        dto.setAuthJson(authJson);
        log.info("FBT授权校验通过, shopAccount:{}, shopId:{}", shopAccount, shopInfo.getId());
        return true;
    }

    private ShopInfoEntity findAuthorizedShopByAccount(String account) {
        ShopInfoEntity preferredTikTok = pickPreferredShop(FeignQuery.create(ShopInfoEntity.class)
                .eq(ShopInfoEntity::getAccount, account)
                .eq(ShopInfoEntity::getDictPlatform, PlatformDictEnum.TIK_TOK.getCode())
                .eq(ShopInfoEntity::getAuthStatus, AuthStatusEnum.ALREADY.getCode())
                .list());
        if (preferredTikTok != null) {
            return preferredTikTok;
        }
        return pickPreferredShop(FeignQuery.create(ShopInfoEntity.class)
                .eq(ShopInfoEntity::getAccount, account)
                .eq(ShopInfoEntity::getDictPlatform, PlatformDictEnum.TIK_TOK_FULLY.getCode())
                .eq(ShopInfoEntity::getAuthStatus, AuthStatusEnum.ALREADY.getCode())
                .list());
    }

    private ShopInfoEntity pickPreferredShop(List<ShopInfoEntity> shops) {
        if (CollUtil.isEmpty(shops)) {
            return null;
        }
        return shops.stream()
                .filter(Objects::nonNull)
                .filter(shop -> !Boolean.TRUE.equals(shop.getDisabled()))
                .findFirst()
                .orElse(shops.get(0));
    }

    private String firstNotBlank(Object... values) {
        for (Object value : values) {
            if (value == null) {
                continue;
            }
            String text = StrUtil.trim(String.valueOf(value));
            if (StrUtil.isNotBlank(text) && !"null".equalsIgnoreCase(text)) {
                return text;
            }
        }
        return null;
    }
}
