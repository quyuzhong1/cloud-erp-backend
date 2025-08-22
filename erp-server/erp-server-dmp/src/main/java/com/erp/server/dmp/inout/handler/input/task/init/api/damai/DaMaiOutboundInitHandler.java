package com.erp.server.dmp.inout.handler.input.task.init.api.damai;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.enums.OmsPlatformEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.erp.model.wms.entity.OverseasProviderWarehouseEntity;
import com.erp.model.wms.entity.ThirdWarehouseDeliveryEntity;
import com.erp.model.wms.enums.SoB2cWarehouseDeliveryStatusEnum;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.wms.feign.WmsOverseasWarehouseFeign;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.init.DmpInputInitHandler;
import com.erp.server.dmp.inout.utils.DmpHandlerCache;
import com.sdk.wms.damai.dto.request.DaMaiGetOrderRequest;
import com.sdk.wms.damai.dto.response.DaMaiBaseResp;
import com.sdk.wms.damai.dto.response.DaMaiGetOrderResp;
import com.sdk.wms.damai.service.DaMaiService;
import com.sdk.wms.jifeng.dto.response.JiFengBaseResp;
import com.sdk.wms.jifeng.dto.response.JiFengOutboundResp;
import com.sdk.wms.jifeng.service.JiFengService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * dmp输入init任务基础处理器下的大卖仓api获取数据方式
 * @author Administrator
 *
 */
@Service
@Slf4j
@Scope("prototype")
public class DaMaiOutboundInitHandler extends DmpInputInitHandler {

	@Resource
    private DmpHandlerCache dmpHandlerCache;

	@Resource
	private DaMaiService daMaiService;
	
	@Resource
    private WmsOverseasWarehouseFeign overseasWarehouseFeign;

	@Resource
	private SoB2cFeign soB2cFeign;
	
	@Override
	public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
		List<OverseasProviderEntity> overseasProviderEntityList = dmpHandlerCache.getOverseasProviderEntityList(d -> d.getCode().equals(getPlatForm().getCode()));
		if (CollUtil.isEmpty(overseasProviderEntityList)) {
			return Collections.emptyList();
		}
		// 取对应授权ID授权
		OverseasProviderEntity overseasProviderEntity = overseasProviderEntityList.stream()
				.filter(e -> e.getId().equalsIgnoreCase(dmpInputTaskEntity.getNextLevelId()))
				.findFirst()
				.orElse(null);
		if(null == overseasProviderEntity) {
			throw new ServiceException("大卖仓对应授权ID信息不存在");
		}
		//查询销售订单海外仓库待发货的订单
		//查询待发货的订单
		List<ThirdWarehouseDeliveryEntity> deliveryList = FeignQuery.create(ThirdWarehouseDeliveryEntity.class)
				.eq(ThirdWarehouseDeliveryEntity::getThirdWarehousePlatform, PlatformDictEnum.DA_MAI.getCode())
				.eq(ThirdWarehouseDeliveryEntity::getStatus, SoB2cWarehouseDeliveryStatusEnum.WAIT_HANDLE.getStatus())
				.list();
		List<String> allCodes = deliveryList.stream().map(ThirdWarehouseDeliveryEntity::getCode).collect(Collectors.toList());
		//分组，每组最多50个
		List<DaMaiGetOrderResp> allResult = new ArrayList<>();
		List<List<String>> partCodeList = ListUtils.partition(allCodes, 50);
		for (List<String> codes : partCodeList) {
			DaMaiGetOrderRequest daMaiGetOrderRequest = new DaMaiGetOrderRequest();
			daMaiGetOrderRequest.setCustRefNoList(codes);
			DaMaiBaseResp<List<DaMaiGetOrderResp>> resp = daMaiService.getOrderList(overseasProviderEntity.getAuthJson(),daMaiGetOrderRequest);
			if(resp.getStatus() == null || !resp.getStatus().equals("success")){
				log.warn("大卖仓获取数据失败，code:{},msg:{}",resp.getStatus(),resp.getMsg());
				throw new ServiceException("大卖仓获取订单数据失败，code:"+resp.getStatus()+",msg:"+resp.getMsg());
			}
			allResult.addAll(resp.getData());
		}

		DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
		dmpInputTaskInitDTO.setMsg(JSONObject.toJSONString(allResult));
		return Collections.singletonList(dmpInputTaskInitDTO);
	}

	public OmsPlatformEnum getPlatForm(){
		return OmsPlatformEnum.getByCode(getDmpBasicSystemCodeEnum().getCode());
	}

	public  DmpBasicSystemCodeEnum getDmpBasicSystemCodeEnum(){
		return DmpBasicSystemCodeEnum.DA_MAI;
	}
}
