package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpEtlTaskEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpEtlTaskDTO;
import com.common.business.vo.PagingVO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * etl任务 服务类
 * </p>
 *
 * @author shukai
 * @since 2025-07-21
 */
public interface DmpEtlTaskService extends SuperService<DmpEtlTaskEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2025-07-21
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpEtlTaskDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2025-07-21
    * @param dto
    * @return
    */
    Boolean update(DmpEtlTaskDTO.UpdateDTO dto);

    /**
    * 分页列表查询
    * @author Jim
    * @date: 2025-10-23
    * @param pagingParamDTO
    * @return PagingVO<DmpEtlTaskDTO.ListDTO>>
    */
    PagingVO<DmpEtlTaskDTO.ListDTO> paging(PagingDTO<DmpEtlTaskDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author Jim
    * @date: 2025-10-23
    * @param dto
    * @return List<DmpEtlTaskDTO.TabListDTO>>
    */
    List<DmpEtlTaskDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author Jim
    * @date: 2025-10-23
    * @param id
    * @return
    */
    DmpEtlTaskDTO.ViewDTO view(String id);

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
    void exportList(DmpEtlTaskDTO.ExportDTO dto, HttpServletResponse response);

}
