package com.erp.server.dmp.inout.handler.output.task;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.utils.ApplicationContextUtils;
import com.common.core.entity.BaseEntity;
import com.common.core.exception.ServiceException;
import com.common.core.utils.StrUtils;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpCfgOutputBlackEntity;
import com.erp.model.dmp.entity.DmpCfgOutputEntity;
import com.erp.model.dmp.entity.DmpOutputTaskEntity;
import com.erp.model.dmp.entity.DmpOutputTaskRecordEntity;
import com.erp.model.dmp.enums.DmpCfgOutputBlackCompareSignEnum;
import com.erp.model.dmp.enums.DmpCfgOutputBlackDataTypeEnum;
import com.erp.model.dmp.enums.DmpOutputTaskStatusEnum;
import com.erp.server.dmp.inout.dto.request.DmpOutputRequest;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputResponse;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.erp.server.dmp.inout.handler.chain.DmpHandlerChain;
import com.erp.server.dmp.inout.handler.output.DmpOutputHandler;
import com.erp.server.dmp.inout.utils.DmpHandlerCache;
import com.erp.server.dmp.inout.utils.DmpOutputUtils;
import com.erp.server.dmp.service.DmpOutputTaskRecordService;
import com.erp.server.dmp.service.DmpOutputTaskService;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;

@Service
@Scope("prototype")
@Slf4j
public abstract class DmpOutputTaskHandler extends DmpOutputHandler{
	@Autowired
	protected DmpOutputTaskService dmpOutputTaskService;
	@Autowired
	protected DmpHandlerCache dmpHandlerCache;
	@Autowired
	protected DmpOutputTaskRecordService dmpOutputTaskRecordService;
	@Autowired
	@Qualifier("dmpOutputExecutorPool")
	protected ExecutorService dmpOutputExecutorPool;
	@Resource
	protected RedisTemplate<String,Object> redisTemplate;
	@Autowired
	protected DmpOutputUtils dmpOutputUtils;
	@Autowired
	protected IdentifierGenerator identifierGenerator;
	
	protected boolean isNotValidate = false;
	
	@Override
	public void doDmpHandler(DmpOutputRequest dmpRequest, DmpOutputResponse dmpResponse, DmpHandlerChain chain) {
		if (!(dmpRequest instanceof DmpOutputTaskRequest)) {
            throw new ServiceException(dmpRequest + " not DmpOutputTaskRequest");
        }
        if (!(dmpResponse instanceof DmpOutputTaskResponse)) {
            throw new ServiceException(dmpResponse + " not DmpOutputTaskResponse");
        }
        doDmpHandler((DmpOutputTaskRequest) dmpRequest, (DmpOutputTaskResponse) dmpResponse, chain);
	}
	
