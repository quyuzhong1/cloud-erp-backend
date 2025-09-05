package com.erp.server.oms.service;
import com.erp.model.oms.entity.ExhibitionOrderDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.ExhibitionOrderDetailDTO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 展会订单详情 服务类
 * </p>
 *
 * @author jack
 * @since 2025-08-29
 */
public interface ExhibitionOrderDetailService extends SuperService<ExhibitionOrderDetailEntity> {

    List<ExhibitionOrderDetailDTO.ViewDTO> listViewByMainId(String id);


    ExhibitionOrderDetailDTO.ImportDTO importFile(MultipartFile excelFile,String id, String recipientUserId, Boolean isTax, HttpServletResponse response);
}
