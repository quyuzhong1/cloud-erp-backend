package com.erp.server.dmp.service;
import com.erp.model.dmp.dto.DmpInoutDTO;
import com.erp.model.dmp.entity.DmpCfgInputDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpCfgInputDetailDTO;
import com.common.business.vo.PagingVO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 外部系统接口明细 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
 */
public interface DmpCfgInputDetailService extends SuperService<DmpCfgInputDetailEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-06-11
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpCfgInputDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-06-11
    * @param dto
    * @return
    */
    Boolean update(DmpCfgInputDetailDTO.UpdateDTO dto);


    /**
     * 根据系统代号和业务代号查询新中台任务
     * @param systemCodeList 系统代号(不分大小写)
     * @param billTypeList 业务类型(对应)
     * @param nextLevelIdList 下一级ID(店铺ID/授权ID)
     * @return 可执行的任务列表-明细维度
     */
    List<DmpInoutDTO.ListDTO> listBySystemCodeAndBillType(List<String> systemCodeList, List<String> billTypeList, List<String> nextLevelIdList);

    /**
     * 分页列表查询
     * @author Jim
     * @date: 2025-10-23
     * @param pagingParamDTO
     * @return PagingVO<DmpCfgInputDetailDTO.ListDTO>>
     */
    PagingVO<DmpCfgInputDetailDTO.ListDTO> paging(PagingDTO<DmpCfgInputDetailDTO.PagingParamDTO> pagingParamDTO);

    /**
     * 状态统计
     * @author Jim
     * @date: 2025-10-23
     * @param dto
     * @return List<DmpCfgInputDetailDTO.TabListDTO>>
     */
    List<DmpCfgInputDetailDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
     * 详情
     * @author Jim
     * @date: 2025-10-23
     * @param id
     * @return
     */
    DmpCfgInputDetailDTO.ViewDTO view(String id);

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
    void exportList(DmpCfgInputDetailDTO.ExportDTO dto, HttpServletResponse response);

    BatchResultDTO enable(DmpCfgInputDetailEntity entity);

    BatchResultDTO disable(DmpCfgInputDetailEntity entity);
}
