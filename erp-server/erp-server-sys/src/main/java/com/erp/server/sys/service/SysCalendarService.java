package com.erp.server.sys.service;

import com.erp.model.sys.dto.SysCalendarDTO;
import com.erp.model.sys.entity.SysCalendarEntity;
import com.common.core.serveice.SuperService;
import com.erp.model.sys.vo.SysCalendarListVO;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Cloud
 * @since 2023-02-24
 */
public interface SysCalendarService extends SuperService<SysCalendarEntity> {

    /**
     * 根据指定条件查询数据
     * @param dto
     * @return
     */
    List<SysCalendarListVO> listByCondition(SysCalendarDTO.ListDTO dto);

    /**
     * 批量更新或新增数据
     * @param updateDTO
     * @return
     */
    Boolean saveOrUpdateBatchDate(SysCalendarDTO.SaveOrUpdateDTO updateDTO);
}
