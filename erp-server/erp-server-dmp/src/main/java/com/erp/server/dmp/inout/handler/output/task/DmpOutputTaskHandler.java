package com.erp.server.dmp.inout.handler.output.task;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.core.exception.ServiceException;
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
import com.erp.server.dmp.service.DmpOutputTaskService;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;

@Service
@Scope("prototype")
public abstract class DmpOutputTaskHandler extends DmpOutputHandler{
	@Autowired
	protected DmpOutputTaskService dmpOutputTaskService;
	@Autowired
	protected DmpHandlerCache dmpHandlerCache;
	
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
		dmpOutputTaskService.lambdaUpdate()
				.eq(DmpOutputTaskEntity::getId, dmpRequest.getOutputTaskId())
				.set(DmpOutputTaskEntity::getStatus, DmpOutputTaskStatusEnum.FINISH.getCode())
				.set(DmpOutputTaskEntity::getUpdateTime, LocalDateTime.now())
				.update();
		chain.doDmpHandler(dmpRequest, dmpResponse);
	}
	
	public abstract List<DmpOutputTaskRecordEntity> outputData(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse);
	
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
