package com.erp.server.oms.service;

import com.common.business.dto.base.BaseDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.KolSampleCostDTO;
import com.erp.model.oms.entity.KolSampleCostEntity;

import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 寄样费用表 服务类
 * </p>
 *
 * @author will
 * @since 2025-12-01
 */
public interface KolSampleCostService extends SuperService<KolSampleCostEntity> {

    /**
    * 新增
    * @author will
    * @date: 2025-12-01
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(KolSampleCostDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2025-12-01
    * @param dto
    * @return
    */
    Boolean update(KolSampleCostDTO.UpdateDTO dto);

    /**
     * 分页查询
     * @author will
     * @date 2025/12/8 10:18
     * @param dto
     * @return PagingVO<ListDTO>
     */
    PagingVO<KolSampleCostDTO.ListDTO> paging(PagingDTO<KolSampleCostDTO.PagingParamDTO> dto);
    /**
     * 更新费用
     * @author will
     * @date 2025/12/8 10:25
     * @param dto
     * @return void
     */
    void updateCost(KolSampleCostDTO.UpdateCostDTO dto);
    /**
     * 导出
     * @author will
     * @date 2025/12/8 10:28
     * @param dto
     * @param response
     * @return void
     */
    void exportList(KolSampleCostDTO.ExportDTO dto, HttpServletResponse response);
    /**
     * 导入
     * @author will
     * @date 2025/12/8 10:32
     * @param dto
     * @param response
     * @return Boolean
     */
    Boolean importFile(BaseDTO.ImportDTO dto, HttpServletResponse response);
    /**
     * 寄样费用定时更新任务
     * @author will
     * @date 2025/12/9 14:37
     * @return void
     */
    void updateKolSampleCostJob();
}
