package com.erp.server.dmp.inout.handler.input.task.init.api.fbt;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.enums.OmsPlatformEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.wms.entity.OverseasProviderEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * FBT授权信息解析器
 * 将授权ID解析为可用的店铺上下文，避免各handler重复写相同逻辑。
 */
@Slf4j
@Service
public class FbtAuthorizedShopResolver {

    public ResolvedAuthContext resolveByAuthId(String authId) {
        List<OverseasProviderEntity> providerList = FeignQuery.create(OverseasProviderEntity.class)
                .eq(OverseasProviderEntity::getId, authId)
                .eq(OverseasProviderEntity::getAuthStatus, AuthStatusEnum.ALREADY.getCode())
                .eq(OverseasProviderEntity::getCode, OmsPlatformEnum.FBT.getCode())
                .list();
        if (CollUtil.isEmpty(providerList)) {
            throw new ServiceException("未找到已授权的FBT仓信息，authId:" + authId);
        }
        OverseasProviderEntity provider = providerList.get(0);

        String shopId = resolveShopId(provider);
        if (StrUtil.isBlank(shopId)) {
            throw new ServiceException("FBT授权信息缺少可用店铺信息，authId:" + provider.getId());
        }

        List<ShopInfoEntity> shopInfoList = FeignQuery.create(ShopInfoEntity.class)
                .eq(ShopInfoEntity::getId, shopId)
                .eq(ShopInfoEntity::getAuthStatus, AuthStatusEnum.ALREADY.getCode())
                .list();
        if (CollUtil.isEmpty(shopInfoList)) {
            throw new ServiceException("TikTok店铺不存在或未授权, shopId:" + shopId);
        }
        ShopInfoEntity shopInfo = shopInfoList.stream()
                .filter(Objects::nonNull)
                .filter(shop -> !Boolean.TRUE.equals(shop.getDisabled()))
                .findFirst()
                .orElse(shopInfoList.get(0));
        return new ResolvedAuthContext(provider, shopInfo.getId(), shopInfo);
    }

    private String resolveShopId(OverseasProviderEntity provider) {
        Map<String, Object> authJson = provider.getAuthJson();
        String shopId = firstNotBlank(authJson == null ? null : authJson.get("shopId"));
        if (StrUtil.isNotBlank(shopId)) {
            return shopId;
        }

        String shopAccount = firstNotBlank(
                authJson == null ? null : authJson.get("shopAccount"),
                provider.getPlatformAccount());
        if (StrUtil.isBlank(shopAccount)) {
            return null;
        }

        ShopInfoEntity shopInfo = findAuthorizedShopByAccount(shopAccount);
        if (shopInfo == null || StrUtil.isBlank(shopInfo.getId())) {
            log.warn("FBT授权账号未匹配到已授权店铺, authId:{}, shopAccount:{}",
                    provider.getId(), shopAccount);
            return null;
        }
        return shopInfo.getId();
    }

    private ShopInfoEntity findAuthorizedShopByAccount(String account) {
        String normalizedAccount = StrUtil.trim(account);
        if (StrUtil.isBlank(normalizedAccount)) {
            return null;
        }
        List<ShopInfoEntity> tiktokShops = FeignQuery.create(ShopInfoEntity.class)
                .eq(ShopInfoEntity::getAccount, normalizedAccount)
                .eq(ShopInfoEntity::getDictPlatform, PlatformDictEnum.TIK_TOK.getCode())
                .eq(ShopInfoEntity::getAuthStatus, AuthStatusEnum.ALREADY.getCode())
                .list();
        ShopInfoEntity preferred = pickPreferredShop(tiktokShops);
        if (preferred != null) {
            return preferred;
        }

        List<ShopInfoEntity> tiktokFullyShops = FeignQuery.create(ShopInfoEntity.class)
                .eq(ShopInfoEntity::getAccount, normalizedAccount)
                .eq(ShopInfoEntity::getDictPlatform, PlatformDictEnum.TIK_TOK_FULLY.getCode())
                .eq(ShopInfoEntity::getAuthStatus, AuthStatusEnum.ALREADY.getCode())
                .list();
        return pickPreferredShop(tiktokFullyShops);
    }

    private ShopInfoEntity pickPreferredShop(List<ShopInfoEntity> shopInfoList) {
        if (CollUtil.isEmpty(shopInfoList)) {
            return null;
        }
        return shopInfoList.stream()
                .filter(Objects::nonNull)
                .filter(shop -> !Boolean.TRUE.equals(shop.getDisabled()))
                .findFirst()
                .orElse(shopInfoList.get(0));
    }

    private String firstNotBlank(Object... values) {
        for (Object value : values) {
            if (value == null) {
                continue;
            }
            String text = String.valueOf(value);
            if (StrUtil.isNotBlank(text) && !"null".equalsIgnoreCase(text)) {
                return text;
            }
        }
        return null;
    }

    @Getter
    @AllArgsConstructor
    public static class ResolvedAuthContext {
        private final OverseasProviderEntity provider;
        private final String shopId;
        private final ShopInfoEntity shopInfo;
    }
}
