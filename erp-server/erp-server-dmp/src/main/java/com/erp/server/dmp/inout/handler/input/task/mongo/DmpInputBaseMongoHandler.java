package com.erp.server.dmp.inout.handler.input.task.mongo;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.core.utils.FastDFSClientUtil;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.model.dmp.entity.DmpInputTaskFileEntity;
import com.erp.model.dmp.enums.DmpInputTaskFileContentTypeEnum;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.server.dmp.inout.dto.base.DmpInputMongoBaseEntity;
import com.erp.server.dmp.inout.dto.request.DmpInputMongoRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputFdsResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputInitResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.utils.IdSequenceUtils;
import com.erp.server.dmp.service.DmpInputTaskService;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.CharsetUtil;

@Service
@Scope("prototype")
public class DmpInputBaseMongoHandler extends DmpInputMongoHandler{

	@Autowired
	private DmpInputTaskService dmpInputTaskService;
	
	@Override
	public List<Map> parseFdsToMongo(DmpInputMongoRequest dmpRequest,
			DmpInputFdsResponse dmpResponse) {
		String inputTaskId = dmpRequest.getInputTaskId();
		List<Map> dmpInputMongoEntityList = new ArrayList<>();
		Map dmpInputMongoEntity = null;
		
		List<DmpInputTaskFileEntity> dmpInputTaskFileEntityList = dmpResponse.getDmpInputTaskFileEntityList();
		for(DmpInputTaskFileEntity dmpInputTaskFileEntity : dmpInputTaskFileEntityList) {
			dmpInputMongoEntity = new HashMap<>();
			String fileUrl = dmpInputTaskFileEntity.getFileUrl();
			InputStream inputStream = FastDFSClientUtil.getInputStream(fileUrl);
			List<String> resultList = new ArrayList<>();
			IoUtil.readLines(inputStream, CharsetUtil.CHARSET_UTF_8, resultList);
			try {
				inputStream.close();
			} catch (IOException e) {
			}
			
			String contentType = dmpInputTaskFileEntity.getContentType();
			if(DmpInputTaskFileContentTypeEnum.JSON.getCode().equals(contentType)) {
				StringBuilder result = new StringBuilder();
				for(String s : resultList) {
					result.append(s);
				}
				JSONObject parseObject = JSON.parseObject(result.toString());
			}else if(DmpInputTaskFileContentTypeEnum.CSV.getCode().equals(contentType)) {
				
			}
			
			dmpInputMongoEntity.put("id", IdSequenceUtils.getId());
			dmpInputMongoEntity.put("inputTaskId" , inputTaskId);
			dmpInputMongoEntity.put("fileId", dmpInputTaskFileEntity.getId());
			
			
			dmpInputMongoEntityList.add(dmpInputMongoEntity);
		}
		
		mongoService.saveMongoDataMult(dmpInputMongoEntityList, mongoStorageName);
		
		dmpInputTaskService.lambdaUpdate()
				.eq(DmpInputTaskEntity::getId, inputTaskId)
				.set(DmpInputTaskEntity::getStatus, DmpInputTaskStatusEnum.MONGO.getCode())
				.update();
		
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
