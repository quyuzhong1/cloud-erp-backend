package com.erp.server.dmp.inout.handler.input.task.init;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.erp.model.dmp.enums.DmpInputTaskFileContentTypeEnum;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;

@Service
@Scope("prototype")
public class DmpInputTestInitHandler extends DmpInputInitHandler{

	@Override
	public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
		List<DmpInputTaskInitDTO> dmpInputTaskInitDTOList = new ArrayList<>();
		
		DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
		dmpInputTaskInitDTO.setCode(200);
		dmpInputTaskInitDTO.setContentType(DmpInputTaskFileContentTypeEnum.JSON);
		dmpInputTaskInitDTO.setMsg("{'size' : '12' , 'name' : '金蝶15' , 'code' : 'kingdee'}");
		dmpInputTaskInitDTOList.add(dmpInputTaskInitDTO);
		
		dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
		dmpInputTaskInitDTO.setCode(200);
		dmpInputTaskInitDTO.setContentType(DmpInputTaskFileContentTypeEnum.TXT);
		dmpInputTaskInitDTO.setMsg("code\tname\tsize\namazon\t亚马逊12\t6");
		dmpInputTaskInitDTOList.add(dmpInputTaskInitDTO);
		return dmpInputTaskInitDTOList;
	}

}
