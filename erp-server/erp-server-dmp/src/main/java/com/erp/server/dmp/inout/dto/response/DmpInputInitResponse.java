package com.erp.server.dmp.inout.dto.response;

import java.util.List;

import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;

import lombok.Data;

@Data
public class DmpInputInitResponse extends DmpInputTaskResponse{
	private List<DmpInputTaskInitDTO> dmpInputTaskInitDTOList;
}
