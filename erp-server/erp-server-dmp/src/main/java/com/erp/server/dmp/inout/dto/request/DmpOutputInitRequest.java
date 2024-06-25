package com.erp.server.dmp.inout.dto.request;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;

import lombok.Data;

@Data
public class DmpOutputInitRequest extends DmpOutputTaskRequest{
	private Map<DmpCfgInputConvertEntity, List<DmpInputTaskInitDTO>> convertInputTaskInitDTOListMaps = new HashMap<>();
}
