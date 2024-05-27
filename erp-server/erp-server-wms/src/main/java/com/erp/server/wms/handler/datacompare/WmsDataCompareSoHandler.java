package com.erp.server.wms.handler.datacompare;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.common.business.wrapper.FeignQuery;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.wms.dto.WmsDataCompareTaskDTO.DataCompareDTO;
import com.erp.model.wms.dto.WmsDataCompareTaskDTO.SoOutstockDTO;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.server.wms.mapper.SoOutstockMapper;
import com.erp.server.wms.mapper.WarehouseMapper;

import cn.hutool.core.collection.CollUtil;

@Service
public class WmsDataCompareSoHandler extends WmsAbstractDataCompareHandler{

	@Autowired
	private SoOutstockMapper baseMapper;
	@Autowired
	private WarehouseMapper warehouseMapper;
	
	@Override
	public List<Map<String, String>> getDbData(String systemDataCondition , String taskId) {
		com.erp.model.wms.dto.WmsDataCompareTaskDTO.SoOutstockDTO params = this.getParams(systemDataCondition);
		params.setTaskId(taskId);
		return baseMapper.getDataCompareByCondition(params);
	}

	@Override
	public Integer getDbSystemDataCount(String systemDataCondition) {
		return baseMapper.getDataCompareByConditionCount(this.getParams(systemDataCondition));
	}

	private com.erp.model.wms.dto.WmsDataCompareTaskDTO.SoOutstockDTO getParams(String systemDataCondition) {
		com.erp.model.wms.dto.WmsDataCompareTaskDTO.SoOutstockDTO params = JSON.parseObject(systemDataCondition , com.erp.model.wms.dto.WmsDataCompareTaskDTO.SoOutstockDTO.class);
		if(StringUtils.isBlank(params.getDictPlatform())) {
			throw new ServiceException("销售出库单的系统数据范围【销售平台】不能为空");
		}

		if(CollUtil.isEmpty(params.getBillDateList())) {
			throw new ServiceException("销售出库单的系统数据范围【出库日期】不能为空");
		}

		List<ShopInfoEntity> shopInfoEntityList =  FeignQuery.create(ShopInfoEntity.class)
			.eq(ShopInfoEntity::getDictPlatform, params.getDictPlatform())
			.eq(StringUtils.isNotBlank(params.getShopId()) , ShopInfoEntity::getId, params.getShopId())
			.list();
		List<String> customerNameList = shopInfoEntityList.stream()
				.filter(s -> StringUtils.isNotBlank(s.getName())).map(ShopInfoEntity::getName).collect(Collectors.toList());

		if(CollUtil.isEmpty(customerNameList)) {
			throw new ServiceException("选择的销售平台下没有店铺");
		}
		params.setCustomerNameList(customerNameList);

		String warehouseId = params.getWarehouseId();
		if(StringUtils.isNotBlank(warehouseId)) {
			WarehouseEntity warehouseEntity = warehouseMapper.selectById(warehouseId);
			if(warehouseEntity != null) {
				params.setWarehouseName(warehouseEntity.getName());
			}
		}
		
		return params;
	}

	@Override
	public <T extends DataCompareDTO> Class<T> getUploadDtoClass() {
		return (Class<T>) SoOutstockDTO.class;
	}

	

}
