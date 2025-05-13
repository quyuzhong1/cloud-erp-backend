package com.erp.server.tms.service;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.entity.FirstMileChangeRecordEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.FirstMileChangeRecordDTO;

/**
 * <p>
 * 头程调整记录 服务类
 * </p>
 *
 * @author zdy
 * @since 2025-05-12
 */
public interface FirstMileChangeRecordService extends SuperService<FirstMileChangeRecordEntity> {

    /**
    * 新增
    * @author zdy
    * @date: 2025-05-12
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(FirstMileChangeRecordDTO.AddDTO dto);

    /**
    * 修改
    * @author zdy
    * @date: 2025-05-12
    * @param dto
    * @return
    */
    Boolean update(FirstMileChangeRecordDTO.UpdateDTO dto);

    /**
     * 分页查询
     * @author zdy
     * @date: 2025-05-12
     * @param dto
     * @return
     */
    PagingVO<FirstMileChangeRecordDTO.PagingVO> paging(PagingDTO<FirstMileChangeRecordDTO.PagingParamDTO> dto);

    /**
     * 导出头程调整记录
     * @param dto
     */
    void exportList(FirstMileChangeRecordDTO.PagingParamDTO dto);
}
