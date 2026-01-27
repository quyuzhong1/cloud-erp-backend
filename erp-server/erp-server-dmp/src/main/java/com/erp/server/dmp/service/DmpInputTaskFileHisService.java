package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpInputTaskFileHisEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpInputTaskFileHisDTO;
import com.common.business.vo.PagingVO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 拉取任务文件存储归档 服务类
 * </p>
 *
 * @author shukai
 * @since 2026-01-26
 */
public interface DmpInputTaskFileHisService extends SuperService<DmpInputTaskFileHisEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2026-01-26
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpInputTaskFileHisDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2026-01-26
    * @param dto
    * @return
    */
    Boolean update(DmpInputTaskFileHisDTO.UpdateDTO dto);


    /**
    * 分页列表查询
    * @author shukai
    * @date: 2026-01-26
    * @param pagingParamDTO
    * @return PagingVO<DmpInputTaskFileHisDTO.ListDTO>>
    */
    PagingVO<DmpInputTaskFileHisDTO.ListDTO> paging(PagingDTO<DmpInputTaskFileHisDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author shukai
    * @date: 2026-01-26
    * @param dto
    * @return List<DmpInputTaskFileHisDTO.TabListDTO>>
    */
    List<DmpInputTaskFileHisDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author shukai
    * @date: 2026-01-26
    * @param id
    * @return
    */
    DmpInputTaskFileHisDTO.ViewDTO view(String id);


    /**
    * 导出Excel
    * @author shukai
    * @date: 2026-01-26
    * @param dto
    * @param response
    * @return
    */
    void exportList(DmpInputTaskFileHisDTO.ExportDTO dto, HttpServletResponse response);
}
