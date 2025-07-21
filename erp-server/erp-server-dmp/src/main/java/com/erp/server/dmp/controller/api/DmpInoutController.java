package com.erp.server.dmp.controller.api;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.exceptions.ExceptionUtil;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.erp.model.dmp.enums.*;
import com.erp.server.dmp.inout.dto.request.DmpInputFinishRequest;
import com.erp.server.dmp.inout.handler.factory.DmpInputTaskFactory;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
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
import com.erp.model.dmp.entity.*;
import com.erp.model.dmp.dto.SdyPushDTO;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertMappingEntity;
import com.erp.model.dmp.entity.DmpCfgInputEntity;
import com.erp.model.dmp.entity.DmpCfgMqEntity;
import com.erp.model.dmp.entity.DmpCfgOutputEntity;
import com.erp.model.dmp.entity.DmpOutputTaskEntity;
import com.erp.model.dmp.entity.DmpOutputTaskRecordEntity;
import com.erp.model.dmp.entity.DmpOutputTaskRecordMergeEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.wms.dto.WarehouseLocationDTO;
import com.erp.model.wms.dto.WarehouseLocationDTO.LocationListDTO;
import com.erp.model.wms.dto.WarehouseLocationMoveDTO;
import com.erp.model.wms.dto.WarehouseLocationMoveDetailDTO;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.entity.WarehouseLocationEntity;
import com.erp.server.dmp.inout.dto.request.DmpInputHotfixCreateRequest;
import com.erp.server.dmp.inout.dto.request.DmpOutputHotfixCreateRequest;
import com.erp.server.dmp.inout.handler.factory.DmpInputCreateFactory;
import com.erp.server.dmp.inout.handler.factory.DmpOutputCreateFactory;
import com.erp.server.dmp.inout.utils.DmpHandlerCache;
import com.erp.server.dmp.service.*;
import com.erp.server.dmp.service.DmpCfgInputConvertMappingService;
import com.erp.server.dmp.service.DmpCfgInputConvertService;
import com.erp.server.dmp.service.DmpCfgMqService;
import com.erp.server.dmp.service.DmpCfgOutputService;
import com.erp.server.dmp.service.DmpOutputTaskRecordMergeService;
import com.erp.server.dmp.service.DmpOutputTaskRecordService;
import com.erp.server.dmp.service.DmpOutputTaskService;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * 输入输出任务
 *
 * @author Administrator
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

    @Autowired
    private DmpOutputTaskRecordMergeService dmpOutputTaskRecordMergeService;

    @Resource
    protected RedisTemplate<String, Object> redisTemplate;

    @Autowired
	@Qualifier("dmpSdyOutputPushExecutorPool")
	private ExecutorService dmpSdyOutputPushExecutorPool;

	@Autowired
	private DmpInputTaskFactory dmpInputTaskFactory;
	@Autowired
	private DmpInputTaskService dmpInputTaskService;
	@Autowired
	@Qualifier("dmpInputExecutorPool")
	private ExecutorService dmpInputExecutorPool;


    @PostMapping("doInputTask")
    public ApiResult<?> doInputTask(@RequestBody DmpInputHotfixCreateRequest dmpInputHotfixCreateRequest) {
        return success(dmpInputCreateFactory.doHotfixInputTask(dmpInputHotfixCreateRequest));
    }

    @PostMapping("doOutputTask")
    public ApiResult<?> doOutputTask(@RequestBody DmpOutputHotfixCreateRequest dmpOutputHotfixCreateRequest) {
        List<QueryParam> queryParams = dmpOutputHotfixCreateRequest.getQueryParams();
        if (CollUtil.isEmpty(queryParams)) {
            throw new ServiceException("过滤条件queryParams不能为空");
        }
        return success(dmpOutputCreateFactory.doHotfixOutputTask(dmpOutputHotfixCreateRequest));
    }

    @PostMapping("getCache")
    public ApiResult<?> getCache(@RequestParam(required = false) String f) {
        Map<String, Object> typeCacheMap = new HashMap<>();
        if(StringUtils.isNotBlank(f)) {
        	try {
				Field field = dmpHandlerCache.getClass().getDeclaredField(f);
				field.setAccessible(true);
				Object object = field.get(dmpHandlerCache);
				field.setAccessible(false);
				typeCacheMap.put(f, object);
			} catch (Exception e) {
				log.error("获取中台缓存错误" , e);
				return ApiResult.error("获取中台缓存错误：" + ExceptionUtil.stacktraceToString(e));
			}
        }else {
        	typeCacheMap.put("dmpBasicSystem", dmpHandlerCache.getDmpBasicSystemEntityList(d -> true));
            typeCacheMap.put("dmpCfgInputConvert", dmpHandlerCache.getDmpCfgInputConvertEntityList(d -> true));

            List<DmpCfgInputConvertEntity> dmpCfgInputConvertEntityList = dmpCfgInputConvertService.lambdaQuery()
                    .eq(DmpCfgInputConvertEntity::getDisabled, false).list();


            Map<String, Map<String, List<String>>> convertMappingCache = new HashMap<>();
            for (DmpCfgInputConvertEntity dmpCfgInputConvertEntity : dmpCfgInputConvertEntityList) {
                String id = dmpCfgInputConvertEntity.getId();
                convertMappingCache.put(id, dmpHandlerCache.getDmpCfgInputConvertMapping(id));

            }

            Map<String, List<DmpCfgInputConvertValueDTO.MappingAndValueDTO>> convertValueCache = new HashMap<>();
            List<DmpCfgInputConvertMappingEntity> dmpCfgInputConvertMappingEntityList = dmpCfgInputConvertMappingService.lambdaQuery().eq(DmpCfgInputConvertMappingEntity::getDisabled, Boolean.FALSE).list();
            for (DmpCfgInputConvertMappingEntity dmpCfgInputConvertMappingEntity : dmpCfgInputConvertMappingEntityList) {
                String id = dmpCfgInputConvertMappingEntity.getId();
                convertValueCache.put(id, dmpHandlerCache.getDmpCfgInputConvertValue(id));
            }

            typeCacheMap.put("dmpCfgInputConvertMapping", convertMappingCache);
            typeCacheMap.put("dmpCfgInputConvertValue", convertValueCache);

            typeCacheMap.put("dmpCfgInputDetail", dmpHandlerCache.getDmpCfgInputDetailEntityList(d -> true));
            typeCacheMap.put("dmpCfgInput", dmpHandlerCache.getDmpCfgInputEntityList(d -> true));
            typeCacheMap.put("dmpCfgOutput", dmpHandlerCache.getDmpCfgOutputEntityList(d -> true));
            typeCacheMap.put("dmpCfgOutputBlack", dmpHandlerCache.getDmpCfgOutputBlackEntityList(d -> true));

            List<DmpCfgMqEntity> dmpCfgMqEntityList = dmpCfgMqService.lambdaQuery()
                    .eq(DmpCfgMqEntity::getMqType, DmpCfgMqMqTypeEnum.ROCKETMQ.getCode())
                    .eq(DmpCfgMqEntity::getDisabled, false).list();
            Map<String, DmpCfgMqEntity> rocketMQDmpCfgMqCache = new HashMap<>();
            Map<String, String> rocketMQTemplateMap = new HashMap<>();
            for (DmpCfgMqEntity dmpCfgMqEntity : dmpCfgMqEntityList) {
                String mqId = dmpCfgMqEntity.getId();
                rocketMQDmpCfgMqCache.put(mqId, dmpHandlerCache.getRocketMQDmpCfgMqCache(mqId));
                rocketMQTemplateMap.put(mqId, dmpHandlerCache.getRocketMQTemplate(mqId).getProducer().getNamesrvAddr() + "@" + dmpHandlerCache.getRocketMQTemplate(mqId).getProducer().getProducerGroup());
            }
            typeCacheMap.put("dmpCfgMqEntity", rocketMQDmpCfgMqCache);
            typeCacheMap.put("rocketMQTemplate", rocketMQTemplateMap);
            typeCacheMap.put("overseasProviderEntity", dmpHandlerCache.getOverseasProviderEntityList(d -> true));
            typeCacheMap.put("dmpCfgApiEntity", dmpHandlerCache.getDmpCfgApiEntityList(d -> true));
            typeCacheMap.put("dorisQueryCfgSettingEntity", dmpHandlerCache.getDorisQueryCfgSettingEntityCache());
        }
        return success(typeCacheMap);
    }

    @PostMapping("initCache")
    public ApiResult<?> initCache() {
        dmpHandlerCache.initCache(false);
        return success();
    }

    /**
     * 查询同步
     *
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
        ServiceImpl serviceImpl = ApplicationContextUtils.getBean("dmpOutputTaskRecordServiceImpl", ServiceImpl.class);
        List<DmpOutputTaskRecordEntity> dmpOutputTaskRecordEntityList = (List<DmpOutputTaskRecordEntity>) serviceImpl.list(queryWrapper);
        if (CollUtil.isNotEmpty(dmpOutputTaskRecordEntityList)) {
        	List<DmpOutputTaskRecordMergeEntity> mergeList = dmpOutputTaskRecordMergeService.lambdaQuery()
	        	.in(DmpOutputTaskRecordMergeEntity::getMergeId, dmpOutputTaskRecordEntityList.stream().map(DmpOutputTaskRecordEntity::getId).collect(Collectors.toList()))
	        	.list();
        	if(CollUtil.isNotEmpty(mergeList)) {
        		Set<String> mergeIdSet = mergeList.stream().map(DmpOutputTaskRecordMergeEntity::getMergeId).collect(Collectors.toSet());
        		mergeIdSet.forEach(m -> log.warn(m + "合并数据不允许查询同步"));
        		dmpOutputTaskRecordEntityList.removeIf(d -> mergeIdSet.contains(d.getId()));
        	}

            Map<String, String> cfgOutputIdEntityMaps = dmpOutputTaskService.lambdaQuery()
                    .in(DmpOutputTaskEntity::getId, dmpOutputTaskRecordEntityList.stream().map(DmpOutputTaskRecordEntity::getMainId).collect(Collectors.toSet()))
                    .select(DmpOutputTaskEntity::getId, DmpOutputTaskEntity::getCfgOutputId)
                    .list().stream().collect(Collectors.toMap(DmpOutputTaskEntity::getId, DmpOutputTaskEntity::getCfgOutputId));

            Map<String, DmpCfgOutputEntity> outputIdEntityMaps = dmpCfgOutputService.lambdaQuery().in(DmpCfgOutputEntity::getId, cfgOutputIdEntityMaps.values())
                    .list().stream().collect(Collectors.toMap(DmpCfgOutputEntity::getId, d -> d));

            Map<String, List<DmpOutputTaskRecordEntity>> cfgOutputRecordEntityListMaps = new HashMap<>();
            for (DmpOutputTaskRecordEntity dmpOutputTaskRecordEntity : dmpOutputTaskRecordEntityList) {
                String cfgOutputId = cfgOutputIdEntityMaps.get(dmpOutputTaskRecordEntity.getMainId());
                List<DmpOutputTaskRecordEntity> list = cfgOutputRecordEntityListMaps.get(cfgOutputId);
                if (CollUtil.isEmpty(list)) {
                    list = new ArrayList<>();
                }
                list.add(dmpOutputTaskRecordEntity);
                cfgOutputRecordEntityListMaps.put(cfgOutputId, list);
            }

            for (Map.Entry<String, List<DmpOutputTaskRecordEntity>> cfgOutputRecordEntityListMap : cfgOutputRecordEntityListMaps.entrySet()) {
                String cfgOutputId = cfgOutputRecordEntityListMap.getKey();
                DmpCfgOutputEntity dmpCfgOutputEntity = outputIdEntityMaps.get(cfgOutputId);
                String inputConvertId = dmpCfgOutputEntity.getInputConvertId();
                String cfgInputId = dmpHandlerCache.getDmpCfgInputConvertEntityList(d -> d.getId().equals(inputConvertId)).get(0).getMainId();
                DmpCfgInputEntity dmpCfgInputEntity = dmpHandlerCache.getDmpCfgInputEntityList(d -> d.getId().equals(cfgInputId)).get(0);
                String systemId = dmpCfgInputEntity.getSystemId();
                String systemCode = dmpHandlerCache.getDmpBasicSystemEntityList(d -> d.getId().equals(systemId)).get(0).getCode();

                String propertie = "id";
                boolean erpFlag = true;
                String extendJson = dmpCfgOutputEntity.getExtendJson();
                if(StringUtils.isNotBlank(extendJson)) {
                	JSONObject parseObject = JSON.parseObject(extendJson);
                	String cfgPropertie = parseObject.getString("propertie");
                	if(StringUtils.isNotBlank(cfgPropertie)) {
                		propertie = cfgPropertie;
                	}
                	Boolean erpFlagValue = parseObject.getBoolean("erpFlag");
                	if(erpFlagValue != null) {
                		erpFlag = erpFlagValue;
                	}
                }
                List<DmpOutputTaskRecordEntity> list = cfgOutputRecordEntityListMap.getValue();
                if (erpFlag && DmpBasicSystemCodeEnum.ERP.getCode().equals(systemCode)) {
                    List<DmpOutputTaskRecordEntity> erpQuerySync = dmpOutputTaskRecordService.erpQuerySync(dmpCfgOutputEntity, list);
                    if (CollUtil.isNotEmpty(erpQuerySync)) {
                    	dmpOutputTaskRecordMergeService.querySyncMergeDeal(dmpCfgOutputEntity , dmpOutputTaskRecordService.listByIds(erpQuerySync.stream().map(DmpOutputTaskRecordEntity::getId).collect(Collectors.toList())));
                    }
                } else {
                    List<String> dataIds = list.stream().map(DmpOutputTaskRecordEntity::getDataId).collect(Collectors.toList());
                    DmpOutputHotfixCreateRequest dmpOutputHotfixCreateRequest = new DmpOutputHotfixCreateRequest();
                    dmpOutputHotfixCreateRequest.setCfgOutputId(cfgOutputId);
                    dmpOutputHotfixCreateRequest.setQueryParams(Arrays.asList(new QueryParam(QueryTypeEnum.IN, propertie, dataIds)));
                    try {
                        Map<String, String> queryPushData = dmpOutputCreateFactory.getQueryPushData(dmpOutputHotfixCreateRequest);
                        if (queryPushData != null) {
                            for (Map.Entry<String, String> queryData : queryPushData.entrySet()) {
                                String key = queryData.getKey();
                                List<DmpOutputTaskRecordEntity> queryDataList = list.stream().filter(l -> l.getDataId().equals(key)).collect(Collectors.toList());
                                if (CollUtil.isNotEmpty(queryDataList)) {
                                    JSONObject parseObject = JSON.parseObject(queryData.getValue());
                                    parseObject.put("dmpOutputTaskRecordDataId", key);
                                    for (DmpOutputTaskRecordEntity q : queryDataList) {
                                        parseObject.put("dmpOutputTaskRecordId", q.getId());
                                        q.setRequestData(JSON.toJSONString(parseObject));
                                        q.setStatus(DmpOutputTaskRecordStatusEnum.INIT.getCode());
                                        q.setIsNeedSync(Boolean.TRUE);
                                    }
                                    dmpOutputTaskRecordService.updateBatchById(queryDataList);
                                    dmpOutputTaskRecordMergeService.querySyncMergeDeal(dmpCfgOutputEntity , queryDataList);
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.error("查询同步调用dmp报错", e);
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
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @GetMapping("getWdtInsufficientInventory")
    public ApiResult<Collection<WdtInsufficientInventoryDTO>> getWdtInsufficientInventory() {
    	Collection<WdtInsufficientInventoryDTO> values = null;
    	String redisKey = "dmp:inout:wdt:inventory";
    	if(redisTemplate.opsForValue().setIfAbsent(redisKey, DateUtil.now(), 300, TimeUnit.SECONDS)) {
    		try {
				List<DmpOutputTaskRecordEntity> dmpOutputTaskRecordEntityList = dmpOutputTaskRecordService.lambdaQuery()
				    	.in(DmpOutputTaskRecordEntity::getStatus, Arrays.asList(DmpOutputTaskRecordStatusEnum.ERROR.getCode()))
				    	.last(" and response_data like '旺店通出库消费数据失败%库存不足%' and response_data not like '%虚拟库存不足%' ")
				    	.list();
					Map<String, WdtInsufficientInventoryDTO> map = new TreeMap<>();
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
				                String[] split5 = split4[1].split("交易数:-");
				                String qty = split5[1].replace(" 库存不足：", "").split(",")[0].trim();
				                
				                String key = warehouseName + "_" + position + "_" + skuNo;
				                WdtInsufficientInventoryDTO dto = map.get(key);
				        		if(dto == null) {
				        			dto = new WdtInsufficientInventoryDTO();
				        			dto.setWarehouse(warehouseName);
				        			dto.setPosition(position);
				        			dto.setSku(skuNo);
				        		}
				        		dto.setNum(dto.getNum() + Integer.valueOf(qty));
				        		map.put(key, dto);
				            }
						}
					}
					values = map.values();
					if(CollUtil.isNotEmpty(values)) {
						List<String> warehouseNames = Arrays.asList("东莞塘厦仓" , "奥莱仓");
						for(String warehouseName : warehouseNames) {
							List<WdtInsufficientInventoryDTO> invertoryList = values.stream().filter(v -> v.getWarehouse().equals(warehouseName)).collect(Collectors.toList());
							if(CollUtil.isNotEmpty(invertoryList)) {
								List<WarehouseEntity> list = FeignQuery.create(WarehouseEntity.class).eq(WarehouseEntity::getName, warehouseName).list();
								if(CollUtil.isNotEmpty(list)) {
									WarehouseLocationMoveDTO.PcAddDTO addDto = new WarehouseLocationMoveDTO.PcAddDTO();
									addDto.setBillDate(LocalDate.now());
									List<WarehouseLocationMoveDetailDTO.AddDTO> detailList = new ArrayList<>();
									
									WarehouseEntity warehouseEntity = list.get(0);
									PagingDTO<WarehouseLocationDTO.SelectDTO> searchDTO = new PagingDTO<>();
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
					    					String position = wdtInsufficientInventoryDTO.getPosition();
				    						if("空仓位".equals(position)) {
				    							position = "";
				    						}else {
				    							position = locationNameCodeMap.get(position);
				    						}
				    						String finalPosition = position;
				    						Map<String, Integer> addNumMaps = new HashMap<>();
				    						Map<String, Integer> lastNumMaps = new HashMap<>();
				    						for(WarehouseLocationMoveDetailDTO.AddDTO lastAdd : detailList) {
				    							if(sku.equals(lastAdd.getSkuNo())) {
				    								String lastKey = lastAdd.getOutWarehouseLocation();
													Integer lastNum = lastNumMaps.get(lastKey);
					    							if(lastNum == null) {
					    								lastNum = 0;
					    							}
					    							lastNum = lastNum + lastAdd.getQty();
					    							lastNumMaps.put(lastKey, lastNum);
				    							}
				    						}
				    						
				    						locationList.forEach(l -> {
				    							Integer usableQty = l.getUsableQty();
				    							Integer lastQty = lastNumMaps.get(l.getCode());
				    							if(lastQty == null) {
				    								lastQty = 0;
				    							}
				    							l.setUsableQty(usableQty - lastQty);
				    						});
				    						
				    						locationList.removeIf(l -> l.getUsableQty() <= 0);
				    						List<Predicate<? super WarehouseLocationDTO.LocationListDTO>> predicateList = new ArrayList<>();
											predicateList.add(l -> l.getCode().equals(finalPosition));
											predicateList.add(l -> l.getCode().startsWith("3"));
											predicateList.add(l -> l.getCode().startsWith("2"));
											predicateList.add(l -> l.getCode().startsWith("4"));
											predicateList.add(l -> l.getCode().equals(""));
				    						for(Predicate<? super WarehouseLocationDTO.LocationListDTO> predicate : predicateList) {
				    							num = this.addWdtMoveNum(num, addNumMaps, locationList, predicate);
				    						}
				    						
					    					if(num <= 0 && CollUtil.isNotEmpty(addNumMaps)) {
					    						for(Map.Entry<String, Integer> addNumMap : addNumMaps.entrySet()) {
					    							WarehouseLocationMoveDetailDTO.AddDTO detailAddDto = new WarehouseLocationMoveDetailDTO.AddDTO();
						    						detailAddDto.setSkuId(skuIdNoMap.get(sku));
						    						detailAddDto.setSkuNo(sku);
						    						detailAddDto.setQty(addNumMap.getValue());
						    						detailAddDto.setOutWarehouseLocation(addNumMap.getKey());
													detailAddDto.setInWarehouseLocation(position);
						    						detailAddDto.setOutInventoryStatus("usable");
						    						detailAddDto.setInInventoryStatus("usable");
						    						detailAddDto.setWarehouseId(warehouseId);
						    						detailAddDto.setRemark("旺店通同步销售出库单库存不足自动仓位移动");
						    						detailList.add(detailAddDto);
					    						}
					    					}
					    				}
									}
									if(CollUtil.isNotEmpty(detailList)) {
										addDto.setDetailList(detailList);
										detailList.removeIf(d -> d.getInWarehouseLocation().equals(d.getOutWarehouseLocation()) || d.getQty() <= 0);
										if(CollUtil.isNotEmpty(detailList)) {
											FeignQuery.invoke("com.erp.server.wms.service.impl.WarehouseLocationMoveServiceImpl", "wdtAutoAdd", Arrays.asList(addDto));
											List<DmpOutputTaskRecordEntity> dealDmpOutputTaskRecordEntityList = dmpOutputTaskRecordService.lambdaQuery()
						    					.eq(DmpOutputTaskRecordEntity::getStatus, DmpOutputTaskRecordStatusEnum.ERROR.getCode())
						    			    	.last(" and response_data like '旺店通出库消费数据失败%库存不足%" + warehouseName + "%'")
						    			    	.list();
											dealDmpOutputTaskRecordEntityList.forEach(d -> d.setStatus(DmpOutputTaskRecordStatusEnum.INIT.getCode()));
											dmpOutputTaskRecordService.updateBatchById(dealDmpOutputTaskRecordEntityList);
											dmpOutputTaskRecordService.batchSync(dealDmpOutputTaskRecordEntityList);
										}
									}
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

    private Integer addWdtMoveNum(Integer currNum , Map<String, Integer> addNumMaps , List<WarehouseLocationDTO.LocationListDTO> locationList , Predicate<? super WarehouseLocationDTO.LocationListDTO> paramPredicate) {
    	if(currNum > 0) {
    		List<LocationListDTO> currDtoList = locationList.stream().filter(paramPredicate).filter(l -> !addNumMaps.containsKey(l.getCode())).sorted((l1 , l2) -> l2.getUsableQty().compareTo(l1.getUsableQty())).collect(Collectors.toList());
        	if(CollUtil.isNotEmpty(currDtoList)) {
        		for(LocationListDTO dto : currDtoList) {
        			String code = dto.getCode();
            		Integer usableQty = dto.getUsableQty();
            		if(usableQty >= currNum) {
            			addNumMaps.put(code, currNum);
            			return 0;
            		}else {
            			addNumMaps.put(code, usableQty);
            			currNum = currNum - usableQty;
            		}
            	}
        	}
    	}
    	return currNum;
    }
    
    @PostMapping("querySyncSdy")
    public ApiResult<?> querySyncSdy(@RequestBody SdyPushDTO dto) {
    	LocalDateTime startTime = dto.getStartTime();
    	LocalDateTime endTime = dto.getEndTime();
    	if(startTime == null) {
    		throw new ServiceException("startTime不能为空");
    	}
    	if(endTime == null) {
    		throw new ServiceException("endTime不能为空");
    	}

    	Map<String, Map<String, String>> bizTypeSourceSystemMaps = new HashMap<>();

    	Map<String, String> sourceSystemMaps = new HashMap<>();
//    	sourceSystemMaps.put("1859426032948370202", DmpBasicSystemCodeEnum.MERCADOLIBRE.getCode());
//    	sourceSystemMaps.put("1859424811332164370", DmpBasicSystemCodeEnum.SHOPEE.getCode());
    	sourceSystemMaps.put("1859425168586201876", DmpBasicSystemCodeEnum.SHOPIFY.getCode());
//    	sourceSystemMaps.put("1859425336446442262", DmpBasicSystemCodeEnum.TIKTOK.getCode());
    	sourceSystemMaps.put("1861317267527064372", DmpBasicSystemCodeEnum.WDT.getCode());
    	sourceSystemMaps.put("1859426447974751005", DmpBasicSystemCodeEnum.ALI_EXPRESS.getCode());
    	sourceSystemMaps.put("1859425468411829017", DmpBasicSystemCodeEnum.AMAZON.getCode());
    	bizTypeSourceSystemMaps.put("soInfo", sourceSystemMaps);

    	sourceSystemMaps = new HashMap<>();
//    	sourceSystemMaps.put("1858832992047225575", DmpBasicSystemCodeEnum.MERCADOLIBRE.getCode());
//    	sourceSystemMaps.put("1859424811332164370", DmpBasicSystemCodeEnum.SHOPEE.getCode());
    	sourceSystemMaps.put("1858832584453151459", DmpBasicSystemCodeEnum.SHOPIFY.getCode());
//    	sourceSystemMaps.put("1858832825793403621", DmpBasicSystemCodeEnum.TIKTOK.getCode());
    	sourceSystemMaps.put("1858830998851050201", "WDT");
    	sourceSystemMaps.put("1858832384133192417", DmpBasicSystemCodeEnum.ALI_EXPRESS.getCode());
    	sourceSystemMaps.put("1858832015038634719", DmpBasicSystemCodeEnum.AMAZON.getCode());
    	bizTypeSourceSystemMaps.put("soReturn", sourceSystemMaps);

    	sourceSystemMaps = new HashMap<>();
//    	sourceSystemMaps.put("1858834023460133611", DmpBasicSystemCodeEnum.WDT.getCode());
    	sourceSystemMaps.put("1858834187159624429", DmpBasicSystemCodeEnum.SHOPIFY.getCode());
    	bizTypeSourceSystemMaps.put("soRefund", sourceSystemMaps);

    	sourceSystemMaps = new HashMap<>();
    	sourceSystemMaps.put("1859427581292469023", DmpBasicSystemCodeEnum.WDT.getCode());
    	bizTypeSourceSystemMaps.put("soDeliver", sourceSystemMaps);
    	
    	sourceSystemMaps = new HashMap<>();
    	sourceSystemMaps.put("1801575292597545159", "DmpOutputSdySoDeliveryHandler");
    	sourceSystemMaps.put("1801575292597545160", "DmpOutputSdySoDeliverySelfAddHandler");
    	sourceSystemMaps.put("1858458760101669422", "DmpOutputSdySoOutstockHandler");
    	sourceSystemMaps.put("1858459889228088846", "DmpOutputSdyLogisticsHandler");
    	sourceSystemMaps.put("1859050822961226666", "DmpOutputSdyReturnInstockHandler");
    	bizTypeSourceSystemMaps.put("erp", sourceSystemMaps);

    	Set<String> cfgOutputIds = dto.getCfgOutputIds();
    	if(CollUtil.isEmpty(cfgOutputIds)) {
    		cfgOutputIds = new HashSet<>();
    		String bizType = dto.getBizType();
    		if(StringUtils.isNotBlank(bizType)) {
    			cfgOutputIds = bizTypeSourceSystemMaps.get(bizType).keySet();
    		}else {
    			if(dto.isAllFlag()) {
    				for(Map.Entry<String, Map<String, String>> bizTypeSourceSystemMap : bizTypeSourceSystemMaps.entrySet()) {
    					cfgOutputIds.addAll(bizTypeSourceSystemMap.getValue().keySet());
    				}
    			}
    		}
    	}

    	for(Map.Entry<String, Map<String, String>> bizTypeSourceSystemMap : bizTypeSourceSystemMaps.entrySet()) {
    		String key = bizTypeSourceSystemMap.getKey();
    		Map<String, String> value = bizTypeSourceSystemMap.getValue();
    		for(Map.Entry<String, String> v : value.entrySet()) {
    			String cfgOutputId = v.getKey();
    			String sourceSystem = v.getValue();
				if(cfgOutputIds.contains(cfgOutputId)) {
					dmpSdyOutputPushExecutorPool.execute(() -> {
						if(key.equals("soInfo") || key.equals("soDeliver")) {
	    					sdySoInfo(cfgOutputId, sourceSystem, startTime, endTime);
	    				}else if(key.equals("soReturn")) {
	    					sdyReturnInfo(cfgOutputId, sourceSystem, startTime, endTime);
	    				}else if(key.equals("soRefund")) {
	    					sdyRefundInfo(cfgOutputId, sourceSystem, startTime, endTime);
	    				}else if(key.equals("erp")) {
	    					sdyErpInfo(cfgOutputId, sourceSystem, startTime, endTime);
	    				}
					});
    			}
    		}
		}

        return success();
    }

    private void sdySoInfo(String cfgOutputId , String sourceSystem ,LocalDateTime startTime , LocalDateTime endTime) {
    	log.warn("开始重推数帝云线上订单，系统：" + sourceSystem);
    	try {
			DmpOutputHotfixCreateRequest dmpOutputHotfixCreateRequest = new DmpOutputHotfixCreateRequest();
			dmpOutputHotfixCreateRequest.setRetryPush(true);
			dmpOutputHotfixCreateRequest.setCfgOutputId(cfgOutputId);
			List<QueryParam> queryParams = new ArrayList<>();
			queryParams.add(new QueryParam(QueryTypeEnum.EQ, "source_system", sourceSystem));
			if("1861317267527064372".equals(cfgOutputId)) {
				queryParams.add(new QueryParam(QueryTypeEnum.IN, "pay_status", Arrays.asList("1" , "2")));
			}else {
				queryParams.add(new QueryParam(QueryTypeEnum.EQ, "pay_status", true));
				queryParams.add(new QueryParam(QueryTypeEnum.EQ, "invalid_status", false));
			}
			queryParams.add(new QueryParam(QueryTypeEnum.GE, "pay_time", startTime));
			queryParams.add(new QueryParam(QueryTypeEnum.LT, "pay_time", endTime));
			dmpOutputHotfixCreateRequest.setQueryParams(queryParams);
			dmpOutputCreateFactory.doHotfixOutputTask(dmpOutputHotfixCreateRequest);
			log.warn("完成重推数帝云有时间的线上订单，系统：" + sourceSystem);

			if(!"1861317267527064372".equals(cfgOutputId)) {
				queryParams = new ArrayList<>();
				queryParams.add(new QueryParam(QueryTypeEnum.EQ, "source_system", sourceSystem));
				queryParams.add(new QueryParam(QueryTypeEnum.EQ, "pay_status", true));
				queryParams.add(new QueryParam(QueryTypeEnum.EQ, "invalid_status", false));
				queryParams.add(new QueryParam(QueryTypeEnum.IS_NULL, "pay_time"));
				queryParams.add(new QueryParam(QueryTypeEnum.GE, "platform_create_time", startTime));
				queryParams.add(new QueryParam(QueryTypeEnum.LT, "platform_create_time", endTime));
				dmpOutputHotfixCreateRequest.setQueryParams(queryParams);
				dmpOutputCreateFactory.doHotfixOutputTask(dmpOutputHotfixCreateRequest);
			}
		} catch (Exception e) {
			log.error("失败重推数帝云没有时间的线上订单，系统：" + sourceSystem , e);
			throw e;
		}
    	log.warn("完成重推数帝云没有时间的线上订单，系统：" + sourceSystem);
    }

    private void sdyReturnInfo(String cfgOutputId , String sourceSystem ,LocalDateTime startTime , LocalDateTime endTime) {
    	log.warn("开始重推数帝云退货单，系统：" + sourceSystem);
    	try {
			DmpOutputHotfixCreateRequest dmpOutputHotfixCreateRequest = new DmpOutputHotfixCreateRequest();
			dmpOutputHotfixCreateRequest.setRetryPush(true);
			dmpOutputHotfixCreateRequest.setCfgOutputId(cfgOutputId);
			List<QueryParam> queryParams = new ArrayList<>();
			queryParams.add(new QueryParam(QueryTypeEnum.EQ, "source_system", sourceSystem));
			queryParams.add(new QueryParam(QueryTypeEnum.GE, "return_time", startTime));
			queryParams.add(new QueryParam(QueryTypeEnum.LT, "return_time", endTime));
			dmpOutputHotfixCreateRequest.setQueryParams(queryParams);
			dmpOutputCreateFactory.doHotfixOutputTask(dmpOutputHotfixCreateRequest);
			
			if("1858830998851050201".equals(cfgOutputId)) {
				queryParams = new ArrayList<>();
				queryParams.add(new QueryParam(QueryTypeEnum.EQ, "source_system", sourceSystem));
				queryParams.add(new QueryParam(QueryTypeEnum.IS_NULL, "return_time"));
				queryParams.add(new QueryParam(QueryTypeEnum.GE, "platform_create_time", startTime));
				queryParams.add(new QueryParam(QueryTypeEnum.LT, "platform_create_time", endTime));
				dmpOutputHotfixCreateRequest.setQueryParams(queryParams);
				dmpOutputCreateFactory.doHotfixOutputTask(dmpOutputHotfixCreateRequest);
			}
		} catch (Exception e) {
			log.warn("失败重推数帝云有时间的退货单，系统：" + sourceSystem);
			throw e;
		}
    	log.warn("完成重推数帝云有时间的退货单，系统：" + sourceSystem);
    }

    private void sdyRefundInfo(String cfgOutputId , String sourceSystem ,LocalDateTime startTime , LocalDateTime endTime) {
    	log.warn("开始重推数帝云退款单，系统：" + sourceSystem);
    	try {
			DmpOutputHotfixCreateRequest dmpOutputHotfixCreateRequest = new DmpOutputHotfixCreateRequest();
			dmpOutputHotfixCreateRequest.setRetryPush(true);
			dmpOutputHotfixCreateRequest.setCfgOutputId(cfgOutputId);
			List<QueryParam> queryParams = new ArrayList<>();
			queryParams.add(new QueryParam(QueryTypeEnum.EQ, "source_system", sourceSystem));
			queryParams.add(new QueryParam(QueryTypeEnum.GE, "refund_time", startTime));
			queryParams.add(new QueryParam(QueryTypeEnum.LT, "refund_time", endTime));
			dmpOutputHotfixCreateRequest.setQueryParams(queryParams);
			dmpOutputCreateFactory.doHotfixOutputTask(dmpOutputHotfixCreateRequest);
		} catch (Exception e) {
			log.warn("失败重推数帝云有时间的退款单，系统：" + sourceSystem);
			throw e;
		}
    	log.warn("完成重推数帝云有时间的退款单，系统：" + sourceSystem);
    }
    
    private void sdyErpInfo(String cfgOutputId , String sourceSystem ,LocalDateTime startTime , LocalDateTime endTime) {
    	if("1801575292597545159".equals(cfgOutputId) || "1801575292597545160".equals(cfgOutputId)) {
    		log.warn("开始重推数帝云erp配货单，系统：" + sourceSystem);
        	try {
    			DmpOutputHotfixCreateRequest dmpOutputHotfixCreateRequest = new DmpOutputHotfixCreateRequest();
    			dmpOutputHotfixCreateRequest.setRetryPush(true);
    			dmpOutputHotfixCreateRequest.setCfgOutputId(cfgOutputId);
    			List<QueryParam> queryParams = new ArrayList<>();
    			queryParams.add(new QueryParam(QueryTypeEnum.EQ, "source_system", "erp"));
    			queryParams.add(new QueryParam(QueryTypeEnum.GE, "pay_time", startTime));
    			queryParams.add(new QueryParam(QueryTypeEnum.LT, "pay_time", endTime));
    			dmpOutputHotfixCreateRequest.setQueryParams(queryParams);
    			dmpOutputCreateFactory.doHotfixOutputTask(dmpOutputHotfixCreateRequest);
    		} catch (Exception e) {
    			log.warn("失败重推数帝云erp配货单，系统：" + sourceSystem);
    			throw e;
    		}
        	try {
    			DmpOutputHotfixCreateRequest dmpOutputHotfixCreateRequest = new DmpOutputHotfixCreateRequest();
    			dmpOutputHotfixCreateRequest.setRetryPush(true);
    			dmpOutputHotfixCreateRequest.setCfgOutputId(cfgOutputId);
    			List<QueryParam> queryParams = new ArrayList<>();
    			queryParams.add(new QueryParam(QueryTypeEnum.EQ, "source_system", "erp"));
    			queryParams.add(new QueryParam(QueryTypeEnum.IS_NULL, "pay_time"));
    			queryParams.add(new QueryParam(QueryTypeEnum.GE, "third_create_time", startTime));
    			queryParams.add(new QueryParam(QueryTypeEnum.LT, "third_create_time", endTime));
    			dmpOutputHotfixCreateRequest.setQueryParams(queryParams);
    			dmpOutputCreateFactory.doHotfixOutputTask(dmpOutputHotfixCreateRequest);
    		} catch (Exception e) {
    			log.warn("失败重推数帝云erp配货单，系统：" + sourceSystem);
    			throw e;
    		}
        	log.warn("完成重推数帝云erp配货单，系统：" + sourceSystem);
    	}else if("1858458760101669422".equals(cfgOutputId)) {
    		log.warn("开始重推数帝云erp出库单，系统：" + sourceSystem);
        	try {
    			DmpOutputHotfixCreateRequest dmpOutputHotfixCreateRequest = new DmpOutputHotfixCreateRequest();
    			dmpOutputHotfixCreateRequest.setRetryPush(true);
    			dmpOutputHotfixCreateRequest.setCfgOutputId(cfgOutputId);
    			List<QueryParam> queryParams = new ArrayList<>();
    			queryParams.add(new QueryParam(QueryTypeEnum.EQ, "source_system", "erp"));
    			queryParams.add(new QueryParam(QueryTypeEnum.GE, "bill_date", startTime));
    			queryParams.add(new QueryParam(QueryTypeEnum.LT, "bill_date", endTime));
    			dmpOutputHotfixCreateRequest.setQueryParams(queryParams);
    			dmpOutputCreateFactory.doHotfixOutputTask(dmpOutputHotfixCreateRequest);
    		} catch (Exception e) {
    			log.warn("失败重推数帝云erp出库单，系统：" + sourceSystem);
    			throw e;
    		}
        	log.warn("完成重推数帝云erp出库单，系统：" + sourceSystem);
    	}else if("1858459889228088846".equals(cfgOutputId)) {
    		log.warn("开始重推数帝云erp物流单，系统：" + sourceSystem);
        	try {
    			DmpOutputHotfixCreateRequest dmpOutputHotfixCreateRequest = new DmpOutputHotfixCreateRequest();
    			dmpOutputHotfixCreateRequest.setRetryPush(true);
    			dmpOutputHotfixCreateRequest.setCfgOutputId(cfgOutputId);
    			List<QueryParam> queryParams = new ArrayList<>();
    			queryParams.add(new QueryParam(QueryTypeEnum.EQ, "source_system", "erp"));
    			queryParams.add(new QueryParam(QueryTypeEnum.GE, "delivery_time", startTime));
    			queryParams.add(new QueryParam(QueryTypeEnum.LT, "delivery_time", endTime));
    			dmpOutputHotfixCreateRequest.setQueryParams(queryParams);
    			dmpOutputCreateFactory.doHotfixOutputTask(dmpOutputHotfixCreateRequest);
    		} catch (Exception e) {
    			log.warn("失败重推数帝云erp物流单，系统：" + sourceSystem);
    			throw e;
    		}
        	log.warn("完成重推数帝云erp物流单，系统：" + sourceSystem);
    	}else if("1859050822961226666".equals(cfgOutputId)) {
    		log.warn("开始重推数帝云erp退货入库单，系统：" + sourceSystem);
        	try {
    			DmpOutputHotfixCreateRequest dmpOutputHotfixCreateRequest = new DmpOutputHotfixCreateRequest();
    			dmpOutputHotfixCreateRequest.setRetryPush(true);
    			dmpOutputHotfixCreateRequest.setCfgOutputId(cfgOutputId);
    			List<QueryParam> queryParams = new ArrayList<>();
    			queryParams.add(new QueryParam(QueryTypeEnum.EQ, "source_system", "erp"));
    			queryParams.add(new QueryParam(QueryTypeEnum.GE, "return_instock_time", startTime));
    			queryParams.add(new QueryParam(QueryTypeEnum.LT, "return_instock_time", endTime));
    			dmpOutputHotfixCreateRequest.setQueryParams(queryParams);
    			dmpOutputCreateFactory.doHotfixOutputTask(dmpOutputHotfixCreateRequest);
    		} catch (Exception e) {
    			log.warn("失败重推数帝云erp退货入库单，系统：" + sourceSystem);
    			throw e;
    		}
        	log.warn("完成重推数帝云erp退货入库单，系统：" + sourceSystem);
    	}
    }


	@PostMapping("doInputNormalTask")
	public void doInputTask(@RequestBody String jobParam) {
		String size = "1000";
		List<String> cfgInputIds = null;
		List<String> ids = null;
		DmpInputTaskTaskTypeEnum dmpInputTaskTaskTypeEnum = DmpInputTaskTaskTypeEnum.NORMAL;
		List<String> execStatusList = Arrays.asList(DmpInputTaskStatusEnum.INIT.getCode()
				, DmpInputTaskStatusEnum.FDS.getCode() , DmpInputTaskStatusEnum.MONGO.getCode()
				, DmpInputTaskStatusEnum.DMP.getCode());
		if(StringUtils.isNotBlank(jobParam)) {
			JSONObject parseObject = JSON.parseObject(jobParam);
			String sizeParam = parseObject.getString("size");
			if(StringUtils.isNotBlank(sizeParam)) {
				size = sizeParam;
			}
			String mainIdsParam = parseObject.getString("cfgInputIds");
			if(StringUtils.isNotBlank(mainIdsParam)) {
				cfgInputIds = Arrays.asList(mainIdsParam.split(","));
			}
			String idsParam = parseObject.getString("ids");
			if(StringUtils.isNotBlank(idsParam)) {
				ids = Arrays.asList(idsParam.split(","));
				if(CollUtil.isNotEmpty(ids)) {
					List<DmpInputTaskEntity> errorList = dmpInputTaskService.lambdaQuery()
							.in(DmpInputTaskEntity::getId, ids)
							.eq(DmpInputTaskEntity::getTaskType, dmpInputTaskTaskTypeEnum.getCode())
							.eq(DmpInputTaskEntity::getStatus, DmpInputTaskStatusEnum.ERROR.getCode())
							.list();
					List<DmpInputTaskEntity> updateList = new ArrayList<>();
					if(CollUtil.isNotEmpty(errorList)) {
						for(DmpInputTaskEntity e : errorList) {
							String errorMessage = e.getErrorMessage();
							if(StringUtils.isNotBlank(errorMessage)) {
								String[] split = errorMessage.split("@@");
								if(split.length > 1) {
									String status = split[0];
									if(execStatusList.contains(status)) {
										e.setErrorCount(0);
										e.setStatus(status);
										updateList.add(e);
									}
								}
							}
						}
					}
					if(CollUtil.isNotEmpty(updateList)) {
						dmpInputTaskService.updateBatchById(updateList);
					}
				}
			}
		}
		List<DmpInputTaskEntity> list = dmpInputTaskService.lambdaQuery()
				.in(DmpInputTaskEntity::getStatus, execStatusList)
				.in(CollUtil.isNotEmpty(ids) ,DmpInputTaskEntity::getId, ids)
				.in(CollUtil.isNotEmpty(cfgInputIds) ,DmpInputTaskEntity::getCfgInputId, cfgInputIds)
				.eq(DmpInputTaskEntity::getTaskType, dmpInputTaskTaskTypeEnum.getCode())
				.select(DmpInputTaskEntity::getId , DmpInputTaskEntity::getCfgInputId , DmpInputTaskEntity::getNextLevelId , DmpInputTaskEntity::getExecTimeout)
				.orderByAsc(DmpInputTaskEntity::getUpdateTime)
				.last(dmpInputTaskTaskTypeEnum != DmpInputTaskTaskTypeEnum.COMPENSATE , " limit " + size)
				.list();
		if(dmpInputTaskTaskTypeEnum == DmpInputTaskTaskTypeEnum.COMPENSATE) {
			Map<String, List<DmpInputTaskEntity>> cfgInputNextLevelMaps = list.stream().collect(Collectors.groupingBy(l -> l.getCfgInputId() + "_" + l.getNextLevelId()));
			list = new ArrayList<>();
			for(Map.Entry<String, List<DmpInputTaskEntity>> cfgInputNextLevelMap : cfgInputNextLevelMaps.entrySet()) {
				list.add(cfgInputNextLevelMap.getValue().get(0));
			}
		}

		for(DmpInputTaskEntity l : list) {
			dmpInputExecutorPool.execute(() -> {
				DmpInputFinishRequest dmpInputFinishRequest = new DmpInputFinishRequest();
				dmpInputFinishRequest.setInputTaskId(l.getId());
				dmpInputFinishRequest.setExecTimeout(l.getExecTimeout());
				dmpInputTaskFactory.dealInputTask(dmpInputFinishRequest);
			});
		}
	}

}
