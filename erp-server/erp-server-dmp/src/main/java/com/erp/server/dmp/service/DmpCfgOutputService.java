package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpCfgOutputEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpCfgOutputDTO;
import com.common.business.vo.PagingVO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 推送数据配置 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
 */
public interface DmpCfgOutputService extends SuperService<DmpCfgOutputEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-06-11
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpCfgOutputDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-06-11
    * @param dto
    * @return
    */
    Boolean update(DmpCfgOutputDTO.UpdateDTO dto);

    /**
    * 分页列表查询
    * @author Jim
    * @date: 2025-10-23
    * @param pagingParamDTO
    * @return PagingVO<DmpCfgOutputDTO.ListDTO>>
    */
    PagingVO<DmpCfgOutputDTO.ListDTO> paging(PagingDTO<DmpCfgOutputDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author Jim
    * @date: 2025-10-23
    * @param dto
    * @return List<DmpCfgOutputDTO.TabListDTO>>
    */
    List<DmpCfgOutputDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author Jim
    * @date: 2025-10-23
    * @param id
    * @return
    */
    DmpCfgOutputDTO.ViewDTO view(String id);

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
    void exportList(DmpCfgOutputDTO.ExportDTO dto, HttpServletResponse response);

    BatchResultDTO enable(DmpCfgOutputEntity entity);

    BatchResultDTO disable(DmpCfgOutputEntity entity);
}
