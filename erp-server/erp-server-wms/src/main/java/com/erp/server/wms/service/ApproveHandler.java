package com.erp.server.wms.service;

import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BatchResultDTO;

public interface ApproveHandler {
    BatchResultDTO submit(String id, Boolean isProcess);

    String getCodeById(String id);

   BatchResultDTO cancelProcess(ApproveDTO.CancelProcessDTO dto);

    BatchResultDTO approve(ApproveOneDTO approveOneDTO);

    BatchResultDTO disApprove(String id);
}
