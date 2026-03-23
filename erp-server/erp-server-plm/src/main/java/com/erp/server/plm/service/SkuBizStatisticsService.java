package com.erp.server.plm.service;
import com.erp.model.plm.entity.SkuBizStatisticsEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.plm.dto.SkuBizStatisticsDTO;
import com.common.business.vo.PagingVO;
import com.common.business.dto.ApproveDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * sku业务统计表 服务类
 * </p>
 *
 * @author shukai
 * @since 2026-03-16
 */
public interface SkuBizStatisticsService extends SuperService<SkuBizStatisticsEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2026-03-16
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(SkuBizStatisticsDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2026-03-16
    * @param dto
    * @return
    */
    Boolean update(SkuBizStatisticsDTO.UpdateDTO dto);


    /**
    * 分页列表查询
    * @author shukai
    * @date: 2026-03-16
    * @param pagingParamDTO
    * @return PagingVO<SkuBizStatisticsDTO.ListDTO>>
    */
    PagingVO<SkuBizStatisticsDTO.ListDTO> paging(PagingDTO<SkuBizStatisticsDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author shukai
    * @date: 2026-03-16
    * @param dto
    * @return List<SkuBizStatisticsDTO.TabListDTO>>
    */
    List<SkuBizStatisticsDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author shukai
    * @date: 2026-03-16
    * @param id
    * @return
    */
    SkuBizStatisticsDTO.ViewDTO view(String id);


    /**
    * 导出Excel
    * @author shukai
    * @date: 2026-03-16
    * @param dto
    * @param response
    * @return
    */
    void exportList(SkuBizStatisticsDTO.ExportDTO dto, HttpServletResponse response);
}
