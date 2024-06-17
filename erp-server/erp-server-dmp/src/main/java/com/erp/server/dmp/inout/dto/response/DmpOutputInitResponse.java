package com.erp.server.dmp.inout.dto.response;

import java.util.List;

import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;

import lombok.Data;

@Data
public class DmpOutputInitResponse extends DmpOutputTaskResponse{
	private List<DmpInputTaskInitDTO> dmpInputTaskInitDTOList;
}
