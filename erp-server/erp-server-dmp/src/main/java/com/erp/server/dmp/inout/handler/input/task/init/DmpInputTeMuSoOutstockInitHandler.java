package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.utils.RedisUtil;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpCfgApiEntity;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.model.dmp.entity.DmpSoDetailEntity;
import com.erp.model.dmp.entity.DmpSoInfoEntity;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.oms.aliexpress.api.IopClient;
import com.erp.oms.aliexpress.api.IopClientImpl;
import com.erp.oms.aliexpress.api.IopRequest;
import com.erp.oms.aliexpress.api.IopResponse;
import com.erp.oms.aliexpress.dto.AliExpressShopInfoDTO;
import com.erp.oms.aliexpress.enums.Protocol;
import com.erp.oms.aliexpress.service.AliExpressOrderService;
import com.erp.oms.aliexpress.util.ApiException;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputInitResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import com.google.common.collect.Lists;
import com.sdk.oms.temu.dto.TemuLogisticShipmentDTO;
import com.sdk.oms.temu.dto.TemuOrderDTO;
import com.sdk.oms.temu.dto.TemuOrderReq;
import com.sdk.oms.temu.dto.TemuResp;
import com.sdk.oms.temu.service.TemuClient;
import com.xxl.job.core.context.XxlJobHelper;
import io.seata.common.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.net.ssl.SSLHandshakeException;
import java.net.SocketTimeoutException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * dmp输入init任务基础处理器，被init任务状态执行器继承，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputTeMuSoOutstockInitHandler extends DmpInputInitHandler{

	@Resource
	private ShopInfoFeign shopInfoFeign;

	@Resource
	private TemuClient temuClient;

	@Resource
	private RedisUtil redisUtil;

	@Override
	public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
		DmpInputTaskEntity taskEntity = this.dmpInputTaskEntity;
		String json = taskEntity.getExtendJson();
		if (StringUtils.isBlank(json)) {
			throw new ServiceException("json为空");
		}
		JSONObject jsonObject = JSON.parseObject(json);
		DmpSoInfoEntity dmpSoInfoEntity = jsonObject.getObject("entity", DmpSoInfoEntity.class);
		DmpSoDetailEntity dmpSoDetailEntity = jsonObject.getObject("detailEntity", DmpSoDetailEntity.class);
		if(dmpSoInfoEntity == null || dmpSoDetailEntity == null) {
			throw new ServiceException("实体类为空");
		}
		String shopId = dmpSoInfoEntity.getShopId();
		if(StringUtils.isBlank(shopId)) {
			throw new ServiceException("店铺ID不能为空");
		}
		List<ShopInfoEntity> shopInfoEntityList = shopInfoFeign.listShopInfoByIds(Arrays.asList(shopId));
		if(CollectionUtils.isEmpty(shopInfoEntityList)) {
			throw new ServiceException("店铺信息不存在");
		}

		ShopInfoEntity shopInfoEntity = shopInfoEntityList.get(0);
		if(StringUtils.isBlank(shopInfoEntity.getDictAreaCode())){
			log.error("店铺信息中dictAreaCode为空,shopId:{}", shopId);
			dmpInputTaskService.updateNextExecTime(inputTaskId, LocalDateTimeUtil.offset(LocalDateTime.now(), 4, ChronoUnit.HOURS));
			throw new ServiceException("店铺信息中dictAreaCode为空,shopId:{}", shopId);
		}
		Map<String,Object> extendMap = shopInfoEntity.getExtendData();
		TemuOrderReq temuOrderReq = new TemuOrderReq();
		temuOrderReq.setAreaCode(shopInfoEntity.getDictAreaCode());
		temuOrderReq.setParentOrderSnList(Arrays.asList(dmpSoInfoEntity.getPlatformCode()));
		temuOrderReq.setToken(shopInfoEntity.getAccessToken());
		temuOrderReq.setAppKey(extendMap.get("clientId").toString());
		temuOrderReq.setAppSecret(extendMap.get("clientSecret").toString());
		TemuResp<TemuOrderDTO> temuResp = temuClient.getOrderList(temuOrderReq);
		if(!temuResp.getSuccess()){
			log.error("查询temu订单数据响应失败,{}",temuResp.getErrorMsg());
			dmpInputTaskService.updateNextExecTime(inputTaskId, LocalDateTimeUtil.offset(LocalDateTime.now(), 4, ChronoUnit.HOURS));
			throw new ServiceException("查询temu订单数据响应失败,{}",temuResp.getErrorMsg());
		}
		TemuOrderDTO temuOrderDTO = temuResp.getResult();
		if(CollectionUtils.isEmpty(temuOrderDTO.getPageItems())){
			dmpInputTaskService.updateNextExecTime(inputTaskId, LocalDateTimeUtil.offset(LocalDateTime.now(), 4, ChronoUnit.HOURS));
			throw new ServiceException("查询temu订单数据内容为空");
		}
		List<TemuOrderDTO.PageItemsDTO> pageItemsDTOList = temuOrderDTO.getPageItems();
		TemuOrderDTO.PageItemsDTO pageItemsDTO = pageItemsDTOList.get(0);
		if(Objects.isNull(pageItemsDTO)){
			dmpInputTaskService.updateNextExecTime(inputTaskId, LocalDateTimeUtil.offset(LocalDateTime.now(), 4, ChronoUnit.HOURS));
			throw new ServiceException("查询temu订单数据内容为空");
		}
		List<TemuOrderDTO.PageItemsDTO.OrderListDTO> orderListDTOList = pageItemsDTO.getOrderList();
		orderListDTOList = orderListDTOList.stream().filter(v->v.getProductList().get(0).getExtCode().equals(dmpSoDetailEntity.getPlatformSku())).collect(Collectors.toList());
		if(CollectionUtils.isEmpty(orderListDTOList)){
			log.error("查询temu订单明细为空,订单号:{}",dmpSoDetailEntity.getPlatformDetailId());
			dmpInputTaskService.updateNextExecTime(inputTaskId, LocalDateTimeUtil.offset(LocalDateTime.now(), 4, ChronoUnit.HOURS));
			throw new ServiceException("查询temu订单数据内容为空,订单号:{}",dmpSoInfoEntity.getPlatformCode());
		}
		pageItemsDTO.setOrderList(orderListDTOList);
		TemuOrderDTO.PageItemsDTO.ParentOrderMapDTO parentOrderMapDTO = pageItemsDTO.getParentOrderMap();
		String parentOrder = parentOrderMapDTO.getParentOrderSn();
		temuOrderReq.setParentOrderSn(parentOrder);
		temuOrderReq.setOrderSn(dmpSoDetailEntity.getPlatformDetailId());
		TemuResp<TemuLogisticShipmentDTO> temuLogisticShipmentDTOTemuResp = temuClient.getLogisticsShipment(temuOrderReq);
		if(!temuLogisticShipmentDTOTemuResp.getSuccess()){
			log.error("查询temu发货数据响应失败,{}",temuResp.getErrorMsg());
			dmpInputTaskService.updateNextExecTime(inputTaskId, LocalDateTimeUtil.offset(LocalDateTime.now(), 4, ChronoUnit.HOURS));
			throw new ServiceException("查询temu发货数据响应失败，{}",temuResp.getErrorMsg());
		}
		TemuLogisticShipmentDTO temuLogisticShipmentDTO = temuLogisticShipmentDTOTemuResp.getResult();
		if(CollectionUtils.isEmpty(temuLogisticShipmentDTO.getShipmentInfoDTO())){
			log.error("查询temu发货数据，没有发货数据,{}", JSONUtil.toJsonStr(temuLogisticShipmentDTO));
			dmpInputTaskService.updateNextExecTime(inputTaskId, LocalDateTimeUtil.offset(LocalDateTime.now(), 4, ChronoUnit.HOURS));
			throw new ServiceException("查询temu发货数据，没有发货数据,{}", JSONUtil.toJsonStr(temuLogisticShipmentDTO));
		}
		TemuLogisticShipmentDTO.ShipmentInfoDTODTO shipmentInfoDTODTO = temuLogisticShipmentDTO.getShipmentInfoDTO().get(0);
		if(shipmentInfoDTODTO.getCooperativeWarehouseDTO() == null || shipmentInfoDTODTO.getCooperativeWarehouseDTO().getWarehouseCode() == null){
			log.error("查询temu发货数据，没有发货仓库,{}",JSONUtil.toJsonStr(temuLogisticShipmentDTO));
			dmpInputTaskService.updateNextExecTime(inputTaskId, LocalDateTimeUtil.offset(LocalDateTime.now(), 4, ChronoUnit.HOURS));
			throw new ServiceException("查询temu发货数据，没有发货仓库,{}",JSONUtil.toJsonStr(temuLogisticShipmentDTO));
		}
		pageItemsDTO.setTrackNo(shipmentInfoDTODTO.getTrackingNumber());
		pageItemsDTO.setShipmentInfoDTODTO(shipmentInfoDTODTO);
		pageItemsDTO.setShopId(shopInfoEntity.getId());
		pageItemsDTO.setShopName(shopInfoEntity.getName());
		pageItemsDTO.setParentOrderSn(parentOrderMapDTO.getParentOrderSn());
		pageItemsDTO.setNextLevelId(taskEntity.getId());
		pageItemsDTO.setSoDetailId(dmpSoDetailEntity.getId());
		List<TemuOrderDTO.PageItemsDTO> allResult = new ArrayList<>();
		allResult.add(pageItemsDTO);
		DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
		dmpInputTaskInitDTO.setMsg(JSONObject.toJSONString(allResult));
		return Collections.singletonList(dmpInputTaskInitDTO);
	}
	
}
