package com.erp.server.dmp.inout.handler.input.task.mongo;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
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
import com.erp.model.dmp.entity.DmpInputTaskFileEntity;
import com.erp.model.dmp.enums.DmpInputTaskFileContentTypeEnum;
import com.erp.model.dmp.enums.DmpInputTaskFileParseStatusEnum;
import com.erp.server.dmp.inout.dto.request.DmpInputMongoRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputFdsResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputInitResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.utils.DmpHandlerUtils;
import com.erp.server.dmp.service.DmpInputTaskFileService;

import cn.hutool.core.collection.CollUtil;
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
	
	@Override
	public List<Map> parseFdsToMongo(DmpInputMongoRequest dmpRequest,
			DmpInputFdsResponse dmpResponse) {
		List<Map> dmpInputMongoEntityList = new ArrayList<>();
		Map dmpInputMongoEntity = null;
		
		Set<String> uniqueFieldSet = new HashSet<>();
		boolean allFieldFlag = false;
		String uniqueFieldName = dmpCfgInputConvertEntity.getUniqueFieldName();
		if(StringUtils.isNotBlank(uniqueFieldName)) {
			if("{all}".equals(uniqueFieldName)) {
				allFieldFlag = true;
			}else {
				uniqueFieldSet = Stream.of(uniqueFieldName.split(",")).collect(Collectors.toSet());
			}
		}
		MD5 md5 = MD5.create();
		
		Map<DmpCfgInputConvertEntity, List<DmpInputTaskFileEntity>> convertInputTaskFileEntityListMaps = dmpResponse.getConvertInputTaskFileEntityListMaps();
		for(Map.Entry<DmpCfgInputConvertEntity, List<DmpInputTaskFileEntity>> convertInputTaskFileEntityListMap : convertInputTaskFileEntityListMaps.entrySet()) {
			List<DmpInputTaskFileEntity> dmpInputTaskFileEntityList = convertInputTaskFileEntityListMap.getValue();
			if(CollUtil.isNotEmpty(dmpInputTaskFileEntityList)) {
				for(DmpInputTaskFileEntity dmpInputTaskFileEntity : dmpInputTaskFileEntityList) {
					String fileUrl = dmpInputTaskFileEntity.getFileUrl();
					List<String> resultList = new ArrayList<>();
					try {
						InputStream inputStream = FastDFSClientUtil.getInputStream(fileUrl);
						IoUtil.readLines(inputStream, CharsetUtil.CHARSET_UTF_8, resultList);
						inputStream.close();
					} catch (Exception e) {
						log.error("读取fds文件错误，文件路径：{}" , fileUrl , e);
						throw new ServiceException("读取fds文件错误，文件路径：" + fileUrl + "，异常信息：" + ExceptionUtil.stacktraceToString(e));
					}
					int currParseCount = 0;
					if(CollUtil.isNotEmpty(resultList)) {
						String contentType = dmpInputTaskFileEntity.getContentType();
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
							}else {
								parseObject = JSON.parseObject(jsonString);
								parseArray.add(parseObject);
							}
							for(int i = 0 ; i < parseArray.size() ; i++) {
								parseObject = parseArray.getJSONObject(i);
								
								dmpInputMongoEntity = new HashMap<>();
								StringBuilder uniqueFieldMd5Sb = new StringBuilder();
								
								dmpInputMongoEntity.put(MONGO_BASE_ID, DmpHandlerUtils.getId());
								dmpInputMongoEntity.put(MONGO_BASE_INPUTTASKID , inputTaskId);
								dmpInputMongoEntity.put(MONGO_BASE_FILEID, dmpInputTaskFileEntity.getId());
								dmpInputMongoEntity.put(MONGO_BASE_CONVERTID, convertId);
								dmpInputMongoEntity.put(MONGO_BASE_ROWNUMBER, i + 1);
								
								uniqueFieldMd5Sb.append(i);
								
								for(Map.Entry<String, Object> object : parseObject.entrySet()) {
									String key = object.getKey();
									Object value = object.getValue();
									dmpInputMongoEntity.put(key, value);
									if(allFieldFlag || uniqueFieldSet.contains(key)) {
										if(value != null) {
											uniqueFieldMd5Sb.append(value);
										}
									}
								}
								
								dmpInputMongoEntity.put(MONGO_BASE_UNIQUEFIELDMD5, md5.digestHex(uniqueFieldMd5Sb.toString()));
								
								dmpInputMongoEntityList.add(dmpInputMongoEntity);
								currParseCount = currParseCount + 1;
							}
						}else if(DmpInputTaskFileContentTypeEnum.TXT.getCode().equals(contentType)) {
							String[] keyArr = resultList.get(0).split("\t");
							for(int i = 1; i < resultList.size(); i++) {
								dmpInputMongoEntity = new HashMap<>();
								StringBuilder uniqueFieldMd5Sb = new StringBuilder();
								
								dmpInputMongoEntity.put(MONGO_BASE_ID, DmpHandlerUtils.getId());
								dmpInputMongoEntity.put(MONGO_BASE_INPUTTASKID , inputTaskId);
								dmpInputMongoEntity.put(MONGO_BASE_FILEID, dmpInputTaskFileEntity.getId());
								dmpInputMongoEntity.put(MONGO_BASE_CONVERTID, convertId);
								dmpInputMongoEntity.put(MONGO_BASE_ROWNUMBER, i);
								
								uniqueFieldMd5Sb.append(i);
								
								String[] valueArr = resultList.get(i).split("\t");
								for(int j = 0 ; j < keyArr.length; j++) {
									String key = keyArr[j];
									String value = valueArr[j];
									dmpInputMongoEntity.put(key, value);
									if(allFieldFlag || uniqueFieldSet.contains(key)) {
										if(value != null) {
											uniqueFieldMd5Sb.append(value);
										}
									}
								}
								
								dmpInputMongoEntity.put(MONGO_BASE_UNIQUEFIELDMD5, md5.digestHex(uniqueFieldMd5Sb.toString()));
								
								dmpInputMongoEntityList.add(dmpInputMongoEntity);
								currParseCount = currParseCount + 1;
							}
							dmpInputTaskFileEntity.setCurrParseCount(currParseCount);
							dmpInputTaskFileEntity.setParseStatus(DmpInputTaskFileParseStatusEnum.FINISH.getCode());
						}
					}
				}
				dmpInputTaskFileService.updateBatchById(dmpInputTaskFileEntityList);
			}
		}
		
		if(CollUtil.isNotEmpty(dmpInputMongoEntityList)) {
			DmpInputMongoUniqueDTO dmpInputMongoUniqueDTO = new DmpInputMongoUniqueDTO();
			dmpInputMongoUniqueDTO.setUniqueFieldMd5(dmpInputMongoEntityList.stream().map(d -> d.get(MONGO_BASE_UNIQUEFIELDMD5).toString()).collect(Collectors.toList()));
			try {
				List<DmpInputMongoUniqueDTO> list = new ArrayList<>();
				list.add(dmpInputMongoUniqueDTO);
				mongoService.deleteMongoDataBatch(list, mongoStorageName);
			} catch (Exception e) {
				throw new ServiceException("删除mongo数据失败，" + "异常信息：" + ExceptionUtil.stacktraceToString(e));
			}
			mongoService.saveMongoDataMult(dmpInputMongoEntityList, mongoStorageName);
		}
		
		return dmpInputMongoEntityList;
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
