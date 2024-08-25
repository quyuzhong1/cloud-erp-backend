package com.erp.server.tms.service;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.model.tms.entity.FirstMileCostAllocationEntity;
import com.erp.model.tms.entity.ReportPeriodMonthEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.ReportPeriodMonthDTO;

import java.util.List;

/**
 * <p>
 * 核算期间月份表 服务类
 * </p>
 *
 * @author zdy
 * @since 2024-08-20
 */
public interface ReportPeriodMonthService extends SuperService<ReportPeriodMonthEntity> {

    /**
    * 新增
    * @author zdy
    * @date: 2024-08-20
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(ReportPeriodMonthDTO.AddDTO dto);

    /**
    * 修改
    * @author zdy
    * @date: 2024-08-20
    * @param dto
    * @return
    */
    Boolean update(ReportPeriodMonthDTO.UpdateDTO dto);

    /**
     * 检查并创建核算记录
     * @param company
     * @param entity
     * @return
     */
    String createOrUpdatePeriod(SysAccountingCompanyEntity company, FirstMileCostAllocationEntity entity);

    /**
     * 获取所有核算期间下拉框
     * @param dto
     * @return
     */
    List<ReportPeriodMonthDTO.SelectDTO> queryList(ReportPeriodMonthDTO.QueryDTO dto);
}
