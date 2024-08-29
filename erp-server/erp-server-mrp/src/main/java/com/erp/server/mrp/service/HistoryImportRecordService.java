package com.erp.server.mrp.service;
import com.common.business.vo.PagingVO;
import com.erp.model.mrp.entity.HistoryImportRecordEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.mrp.dto.HistoryImportRecordDTO;

/**
 * <p>
 * 历史导入记录 服务类
 * </p>
 *
 * @author will
 * @since 2024-08-27
 */
public interface HistoryImportRecordService extends SuperService<HistoryImportRecordEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-08-27
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(HistoryImportRecordDTO.AddDTO dto);

    /**
     * 分页查询
     * @author will
     * @date 2024/8/29 9:54
     * @param dto
     * @return PagingVO<ListDTO>
     */
    PagingVO<HistoryImportRecordDTO.ListDTO> paging(PagingDTO<HistoryImportRecordDTO.PagingParamDTO> dto);
}