	protected void doDmpHandler(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse, DmpHandlerChain chain) {
		isNotValidate = dmpRequest.isNotValidate();
		DmpCfgOutputEntity dmpCfgOutputEntity = dmpResponse.getDmpCfgOutputEntity();
		List<DmpCfgInputConvertEntity> dmpCfgInputConvertEntityList = dmpHandlerCache.getDmpCfgInputConvertEntityList(d -> d.getId().equals(dmpCfgOutputEntity.getInputConvertId()));
		if(CollUtil.isNotEmpty(dmpCfgInputConvertEntityList)) {
			dmpResponse.setDmpCfgInputConvertEntity(dmpCfgInputConvertEntityList.get(0));
		}
		List<DmpOutputTaskRecordEntity> outputData = this.outputData(dmpRequest, dmpResponse);
		dmpResponse.setOutputData(outputData);
		if(CollUtil.isNotEmpty(outputData)) {
			dmpOutputTaskRecordService.saveBatch(outputData);
		}
		DmpOutputTaskHandler bean = ApplicationContextUtils.getBean(this.getClass());
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
		    @Override
		    public void afterCommit() {
		    	bean.dealDmpOutputTaskRecordEntityList(dmpCfgOutputEntity , outputData);
		    }
		});
		dmpOutputTaskService.lambdaUpdate()
				.eq(DmpOutputTaskEntity::getId, dmpRequest.getOutputTaskId())
				.set(DmpOutputTaskEntity::getStatus, DmpOutputTaskStatusEnum.FINISH.getCode())
				.set(DmpOutputTaskEntity::getUpdateTime, LocalDateTime.now())
				.update();
		chain.doDmpHandler(dmpRequest, dmpResponse);
	}
	
	protected abstract List<DmpOutputTaskRecordEntity> outputData(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse);
	
	public void dealDmpOutputTaskRecordEntityList(DmpCfgOutputEntity dmpCfgOutputEntity , List<DmpOutputTaskRecordEntity> dmpOutputTaskRecordEntityList) {
    	if(CollUtil.isEmpty(dmpOutputTaskRecordEntityList)) {
    		return;
    	}
		
		List<DmpOutputTaskRecordEntity> pushDmpOutputTaskRecordEntityList = new ArrayList<>();
		for(DmpOutputTaskRecordEntity dmpOutputTaskRecordEntity : dmpOutputTaskRecordEntityList) {
			String dataId = dmpOutputTaskRecordEntity.getDataId();
			String redisKey = "dmp:output:task:" + dataId;
			if(redisTemplate.opsForValue().setIfAbsent(redisKey, DateUtil.now(), 1800, TimeUnit.SECONDS)) {
				pushDmpOutputTaskRecordEntityList.add(dmpOutputTaskRecordEntity);
			}else {
				log.error(redisKey + "任务正在执行中");
			}
		}
		
		if(CollUtil.isEmpty(pushDmpOutputTaskRecordEntityList)) {
    		return;
    	}
		
		pushDmpOutputTaskRecordEntityList.sort((d1 , d2) -> d1.getUpdateTime().compareTo(d2.getUpdateTime()));
		dmpOutputExecutorPool.execute(() -> {
			int i = 0;
	    	Integer pushRate = dmpCfgOutputEntity.getPushRate();
			if(pushRate == null) {
				pushRate = 3;
			}
			for(DmpOutputTaskRecordEntity dmpOutputTaskRecordEntity : pushDmpOutputTaskRecordEntityList) {
				MDC.put("traceId", dmpOutputTaskRecordEntity.getId());
				String dataId = dmpOutputTaskRecordEntity.getDataId();
				String redisKey = "dmp:output:task:" + dataId;
				try {
					this.pushData(dmpCfgOutputEntity, dmpOutputTaskRecordEntity);
				} catch (Exception e) {
					log.error("处理推送数据失败{}" , dmpOutputTaskRecordEntity.getId() , e);
				}finally {
					redisTemplate.delete(redisKey);
				}
	    		
	    		if(pushRate > 0) {
					i = i + 1;
	    			if(i % pushRate == 0) {
	    				try {
	    					Thread.sleep(1000);
	    				} catch (InterruptedException e) {}
	    			}
				}
	    	}
		});
	}
	
	protected abstract void pushData(DmpCfgOutputEntity dmpCfgOutputEntity , DmpOutputTaskRecordEntity dmpOutputTaskRecordEntity);
	
	public void getRetryPushSourceData(List<DmpCfgInputConvertEntity> dmpCfgInputConvertEntityList , DmpOutputTaskRequest dmpOutputTaskRequest) {
		DmpCfgInputConvertEntity dmpCfgInputConvertEntity = dmpCfgInputConvertEntityList.get(0);
		List<String> mainIds = dmpOutputTaskRequest.getConvertInputDmpBaseEntityListMaps().get(dmpCfgInputConvertEntity).stream().map(BaseEntity::getId).collect(Collectors.toList());
		for(int i = 1; i < dmpCfgInputConvertEntityList.size(); i++) {
			DmpCfgInputConvertEntity childDmpCfgInputConvertEntity = dmpCfgInputConvertEntityList.get(i);
			ServiceImpl serviceImpl = ApplicationContextUtils.getBean(StrUtils.underlineToCamel(childDmpCfgInputConvertEntity.getStorageName(), true) + "ServiceImpl" , ServiceImpl.class);
			QueryWrapper<?> wrapper = new QueryWrapper<>();
			wrapper.in("main_id", mainIds);
			List<BaseEntity> childEntityList = serviceImpl.list(wrapper);
			dmpOutputTaskRequest.getConvertInputDmpBaseEntityListMaps().put(childDmpCfgInputConvertEntity, childEntityList);
			dmpOutputTaskRequest.getChangeConvertInputDmpBaseEntityListMaps().put(childDmpCfgInputConvertEntity, childEntityList);
		}
	}
	
	protected boolean validateDataBlack(Object object , String cfgOutputId) {
		if(object != null) {
			List<DmpCfgOutputBlackEntity> dmpCfgOutputBlackEntityList = dmpHandlerCache.getDmpCfgOutputBlackEntityList(d -> d.getMainId().equals(cfgOutputId));
			if(CollUtil.isNotEmpty(dmpCfgOutputBlackEntityList)) {
				JSONObject parseObject = JSON.parseObject(JSON.toJSONString(object));
				for(DmpCfgOutputBlackEntity dmpCfgOutputBlackEntity : dmpCfgOutputBlackEntityList) {
					Object value = parseObject.get(dmpCfgOutputBlackEntity.getFieldName());
					if(this.validate(value, dmpCfgOutputBlackEntity)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	private boolean validate(Object value , DmpCfgOutputBlackEntity dmpCfgOutputBlackEntity) {
		if(isNotValidate) {
			return false;
		}
		String compareSign = dmpCfgOutputBlackEntity.getCompareSign();
		String dataType = dmpCfgOutputBlackEntity.getDataType();
		if(StringUtils.isBlank(compareSign) || StringUtils.isBlank(dataType)) {
			return true;
		}
		if(DmpCfgOutputBlackCompareSignEnum.EQ.getCode().equals(compareSign)
				|| DmpCfgOutputBlackCompareSignEnum.NE.getCode().equals(compareSign)
				|| DmpCfgOutputBlackCompareSignEnum.GT.getCode().equals(compareSign)
				|| DmpCfgOutputBlackCompareSignEnum.GE.getCode().equals(compareSign)
				|| DmpCfgOutputBlackCompareSignEnum.LT.getCode().equals(compareSign)
				|| DmpCfgOutputBlackCompareSignEnum.LE.getCode().equals(compareSign)
				|| DmpCfgOutputBlackCompareSignEnum.LIKE.getCode().equals(compareSign)
				|| DmpCfgOutputBlackCompareSignEnum.NOTLIKE.getCode().equals(compareSign)
				|| DmpCfgOutputBlackCompareSignEnum.IN.getCode().equals(compareSign)
				|| DmpCfgOutputBlackCompareSignEnum.BE.getCode().equals(compareSign)){
			if(value != null) {
				String valueString = value.toString();
				String fieldValue = dmpCfgOutputBlackEntity.getFieldValue();
				if(DmpCfgOutputBlackDataTypeEnum.STRING.getCode().equals(dataType)) {
					if(DmpCfgOutputBlackCompareSignEnum.EQ.getCode().equals(compareSign)) {
						return StringUtils.equals(valueString, fieldValue);
					}else if(DmpCfgOutputBlackCompareSignEnum.NE.getCode().equals(compareSign)) {
						return !StringUtils.equals(valueString, fieldValue);
					}else if(DmpCfgOutputBlackCompareSignEnum.GT.getCode().equals(compareSign)) {
						return valueString.compareTo(fieldValue) > 0;
					}else if(DmpCfgOutputBlackCompareSignEnum.GE.getCode().equals(compareSign)) {
						return valueString.compareTo(fieldValue) >= 0;
					}else if(DmpCfgOutputBlackCompareSignEnum.LT.getCode().equals(compareSign)) {
						return valueString.compareTo(fieldValue) < 0;
					}else if(DmpCfgOutputBlackCompareSignEnum.LE.getCode().equals(compareSign)) {
						return valueString.compareTo(fieldValue) <= 0;
					}else if(DmpCfgOutputBlackCompareSignEnum.LIKE.getCode().equals(compareSign)) {
						return valueString.contains(fieldValue);
					}else if(DmpCfgOutputBlackCompareSignEnum.NOTLIKE.getCode().equals(compareSign)) {
						return !valueString.contains(fieldValue);
					}else if(DmpCfgOutputBlackCompareSignEnum.IN.getCode().equals(compareSign)) {
						String[] fieldValueList = fieldValue.split(",");
						if(fieldValueList.length == 1) {
							fieldValueList = fieldValue.split("，");
						}
						for(String s : fieldValueList) {
							if(StringUtils.equals(valueString, s)) {
								return true;
							}
						}
					}
				}else if(DmpCfgOutputBlackDataTypeEnum.INT.getCode().equals(dataType)) {
					Integer intValue = null;
					try {
						intValue = Integer.valueOf(valueString);
					} catch (NumberFormatException e) {}
					Integer fieldIntValue = null;
					if(StringUtils.isNotBlank(fieldValue)) {
						try {
							fieldIntValue = Integer.valueOf(fieldValue);
						} catch (NumberFormatException e) {}
					}
					if(intValue != null) {
						if(fieldIntValue != null) {
							if(DmpCfgOutputBlackCompareSignEnum.EQ.getCode().equals(compareSign)) {
								return intValue.compareTo(fieldIntValue) == 0;
							}else if(DmpCfgOutputBlackCompareSignEnum.NE.getCode().equals(compareSign)) {
								return intValue.compareTo(fieldIntValue) != 0;
							}else if(DmpCfgOutputBlackCompareSignEnum.GT.getCode().equals(compareSign)) {
								return intValue.compareTo(fieldIntValue) > 0;
							}else if(DmpCfgOutputBlackCompareSignEnum.GE.getCode().equals(compareSign)) {
								return intValue.compareTo(fieldIntValue) >= 0;
							}else if(DmpCfgOutputBlackCompareSignEnum.LT.getCode().equals(compareSign)) {
								return intValue.compareTo(fieldIntValue) < 0;
							}else if(DmpCfgOutputBlackCompareSignEnum.LE.getCode().equals(compareSign)) {
								return intValue.compareTo(fieldIntValue) <= 0;
							}
						}else {
							if(DmpCfgOutputBlackCompareSignEnum.IN.getCode().equals(compareSign)) {
								String[] fieldValueList = fieldValue.split(",");
								if(fieldValueList.length == 1) {
									fieldValueList = fieldValue.split("，");
								}
								for(String s : fieldValueList) {
									if(StringUtils.isNotBlank(s)) {
										fieldIntValue = null;
										try {
											fieldIntValue = Integer.valueOf(s);
										} catch (NumberFormatException e) {}
										if(fieldIntValue != null && (intValue.compareTo(fieldIntValue) == 0)) {
											return true;
										}
									}
								}
							}else if(DmpCfgOutputBlackCompareSignEnum.BE.getCode().equals(compareSign)) {
								String[] fieldValueList = fieldValue.split(",");
								if(fieldValueList.length == 1) {
									fieldValueList = fieldValue.split("，");
								}
								List<Integer> intList = new ArrayList<>();
								for(String s : fieldValueList) {
									if(StringUtils.isNotBlank(s)) {
										Integer parseFieldIntValue = null;
										try {
											parseFieldIntValue = Integer.valueOf(s);
										} catch (NumberFormatException e) {}
										if(parseFieldIntValue != null) {
											intList.add(parseFieldIntValue);
										}
									}
								}
								if(intList.size() > 1) {
									return intValue.compareTo(intList.get(0)) >= 0 && intValue.compareTo(intList.get(1)) <= 0;
								}
							}
						}
					}
				}else if(DmpCfgOutputBlackDataTypeEnum.DATE.getCode().equals(dataType)) {
					Date dateValue = null;
					if(value instanceof Date) {
						dateValue = (Date)value;
					}else if(value instanceof LocalDate) {
						dateValue = Date.from(((LocalDate) value).atStartOfDay(ZoneId.systemDefault()).toInstant());
					}else if(value instanceof LocalDateTime) {
						dateValue = Date.from(((LocalDateTime) value).atZone(ZoneId.systemDefault()).toInstant());
					}else if(value instanceof String) {
						try {
							dateValue = DateUtil.parse((String)value);
						} catch (Exception e) {}
					}
					
					Date fieldDateValue = null;
					try {
						fieldDateValue = DateUtil.parse(fieldValue);
					} catch (Exception e) {}
					
					if(dateValue != null) {
						if(fieldDateValue != null) {
							if(DmpCfgOutputBlackCompareSignEnum.EQ.getCode().equals(compareSign)) {
								return dateValue.compareTo(fieldDateValue) == 0;
							}else if(DmpCfgOutputBlackCompareSignEnum.NE.getCode().equals(compareSign)) {
								return dateValue.compareTo(fieldDateValue) != 0;
							}else if(DmpCfgOutputBlackCompareSignEnum.GT.getCode().equals(compareSign)) {
								return dateValue.compareTo(fieldDateValue) > 0;
							}else if(DmpCfgOutputBlackCompareSignEnum.GE.getCode().equals(compareSign)) {
								return dateValue.compareTo(fieldDateValue) >= 0;
							}else if(DmpCfgOutputBlackCompareSignEnum.LT.getCode().equals(compareSign)) {
								return dateValue.compareTo(fieldDateValue) < 0;
							}else if(DmpCfgOutputBlackCompareSignEnum.LE.getCode().equals(compareSign)) {
								return dateValue.compareTo(fieldDateValue) <= 0;
							}
						}else{
							if(DmpCfgOutputBlackCompareSignEnum.IN.getCode().equals(compareSign)) {
								String[] fieldValueList = fieldValue.split(",");
								if(fieldValueList.length == 1) {
									fieldValueList = fieldValue.split("，");
								}
								for(String s : fieldValueList) {
									if(StringUtils.isNotBlank(s)) {
										fieldDateValue = null;
										try {
											fieldDateValue = DateUtil.parse(s);
										} catch (NumberFormatException e) {}
										if(fieldDateValue != null && (dateValue.compareTo(fieldDateValue) == 0)) {
											return true;
										}
									}
								}
							}else if(DmpCfgOutputBlackCompareSignEnum.BE.getCode().equals(compareSign)) {
								String[] fieldValueList = fieldValue.split(",");
								if(fieldValueList.length == 1) {
									fieldValueList = fieldValue.split("，");
								}
								List<Date> dateList = new ArrayList<>();
								for(String s : fieldValueList) {
									if(StringUtils.isNotBlank(s)) {
										Date parseFieldDateValue = null;
										try {
											parseFieldDateValue = DateUtil.parse(s);
										} catch (NumberFormatException e) {}
										if(parseFieldDateValue != null) {
											dateList.add(parseFieldDateValue);
										}
									}
								}
								if(dateList.size() > 1) {
									return dateValue.compareTo(dateList.get(0)) >= 0 && dateValue.compareTo(dateList.get(1)) <= 0;
								}
							}
						}
					}
				}
			}
		}else if(DmpCfgOutputBlackCompareSignEnum.ISNULL.getCode().equals(compareSign)) {
			if(DmpCfgOutputBlackDataTypeEnum.STRING.getCode().equals(dataType)) {
				if(value == null || StringUtils.isBlank(value.toString())) {
					return true;
				}
			}else {
				if(value == null) {
					return true;
				}
			}
		}else if(DmpCfgOutputBlackCompareSignEnum.ISNOTNULL.getCode().equals(compareSign)) {
			if(DmpCfgOutputBlackDataTypeEnum.STRING.getCode().equals(dataType)) {
				if(value != null && StringUtils.isNotBlank(value.toString())) {
					return true;
				}
			}else {
				if(value != null) {
					return true;
				}
			}
		}
		return false;
	}
}
