package com.erp.server.dmp.inout.handler.input.task.init.api.jifeng;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.enums.OmsPlatformEnum;
import com.common.business.enums.OverseasInstockStatusEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.erp.model.wms.entity.OverseasProviderWarehouseEntity;
import com.erp.model.wms.entity.SoB2cDeliveryEntity;
import com.erp.model.wms.entity.ThirdWarehouseDeliveryEntity;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.wms.feign.OverseasProviderFeign;
import com.erp.rpc.wms.feign.ThirdWarehouseDeliveryFeign;
import com.erp.rpc.wms.feign.WmsOverseasWarehouseFeign;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.init.DmpInputInitHandler;
import com.erp.server.dmp.inout.utils.DmpHandlerCache;
import com.sdk.wms.jifeng.dto.response.JiFengBaseResp;
import com.sdk.wms.jifeng.dto.response.JiFengInboundResp;
import com.sdk.wms.jifeng.dto.response.JiFengOutboundResp;
import com.sdk.wms.jifeng.service.JiFengService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * dmp输入init任务基础处理器下的极风api获取数据方式
 * @author Administrator
 *
 */
@Service
@Slf4j
@Scope("prototype")
public class JiFengOutboundInitHandler extends DmpInputInitHandler {

	@Resource
    private DmpHandlerCache dmpHandlerCache;

	@Resource
	private JiFengService jiFengService;
	
	@Resource
    private WmsOverseasWarehouseFeign overseasWarehouseFeign;

	@Resource
	private SoB2cFeign soB2cFeign;

	@Resource
	private ThirdWarehouseDeliveryFeign thirdWarehouseDeliveryFeign;

	@Resource
	private OverseasProviderFeign overseasProviderFeign;

	@Override
	public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
		List<OverseasProviderEntity> overseasProviderEntityList = FeignQuery.create(OverseasProviderEntity.class)
				.eq(OverseasProviderEntity::getAuthStatus, AuthStatusEnum.ALREADY.getCode())
				.eq(OverseasProviderEntity::getCode, DmpBasicSystemCodeEnum.JIFENG.getCode())
				.list();
		if(CollUtil.isEmpty(overseasProviderEntityList)) {
			throw new ServiceException("极风授权信息不存在");
		}
		// 取对应授权ID授权
		OverseasProviderEntity overseasProviderEntity = overseasProviderEntityList.stream()
				.filter(e -> e.getId().equalsIgnoreCase(dmpInputTaskEntity.getNextLevelId()))
				.findFirst()
				.orElse(null);
		if(null == overseasProviderEntity) {
			throw new ServiceException("极风对应授权ID信息不存在");
		}
		//查询海外仓库
		List<OverseasProviderWarehouseEntity> overseasProviderWarehouseEntities = FeignQuery.create(OverseasProviderWarehouseEntity.class).eq(OverseasProviderWarehouseEntity::getMainId,overseasProviderEntity.getId()).list();
		overseasProviderWarehouseEntities = overseasProviderWarehouseEntities.stream().filter(v-> !v.getDisabled() && StringUtils.isNotBlank(v.getWarehouseId())).collect(Collectors.toList());
		if(CollectionUtils.isEmpty(overseasProviderWarehouseEntities)){
			log.warn("极风对应海外仓库信息不存在");
			return Collections.emptyList();
		}
		//查询销售订单海外仓库待发货的发货单
		List<ThirdWarehouseDeliveryEntity> thirdWarehouseDeliveryEntityList = thirdWarehouseDeliveryFeign.listWaitShipByWarehouseIds(overseasProviderWarehouseEntities.stream().map(OverseasProviderWarehouseEntity::getWarehouseId).collect(Collectors.toList()));
		if(CollectionUtils.isEmpty(thirdWarehouseDeliveryEntityList)){
			return Collections.emptyList();
		}
		List<String> allSoIds = thirdWarehouseDeliveryEntityList.stream().map(ThirdWarehouseDeliveryEntity::getSoId).distinct().collect(Collectors.toList());
		//查询销售订单，过滤掉销售订单中shipping_order_no为空的数据
		List<SoB2cEntity> soB2cEntityList = soB2cFeign.listByIds(allSoIds);
		if(CollectionUtils.isEmpty(soB2cEntityList)){
			return Collections.emptyList();
		}
		List<String> filterSoIds = soB2cEntityList.stream()
				.filter(v-> StringUtils.isNotBlank(v.getShippingOrderNo()))
				.map(SoB2cEntity::getId)
				.collect(Collectors.toList());
		thirdWarehouseDeliveryEntityList = thirdWarehouseDeliveryEntityList.stream()
				.filter(v-> filterSoIds.contains(v.getSoId()))
				.collect(Collectors.toList());
		if(CollectionUtils.isEmpty(thirdWarehouseDeliveryEntityList)){
			return Collections.emptyList();
		}
		List<String> allCodes = thirdWarehouseDeliveryEntityList.stream().map(ThirdWarehouseDeliveryEntity::getCode).distinct().collect(Collectors.toList());

		//分组，每组最多50个
		List<JiFengOutboundResp> allResult = new ArrayList<>();
		List<List<String>> partCodeList = ListUtils.partition(allCodes, 50);
		for (List<String> codes : partCodeList) {
			JiFengBaseResp<List<JiFengOutboundResp>> resp = jiFengService.getOrder(overseasProviderEntity.getAuthJson(),codes);
			if(Objects.isNull(resp)){
				log.warn("极风获取订单数据失败，响应结果为空");
				throw new ServiceException("极风获取订单数据失败，响应结果为空");
			}
			if(resp.getCode() != 0){
				if(resp.getMessage().contains("Invalid ACCESS TOKEN")){
					overseasProviderEntity = overseasProviderFeign.refreshToken(overseasProviderEntity);
					resp = jiFengService.getOrder(overseasProviderEntity.getAuthJson(),codes);
					if(Objects.isNull(resp)){
						log.warn("极风获取订单数据失败，响应结果为空");
						throw new ServiceException("极风获取订单数据失败，响应结果为空");
					}
					if(resp.getCode() != 0){
						log.warn("极风获取数据失败，code:{},msg:{}",resp.getCode(),resp.getMessage());
						throw new ServiceException("极风获取订单数据失败，code:"+resp.getCode()+",msg:"+resp.getMessage());
					}
					allResult.addAll(resp.getData());
				}else{
					log.warn("极风获取数据失败，code:{},msg:{}",resp.getCode(),resp.getMessage());
					throw new ServiceException("极风获取订单数据失败，code:"+resp.getCode()+",msg:"+resp.getMessage());
				}
			}else{
				allResult.addAll(resp.getData());
			}
		}

		DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
		dmpInputTaskInitDTO.setMsg(JSONObject.toJSONString(allResult));
		return Collections.singletonList(dmpInputTaskInitDTO);
	}

	
	
}
