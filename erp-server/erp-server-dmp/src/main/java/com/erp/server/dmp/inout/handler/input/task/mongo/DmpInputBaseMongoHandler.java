package com.erp.server.dmp.inout.handler.input.task.mongo;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.ObjUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.core.constant.EnumMessage;
import com.common.core.exception.ServiceException;
import com.common.core.utils.FastDFSClientUtil;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpInputTaskFileEntity;
import com.erp.model.dmp.enums.DmpInputTaskFileContentTypeEnum;
import com.erp.model.dmp.enums.DmpInputTaskFileParseStatusEnum;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputMongoRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputFdsResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputInitResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;

/**
 * dmp输入任务mongo基础处理器，被mongo任务状态执行器继承，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
@Slf4j
public class DmpInputBaseMongoHandler extends DmpInputMongoHandler{

	@Override
	public List<Map<String, Object>> parseFdsToMongo(DmpInputMongoRequest dmpRequest,
			DmpInputFdsResponse dmpResponse) {
		List<Map<String, Object>> dmpInputMongoEntityList = new ArrayList<>();
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
					DmpInputTaskFileContentTypeEnum contentType = EnumMessage.getByCode(DmpInputTaskFileContentTypeEnum.class, dmpInputTaskFileEntity.getContentType());
					List<Map<String, Object>> dataList = this.getDataList(contentType, resultList);
					int currParseCount = 0;
					for(Map<String, Object> data : dataList) {
						if (ObjUtil.isEmpty(data)) {
							continue;
						}
						dmpInputMongoEntityList.addAll(this.getDmpInputMongoEntityList(contentType , dmpInputTaskFileEntity.getId() , currParseCount, data, allFieldFlag, uniqueFieldSet));
						currParseCount = currParseCount + 1;
					}
					dmpInputTaskFileEntity.setCurrParseCount(currParseCount);
					dmpInputTaskFileEntity.setParseStatus(DmpInputTaskFileParseStatusEnum.FINISH.getCode());
				}
				dmpInputTaskFileService.updateBatchById(dmpInputTaskFileEntityList);
			}
		}
		return dmpInputMongoEntityList;
	}

	/**
	 * 获取文件数据
	 * @param contentType
	 * @param fileUrl
	 * @return
	 */
	protected List<Map<String, Object>> getDataList(DmpInputTaskFileContentTypeEnum contentType , List<String> resultList) {
		List<Map<String, Object>> dataList = new ArrayList<>();
		if(DmpInputTaskFileContentTypeEnum.TXT == contentType) {
			String[] keyArr = resultList.get(0).split("\t");
			for(int i = 1; i < resultList.size(); i++) {
				String[] valueArr = resultList.get(i).split("\t");
				Map<String, Object> data = new HashMap<>();
				for(int j = 0 ; j < keyArr.length; j++) {
					data.put(keyArr[j], valueArr[j]);
				}
				dataList.add(data);
			}
		}else {
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
		}
		return dataList;
	}
	
	
	@Override
	public List<Map<String , Object>> parseInitToMongo(DmpInputMongoRequest dmpRequest, DmpInputInitResponse dmpResponse) {
		List<Map<String, Object>> dmpInputMongoEntityList = new ArrayList<>();
		Map<DmpCfgInputConvertEntity, List<DmpInputTaskInitDTO>> convertInputTaskInitDTOListMaps = dmpResponse.getConvertInputTaskInitDTOListMaps();
		for(Map.Entry<DmpCfgInputConvertEntity, List<DmpInputTaskInitDTO>> convertInputTaskInitDTOListMap : convertInputTaskInitDTOListMaps.entrySet()) {
			List<DmpInputTaskInitDTO> dmpInputTaskInitDTOList = convertInputTaskInitDTOListMap.getValue();
			for(DmpInputTaskInitDTO dmpInputTaskInitDTO : dmpInputTaskInitDTOList) {
				DmpInputTaskFileContentTypeEnum contentType = dmpInputTaskInitDTO.getContentType();
				List<Map<String, Object>> dataList = this.getDataList(contentType, Collections.singletonList(dmpInputTaskInitDTO.getMsg()));
				int currParseCount = 0;
				for(Map<String, Object> data : dataList) {
					dmpInputMongoEntityList.addAll(this.getDmpInputMongoEntityList(contentType , "", currParseCount, data, allFieldFlag, uniqueFieldSet));
					currParseCount = currParseCount + 1;
				}
			}
		}
		return dmpInputMongoEntityList;
	}

	@Override
	public List<Map<String , Object>> parseNoneToMongo(DmpInputMongoRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
		return new ArrayList<>();
	}
}
