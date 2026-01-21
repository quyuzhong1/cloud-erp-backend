package com.erp.server.tms.service;

import com.common.business.dto.base.BaseDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.ImportHistoryRecordDTO;
import com.erp.model.tms.entity.ImportHistoryRecordEntity;

/**
 * <p>
 * 物流授权表 服务类
 * </p>
 *
 * @author will
 * @since 2026-01-19
 */
public interface ImportHistoryRecordService extends SuperService<ImportHistoryRecordEntity> {

    /**
    * 新增
    * @author will
    * @date: 2026-01-19
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(ImportHistoryRecordDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2026-01-19
    * @param dto
    * @return
    */
    Boolean update(ImportHistoryRecordDTO.UpdateDTO dto);


    /**
    * 分页列表查询
    * @author will
    * @date: 2026-01-19
    * @param pagingParamDTO
    * @return PagingVO<ImportHistoryRecordDTO.ListDTO>>
    */
    PagingVO<ImportHistoryRecordDTO.ListDTO> paging(PagingDTO<ImportHistoryRecordDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 详情
    * @author will
    * @date: 2026-01-19
    * @param id
    * @return
    */
    ImportHistoryRecordDTO.ViewDTO view(String id);
    /**
     * 预处理导入的Excel数据
     * @author will
     * @date 2026/1/20 18:43
     * @param dto
     * @return Boolean
     */
    Boolean preprocessingImportExcel(BaseDTO.ImportDTO dto);
}
