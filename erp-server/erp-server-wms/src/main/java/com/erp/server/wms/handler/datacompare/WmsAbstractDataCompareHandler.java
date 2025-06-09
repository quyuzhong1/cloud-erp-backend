package com.erp.server.wms.handler.datacompare;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.common.core.utils.ExcelUtil;
import com.common.core.utils.FastDFSClientUtil;
import com.erp.model.wms.dto.WmsDataCompareTaskDTO.DataCompareDTO;
import com.erp.model.wms.entity.WmsDataCompareImportEntity;
import com.erp.model.wms.entity.WmsDataCompareTaskEntity;
import com.erp.model.wms.enums.WmsDataCompareTaskBillTypeEnum;
import com.erp.server.wms.service.WmsDataCompareBillService;
import com.erp.server.wms.service.WmsDataCompareImportService;
import com.erp.server.wms.utils.WmsDataCompareUtils;
import com.erp.server.wms.utils.WmsDataCompareUtils.WmsDataCompareExcelDto;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.lang.UUID;

import javax.annotation.Resource;

@Service
public abstract class WmsAbstractDataCompareHandler implements WmsDataCompareBillService{
	
	private String billType;
	
	@Resource
	private WmsDataCompareImportService wmsDataCompareImportService;
	
	@Override
	public List<Map<String, String>> getSystemData(WmsDataCompareTaskEntity wmsDataCompareTaskEntity){
		String id = wmsDataCompareTaskEntity.getId();
		Pair<Boolean, List<Map<String, String>>> sysExcelData = getSysExcelData(id , true);
		if(sysExcelData.getKey()) {
			return sysExcelData.getValue();
		}else {
			List<Map<String, String>> dbData = getDbData(wmsDataCompareTaskEntity.getSystemDataCondition(), id);
			if(CollUtil.isNotEmpty(dbData)) {
				dbData = dbData.stream().map(dm -> {
					Map<String, String> map = new HashMap<>();
					if(dm != null) {
						for(Map.Entry<String, String> d : dm.entrySet()) {
							map.put(WmsDataCompareUtils.convertToCamel(d.getKey()), d.getValue());
						}
					}
					return map;
				}).collect(Collectors.toList());
			}
			return dbData;
		}
	}
	
	private Pair<Boolean, List<Map<String, String>>> getSysExcelData(String taskId , boolean needData){
		List<WmsDataCompareImportEntity> wmsDataCompareImportEntityList = wmsDataCompareImportService.lambdaQuery()
				.eq(WmsDataCompareImportEntity::getTaskId, taskId)
				.eq(WmsDataCompareImportEntity::getMainFlag, Boolean.TRUE)
				.list();
		if(CollUtil.isNotEmpty(wmsDataCompareImportEntityList)) {
			List<String> excelFiles = wmsDataCompareImportEntityList.stream().map(WmsDataCompareImportEntity::getFileUrl).collect(Collectors.toList());
			if(needData) {
				WmsDataCompareExcelDto wmsDataCompareExcelDto = WmsDataCompareUtils.getWmsDataCompareExcelDto(excelFiles);
				return new Pair<Boolean, List<Map<String,String>>>(Boolean.TRUE, wmsDataCompareExcelDto.getDatas());
			}else {
				Map<String, String> map = new HashMap<>();
				for(int i = 0; i < excelFiles.size(); i++) {
					map.put(Integer.valueOf(i).toString(), excelFiles.get(i));
				}
				return new Pair<Boolean, List<Map<String,String>>>(Boolean.TRUE, Collections.singletonList(map));
			}
		}else {
			return new Pair<Boolean, List<Map<String,String>>>(Boolean.FALSE, null);
		}
	}
	
	@Override
	public String uploadSystemDataByCondition(WmsDataCompareTaskEntity wmsDataCompareTaskEntity) {
		String id = wmsDataCompareTaskEntity.getId();
		Pair<Boolean, List<Map<String, String>>> sysExcelData = getSysExcelData(id, false);
		if(sysExcelData.getKey()) {
			return WmsDataCompareUtils.mergeExcel(sysExcelData.getValue().stream().map(s -> s.values().iterator().next()).collect(Collectors.toList()));
		}else {
	    	String fileName = UUID.fastUUID().toString() + ".xlsx";
	    	List<Map<String, String>> dbData = this.getDbData(wmsDataCompareTaskEntity.getSystemDataCondition() , null);
			Class<DataCompareDTO> uploadDtoClass = getUploadDtoClass();
			
			List<DataCompareDTO> excelData = new ArrayList<>();
			try {
				Map<String, Method> setMethodMap = new HashMap<>();
				for(Map<String, String> data : dbData) {
					DataCompareDTO newInstance = uploadDtoClass.newInstance();
					for(Map.Entry<String, String> d : data.entrySet()) {
						String key = d.getKey();
						Method method = setMethodMap.get(key);
						if(method == null) {
							method = uploadDtoClass.getMethod("set" + StringUtils.capitalize(WmsDataCompareUtils.convertToCamel(key)) , String.class);
							setMethodMap.put(key, method);
						}
						method.invoke(newInstance, d.getValue());
					}
					excelData.add(newInstance);
				}
			} catch (Exception e) {
				
			}
			File file = ExcelUtil.exportFile(fileName, WmsDataCompareTaskBillTypeEnum.getName(billType) + "系统数据", excelData , uploadDtoClass);
	        return FastDFSClientUtil.uploadFile(file, fileName);
		}
	}
	
	@Override
	public void setBillType(String billType) {
		this.billType = billType;
	}
	
	protected abstract <T extends DataCompareDTO> Class<T> getUploadDtoClass();
	
	protected abstract List<Map<String, String>> getDbData(String systemDataCondition , String taskId);

	@Override
	public Integer getDbSystemDataCount(String systemDataCondition) {
		return null;
	}
	
}