package com.erp.server.wms.handler.datacompare;

import java.util.List;
import java.util.Map;

import cn.hutool.core.text.CharSequenceUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.dto.WmsDataCompareTaskDTO.DataCompareDTO;
import com.erp.model.wms.dto.WmsDataCompareTaskDTO.OverseasInboundDTO;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.server.wms.mapper.OverseasWarehouseInboundMapper;
import com.erp.server.wms.mapper.WarehouseMapper;

import cn.hutool.core.collection.CollUtil;

import javax.annotation.Resource;

@Service
public class WmsDataCompareOverHandler extends WmsAbstractDataCompareHandler{

	@Resource
	private OverseasWarehouseInboundMapper baseMapper;
	
	@Resource
	private WarehouseMapper warehouseMapper;
	
	@Override
	public List<Map<String, String>> getDbData(String systemDataCondition , String taskId) {
    	OverseasInboundDTO params = this.getParams(systemDataCondition);
    	params.setTaskId(taskId);
		return baseMapper.getDataCompareByCondition(params);
	}

	@Override
	public Integer getDbSystemDataCount(String systemDataCondition) {
		return baseMapper.getDataCompareByConditionCount(this.getParams(systemDataCondition));
	}
	
	private OverseasInboundDTO getParams(String systemDataCondition) {
		OverseasInboundDTO params = JSON.parseObject(systemDataCondition , OverseasInboundDTO.class);
		if(CollUtil.isEmpty(params.getReceiveDateList())) {
			throw new ServiceException("第三方仓货件签收的系统数据范围【签收日期】不能为空");
		}
		String toWarehouseId = params.getToWarehouseId();
		if(CharSequenceUtil.isNotBlank(toWarehouseId)) {
			WarehouseEntity warehouseEntity = warehouseMapper.selectById(toWarehouseId);
			if(warehouseEntity != null) {
				params.setToWarehouseName(warehouseEntity.getName());
			}
		}
		return params;
	}

	@Override
	public <T extends DataCompareDTO> Class<T> getUploadDtoClass() {
		return (Class<T>) OverseasInboundDTO.class;
	}


}
