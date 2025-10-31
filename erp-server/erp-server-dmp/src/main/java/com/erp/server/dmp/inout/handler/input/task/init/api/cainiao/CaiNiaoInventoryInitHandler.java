package com.erp.server.dmp.inout.handler.input.task.init.api.cainiao;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.business.enums.OmsPlatformEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpThirdWarehouseInfoEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.erp.oms.aliexpress.dto.AliExpressShopInfoDTO;
import com.erp.oms.aliexpress.service.AliExpressOrderService;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.init.DmpInputInitHandler;
import com.erp.server.dmp.service.DmpThirdWarehouseInfoService;
import com.erp.wms.aliexpress.model.AliexpressAuthDTO;
import com.erp.wms.aliexpress.model.inventory.ApiInventoryResponseDTO;
import com.erp.wms.aliexpress.service.AliexpressWarehouseService;
import com.erp.wms.aliexpress.util.ApiException;
import com.sdk.wms.jifeng.dto.response.JiFengBaseResp;
import com.sdk.wms.jifeng.dto.response.JiFengInventoryResp;
import com.sdk.wms.jifeng.service.JiFengService;
import io.seata.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
@Scope("prototype")
public class CaiNiaoInventoryInitHandler extends DmpInputInitHandler {

	public static final String SOURCE_PLATFORM = "sourcePlatform";
	public static final String AUTH_ID = "authId";

	@Resource
	private AliexpressWarehouseService aliexpressWarehouseService;

	@Resource
	private DmpThirdWarehouseInfoService dmpThirdWarehouseInfoService;

	@Resource
	private AliExpressOrderService aliExpressOrderService;

	@Override
	public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
		List<DmpInputTaskInitDTO> resultList = new ArrayList<>();

		List<OverseasProviderEntity> overseasProviderEntityList = dmpHandlerCache.getOverseasProviderEntityList(d -> d.getCode().equals(getDmpBasicSystemCodeEnum().getCode()));
		if (CollUtil.isEmpty(overseasProviderEntityList)) {
			return Collections.emptyList();
		}
		// 取对应授权ID授权
		OverseasProviderEntity overseasProviderEntity = overseasProviderEntityList.stream()
				.filter(e -> e.getId().equalsIgnoreCase(dmpInputTaskEntity.getNextLevelId()))
				.findFirst()
				.orElse(null);
		if (null == overseasProviderEntity) {
			throw new ServiceException(getPlatForm().getName() + "对应授权ID信息不存在");
		}
		if (overseasProviderEntity.getEnableDate().isAfter(LocalDate.now())) {
			return Collections.emptyList();
		}
		String id = overseasProviderEntity.getId();
		String shopId = overseasProviderEntity.getAuthJson().getOrDefault("shopId","").toString();
		if(StringUtils.isBlank(shopId)){
			throw new ServiceException(getPlatForm().getName() + "对应授权ID信息不存在shopId");
		}
		AliExpressShopInfoDTO aliExpressShopInfoDTO = aliExpressOrderService.getShopInfoByShopId(shopId);
		AliexpressAuthDTO aliexpressAuthDTO = new AliexpressAuthDTO();
		aliexpressAuthDTO.setUrl(aliExpressShopInfoDTO.getBaseUrl());
		aliexpressAuthDTO.setAppKey(aliExpressShopInfoDTO.getClientId());
		aliexpressAuthDTO.setAppSecret(aliExpressShopInfoDTO.getClientSecret());
		aliexpressAuthDTO.setAccessToken(aliExpressShopInfoDTO.getToken());
		aliexpressAuthDTO.setOwnerCode(overseasProviderEntity.getOwnerCode());
		aliexpressAuthDTO.setShopId(shopId);

		List<ApiInventoryResponseDTO.Result.Data.ItemsDTO> allResult = new ArrayList<>();
        try {
			ApiInventoryResponseDTO apiInventoryResponseDTO = aliexpressWarehouseService.getInventory(aliexpressAuthDTO);
			if(!apiInventoryResponseDTO.isSuccess()){
				log.error("菜鸟获取库存列表失败,code:{},msg:{}", apiInventoryResponseDTO.getErrorResponse().getCode(), apiInventoryResponseDTO.getErrorResponse().getMsg());
				throw new ServiceException("菜鸟获取库存列表失败,code:" + apiInventoryResponseDTO.getErrorResponse().getCode() + ",msg:" + apiInventoryResponseDTO.getErrorResponse().getMsg());
			}
			allResult = apiInventoryResponseDTO.getDataList();
        } catch (ApiException e) {
			log.error("菜鸟获取库存列表失败,code:{},msg:{}", e.getErrorCode(), e.getMessage());
			throw new ServiceException("菜鸟获取库存列表失败,code:" + e.getErrorCode() + ",msg:" + e.getMessage());
        }

        DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
		JSONArray parseArray = JSON.parseArray(JSONObject.toJSONString(allResult));
		parseArray.forEach(p -> {
			JSONObject j = (JSONObject) p;
			j.put(AUTH_ID, id);
			j.put(SOURCE_PLATFORM, getDmpBasicSystemCodeEnum().getCode());
		});
		dmpInputTaskInitDTO.setMsg(parseArray.toJSONString());

		resultList.add(dmpInputTaskInitDTO);

		return resultList;
	}
	public OmsPlatformEnum getPlatForm(){
		return OmsPlatformEnum.getByCode(getDmpBasicSystemCodeEnum().getCode());
	}

	public  DmpBasicSystemCodeEnum getDmpBasicSystemCodeEnum(){
		return DmpBasicSystemCodeEnum.CAINIAO;
	}
}
