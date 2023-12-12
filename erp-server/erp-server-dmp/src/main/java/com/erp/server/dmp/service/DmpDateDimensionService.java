package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpDateDimensionEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpDateDimensionDTO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 时间维度表 服务类
 * </p>
 *
 * @author zdy
 * @since 2023-12-08
 */
public interface DmpDateDimensionService extends SuperService<DmpDateDimensionEntity> {

    /**
    * 新增
    * @author zdy
    * @date: 2023-12-08
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpDateDimensionDTO.AddDTO dto);

    /**
    * 修改
    * @author zdy
    * @date: 2023-12-08
    * @param dto
    * @return
    */
    Boolean update(DmpDateDimensionDTO.UpdateDTO dto);

    /**
     * 按照年维度删除数据
     *
     * @param year
     */
    void deleteByYear(String year);

    /**
     * 按照时间维度进行批量新增
     *
     * @param dateTimes
     */
    void batchInsertDateDimensions(List<LocalDateTime> dateTimes);
}
