package com.erp.server.dmp.service;

import com.common.business.dto.base.*;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.DmpBasicSystemDTO;
import com.erp.model.dmp.entity.DmpBasicSystemEntity;
import com.erp.model.plm.dto.DictControllerDTO;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 外部系统 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
 */
public interface DmpBasicSystemService extends SuperService<DmpBasicSystemEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-06-11
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpBasicSystemDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-06-11
    * @param dto
    * @return
    */
    Boolean update(DmpBasicSystemDTO.UpdateDTO dto);

    /**
     * 查询所有系统（下拉接口）
     * @Author Luo_WG
     * @Date 2024/9/5 18:42
     * @return java.util.List<com.erp.model.plm.dto.DictControllerDTO.DictDropDownDTO>
     **/
    List<BaseDropDownDTO.DictDropDownDTO> listDmpBasicSystem();

    /**
     * 根据系统编号查询数据
     * @Author Luo_WG
     * @Date 2024/9/19 10:04
     * @param code
     * @return java.util.List<com.erp.model.dmp.entity.DmpBasicSystemEntity>
     **/
    DmpBasicSystemEntity listByCode(String code);

    /**
     * 分页列表查询
     *
     * @param pagingParamDTO
     * @return PagingVO<DmpBasicSystemDTO.ListDTO>>
     * @author Jim
     * @date: 2025-10-23
     */
    PagingVO<DmpBasicSystemDTO.ListDTO> paging(PagingDTO<DmpBasicSystemDTO.PagingParamDTO> pagingParamDTO);

    /**
     * 状态统计
     *
     * @param dto
     * @return List<DmpBasicSystemDTO.TabListDTO>>
     * @author Jim
     * @date: 2025-10-23
     */
    List<DmpBasicSystemDTO.TabListDTO> tabList(PermissionsDTO dto);


    /**
     * 删除
     *
     * @param id
     * @return
     * @author Jim
     * @date: 2025-10-23
     */
    BatchResultDTO delete(String id);

    /**
     * 详情
     *
     * @param id
     * @return
     * @author Jim
     * @date: 2025-10-23
     */
    DmpBasicSystemDTO.ViewDTO view(String id);


    /**
     * 导出Excel
     *
     * @param dto
     * @param response
     * @return
     * @author Jim
     * @date: 2025-10-23
     */
    void exportList(DmpBasicSystemDTO.ExportDTO dto, HttpServletResponse response);

    /**
     * 启用
     * @param entity 实体
     * @return 批处理对象
     */
    BatchResultDTO enable(DmpBasicSystemEntity entity);

    /**
     * 启用
     * @param entity 实体
     * @return 批处理对象
     */
    BatchResultDTO disable(DmpBasicSystemEntity entity);
}
