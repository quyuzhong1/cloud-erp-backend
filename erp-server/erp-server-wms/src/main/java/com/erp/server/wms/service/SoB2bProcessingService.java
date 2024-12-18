package com.erp.server.wms.service;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.entity.SoB2bProcessingEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.SoB2bProcessingDTO;

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
    * 新增
    * @author will
    * @date: 2024-12-18
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(SoB2bProcessingDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2024-12-18
    * @param dto
    * @return
    */
    Boolean update(SoB2bProcessingDTO.UpdateDTO dto);

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
}
