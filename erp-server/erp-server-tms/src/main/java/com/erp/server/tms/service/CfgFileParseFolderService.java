package com.erp.server.tms.service;
import com.erp.model.tms.entity.CfgFileParseFolderEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.CfgFileParseFolderDTO;
import com.common.business.vo.PagingVO;
import com.common.business.dto.ApproveDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 清洗配置-文件夹映射子表 服务类
 * </p>
 *
 * @author jack
 * @since 2026-06-29
 */
public interface CfgFileParseFolderService extends SuperService<CfgFileParseFolderEntity> {

    /**
    * 新增
    * @author jack
    * @date: 2026-06-29
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(CfgFileParseFolderDTO.AddDTO dto);

    /**
    * 修改
    * @author jack
    * @date: 2026-06-29
    * @param dto
    * @return
    */
    Boolean update(CfgFileParseFolderDTO.UpdateDTO dto);


    /**
    * 分页列表查询
    * @author jack
    * @date: 2026-06-29
    * @param pagingParamDTO
    * @return PagingVO<CfgFileParseFolderDTO.ListDTO>>
    */
    PagingVO<CfgFileParseFolderDTO.ListDTO> paging(PagingDTO<CfgFileParseFolderDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author jack
    * @date: 2026-06-29
    * @param dto
    * @return List<CfgFileParseFolderDTO.TabListDTO>>
    */
    List<CfgFileParseFolderDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author jack
    * @date: 2026-06-29
    * @param id
    * @return
    */
    CfgFileParseFolderDTO.ViewDTO view(String id);


    /**
    * 导出Excel
    * @author jack
    * @date: 2026-06-29
    * @param dto
    * @param response
    * @return
    */
    void exportList(CfgFileParseFolderDTO.ExportDTO dto, HttpServletResponse response);
}
