package com.erp.server.dmp.inout.handler.input.task.mongo;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.core.exception.ServiceException;
import com.common.core.utils.FastDFSClientUtil;
import com.erp.model.dmp.dto.DmpInputMongoUniqueDTO;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpInputFileMongoRelationEntity;
import com.erp.model.dmp.entity.DmpInputTaskFileEntity;
import com.erp.model.dmp.enums.DmpInputTaskFileContentTypeEnum;
import com.erp.model.dmp.enums.DmpInputTaskFileParseStatusEnum;
import com.erp.server.dmp.inout.dto.request.DmpInputMongoRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputFdsResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputInitResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.utils.DmpHandlerUtils;
import com.erp.server.dmp.service.DmpInputFileMongoRelationService;
import com.erp.server.dmp.service.DmpInputTaskFileService;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.crypto.digest.MD5;
import lombok.extern.slf4j.Slf4j;

@Service
@Scope("prototype")
@Slf4j
public class DmpInputBaseMongoHandler extends DmpInputMongoHandler{

	@Autowired
	private DmpInputTaskFileService dmpInputTaskFileService;
	
	@Autowired
	private DmpInputFileMongoRelationService dmpInputFileMongoRelationService;
	
	protected final MD5 md5 = MD5.create();
	
	@Override
	public List<Map> parseFdsToMongo(DmpInputMongoRequest dmpRequest,
			DmpInputFdsResponse dmpResponse) {
		Map<String, Map> md5DmpInputMongoEntityMaps = new HashMap<>();
		
		Map<DmpCfgInputConvertEntity, List<DmpInputTaskFileEntity>> convertInputTaskFileEntityListMaps = dmpResponse.getConvertInputTaskFileEntityListMaps();
		for(Map.Entry<DmpCfgInputConvertEntity, List<DmpInputTaskFileEntity>> convertInputTaskFileEntityListMap : convertInputTaskFileEntityListMaps.entrySet()) {
			List<DmpInputTaskFileEntity> dmpInputTaskFileEntityList = convertInputTaskFileEntityListMap.getValue();
			if(CollUtil.isNotEmpty(dmpInputTaskFileEntityList)) {
				for(DmpInputTaskFileEntity dmpInputTaskFileEntity : dmpInputTaskFileEntityList) {
					List<Map<String, Object>> dataList = this.getDataList(dmpInputTaskFileEntity.getContentType(), dmpInputTaskFileEntity.getFileUrl());
					int currParseCount = 0;
					for(Map<String, Object> data : dataList) {
						this.putDmpInputMongoEntity(dmpInputTaskFileEntity, currParseCount, data, allFieldFlag, uniqueFieldSet, md5DmpInputMongoEntityMaps);
						currParseCount = currParseCount + 1;
					}
					dmpInputTaskFileEntity.setCurrParseCount(currParseCount);
					dmpInputTaskFileEntity.setParseStatus(DmpInputTaskFileParseStatusEnum.FINISH.getCode());
				}
				dmpInputTaskFileService.updateBatchById(dmpInputTaskFileEntityList);
			}
		}
		
		List<Map> saveDmpInputMongoEntityList = new ArrayList<>();
		if(md5DmpInputMongoEntityMaps.size() > 0) {
			List<Map<String, Object>> updateDmpInputMongoEntityList = new ArrayList<>();
			this.compareData(saveDmpInputMongoEntityList, updateDmpInputMongoEntityList, md5DmpInputMongoEntityMaps);
			if(CollUtil.isNotEmpty(saveDmpInputMongoEntityList)) {
				mongoService.saveMongoDataMult(saveDmpInputMongoEntityList, mongoStorageName);
			}
			if(CollUtil.isNotEmpty(updateDmpInputMongoEntityList)) {
				mongoService.upsertMongoDataBatch(updateDmpInputMongoEntityList, mongoStorageName);
				saveDmpInputMongoEntityList.addAll(updateDmpInputMongoEntityList);
			}
		}
		
		if(CollUtil.isNotEmpty(saveDmpInputMongoEntityList)) {
			List<DmpInputFileMongoRelationEntity> dmpInputFileMongoRelationEntityList = new ArrayList<>(saveDmpInputMongoEntityList.size());
			DmpInputFileMongoRelationEntity dmpInputFileMongoRelationEntity = null;
			for(Map allDmpInputMongoEntity : saveDmpInputMongoEntityList) {
				dmpInputFileMongoRelationEntity = new DmpInputFileMongoRelationEntity();
				dmpInputFileMongoRelationEntity.setMongoId(allDmpInputMongoEntity.get(MONGO_BASE_ID).toString());
				dmpInputFileMongoRelationEntity.setFileId(allDmpInputMongoEntity.get(MONGO_BASE_FILEID).toString());
				
				dmpInputFileMongoRelationEntityList.add(dmpInputFileMongoRelationEntity);
			}
			dmpInputFileMongoRelationService.saveBatch(dmpInputFileMongoRelationEntityList);
		}
		
		return saveDmpInputMongoEntityList;
	}

