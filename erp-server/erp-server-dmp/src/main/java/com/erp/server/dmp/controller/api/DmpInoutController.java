package com.erp.server.dmp.controller.api;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncMqDTO.SyncParamDetailDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.utils.ApplicationContextUtils;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.business.wrapper.QueryParam;
import com.common.business.wrapper.QueryTypeEnum;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.DmpCfgInputConvertValueDTO;
import com.erp.model.dmp.dto.DmpOutputTaskRecordDTO.WdtInsufficientInventoryDTO;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertMappingEntity;
import com.erp.model.dmp.entity.DmpCfgInputEntity;
import com.erp.model.dmp.entity.DmpCfgMqEntity;
import com.erp.model.dmp.entity.DmpCfgOutputEntity;
import com.erp.model.dmp.entity.DmpOutputTaskEntity;
import com.erp.model.dmp.entity.DmpOutputTaskRecordEntity;
import com.erp.model.dmp.entity.DmpPushMsgEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.dmp.enums.DmpCfgMqMqTypeEnum;
import com.erp.model.dmp.enums.DmpOutputTaskRecordStatusEnum;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.wms.dto.WarehouseLocationDTO;
import com.erp.model.wms.dto.WarehouseLocationMoveDTO;
import com.erp.model.wms.dto.WarehouseLocationMoveDetailDTO;
import com.erp.model.wms.dto.WarehouseLocationDTO.LocationListDTO;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.entity.WarehouseLocationEntity;
import com.erp.server.dmp.inout.dto.request.DmpInputHotfixCreateRequest;
import com.erp.server.dmp.inout.dto.request.DmpOutputHotfixCreateRequest;
import com.erp.server.dmp.inout.handler.factory.DmpInputCreateFactory;
import com.erp.server.dmp.inout.handler.factory.DmpOutputCreateFactory;
import com.erp.server.dmp.inout.utils.DmpHandlerCache;
import com.erp.server.dmp.service.DmpCfgInputConvertMappingService;
import com.erp.server.dmp.service.DmpCfgInputConvertService;
import com.erp.server.dmp.service.DmpCfgMqService;
import com.erp.server.dmp.service.DmpCfgOutputService;
import com.erp.server.dmp.service.DmpOutputTaskRecordService;
import com.erp.server.dmp.service.DmpOutputTaskService;
import com.erp.server.dmp.service.DmpPushMsgService;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;


/**
 * 输入输出任务
 * @author Administrator
 *
 */
@Slf4j
@RestController
@RequestMapping("/dmpInout")
public class DmpInoutController extends BaseController {

	@Autowired
	private DmpInputCreateFactory dmpInputCreateFactory;
	
	@Autowired
	private DmpHandlerCache dmpHandlerCache;

	@Autowired
	private DmpCfgInputConvertService dmpCfgInputConvertService;
	
	@Autowired
	private DmpCfgMqService dmpCfgMqService;
	
	@Autowired
	private DmpOutputCreateFactory dmpOutputCreateFactory;

	@Autowired
	private DmpCfgInputConvertMappingService dmpCfgInputConvertMappingService;
	
	@Autowired
	private DmpOutputTaskService dmpOutputTaskService;
	
	@Autowired
	private DmpCfgOutputService dmpCfgOutputService;
	
	@Autowired
	private DmpOutputTaskRecordService dmpOutputTaskRecordService;
	
	@Resource
	protected RedisTemplate<String,Object> redisTemplate;
	
    @PostMapping("doInputTask")
    public ApiResult<?> doInputTask(@RequestBody DmpInputHotfixCreateRequest dmpInputHotfixCreateRequest) {
    	return success(dmpInputCreateFactory.doHotfixInputTask(dmpInputHotfixCreateRequest));
    }
    
