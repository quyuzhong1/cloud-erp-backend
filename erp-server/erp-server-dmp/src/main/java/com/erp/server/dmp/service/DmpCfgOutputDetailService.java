package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpCfgOutputDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpCfgOutputDetailDTO;
import com.common.business.vo.PagingVO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 推送数据配置明细 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
 */
public interface DmpCfgOutputDetailService extends SuperService<DmpCfgOutputDetailEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-06-11
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpCfgOutputDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-06-11
    * @param dto
    * @return
    */
    Boolean update(DmpCfgOutputDetailDTO.UpdateDTO dto);

    /**
    * 分页列表查询
    * @author Jim
    * @date: 2025-10-23
    * @param pagingParamDTO
    * @return PagingVO<DmpCfgOutputDetailDTO.ListDTO>>
    */
    PagingVO<DmpCfgOutputDetailDTO.ListDTO> paging(PagingDTO<DmpCfgOutputDetailDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author Jim
    * @date: 2025-10-23
    * @param dto
    * @return List<DmpCfgOutputDetailDTO.TabListDTO>>
    */
    List<DmpCfgOutputDetailDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author Jim
    * @date: 2025-10-23
    * @param id
    * @return
    */
    DmpCfgOutputDetailDTO.ViewDTO view(String id);

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
    void exportList(DmpCfgOutputDetailDTO.ExportDTO dto, HttpServletResponse response);

    BatchResultDTO enable(DmpCfgOutputDetailEntity entity);

    BatchResultDTO disable(DmpCfgOutputDetailEntity entity);
}