	/**
	 * 获取文件数据
	 * @param contentType
	 * @param fileUrl
	 * @return
	 */
	private List<Map<String, Object>> getDataList(String contentType , String fileUrl) {
		List<String> resultList = new ArrayList<>();
		try {
			InputStream inputStream = FastDFSClientUtil.getInputStream(fileUrl);
			IoUtil.readLines(inputStream, CharsetUtil.CHARSET_UTF_8, resultList);
			inputStream.close();
		} catch (Exception e) {
			log.error("读取fds文件错误，文件路径：{}" , fileUrl , e);
			throw new ServiceException("读取fds文件错误，文件路径：" + fileUrl + "，异常信息：" + ExceptionUtil.stacktraceToString(e));
		}
		
		List<Map<String, Object>> dataList = new ArrayList<>();
		if(DmpInputTaskFileContentTypeEnum.JSON.getCode().equals(contentType)) {
			StringBuilder sb = new StringBuilder();
			for(String s : resultList) {
				sb.append(s);
			}
			String jsonString = sb.toString().trim();
			JSONArray parseArray = new JSONArray();
			JSONObject parseObject = null;
			if(jsonString.startsWith("[")) {
				parseArray = JSON.parseArray(jsonString);
				for(int i = 0 ; i < parseArray.size() ; i++) {
					dataList.add(parseArray.getJSONObject(i));
				}
			}else {
				parseObject = JSON.parseObject(jsonString);
				dataList.add(parseObject);
			}
		}else if(DmpInputTaskFileContentTypeEnum.TXT.getCode().equals(contentType)) {
			String[] keyArr = resultList.get(0).split("\t");
			for(int i = 1; i < resultList.size(); i++) {
				String[] valueArr = resultList.get(i).split("\t");
				Map<String, Object> data = new HashMap<>();
				for(int j = 0 ; j < keyArr.length; j++) {
					data.put(keyArr[j], valueArr[j]);
				}
				dataList.add(data);
			}
		}
		return dataList;
	}
	
	
	/**
	 * 设置mongo对象
	 * @param fileId
	 * @param i
	 * @param data
	 * @param allFieldFlag
	 * @param uniqueFieldSet
	 * @param md5DmpInputMongoEntityMaps
	 */
	private void putDmpInputMongoEntity(DmpInputTaskFileEntity dmpInputTaskFileEntity , Integer i , Map<String, Object> data , boolean allFieldFlag , Set<String> uniqueFieldSet , Map<String, Map> md5DmpInputMongoEntityMaps) {
		List<TreeMap> resultDataList = this.convertData(data);
		if(CollUtil.isNotEmpty(resultDataList)) {
			for(Map dmpInputMongoEntity : resultDataList) {
				Integer rowNumber = i + 1;
				StringBuilder uniqueFieldMd5Sb = new StringBuilder();
				uniqueFieldMd5Sb.append(nextLevelId);
				StringBuilder dataMd5Sb = new StringBuilder();
				dataMd5Sb.append(nextLevelId);
				Set<Entry> entrySet = dmpInputMongoEntity.entrySet();
				for(Map.Entry dmpInputMongo : entrySet) {
					String key = dmpInputMongo.getKey().toString();
					Object value = dmpInputMongo.getValue();
					if(value != null) {
						if(allFieldFlag || uniqueFieldSet.contains(key)) {
							uniqueFieldMd5Sb.append(value);
						}
						dataMd5Sb.append(value);
					}
				}
				String contentType = dmpInputTaskFileEntity.getContentType();
				if(DmpInputTaskFileContentTypeEnum.TXT.getCode().equals(contentType) || DmpInputTaskFileContentTypeEnum.CSV.getCode().equals(contentType)) {
					uniqueFieldMd5Sb.append(rowNumber);
					dataMd5Sb.append(rowNumber);
				}
				this.afterDmpInputMongoEntity(dmpInputMongoEntity, dmpInputTaskFileEntity.getId() , rowNumber ,uniqueFieldMd5Sb.toString(), dataMd5Sb.toString());
				md5DmpInputMongoEntityMaps.put(dmpInputMongoEntity.get(MONGO_BASE_UNIQUEENCRYPT).toString(), dmpInputMongoEntity);
			}
		}
	}
	
