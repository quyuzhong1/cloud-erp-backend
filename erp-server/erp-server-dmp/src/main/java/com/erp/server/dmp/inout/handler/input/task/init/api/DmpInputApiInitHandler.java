package com.erp.server.dmp.inout.handler.input.task.init.api;

import java.util.List;

import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputApiInitRequest;

public interface DmpInputApiInitHandler {
	List<DmpInputTaskInitDTO> getApiData(DmpInputApiInitRequest dmpInputApiInitRequest);
}