    @PostMapping("doOutputTask")
    public ApiResult<?> doOutputTask(@RequestBody DmpOutputHotfixCreateRequest dmpOutputHotfixCreateRequest) {
    	List<QueryParam> queryParams = dmpOutputHotfixCreateRequest.getQueryParams();
    	if(CollUtil.isEmpty(queryParams)) {
    		throw new ServiceException("过滤条件queryParams不能为空");
    	}
    	return success(dmpOutputCreateFactory.doHotfixOutputTask(dmpOutputHotfixCreateRequest));
    }
    
    @PostMapping("getCache")
    public ApiResult<?> getCache() {
    	Map<String, Object> typeCacheMap = new HashMap<>();
    	typeCacheMap.put("dmpBasicSystem", dmpHandlerCache.getDmpBasicSystemEntityList(d -> true));
		typeCacheMap.put("dmpCfgInputConvert", dmpHandlerCache.getDmpCfgInputConvertEntityList(d -> true));
		
		List<DmpCfgInputConvertEntity> dmpCfgInputConvertEntityList = dmpCfgInputConvertService.lambdaQuery()
				.eq(DmpCfgInputConvertEntity::getDisabled, false).list();


		Map<String, Map<String, List<String>>> convertMappingCache = new HashMap<>();
    	for(DmpCfgInputConvertEntity dmpCfgInputConvertEntity : dmpCfgInputConvertEntityList) {
    		String id = dmpCfgInputConvertEntity.getId();
    		convertMappingCache.put(id, dmpHandlerCache.getDmpCfgInputConvertMapping(id));

    	}

		Map<String, List<DmpCfgInputConvertValueDTO.MappingAndValueDTO>> convertValueCache = new HashMap<>();
		List<DmpCfgInputConvertMappingEntity> dmpCfgInputConvertMappingEntityList = dmpCfgInputConvertMappingService.lambdaQuery().eq(DmpCfgInputConvertMappingEntity::getDisabled, Boolean.FALSE).list();
		for (DmpCfgInputConvertMappingEntity dmpCfgInputConvertMappingEntity : dmpCfgInputConvertMappingEntityList) {
			String id = dmpCfgInputConvertMappingEntity.getId();
			convertValueCache.put(id, dmpHandlerCache.getDmpCfgInputConvertValue(id));
		}

		typeCacheMap.put("dmpCfgInputConvertMapping" , convertMappingCache);
		typeCacheMap.put("dmpCfgInputConvertValue" , convertValueCache);

    	typeCacheMap.put("dmpCfgInputDetail", dmpHandlerCache.getDmpCfgInputDetailEntityList(d -> true));
    	typeCacheMap.put("dmpCfgInput", dmpHandlerCache.getDmpCfgInputEntityList(d -> true));
    	typeCacheMap.put("dmpCfgOutputBlack", dmpHandlerCache.getDmpCfgOutputBlackEntityList(d -> true));
    	
    	List<DmpCfgMqEntity> dmpCfgMqEntityList = dmpCfgMqService.lambdaQuery()
			.eq(DmpCfgMqEntity::getMqType, DmpCfgMqMqTypeEnum.ROCKETMQ.getCode())
			.eq(DmpCfgMqEntity::getDisabled, false).list();
    	Map<String , DmpCfgMqEntity> rocketMQDmpCfgMqCache = new HashMap<>();
    	Map<String , String> rocketMQTemplateMap = new HashMap<>();
    	for(DmpCfgMqEntity dmpCfgMqEntity : dmpCfgMqEntityList) {
    		String mqId = dmpCfgMqEntity.getId();
    		rocketMQDmpCfgMqCache.put(mqId, dmpHandlerCache.getRocketMQDmpCfgMqCache(mqId));
    		rocketMQTemplateMap.put(mqId, dmpHandlerCache.getRocketMQTemplate(mqId).getProducer().getNamesrvAddr() + "@" + dmpHandlerCache.getRocketMQTemplate(mqId).getProducer().getProducerGroup());
    	}
    	typeCacheMap.put("dmpCfgMqEntity", rocketMQDmpCfgMqCache);
    	typeCacheMap.put("rocketMQTemplate", rocketMQTemplateMap);
    	typeCacheMap.put("overseasProviderEntity", dmpHandlerCache.getOverseasProviderEntityList(d -> true));
    	typeCacheMap.put("dmpCfgApiEntity", dmpHandlerCache.getDmpCfgApiEntityList(d -> true));
    	
    	return success(typeCacheMap);
    }
    
