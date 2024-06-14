package com.erp.server.dmp.inout.handler.input.task.mongo;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
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
import com.erp.server.dmp.service.DmpInputTaskService;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.CharsetUtil;

@Service
public class DmpInputBaseMongoHandler extends DmpInputMongoHandler{

	@Autowired
	private DmpInputTaskService dmpInputTaskService;
	
	@Override
	public List<DmpInputMongoBaseEntity> parseFdsToMongo(DmpInputMongoRequest dmpRequest,
			DmpInputFdsResponse dmpResponse) {
		String inputTaskId = dmpRequest.getInputTaskId();
		List<DmpInputMongoBaseEntity> dmpInputMongoBaseEntityList = new ArrayList<>();
		DmpInputMongoBaseEntity dmpInputMongoBaseEntity = null;
		
		List<DmpInputTaskFileEntity> dmpInputTaskFileEntityList = dmpResponse.getDmpInputTaskFileEntityList();
		for(DmpInputTaskFileEntity dmpInputTaskFileEntity : dmpInputTaskFileEntityList) {
			dmpInputMongoBaseEntity = new DmpInputMongoBaseEntity();
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
				String result = "";
				for(String s : resultList) {
					result = result + s;
				}
				JSON.parseObject(result);
			}else if(DmpInputTaskFileContentTypeEnum.CSV.getCode().equals(contentType)) {
				
			}
			
			dmpInputMongoBaseEntity.setInputTaskId(inputTaskId);
			dmpInputMongoBaseEntity.setFileId(dmpInputTaskFileEntity.getId());
			
			dmpInputMongoBaseEntityList.add(dmpInputMongoBaseEntity);
		}
		
		dmpInputTaskService.lambdaUpdate()
				.eq(DmpInputTaskEntity::getId, inputTaskId)
				.set(DmpInputTaskEntity::getStatus, DmpInputTaskStatusEnum.MONGO.getCode())
				.update();
		
		return dmpInputMongoBaseEntityList;
	}

	@Override
	public List<DmpInputMongoBaseEntity> parseInitToMongo(DmpInputMongoRequest dmpRequest,
			DmpInputInitResponse dmpResponse) {
		return null;
	}

	@Override
	public List<DmpInputMongoBaseEntity> parseNoneToMongo(DmpInputMongoRequest dmpRequest,
			DmpInputTaskResponse dmpResponse) {
		return null;
	}

}
