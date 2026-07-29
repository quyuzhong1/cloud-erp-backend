package com.erp.server.dmp.inout.handler.input.task.init.api.aliexpress;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.business.enums.OmsPlatformEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.wms.entity.OverseasProviderEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 菜鸟仓授权配置解析器。
 */
@Service
public class CaiNiaoAuthorizedShopResolver {

    private static final String CAINIAO_AUTH_ID = "cainiaoAuthId";

    /**
     * 从海外托管主任务扩展配置中读取菜鸟仓授权配置 ID。
     *
     * @param taskExtendJson DMP 主任务 extend_json
     * @return overseas_provider.id
     */
    public String resolveAuthIdFromTaskExtendJson(String taskExtendJson) {
        if (StrUtil.isBlank(taskExtendJson)) {
            throw new ServiceException("速卖通海外托管主任务未配置cainiaoAuthId");
        }
        JSONObject extend = JSON.parseObject(taskExtendJson);
        String authId = extend.getString(CAINIAO_AUTH_ID);
        if (StrUtil.isBlank(authId)) {
            throw new ServiceException("速卖通海外托管主任务未配置cainiaoAuthId");
        }
        return authId;
    }

    /**
     * 兼容普通速卖通店铺 ID 和菜鸟仓授权配置 ID。
     *
     * @param nextLevelId DMP 主任务 next_level_id
     * @return 可用于速卖通接口调用的店铺 ID
     */
    public String resolveShopIdOrOriginal(String nextLevelId) {
        if (StrUtil.isBlank(nextLevelId)) {
            throw new ServiceException("速卖通任务next_level_id不能为空");
        }
        if (isAliExpressShopId(nextLevelId)) {
            return nextLevelId;
        }
        List<OverseasProviderEntity> providerList = FeignQuery.create(OverseasProviderEntity.class)
                .eq(OverseasProviderEntity::getId, nextLevelId)
                .eq(OverseasProviderEntity::getCode, OmsPlatformEnum.CAI_NIAO.getCode())
                .eq(OverseasProviderEntity::getAuthStatus, AuthStatusEnum.ALREADY.getCode())
                .eq(OverseasProviderEntity::getIsDeleted, false)
                .list();
        if (CollUtil.isEmpty(providerList)) {
            return nextLevelId;
        }
        if (providerList.size() != 1) {
            throw new ServiceException("菜鸟仓授权配置不唯一，authId:" + nextLevelId);
        }
        return resolveProvider(providerList.get(0)).getShopInfo().getId();
    }

    /**
     * 判断 next_level_id 是否为现有速卖通店铺 ID。
     *
     * @param nextLevelId DMP 主任务 next_level_id
     * @return 是否为普通速卖通或海外托管店铺
     */
    private boolean isAliExpressShopId(String nextLevelId) {
        List<ShopInfoEntity> shopList = FeignQuery.create(ShopInfoEntity.class)
                .eq(ShopInfoEntity::getId, nextLevelId)
                .eq(ShopInfoEntity::getIsDeleted, false)
                .in(ShopInfoEntity::getDictPlatform,
                        PlatformDictEnum.ALI_EXPRESS.getCode(),
                        PlatformDictEnum.ALI_EXPRESS_OVERSEAS_MANAGED.getCode())
                .list();
        return CollUtil.isNotEmpty(shopList);
    }

    /**
     * 将 DMP next_level_id 解析为菜鸟仓配置及其绑定的普通速卖通授权店铺。
     *
     * @param authId overseas_provider.id
     * @return 菜鸟仓授权上下文
     */
    public ResolvedAuthContext resolveByAuthId(String authId) {
        if (StrUtil.isBlank(authId)) {
            throw new ServiceException("菜鸟仓授权配置ID不能为空");
        }
        List<OverseasProviderEntity> providerList = FeignQuery.create(OverseasProviderEntity.class)
                .eq(OverseasProviderEntity::getId, authId)
                .eq(OverseasProviderEntity::getCode, OmsPlatformEnum.CAI_NIAO.getCode())
                .eq(OverseasProviderEntity::getAuthStatus, AuthStatusEnum.ALREADY.getCode())
                .eq(OverseasProviderEntity::getIsDeleted, false)
                .list();
        if (providerList.size() != 1) {
            throw new ServiceException("未找到已授权的菜鸟仓配置，authId:" + authId);
        }
        return resolveProvider(providerList.get(0));
    }

