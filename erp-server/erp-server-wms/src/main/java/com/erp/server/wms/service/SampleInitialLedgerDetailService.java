package com.erp.server.wms.service;
import com.erp.model.wms.entity.SampleInitialLedgerDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.SampleInitialLedgerDetailDTO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 样品期初台账详情 服务类
 * </p>
 *
 * @author wuhaotian
 * @since 2025-08-21
 */
public interface SampleInitialLedgerDetailService extends SuperService<SampleInitialLedgerDetailEntity> {

    /**
    * 新增
    * @author wuhaotian
    * @date: 2025-08-21
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(SampleInitialLedgerDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author wuhaotian
    * @date: 2025-08-21
    * @param dto
    * @return
    */
    Boolean update(SampleInitialLedgerDetailDTO.UpdateDTO dto);

    /**
     * 导入Excel
     * @author wuhaotian
     * @date: 2025-09-01
     * @param excelFile
     * @param response
     * @return
     */
    SampleInitialLedgerDetailDTO.ImportDTO importFile(MultipartFile excelFile, HttpServletResponse response);

}
