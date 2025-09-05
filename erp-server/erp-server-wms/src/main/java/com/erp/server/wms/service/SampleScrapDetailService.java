package com.erp.server.wms.service;
import com.erp.model.wms.dto.SampleScrapInfoDTO;
import com.erp.model.wms.entity.SampleScrapDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.SampleScrapDetailDTO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 样品报废单明细表 服务类
 * </p>
 *
 * @author jack
 * @since 2025-08-20
 */
public interface SampleScrapDetailService extends SuperService<SampleScrapDetailEntity> {

    /**
     * 导入Excel
     * @author jack
     * @date: 2025-08-20
     * @param excelFile
     * @param response
     * @return
     */
    SampleScrapDetailDTO.ImportDTO importFile(MultipartFile excelFile, HttpServletResponse response);

    List<SampleScrapDetailEntity> listByMainId(String id);

    Map<String,Integer> listBySku(String id,List<String> skuNos);
}