    /**
     * 解析菜鸟仓配置中绑定的普通速卖通授权店铺。
     *
     * @param provider 已授权菜鸟仓配置
     * @return 菜鸟仓授权上下文
     */
    private ResolvedAuthContext resolveProvider(OverseasProviderEntity provider) {
        String shopId = resolveShopId(provider);
        List<ShopInfoEntity> shopList = FeignQuery.create(ShopInfoEntity.class)
                .eq(ShopInfoEntity::getId, shopId)
                .eq(ShopInfoEntity::getDictPlatform, PlatformDictEnum.ALI_EXPRESS.getCode())
                .eq(ShopInfoEntity::getAuthStatus, AuthStatusEnum.ALREADY.getCode())
                .eq(ShopInfoEntity::getIsDeleted, false)
                .list();
        ShopInfoEntity shopInfo = pickEnabledShop(shopList);
        if (Objects.isNull(shopInfo)) {
            throw new ServiceException("菜鸟仓配置绑定的速卖通店铺不存在或未授权，shopId:" + shopId);
        }
        return new ResolvedAuthContext(provider, shopInfo);
    }

    /**
     * 优先读取授权配置中固化的 shopId，旧数据再按 shopAccount 兜底。
     *
     * @param provider 菜鸟仓配置
     * @return 普通速卖通店铺 ID
     */
    private String resolveShopId(OverseasProviderEntity provider) {
        Map<String, Object> authJson = provider.getAuthJson();
        String shopId = text(authJson == null ? null : authJson.get("shopId"));
        if (StrUtil.isNotBlank(shopId)) {
            return shopId;
        }
        String shopAccount = StrUtil.blankToDefault(
                text(authJson == null ? null : authJson.get("shopAccount")),
                provider.getPlatformAccount());
        if (StrUtil.isBlank(shopAccount)) {
            throw new ServiceException("菜鸟仓授权配置缺少shopId和shopAccount，authId:" + provider.getId());
        }
        List<ShopInfoEntity> shopList = FeignQuery.create(ShopInfoEntity.class)
                .eq(ShopInfoEntity::getAccount, shopAccount)
                .eq(ShopInfoEntity::getDictPlatform, PlatformDictEnum.ALI_EXPRESS.getCode())
                .eq(ShopInfoEntity::getAuthStatus, AuthStatusEnum.ALREADY.getCode())
                .eq(ShopInfoEntity::getIsDeleted, false)
                .list();
        ShopInfoEntity shopInfo = pickEnabledShop(shopList);
        if (Objects.isNull(shopInfo)) {
            throw new ServiceException("菜鸟仓授权账号未匹配到已授权速卖通店铺，shopAccount:" + shopAccount);
        }
        return shopInfo.getId();
    }

    /**
     * 从授权店铺列表中选择启用记录。
     *
     * @param shopList 店铺列表
     * @return 启用店铺，不存在时返回 null
     */
    private ShopInfoEntity pickEnabledShop(List<ShopInfoEntity> shopList) {
        if (CollUtil.isEmpty(shopList)) {
            return null;
        }
        ShopInfoEntity enabledShop = shopList.stream()
                .filter(Objects::nonNull)
                .filter(shop -> !Boolean.TRUE.equals(shop.getDisabled()))
                .findFirst()
                .orElse(null);
        if (Objects.nonNull(enabledShop)) {
            return enabledShop;
        }
        return shopList.stream().filter(Objects::nonNull).findFirst().orElse(null);
    }

    /**
     * 将授权 JSON 字段安全转换为文本。
     *
     * @param value 原始值
     * @return 非空文本
     */
    private String text(Object value) {
        if (Objects.isNull(value)) {
            return null;
        }
        String result = String.valueOf(value);
        return StrUtil.isBlank(result) || "null".equalsIgnoreCase(result) ? null : result.trim();
    }

    /**
     * 菜鸟仓配置与其 API 授权店铺上下文。
     */
    @Getter
    @AllArgsConstructor
    public static class ResolvedAuthContext {
        private final OverseasProviderEntity provider;
        private final ShopInfoEntity shopInfo;
    }
}
