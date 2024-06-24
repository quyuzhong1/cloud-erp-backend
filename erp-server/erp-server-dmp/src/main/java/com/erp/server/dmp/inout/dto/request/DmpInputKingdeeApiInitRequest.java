package com.erp.server.dmp.inout.dto.request;

import lombok.Data;

@Data
public class DmpInputKingdeeApiInitRequest extends DmpInputApiInitRequest{
	private String filterStr;
	private String fieldKeys;
	private String formId;
}
