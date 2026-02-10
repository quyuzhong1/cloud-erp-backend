package com.erp.server.dmp.inout.handler.input.task.init.api.fbt;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.common.business.enums.OmsPlatformEnum;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.wms.feign.OverseasProviderFeign;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputApiInitRequest;
import com.erp.server.dmp.inout.handler.input.task.init.api.DmpInputApiInitHandler;
import com.sdk.oms.tiktok.dto.TikTokShopInfoDTO;
import com.sdk.oms.tiktok.service.TikTokSdkClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * FBT产品API初始化Handler
 * 定时拉取FBT仓库商品列表（Search Goods Info接口）
 * 
 * 字段对应关系：
 * - 库存SKU -> goods/id
 * - 库存产品名称 -> goods/name
 * - 三方仓商品条码 -> goods/code
 * - 仓库 -> 中台配置绑定的FBT仓库名称
 * - 服务商 -> FBT仓
 *
 * @author System
 * @since 2026-02-10
 */
@Slf4j
@Service
@Scope("prototype")
public class FbtProductApiInitHandler implements DmpInputApiInitHandler {

    @Resource
    private TikTokSdkClientService tikTokSdkClientService;

    @Resource
    private ShopInfoFeign shopInfoFeign;

    @Resource
    private OverseasProviderFeign overseasProviderFeign;

    @Override
    public List<DmpInputTaskInitDTO> getApiData(DmpInputApiInitRequest dmpInputApiInitRequest) {
        List<DmpInputTaskInitDTO> resultList = new ArrayList<>();

        String nextLevelId = dmpInputApiInitRequest.getNextLevelId();
        log.info("开始拉取FBT商品列表，nextLevelId: {}", nextLevelId);

        // 查询FBT仓授权信息（从overseas_provider表）
        List<OverseasProviderEntity> overseasProviderEntityList = FeignQuery.create(OverseasProviderEntity.class)
                .eq(OverseasProviderEntity::getId, nextLevelId)
                .eq(OverseasProviderEntity::getAuthStatus, AuthStatusEnum.ALREADY.getCode())
                .eq(OverseasProviderEntity::getCode, OmsPlatformEnum.FBT.getCode())
                .list();

        if (CollUtil.isEmpty(overseasProviderEntityList)) {
            log.warn("未找到已授权的FBT仓信息，nextLevelId: {}", nextLevelId);
            throw new ServiceException("未找到已授权的FBT仓信息，nextLevelId: " + nextLevelId);
        }

        OverseasProviderEntity overseasProvider = overseasProviderEntityList.get(0);
        log.info("FBT仓信息：ID={}, 简称={}, 名称={}", overseasProvider.getId(), 
                overseasProvider.getShortName(), overseasProvider.getName());

        try {
            // 从authJson中获取关联的TikTok店铺ID
            Map<String, Object> authJson = overseasProvider.getAuthJson();
            if (authJson == null || !authJson.containsKey("shopId")) {
                throw new ServiceException("FBT仓授权信息中未找到关联的TikTok店铺ID");
            }

            String shopId = (String) authJson.get("shopId");
            log.info("关联的TikTok店铺ID: {}", shopId);

            // 查询TikTok店铺信息
            List<ShopInfoEntity> shopInfoEntityList = FeignQuery.create(ShopInfoEntity.class)
                    .eq(ShopInfoEntity::getId, shopId)
                    .eq(ShopInfoEntity::getAuthStatus, AuthStatusEnum.ALREADY.getCode())
                    .list();

            if (shopInfoEntityList.isEmpty()) {
                throw new ServiceException("未找到关联的TikTok店铺或店铺未授权，shopId: " + shopId);
            }

            ShopInfoEntity shopInfo = shopInfoEntityList.get(0);

            // 获取店铺授权信息
            TikTokShopInfoDTO shopInfoDTO = tikTokSdkClientService.getShopInfoByShopId(shopInfo.getId());
            if (shopInfoDTO == null) {
                throw new ServiceException("TikTok店铺授权信息为空，shopId: " + shopId);
            }

            // 调用Search Goods Info接口获取FBT商品列表
            log.info("调用searchGoodsInfo接口获取FBT商品列表");
            Map<String, Object> response = tikTokSdkClientService.searchGoodsInfo(shopInfoDTO);

            if (response == null || !response.containsKey("goods")) {
                log.warn("FBT商品列表响应数据为空");
                return Collections.emptyList();
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> goodsList = (List<Map<String, Object>>) response.get("goods");
            if (CollUtil.isEmpty(goodsList)) {
                log.info("FBT仓没有商品数据");
                return Collections.emptyList();
            }

            log.info("查询到 {} 个FBT商品", goodsList.size());

            // 组装数据并添加关联信息
            JSONArray goodsArray = new JSONArray();
            for (Map<String, Object> goods : goodsList) {
                // 添加FBT仓授权ID和店铺ID
                goods.put("authId", overseasProvider.getId());
                goods.put("shopId", shopInfo.getId());
                goods.put("warehouseName", overseasProvider.getName());
                goods.put("warehouseShortName", overseasProvider.getShortName());
                goods.put("serviceProvider", "FBT仓");
                goodsArray.add(goods);
            }

            DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
            dmpInputTaskInitDTO.setMsg(goodsArray.toJSONString());
            resultList.add(dmpInputTaskInitDTO);

            log.info("FBT商品列表拉取完成，共 {} 个商品", goodsList.size());

        } catch (Exception e) {
            log.error("拉取FBT商品列表失败: {}", e.getMessage(), e);
            throw new ServiceException("拉取FBT商品列表失败: " + e.getMessage(), e);
        }

        return resultList;
    }
}
