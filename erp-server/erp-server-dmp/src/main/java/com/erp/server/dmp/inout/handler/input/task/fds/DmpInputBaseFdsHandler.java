package com.erp.server.dmp.inout.handler.input.task.fds;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.toolkit.Sequence;
import com.common.core.utils.FastDFSClientUtil;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.model.dmp.entity.DmpInputTaskFileEntity;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputFdsRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputInitResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.service.DmpInputTaskFileService;
import com.erp.server.dmp.service.DmpInputTaskService;

@Service
public class DmpInputBaseFdsHandler extends DmpInputFdsHandler{
	
	@Autowired
	private DmpInputTaskService dmpInputTaskService;
	
	@Autowired
	private DmpInputTaskFileService dmpInputTaskFileService;

	@Transactional
	@Override
	public List<DmpInputTaskFileEntity> uploadInitToFds(DmpInputFdsRequest dmpRequest,
			DmpInputInitResponse dmpResponse) {
		List<DmpInputTaskInitDTO> dmpInputTaskInitDTOList = dmpResponse.getDmpInputTaskInitDTOList();
		List<DmpInputTaskFileEntity> dmpInputTaskFileEntityList = new ArrayList<>(dmpInputTaskInitDTOList.size());
		DmpInputTaskFileEntity dmpInputTaskFileEntity = null;
		String inputTaskId = dmpRequest.getInputTaskId();
		Sequence sequence = new Sequence();
		for(DmpInputTaskInitDTO dmpInputTaskInitDTO : dmpInputTaskInitDTOList) {
			dmpInputTaskFileEntity = new DmpInputTaskFileEntity();
			String id = Long.valueOf(sequence.nextId()).toString();
			String contentType = dmpInputTaskInitDTO.getContentType().getCode();
			byte[] bytes = dmpInputTaskInitDTO.getMsg().getBytes();
			String fileUrl = FastDFSClientUtil.uploadFile(bytes, id + "." + contentType, null);
			dmpInputTaskFileEntity.setId(id);
			dmpInputTaskFileEntity.setMainId(inputTaskId);
			dmpInputTaskFileEntity.setFileUrl(fileUrl);
			dmpInputTaskFileEntity.setFileSize(bytes.length);
			dmpInputTaskFileEntity.setContentType(contentType);
			
			dmpInputTaskFileEntityList.add(dmpInputTaskFileEntity);
		}
		dmpInputTaskFileService.saveBatch(dmpInputTaskFileEntityList);
		dmpInputTaskService.lambdaUpdate()
				.eq(DmpInputTaskEntity::getId, inputTaskId)
				.set(DmpInputTaskEntity::getStatus, DmpInputTaskStatusEnum.FDS.getCode())
				.update();
		return dmpInputTaskFileEntityList;
	}

	@Override
	public List<DmpInputTaskFileEntity> uploadNoneToFds(DmpInputFdsRequest dmpRequest,
			DmpInputTaskResponse dmpResponse) {
		// TODO Auto-generated method stub
		return null;
	}

	
	
}
