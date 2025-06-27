package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.business.wrapper.FeignQuery;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpCfgApiEntity;
import com.erp.model.dmp.entity.ThirdMappingEntity;
import com.erp.model.dmp.entity.ThirdShopEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.model.dmp.enums.LingxingPlatformCodeEnum;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import com.erp.server.dmp.service.ThirdMappingService;
import com.erp.server.dmp.service.ThirdShopService;
import com.sdk.oms.temu.dto.TemuCommonDTO;
import com.sdk.oms.temu.dto.TemuResp;
import com.sdk.oms.temu.dto.TemuShippingDTO;
import com.sdk.oms.temu.dto.TemuShippingInfoReq;
import com.sdk.oms.temu.service.TemuClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * dmp输入init任务基础处理器，被init任务状态执行器继承，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputTeMuAddressInitHandler extends DmpInputInitHandler{

	@Resource
	private TemuClient temuClient;
	@Resource
	private ThirdShopService thirdShopService;
	@Resource
	private ThirdMappingService thirdMappingService;

	@Override
	public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
		List<Map<String, Object>> findMongoData = null;
		String parentStorageName = this.getParentStorageName(DmpInputTaskStatusEnum.MONGO);
		if(StringUtils.isNotBlank(parentStorageName)) {
			List<ParamData> paramDataList = new ArrayList<>();
			paramDataList.add(new ParamData(DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, PannoEnum.EQ, dmpInputTaskEntity.getParentTaskId()));
			findMongoData = mongoService.findMongoData(paramDataList, parentStorageName);
		}
		if(CollUtil.isEmpty(findMongoData)) {
			return new ArrayList<>();
		}
		List<DmpInputTaskInitDTO> dmpInputTaskInitDTOList = new ArrayList<>();
        String typeId = dmpCfgInputEntity.getTypeId();
        DmpCfgApiEntity dmpCfgApiEntity = dmpCfgApiService.getById(typeId);
        String apiType = dmpCfgApiEntity.getApiType();
		List<JSONObject> dataList = new ArrayList<>();
		// 店铺和映射
		List<ThirdShopEntity> shopList = thirdShopService.lambdaQuery()
				.eq(ThirdShopEntity::getSysType, DmpBasicSystemCodeEnum.LING_XING.getCode())
				.list();
		List<ThirdMappingEntity> mappingList = thirdMappingService.lambdaQuery()
				.eq(ThirdMappingEntity::getType, "shop")
				.eq(ThirdMappingEntity::getThirdSysType, DmpBasicSystemCodeEnum.LING_XING.getCode())
				.list();
        for(Map<String, Object> findMongo : findMongoData) {
			String deliveryTypeStr = findMongo.getOrDefault("delivery_type", "").toString();
			Boolean isPlatformWarehouseOrder = convertIsPlatformWarehouseOrder(deliveryTypeStr);
			Boolean isTemu = false;
			String platformCode = "";
			// 平台原始信息
			Object platformInfoListObj = findMongo.get("platform_info");
			if (null != platformInfoListObj) {
				JSONArray jsonArray = JSON.parseArray(JSON.toJSONString(platformInfoListObj));
				if (CollectionUtils.isNotEmpty(jsonArray)) {
					Object platformInfoObjIndex1 = jsonArray.get(0);
					Map<String, Object> platformInfoMap = (JSONObject) platformInfoObjIndex1;
					// 平台订单
					platformCode = platformInfoMap.getOrDefault("platform_order_no", "").toString();
					// 解析来源平台
					String platformCodeStr = platformInfoMap.getOrDefault("platform_code", "").toString();
					isTemu = platformCodeStr.equals(LingxingPlatformCodeEnum.TEMU_FBP.getCode());
				}
			}
			// 店铺ID
			String storeId = findMongo.getOrDefault("store_id", "").toString();

			// 校验和获取ERP店铺
			ThirdMappingEntity mappingEntity = checkAndGetErpShopId(shopList, mappingList, storeId);
			String shopId = mappingEntity.getSysId();
			if(isTemu&&!isPlatformWarehouseOrder && StringUtils.isNotBlank(platformCode)){
				TemuCommonDTO temuCommonDTO = temuClient.getAuthInfo(shopId);
				if(Objects.isNull(temuCommonDTO)){
					ServiceException.runError("temu半托管店铺未授权，ID=" + shopId);
				}
				TemuShippingInfoReq temuShippingInfoReq = BeanUtil.copyProperties(temuCommonDTO,TemuShippingInfoReq.class);
				temuShippingInfoReq.setParentOrderSn(platformCode);
				TemuResp<TemuShippingDTO> temuResp = temuClient.getShippingInfo(temuShippingInfoReq);
				if (Objects.isNull(temuResp) || !temuResp.getSuccess() || Objects.isNull(temuResp.getResult())) {
					log.error("temu半托管查询地址失败，返回值 responseMap={}", JSON.toJSONString(temuResp));
					//如果已发货，不更新订单异常
					Map<String, Object> map = new HashMap<>();
					if(StringUtils.isNotBlank(temuResp.getErrorMsg()) && (temuResp.getErrorMsg().contains("It has been signed") ||temuResp.getErrorMsg().contains("One app key can't get the shipping information successfully more than 6 times for per parent order in one day")) ){
						map.put("is_update_error",false);
					}else{
						map.put("is_update_error",true);
					}
					JSONObject object = JSONObject.parseObject(JSON.toJSONString(map));
					object.put("orderId",platformCode);
					dataList.add(object);
					continue;
				}
				TemuShippingDTO temuShippingDTO = temuResp.getResult();
				JSONObject object = JSONObject.parseObject(JSON.toJSONString(temuShippingDTO));
				object.put("orderId",platformCode);
				dataList.add(object);
			}
        }
        
        DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
		dmpInputTaskInitDTO.setMsg(JSON.toJSONString(dataList));
		dmpInputTaskInitDTOList.add(dmpInputTaskInitDTO);
        
		return dmpInputTaskInitDTOList;
	}


	private String getCountryByEnName(String enName){
		List<DictCountryEntity> dictCountryEntityList = FeignQuery.create(DictCountryEntity.class)
				.eq(DictCountryEntity::getNameEn, enName)
				.list();
		if(CollectionUtils.isEmpty(dictCountryEntityList)){
			log.error("Temu半托管查询地址，未找到ERP国家映射关系,国家英文名={}", enName);
			return null;
		}
		return dictCountryEntityList.get(0).getId();
	}

	/**
	 * 转换是否是平台仓
	 */
	private Boolean convertIsPlatformWarehouseOrder(String deliveryTypeStr) {
		// 对应ERP的订单发货类型：
		// 1 混合方式【中转值，最终会转为2或3】
		// 2 自发货：对应ERP自发货订单
		// 3 平台发货【指由平台仓库自动完成履约的订单，如Walmart的WFS订单】：对应ERP平台仓发货订单
		if ("2".equalsIgnoreCase(deliveryTypeStr)) {
			return false;
		}
		if ("3".equalsIgnoreCase(deliveryTypeStr)) {
			return true;
		}
		// 无法判断为null, 黑名单拦截
		return false;
	}
	/**
	 * 校验和获取ERP店铺
	 */
	private ThirdMappingEntity checkAndGetErpShopId(List<ThirdShopEntity> shopList, List<ThirdMappingEntity> mappingList, String storeId) {
		ThirdShopEntity thirdShopEntity = shopList.stream().filter(e -> e.getCode().equalsIgnoreCase(storeId)).findFirst().orElse(null);
		if (null == thirdShopEntity) {
			ServiceException.runError("未找到领星店铺对应映射记录:领星店铺ID=" + storeId);
		}
		ThirdMappingEntity mappingEntity = mappingList.stream().filter(e -> e.getThirdInfoId().equalsIgnoreCase(thirdShopEntity.getId())).findFirst().orElse(null);
		if (null == mappingEntity) {
			ServiceException.runError("领星店铺未映射:领星店铺ID=" + storeId);
		}
		if (StringUtils.isBlank(mappingEntity.getSysId())){
			ServiceException.runError("领星店铺未映射:领星店铺ID=" + storeId);
		}
		return mappingEntity;
	}

}
