package com.erp.server.dmp.inout.handler.input.task.init.api.weishi;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSONObject;
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
import com.erp.model.wms.enums.ThirdWaresouseRequestStatusEnums;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.wms.feign.WmsOverseasWarehouseFeign;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.init.DmpInputInitHandler;
import com.erp.server.dmp.inout.utils.DmpHandlerCache;
import com.sdk.wms.jifeng.dto.response.JiFengBaseResp;
import com.sdk.wms.jifeng.dto.response.JiFengOutboundResp;
import com.sdk.wms.jifeng.service.JiFengService;
import com.sdk.wms.weishi.dto.request.WeiShiGetOutboundRequest;
import com.sdk.wms.weishi.dto.response.WeiShiBaseResp;
import com.sdk.wms.weishi.dto.response.WeiShiOutboundResp;
import com.sdk.wms.weishi.service.WeiShiService;
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
 * dmp输入init任务基础处理器下的纬狮api获取数据方式
 * @author Administrator
 *
 */
@Service
@Slf4j
@Scope("prototype")
public class WeiShiOutboundInitHandler extends DmpInputInitHandler {

	@Resource
	private WeiShiService weiShiService;

	@Resource
	private SoB2cFeign soB2cFeign;
	
	@Override
	public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
		List<OverseasProviderEntity> overseasProviderEntityList = FeignQuery.create(OverseasProviderEntity.class)
				.eq(OverseasProviderEntity::getAuthStatus, AuthStatusEnum.ALREADY.getCode())
				.eq(OverseasProviderEntity::getCode, DmpBasicSystemCodeEnum.WEI_SHI.getCode())
				.list();
		if(CollUtil.isEmpty(overseasProviderEntityList)) {
			throw new ServiceException("纬狮授权信息不存在");
		}
		// 取对应授权ID授权
		OverseasProviderEntity overseasProviderEntity = overseasProviderEntityList.stream()
				.filter(e -> e.getId().equalsIgnoreCase(dmpInputTaskEntity.getNextLevelId()))
				.findFirst()
				.orElse(null);
		if(null == overseasProviderEntity) {
			throw new ServiceException("纬狮对应授权ID信息不存在");
		}
		//查询待发货的订单
		List<ThirdWarehouseDeliveryEntity> deliveryList = FeignQuery.create(ThirdWarehouseDeliveryEntity.class)
				.eq(ThirdWarehouseDeliveryEntity::getThirdWarehousePlatform, PlatformDictEnum.WEISHI.getCode())
				.eq(ThirdWarehouseDeliveryEntity::getStatus, SoB2cWarehouseDeliveryStatusEnum.WAIT_HANDLE.getStatus())
				.list();

		if(CollectionUtils.isEmpty(deliveryList)){
			log.warn("纬狮没有待发货的订单");
			return Collections.emptyList();
		}
		List<String> soIds = deliveryList.stream().map(ThirdWarehouseDeliveryEntity::getSoId).collect(Collectors.toList());
		List<SoB2cEntity> soB2cList = soB2cFeign.listByIds(soIds);
		if(CollectionUtils.isEmpty(soB2cList)){
			log.warn("纬狮没有待发货的订单");
			return Collections.emptyList();
		}

		List<String> allCodes = soB2cList.stream()
				.map(SoB2cEntity::getShippingOrderNo)
				.filter(StringUtils::isNotBlank)
				.collect(Collectors.toList());

		//分组，每组最多50个
		List<WeiShiOutboundResp> allResult = new ArrayList<>();
		for (String code : allCodes) {
			WeiShiGetOutboundRequest weiShiGetOutboundRequest = new WeiShiGetOutboundRequest();
			weiShiGetOutboundRequest.setOrderNo(code);
			WeiShiBaseResp<WeiShiOutboundResp> resp = weiShiService.getOutbound(weiShiGetOutboundRequest,overseasProviderEntity.getAuthJson());
			if (null == resp || resp.getData() == null) {
				throw new ServiceException("纬狮获取出库单数据失败: 响应结果为空");
			}
			if(resp.getCode() != 200){
				log.warn("纬狮获取数据失败，code:{},msg:{}",resp.getCode(),resp.getMsg());
				throw new ServiceException("纬狮获取订单数据失败，code:"+resp.getCode()+",msg:"+resp.getMsg());
			}
			allResult.add(resp.getData());
		}

		DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
		dmpInputTaskInitDTO.setMsg(JSONObject.toJSONString(allResult));
		return Collections.singletonList(dmpInputTaskInitDTO);
	}

	
	
}
