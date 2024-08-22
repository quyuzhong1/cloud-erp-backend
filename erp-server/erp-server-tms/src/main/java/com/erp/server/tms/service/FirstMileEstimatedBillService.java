package com.erp.server.tms.service;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.FirstMileEstimatedBillDTO;
import com.erp.model.tms.entity.FirstMileEstimatedBillEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 头程暂估账单业务接口
 * @date 2024-08-16
 * @author tanmujin
 */
public interface FirstMileEstimatedBillService extends SuperService<FirstMileEstimatedBillEntity> {
    /**
     * 高级查询
     */
    PagingVO<FirstMileEstimatedBillDTO.View> paging(PagingDTO<FirstMileEstimatedBillDTO.PagingParam> dto);

    /**
     * 更新状态
     */
    BatchResultDTO updateStatus(String id, String status);

    /**
     * 新增暂估账单
     * @param id 头程物流单ID
     */
    BaseResultDTO.AddDTO add(String id);

    List<FirstMileEstimatedBillDTO.Tab> tabList();

    void importExcel(MultipartFile excelFile, HttpServletResponse response);

    void exportExcel(FirstMileEstimatedBillDTO.ExportParam dto, HttpServletResponse response);
}
