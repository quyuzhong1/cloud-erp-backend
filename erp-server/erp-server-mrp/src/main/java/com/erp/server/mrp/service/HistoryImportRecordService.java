package com.erp.server.mrp.service;
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
    * 修改
    * @author will
    * @date: 2024-08-27
    * @param dto
    * @return
    */
    Boolean update(HistoryImportRecordDTO.UpdateDTO dto);


}
