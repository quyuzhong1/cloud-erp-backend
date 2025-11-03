package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpCfgEtlEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpCfgEtlDTO;
import com.common.business.vo.PagingVO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * etl配置信息 服务类
 * </p>
 *
 * @author shukai
 * @since 2025-07-21
 */
public interface DmpCfgEtlService extends SuperService<DmpCfgEtlEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2025-07-21
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpCfgEtlDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2025-07-21
    * @param dto
    * @return
    */
    Boolean update(DmpCfgEtlDTO.UpdateDTO dto);

    /**
    * 分页列表查询
    * @author Jim
    * @date: 2025-10-23
    * @param pagingParamDTO
    * @return PagingVO<DmpCfgEtlDTO.ListDTO>>
    */
    PagingVO<DmpCfgEtlDTO.ListDTO> paging(PagingDTO<DmpCfgEtlDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author Jim
    * @date: 2025-10-23
    * @param dto
    * @return List<DmpCfgEtlDTO.TabListDTO>>
    */
    List<DmpCfgEtlDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author Jim
    * @date: 2025-10-23
    * @param id
    * @return
    */
    DmpCfgEtlDTO.ViewDTO view(String id);


    /**
    * 删除
    * @author Jim
    * @date: 2025-10-23
    * @param id
    * @return
    */
    BatchResultDTO delete(String id);

    /**
    * 导出Excel
    * @author Jim
    * @date: 2025-10-23
    * @param dto
    * @param response
    * @return
    */
    void exportList(DmpCfgEtlDTO.ExportDTO dto, HttpServletResponse response);


    /**
     * 启用
     * @author Jim
     * @date: 2025-10-23
     * @param entity
     * @return
     */
    BatchResultDTO enable(DmpCfgEtlEntity entity);

    /**
     * 禁用
     * @author Jim
     * @date: 2025-10-23
     * @param entity
     * @return
     */
    BatchResultDTO disable(DmpCfgEtlEntity entity);
}