    @PostMapping("initCache")
    public ApiResult<?> initCache() {
    	dmpHandlerCache.initCache(false);
    	return success();
    }

	/**
	 * 查询同步
	 * @param dto
	 * @return
	 */
	@PostMapping("querySyncByIds")
    public ApiResult<?> querySyncIds(@RequestBody BaseIdsDTO.IdsDTO dto) {
    	return this.querySync(Arrays.asList(new QueryParam(QueryTypeEnum.IN, "id", dto.getIds())));
    }
    
    @PostMapping("querySync")
    public ApiResult<?> querySync(@RequestBody List<QueryParam> queryParams) {
    	QueryWrapper<?> queryWrapper = QueryParam.getQueryWrapper(queryParams);
    	queryWrapper.eq("status", DmpOutputTaskRecordStatusEnum.ERROR.getCode());
    	ServiceImpl serviceImpl = ApplicationContextUtils.getBean("dmpOutputTaskRecordServiceImpl" , ServiceImpl.class);
    	List<DmpOutputTaskRecordEntity> dmpOutputTaskRecordEntityList = (List<DmpOutputTaskRecordEntity>)serviceImpl.list(queryWrapper);
    	if(CollUtil.isNotEmpty(dmpOutputTaskRecordEntityList)) {
    		Map<String, String> cfgOutputIdEntityMaps = dmpOutputTaskService.lambdaQuery()
	    			.in(DmpOutputTaskEntity::getId, dmpOutputTaskRecordEntityList.stream().map(DmpOutputTaskRecordEntity::getMainId).collect(Collectors.toSet()))
	    			.select(DmpOutputTaskEntity::getId , DmpOutputTaskEntity::getCfgOutputId)
	    			.list().stream().collect(Collectors.toMap(DmpOutputTaskEntity::getId, DmpOutputTaskEntity::getCfgOutputId));
	    	
	    	Map<String, DmpCfgOutputEntity> outputIdEntityMaps = dmpCfgOutputService.lambdaQuery().in(DmpCfgOutputEntity::getId, cfgOutputIdEntityMaps.values())
	    			.list().stream().collect(Collectors.toMap(DmpCfgOutputEntity::getId, d -> d));
	    	
	    	Map<String, List<DmpOutputTaskRecordEntity>> cfgOutputRecordEntityListMaps = new HashMap<>();
	    	for(DmpOutputTaskRecordEntity dmpOutputTaskRecordEntity : dmpOutputTaskRecordEntityList) {
	    		String cfgOutputId = cfgOutputIdEntityMaps.get(dmpOutputTaskRecordEntity.getMainId());
	    		List<DmpOutputTaskRecordEntity> list = cfgOutputRecordEntityListMaps.get(cfgOutputId);
	    		if(CollUtil.isEmpty(list)) {
	    			list = new ArrayList<>();
	    		}
	    		list.add(dmpOutputTaskRecordEntity);
	    		cfgOutputRecordEntityListMaps.put(cfgOutputId, list);
	    	}
    		
	    	for(Map.Entry<String, List<DmpOutputTaskRecordEntity>> cfgOutputRecordEntityListMap : cfgOutputRecordEntityListMaps.entrySet()) {
	    		String cfgOutputId = cfgOutputRecordEntityListMap.getKey();
				DmpCfgOutputEntity dmpCfgOutputEntity = outputIdEntityMaps.get(cfgOutputId);
	    		String inputConvertId = dmpCfgOutputEntity.getInputConvertId();
	    		String cfgInputId = dmpHandlerCache.getDmpCfgInputConvertEntityList(d -> d.getId().equals(inputConvertId)).get(0).getMainId();
	    		DmpCfgInputEntity dmpCfgInputEntity = dmpHandlerCache.getDmpCfgInputEntityList(d -> d.getId().equals(cfgInputId)).get(0);
	    		String systemId = dmpCfgInputEntity.getSystemId();
	    		String systemCode = dmpHandlerCache.getDmpBasicSystemEntityList(d -> d.getId().equals(systemId)).get(0).getCode();
	    		
	    		List<DmpOutputTaskRecordEntity> list = cfgOutputRecordEntityListMap.getValue();
	    		if(DmpBasicSystemCodeEnum.ERP.getCode().equals(systemCode)) {
	    			List<DmpOutputTaskRecordEntity> erpQuerySync = dmpOutputTaskRecordService.erpQuerySync(dmpCfgOutputEntity, list);
	    			if(CollUtil.isNotEmpty(erpQuerySync)) {
	    				dmpOutputTaskRecordService.batchSync(dmpOutputTaskRecordService.listByIds(erpQuerySync.stream().map(DmpOutputTaskRecordEntity::getId).collect(Collectors.toList())));
	    			}
	    		}else {
	    			List<String> dataIds = list.stream().map(DmpOutputTaskRecordEntity::getDataId).collect(Collectors.toList());
		    		List<String> ids = list.stream().map(DmpOutputTaskRecordEntity::getId).collect(Collectors.toList());
	    			DmpOutputHotfixCreateRequest dmpOutputHotfixCreateRequest = new DmpOutputHotfixCreateRequest();
	    			dmpOutputHotfixCreateRequest.setCfgOutputId(cfgOutputId);
	    			dmpOutputHotfixCreateRequest.setQueryParams(Arrays.asList(new QueryParam(QueryTypeEnum.IN, "id", dataIds)));
	    			try {
						dmpOutputCreateFactory.doHotfixOutputTask(dmpOutputHotfixCreateRequest);
						dmpOutputTaskRecordService.lambdaUpdate()
							.in(DmpOutputTaskRecordEntity::getId, ids)
							.eq(DmpOutputTaskRecordEntity::getStatus, DmpOutputTaskRecordStatusEnum.ERROR.getCode())
							.set(DmpOutputTaskRecordEntity::getStatus, DmpOutputTaskRecordStatusEnum.FINISH.getCode())
							.set(DmpOutputTaskRecordEntity::getIsNeedSync, false)
							.update();
					} catch (Exception e) {
						log.error("查询同步调用dmp报错" , e);
					}
	    		}
	    	}
	    	
    	}
    	return success(dmpOutputTaskRecordEntityList);
    }
    
