package com.erp.server.wms.service;

import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.SoB2bProcessingDTO;
import com.erp.model.wms.entity.SoB2bProcessingEntity;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * B2B虚拟仓订单跟踪 服务类
 * </p>
 *
 * @author will
 * @since 2024-12-18
 */
public interface SoB2bProcessingService extends SuperService<SoB2bProcessingEntity> {


    /**
    * 修改
    * @author will
    * @date: 2024-12-18
    * @param list
    * @return
    */
    Boolean update(List<SoB2bProcessingDTO.AddOrUpdateDTO> list);

    /**
     * 分页查询
     * @author will
     * @date 2024/12/18 11:23
     * @param dto
     * @return PagingVO<ListDTO>
     */
    PagingVO<SoB2bProcessingDTO.ListDTO> paging(PagingDTO<SoB2bProcessingDTO.PagingParamDTO> dto);
    /**
     * 导出
     * @author will
     * @date 2024/12/18 11:51
     * @param dto
     * @return Boolean
     */
    Boolean exportExcel(SoB2bProcessingDTO.PagingParamDTO dto);
    /**
     * 自动更新b2b销售订单跟踪
     * @author will
     * @date 2024/12/18 18:29
     * @param startDate
     */
    void autoUpdateSoB2bProcessing(LocalDate startDate);
}
