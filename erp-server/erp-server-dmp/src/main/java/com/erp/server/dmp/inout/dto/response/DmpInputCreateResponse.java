package com.erp.server.dmp.inout.dto.response;

import com.erp.model.dmp.entity.DmpCfgInputEntity;

import lombok.Data;

@Data
public class DmpInputCreateResponse extends DmpInputResponse{
	private DmpCfgInputEntity dmpCfgInputEntity;
}
