package com.erp.server.dmp.inout.handler.input.task.init.api.tiktok;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.business.enums.OmsPlatformEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.wms.feign.OverseasProviderFeign;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.init.DmpInputInitHandler;
import com.sdk.oms.tiktok.dto.TikTokShopInfoDTO;
import com.sdk.oms.tiktok.service.TikTokSdkClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * FBT仓库初始化Handler
 * 定时拉取TikTok FBT仓库列表并存储到系统中
 *
 * @author System
 * @since 2026-02-10
 */
@Slf4j
@Service
@Scope("prototype")
public class FbtWarehouseInitHandler extends DmpInputInitHandler {

    @Resource
    private TikTokSdkClientService tikTokSdkClientService;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        List<DmpInputTaskInitDTO> resultList = new ArrayList<>();

        // 查询所有已授权的FBT仓（overseas_provider表）
        List<OverseasProviderEntity> overseasProviderEntityList = FeignQuery.create(OverseasProviderEntity.class)
                .eq(OverseasProviderEntity::getAuthStatus, AuthStatusEnum.ALREADY.getCode())
                .eq(OverseasProviderEntity::getCode, OmsPlatformEnum.FBT.getCode())
                .list();

        if (CollUtil.isEmpty(overseasProviderEntityList)) {
            log.warn("未找到已授权的FBT仓，无法拉取FBT仓库列表");
            return Collections.emptyList();
        }

        // 取对应授权ID授权
        OverseasProviderEntity overseasProviderEntity = overseasProviderEntityList.stream()
                .filter(e -> e.getId().equalsIgnoreCase(dmpInputTaskEntity.getNextLevelId()))
                .findFirst()
                .orElse(null);

        if (overseasProviderEntity == null) {
            throw new ServiceException(OmsPlatformEnum.FBT.getName() + "对应授权ID信息不存在,nextId:" + dmpInputTaskEntity.getNextLevelId());
        }

        log.info("开始拉取FBT仓库列表，授权ID: {}, 仓库简称: {}", overseasProviderEntity.getId(), overseasProviderEntity.getShortName());

        try {
            // 从authJson中获取关联的TikTok店铺ID
            Map<String, Object> authJson = overseasProviderEntity.getAuthJson();
            String shopId = (String) authJson.get("shopId");

            if (shopId == null) {
                throw new ServiceException("FBT仓授权信息中未找到关联的TikTok店铺ID");
            }

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
                log.warn("店铺ID: {} 授权信息为空", shopInfo.getId());
                return Collections.emptyList();
            }

            // 调用TikTok API获取FBT仓库列表
            Map<String, Object> response = tikTokSdkClientService.getFbtWarehouseList(shopInfoDTO);

            if (response == null || !response.containsKey("data")) {
                log.warn("店铺ID: {} FBT仓库列表响应数据为空", shopInfo.getId());
                return Collections.emptyList();
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.get("data");
            if (data == null || !data.containsKey("warehouses")) {
                log.warn("店铺ID: {} FBT仓库列表数据为空", shopInfo.getId());
                return Collections.emptyList();
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> warehouses = (List<Map<String, Object>>) data.get("warehouses");
            if (CollUtil.isEmpty(warehouses)) {
                log.info("店铺ID: {} 没有FBT仓库", shopInfo.getId());
                return Collections.emptyList();
            }

            log.info("店铺ID: {} 查询到 {} 个FBT仓库", shopInfo.getId(), warehouses.size());

            // 组装数据并添加授权ID
            String authId = overseasProviderEntity.getId();
            JSONArray warehouseArray = JSON.parseArray(JSON.toJSONString(warehouses));
            warehouseArray.forEach(item -> {
                JSONObject warehouse = (JSONObject) item;
                // 添加授权ID，用于关联overseas_provider_warehouse表
                warehouse.put("authId", authId);
                warehouse.put("shopId", shopInfo.getId());
            });

            DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
            dmpInputTaskInitDTO.setMsg(warehouseArray.toJSONString());
            resultList.add(dmpInputTaskInitDTO);

            log.info("FBT仓库列表拉取完成，共 {} 个仓库", warehouses.size());

        } catch (Exception e) {
            log.error("拉取FBT仓库列表失败: {}", e.getMessage(), e);
            throw new ServiceException("拉取FBT仓库列表失败: " + e.getMessage(), e);
        }

        return resultList;
    }
}