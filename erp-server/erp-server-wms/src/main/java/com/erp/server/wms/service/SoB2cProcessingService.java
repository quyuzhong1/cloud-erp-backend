package com.erp.server.wms.service;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.entity.SoB2cProcessingEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.SoB2cProcessingDTO;

/**
 * <p>
 * B2C虚拟仓订单跟踪 服务类
 * </p>
 *
 * @author will
 * @since 2024-12-18
 */
public interface SoB2cProcessingService extends SuperService<SoB2cProcessingEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-12-18
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(SoB2cProcessingDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2024-12-18
    * @param dto
    * @return
    */
    Boolean update(SoB2cProcessingDTO.UpdateDTO dto);

    /**
     * 分页查询
     * @author will
     * @date 2024/12/18 11:31
     * @param dto
     * @return PagingVO<ListDTO>
     */
    PagingVO<SoB2cProcessingDTO.ListDTO> paging(PagingDTO<SoB2cProcessingDTO.PagingParamDTO> dto);
    /**
     * 导出
     * @author will
     * @date 2024/12/18 11:55
     * @param dto
     * @return Boolean
     */
    Boolean exportExcel(SoB2cProcessingDTO.PagingParamDTO dto);
}
