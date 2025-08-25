package com.erp.server.wms.service;

import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.FirstMileProcessingDTO;
import com.erp.model.wms.dto.ReportProcessingDTO;
import com.erp.model.wms.entity.FirstMileProcessingEntity;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 头程虚拟仓订单跟踪 服务类
 * </p>
 *
 * @author will
 * @since 2024-12-18
 */
public interface FirstMileProcessingService extends SuperService<FirstMileProcessingEntity> {

    /**
    * 修改
    * @author will
    * @date: 2024-12-18
    * @param list
    * @return
    */
    Boolean addOrupdate(List<FirstMileProcessingDTO.AddOrUpdateDTO> list,LocalDate startDate);

    /**
     * 分页查询
     * @author will
     * @date 2024/12/18 11:47
     * @param dto
     * @return PagingVO<ListDTO>
     */
    PagingVO<FirstMileProcessingDTO.ListDTO> paging(PagingDTO<FirstMileProcessingDTO.PagingParamDTO> dto);

    /**
     * 导出分页列表数据
     * @param dto
     * @return
     */
    PagingVO<FirstMileProcessingDTO.ListDTO> exportPaging(PagingDTO<FirstMileProcessingDTO.PagingParamDTO> dto);
    /**
     * 导出
     * @author will
     * @date 2024/12/18 12:13
     * @param dto
     * @return Boolean
     */
    Boolean exportExcel(FirstMileProcessingDTO.PagingParamDTO dto);
    /**
     * 自动更新头程订单跟踪
     * @author will
     * @date 2024/12/18 18:30
     * @param startDate
     */
    void autoUpdateFirstMileProcessing(LocalDate startDate);
    /**
     * 删除订单跟踪数据
     * @author will
     * @date 2025/2/18 09:56
     * @param dto 
     * @return java.lang.Boolean
     */
    Boolean deleteFirstMileProcessing(FirstMileProcessingDTO.DeleteDTO dto);
    /**
     * 头程合计分页查询
     * @author will
     * @date 2025/8/20 14:10
     * @param dto
     * @return PagingVO<ListDTO>
     */
    PagingVO<ReportProcessingDTO.ListDTO> firstMileTotalPaging(PagingDTO<ReportProcessingDTO.PagingParamDTO> dto);
    /**
     * 头程汇总导出
     * @author will
     * @date 2025/8/21 14:27
     * @param dto
     * @return Boolean
     */
    Boolean firstMileTotalExportExcel(ReportProcessingDTO.PagingParamDTO dto);
}
