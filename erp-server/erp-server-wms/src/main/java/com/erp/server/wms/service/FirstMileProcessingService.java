package com.erp.server.wms.service;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.entity.FirstMileProcessingEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.FirstMileProcessingDTO;

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
    * 新增
    * @author will
    * @date: 2024-12-18
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(FirstMileProcessingDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2024-12-18
    * @param dto
    * @return
    */
    Boolean update(FirstMileProcessingDTO.UpdateDTO dto);

    /**
     * 分页查询
     * @author will
     * @date 2024/12/18 11:47
     * @param dto
     * @return PagingVO<ListDTO>
     */
    PagingVO<FirstMileProcessingDTO.ListDTO> paging(PagingDTO<FirstMileProcessingDTO.PagingParamDTO> dto);
    /**
     * 导出
     * @author will
     * @date 2024/12/18 12:13
     * @param dto
     * @return Boolean
     */
    Boolean exportExcel(FirstMileProcessingDTO.PagingParamDTO dto);
}