	/**
	 * 转换数据，可重写
	 * @param data 原始数据
	 */
	protected List<TreeMap> convertData(Map<String, Object> data) {
		List<TreeMap> resultDataList = new ArrayList<>();
		TreeMap dmpInputMongoEntity = new TreeMap<>();
		for(Map.Entry<String, Object> d : data.entrySet()) {
			String key = d.getKey();
			Object value = d.getValue();
			dmpInputMongoEntity.put(this.convertKey(key).replace(".", ""), value);
		}
		resultDataList.add(dmpInputMongoEntity);
		return resultDataList;
	}
	
	/**
	 * 转换key
	 * @param originalKey
	 * @return
	 */
	protected String convertKey(String originalKey) {
		return originalKey;
	}
	
	
	/**
	 * 设置mongo公共字段
	 * @param dmpInputMongoEntity
	 * @param fileId
	 * @param i
	 * @param uniqueFieldString
	 * @param dataString
	 */
	private void afterDmpInputMongoEntity(Map dmpInputMongoEntity ,String fileId , Integer rowNumber ,  String uniqueFieldString , String dataString) {
		dmpInputMongoEntity.put(MONGO_BASE_ID, DmpHandlerUtils.getId());
		dmpInputMongoEntity.put(MONGO_BASE_INPUTTASKID , inputTaskId);
		dmpInputMongoEntity.put(MONGO_BASE_NEXTLEVELID , nextLevelId);
		dmpInputMongoEntity.put(MONGO_BASE_FILEID, fileId);
		dmpInputMongoEntity.put(MONGO_BASE_CONVERTID, convertId);
		dmpInputMongoEntity.put(MONGO_BASE_ROWNUMBER, rowNumber);
		dmpInputMongoEntity.put(MONGO_BASE_UNIQUEENCRYPT, md5.digestHex(uniqueFieldString));
		dmpInputMongoEntity.put(MONGO_BASE_DATAENCRYPT, md5.digestHex(dataString));
		String now = DateUtil.now();
		dmpInputMongoEntity.put(MONGO_BASE_MONGOCREATETIME, now);
		dmpInputMongoEntity.put(MONGO_BASE_MONGOUPDATETIME, now);
	}
	
	
	/**
	 * 对比mongo数据
	 * @param saveDmpInputMongoEntityList
	 * @param updateDmpInputMongoEntityList
	 * @param md5DmpInputMongoEntityMaps
	 */
	private void compareData(List<Map> saveDmpInputMongoEntityList , List<Map<String, Object>> updateDmpInputMongoEntityList , Map<String, Map> md5DmpInputMongoEntityMaps) {
		DmpInputMongoUniqueDTO dmpInputMongoUniqueDTO = new DmpInputMongoUniqueDTO();
		dmpInputMongoUniqueDTO.setUniqueEncrypt(new ArrayList<>(md5DmpInputMongoEntityMaps.keySet()));
		List<Map> findDmpInputMongoEntityList = mongoService.findMongoData(dmpInputMongoUniqueDTO, 0, 0, mongoStorageName, Map.class);
		if(CollUtil.isNotEmpty(findDmpInputMongoEntityList)) {
			Map<String, Map> uniqueFieldDataMd5Maps = new HashMap<>();
			for(Map findDmpInputMongoEntity : findDmpInputMongoEntityList) {
				uniqueFieldDataMd5Maps.put(findDmpInputMongoEntity.get(MONGO_BASE_UNIQUEENCRYPT).toString(), findDmpInputMongoEntity);
			}
			
			for(Map.Entry<String, Map> md5DmpInputMongoEntityMap : md5DmpInputMongoEntityMaps.entrySet()) {
				Map findEntity = uniqueFieldDataMd5Maps.get(md5DmpInputMongoEntityMap.getKey());
				Map waitEntity = md5DmpInputMongoEntityMap.getValue();
				
				if(findEntity != null) {
					waitEntity.put(MONGO_BASE_ID, findEntity.get(MONGO_BASE_ID).toString());
					waitEntity.put(MONGO_BASE_MONGOCREATETIME, findEntity.get(MONGO_BASE_MONGOCREATETIME).toString());
					updateDmpInputMongoEntityList.add(waitEntity);
				}else {
					saveDmpInputMongoEntityList.add(waitEntity);
				}
			}
		}else {
			saveDmpInputMongoEntityList.addAll(md5DmpInputMongoEntityMaps.values());
		}
	}
	
	@Override
	public List<Map> parseInitToMongo(DmpInputMongoRequest dmpRequest, DmpInputInitResponse dmpResponse) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Map> parseNoneToMongo(DmpInputMongoRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
		// TODO Auto-generated method stub
		return null;
	}
}