    /**
     * 获取旺店通库存不足单据
     * @return
     */
    @GetMapping("getWdtInsufficientInventory")
    public ApiResult<Collection<WdtInsufficientInventoryDTO>> getWdtInsufficientInventory() {
    	Collection<WdtInsufficientInventoryDTO> values = null;
    	String redisKey = "dmp:inout:wdt:inventory";
    	if(redisTemplate.opsForValue().setIfAbsent(redisKey, DateUtil.now(), 300, TimeUnit.SECONDS)) {
    		try {
				List<DmpOutputTaskRecordEntity> dmpOutputTaskRecordEntityList = dmpOutputTaskRecordService.lambdaQuery()
				    	.in(DmpOutputTaskRecordEntity::getStatus, Arrays.asList(DmpOutputTaskRecordStatusEnum.ERROR.getCode()))
				    	.last(" and response_data like '旺店通出库消费数据失败%库存不足%' ")
				    	.list();
					Map<String, WdtInsufficientInventoryDTO> map = new HashMap<>();
					if(CollUtil.isNotEmpty(dmpOutputTaskRecordEntityList)) {
						for(DmpOutputTaskRecordEntity dmpOutputTaskRecordEntity : dmpOutputTaskRecordEntityList) {
							String requestData = dmpOutputTaskRecordEntity.getResponseData();
				            String[] split = requestData.split("sku=");
				            for(int i = 1; i < split.length ; i++) {
				            	String[] split2 = split[i].split(",仓库=");
				                String skuNo = split2[0].replace("[", "").replace("]", "");
				                String[] split3 = split2[1].split(",仓位=");
				                String warehouseName = split3[0].replace("[", "").replace("]", "");
				                String[] split4 = split3[1].split(",库存状态");
				                String position = split4[0].replace("[", "").replace("]", "");
				                String[] split5 = split4[1].split("交易数:");
								String qty = split5[1].substring(0, 3).replace("[", "").replace("]", "").trim();
				                
				                String key = warehouseName + "_" + position + "_" + skuNo;
				                WdtInsufficientInventoryDTO dto = map.get(key);
				        		if(dto == null) {
				        			dto = new WdtInsufficientInventoryDTO();
				        			dto.setWarehouse(warehouseName);
				        			dto.setPosition(position);
				        			dto.setSku(skuNo);
				        		}
				        		dto.setNum(dto.getNum() + (Integer.valueOf(qty) * -1));
				        		map.put(key, dto);
				            }
						}
					}
					values = map.values();
					if(CollUtil.isNotEmpty(values)) {
						String warehouseName = "东莞塘厦仓";
						List<WdtInsufficientInventoryDTO> invertoryList = values.stream().filter(v -> v.getWarehouse().equals(warehouseName)).collect(Collectors.toList());
						if(CollUtil.isNotEmpty(invertoryList)) {
							List<WarehouseEntity> list = FeignQuery.create(WarehouseEntity.class).eq(WarehouseEntity::getName, warehouseName).list();
							if(CollUtil.isNotEmpty(list)) {
								WarehouseLocationMoveDTO.PcAddDTO addDto = new WarehouseLocationMoveDTO.PcAddDTO();
								addDto.setBillDate(LocalDate.now());
								List<WarehouseLocationMoveDetailDTO.AddDTO> detailList = new ArrayList<>();
								
								WarehouseEntity warehouseEntity = list.get(0);
								PagingDTO<WarehouseLocationDTO.SelectDTO> searchDTO = new PagingDTO<WarehouseLocationDTO.SelectDTO>();
								searchDTO.setPageSize(-1);
								searchDTO.setCurrPage(1);
								WarehouseLocationDTO.SelectDTO params = new WarehouseLocationDTO.SelectDTO();
								String warehouseId = warehouseEntity.getId();
								params.setWarehouseId(warehouseId);
								params.setFilterZero(true);
								searchDTO.setParams(params);
								
								List<ProductDetailEntity> productDetailList = FeignQuery.create(ProductDetailEntity.class)
				    				.in(ProductDetailEntity::getSkuNo, invertoryList.stream().map(WdtInsufficientInventoryDTO::getSku).collect(Collectors.toList()))
				    				.list();
								List<WarehouseLocationEntity> warehouseLocationList = FeignQuery.create(WarehouseLocationEntity.class)
				    				.eq(WarehouseLocationEntity::getWarehouseId, warehouseId)
				    				.in(WarehouseLocationEntity::getName, invertoryList.stream().map(WdtInsufficientInventoryDTO::getPosition).collect(Collectors.toList()))
				    				.list();
								Map<String, String> skuIdNoMap = productDetailList.stream().collect(Collectors.toMap(ProductDetailEntity::getSkuNo, ProductDetailEntity::getId));
								Map<String, String> locationNameCodeMap = warehouseLocationList.stream().collect(Collectors.toMap(WarehouseLocationEntity::getName, WarehouseLocationEntity::getCode));
								for(WdtInsufficientInventoryDTO wdtInsufficientInventoryDTO : invertoryList) {
									String sku = wdtInsufficientInventoryDTO.getSku();
									params.setSkuNo(sku);
				    				PagingVO pagingVO = FeignQuery.invoke(PagingVO.class, "com.erp.server.wms.service.impl.WarehouseLocationServiceImpl", "pagingSelect", Arrays.asList(searchDTO));
				    				List dataList = pagingVO.getList();
				    				if(CollUtil.isNotEmpty(dataList)) {
				    					Integer num = wdtInsufficientInventoryDTO.getNum();
				    					List<WarehouseLocationDTO.LocationListDTO> locationList = JSON.parseArray(JSON.toJSONString(dataList), LocationListDTO.class);
				    					LocationListDTO dto = locationList.stream().filter(l -> l.getCode().startsWith("3") && l.getUsableQty().compareTo(num) >= 0).sorted((l1 , l2) -> l2.getUsableQty().compareTo(l1.getUsableQty())).findFirst().orElse(null);
				    					if(dto == null) {
				    						dto = locationList.stream().filter(l -> l.getCode().startsWith("2") && l.getUsableQty().compareTo(num) >= 0).sorted((l1 , l2) -> l2.getUsableQty().compareTo(l1.getUsableQty())).findFirst().orElse(null);
				    						if(dto == null) {
				    							dto = locationList.stream().filter(l -> l.getCode().startsWith("4") && l.getUsableQty().compareTo(num) >= 0).sorted((l1 , l2) -> l2.getUsableQty().compareTo(l1.getUsableQty())).findFirst().orElse(null);
				    						}
				    					}
				    					if(dto != null) {
				    						WarehouseLocationMoveDetailDTO.AddDTO detailAddDto = new WarehouseLocationMoveDetailDTO.AddDTO();
				    						detailAddDto.setSkuId(skuIdNoMap.get(sku));
				    						detailAddDto.setSkuNo(sku);
				    						detailAddDto.setQty(num);
				    						detailAddDto.setOutWarehouseLocation(dto.getCode());
				    						String position = wdtInsufficientInventoryDTO.getPosition();
				    						if("空仓位".equals(position)) {
				    							position = "";
				    						}else {
				    							position = locationNameCodeMap.get(position);
				    						}
											detailAddDto.setInWarehouseLocation(position);
				    						detailAddDto.setOutInventoryStatus("usable");
				    						detailAddDto.setInInventoryStatus("usable");
				    						detailAddDto.setWarehouseId(warehouseId);
				    						detailAddDto.setRemark("旺店通同步销售出库单库存不足自动仓位移动");
				    						
				    						detailList.add(detailAddDto);
				    					}
				    				}
								}
								if(CollUtil.isNotEmpty(detailList)) {
									addDto.setDetailList(detailList);
									String moveId = FeignQuery.invoke(String.class, "com.erp.server.wms.service.impl.WarehouseLocationMoveServiceImpl", "pcAdd", Arrays.asList(addDto));
									FeignQuery.invoke("com.erp.server.wms.service.impl.WarehouseLocationMoveServiceImpl", "submit", Arrays.asList(moveId));
									ApproveOneDTO approveOneDTO = new ApproveOneDTO();
									approveOneDTO.setId(moveId);
									approveOneDTO.setType("pass");
									approveOneDTO.setComment("旺店通同步销售出库单库存不足自动仓位移动");
									FeignQuery.invoke("com.erp.server.wms.service.impl.WarehouseLocationMoveServiceImpl", "pcApprove", Arrays.asList(approveOneDTO));
									
									List<DmpOutputTaskRecordEntity> dealDmpOutputTaskRecordEntityList = dmpOutputTaskRecordService.lambdaQuery()
				    					.eq(DmpOutputTaskRecordEntity::getStatus, DmpOutputTaskRecordStatusEnum.ERROR.getCode())
				    			    	.last(" and response_data like '旺店通出库消费数据失败%库存不足%" + warehouseName + "%'")
				    			    	.list();
									dmpOutputTaskRecordService.batchSync(dealDmpOutputTaskRecordEntityList);
								}
							}
						}
					}
			}catch (Exception e) {
				log.error("wdt库存不足失败" , e);
				throw e;
			}finally {
				redisTemplate.delete(redisKey);
			}
    	}else {
    		throw new ServiceException("请勿重复点击");
    	}
		return success(values);
    }
}
