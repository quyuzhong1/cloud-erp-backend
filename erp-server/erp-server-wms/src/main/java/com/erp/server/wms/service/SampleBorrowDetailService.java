package com.erp.server.wms.service;
import com.erp.model.wms.entity.SampleBorrowDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.SampleBorrowDetailDTO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 借用变更单明细表 服务类
 * </p>
 *
 * @author jack
 * @since 2025-08-20
 */
public interface SampleBorrowDetailService extends SuperService<SampleBorrowDetailEntity> {


    SampleBorrowDetailDTO.ImportDTO importFile(MultipartFile excelFile, HttpServletResponse response);
}
