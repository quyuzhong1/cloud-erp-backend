package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpOutputTaskEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpOutputTaskDTO;
import com.common.business.vo.PagingVO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 推送任务 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-07-01
 */
public interface DmpOutputTaskService extends SuperService<DmpOutputTaskEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-07-01
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpOutputTaskDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-07-01
    * @param dto
    * @return
    */
    Boolean update(DmpOutputTaskDTO.UpdateDTO dto);

    boolean updateErrorStatus(String id , boolean errorFlag , Integer errorCount , Exception e);

    /**
     * 分页列表查询
     * @author Jim
     * @date: 2025-10-23
     * @param pagingParamDTO
     * @return PagingVO<DmpOutputTaskDTO.ListDTO>>
     */
    PagingVO<DmpOutputTaskDTO.ListDTO> paging(PagingDTO<DmpOutputTaskDTO.PagingParamDTO> pagingParamDTO);

    /**
     * 状态统计
     * @author Jim
     * @date: 2025-10-23
     * @param dto
     * @return List<DmpOutputTaskDTO.TabListDTO>>
     */
    List<DmpOutputTaskDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
     * 详情
     * @author Jim
     * @date: 2025-10-23
     * @param id
     * @return
     */
    DmpOutputTaskDTO.ViewDTO view(String id);

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
    void exportList(DmpOutputTaskDTO.ExportDTO dto, HttpServletResponse response);
}
