package com.erp.server.dmp.inout.handler.input.task.init;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.erp.model.dmp.enums.DmpInputTaskFileContentTypeEnum;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;

@Service
public class DmpInputTestInitHandler extends DmpInputInitHandler{

	@Override
	public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
		List<DmpInputTaskInitDTO> dmpInputTaskInitDTOList = new ArrayList<>();
		
		DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
		dmpInputTaskInitDTO.setCode(200);
		dmpInputTaskInitDTO.setContentType(DmpInputTaskFileContentTypeEnum.JSON);
		dmpInputTaskInitDTO.setMsg("{'code' : 'kingdee' , 'name' : '金蝶'}");
		dmpInputTaskInitDTOList.add(dmpInputTaskInitDTO);
		
		dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
		dmpInputTaskInitDTO.setCode(200);
		dmpInputTaskInitDTO.setContentType(DmpInputTaskFileContentTypeEnum.CSV);
		dmpInputTaskInitDTO.setMsg("code\tname\namazon\t亚马逊");
		dmpInputTaskInitDTOList.add(dmpInputTaskInitDTO);
		return dmpInputTaskInitDTOList;
	}

}
