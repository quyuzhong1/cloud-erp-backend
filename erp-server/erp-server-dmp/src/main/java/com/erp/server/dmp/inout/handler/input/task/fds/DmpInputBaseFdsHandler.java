package com.erp.server.dmp.inout.handler.input.task.fds;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.common.core.utils.FastDFSClientUtil;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpInputTaskFileEntity;
import com.erp.model.dmp.enums.DmpInputTaskFileParseStatusEnum;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputFdsRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputInitResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.utils.DmpHandlerUtils;
import com.erp.server.dmp.service.DmpInputTaskFileService;

import cn.hutool.core.collection.CollUtil;

@Service
@Scope("prototype")
public class DmpInputBaseFdsHandler extends DmpInputFdsHandler{
	
	@Autowired
	private DmpInputTaskFileService dmpInputTaskFileService;

	@Override
	public List<DmpInputTaskFileEntity> uploadInitToFds(DmpInputFdsRequest dmpRequest,
			DmpInputInitResponse dmpResponse) {
		Map<DmpCfgInputConvertEntity, List<DmpInputTaskInitDTO>> convertInputTaskInitDTOListMaps = dmpResponse.getConvertInputTaskInitDTOListMaps();
		List<DmpInputTaskFileEntity> dmpInputTaskFileEntityList = new ArrayList<>();
		DmpInputTaskFileEntity dmpInputTaskFileEntity = null;
		
		for(Map.Entry<DmpCfgInputConvertEntity, List<DmpInputTaskInitDTO>> convertInputTaskInitDTOListMap : convertInputTaskInitDTOListMaps.entrySet()) {
			DmpCfgInputConvertEntity initDmpCfgInputConvertEntity = convertInputTaskInitDTOListMap.getKey();
			List<DmpInputTaskInitDTO> dmpInputTaskInitDTOList = convertInputTaskInitDTOListMap.getValue();
			if(CollUtil.isNotEmpty(dmpInputTaskInitDTOList)) {
				for(DmpInputTaskInitDTO dmpInputTaskInitDTO : dmpInputTaskInitDTOList) {
					dmpInputTaskFileEntity = new DmpInputTaskFileEntity();
					String id = DmpHandlerUtils.getId();
					String contentType = dmpInputTaskInitDTO.getContentType().getCode();
					byte[] bytes = dmpInputTaskInitDTO.getMsg().getBytes();
					String fileUrl = FastDFSClientUtil.uploadFile(bytes, id + "." + contentType, null);
					dmpInputTaskFileEntity.setId(id);
					dmpInputTaskFileEntity.setMainId(inputTaskId);
					dmpInputTaskFileEntity.setInitConvertId(initDmpCfgInputConvertEntity.getId());
					dmpInputTaskFileEntity.setFdsConvertId(convertId);
					dmpInputTaskFileEntity.setFileUrl(fileUrl);
					dmpInputTaskFileEntity.setParseStatus(DmpInputTaskFileParseStatusEnum.WAIT.getCode());
					dmpInputTaskFileEntity.setFileSize(bytes.length);
					dmpInputTaskFileEntity.setContentType(contentType);
					
					dmpInputTaskFileEntityList.add(dmpInputTaskFileEntity);
				}
			}
		}
		
		if(CollUtil.isNotEmpty(dmpInputTaskFileEntityList)) {
			dmpInputTaskFileService.saveBatch(dmpInputTaskFileEntityList);
		}
		return dmpInputTaskFileEntityList;
	}

	@Override
	public List<DmpInputTaskFileEntity> uploadNoneToFds(DmpInputFdsRequest dmpRequest,
			DmpInputTaskResponse dmpResponse) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
