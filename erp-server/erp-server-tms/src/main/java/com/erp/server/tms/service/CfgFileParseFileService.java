package com.erp.server.tms.service;
import com.erp.model.tms.entity.CfgFileParseFileEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.CfgFileParseFileDTO;
import com.common.business.vo.PagingVO;
import com.common.business.dto.ApproveDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 清洗配置-文件识别规则子表 服务类
 * </p>
 *
 * @author jack
 * @since 2026-06-29
 */
public interface CfgFileParseFileService extends SuperService<CfgFileParseFileEntity> {

    /**
    * 新增
    * @author jack
    * @date: 2026-06-29
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(CfgFileParseFileDTO.AddDTO dto);

    /**
    * 修改
    * @author jack
    * @date: 2026-06-29
    * @param dto
    * @return
    */
    Boolean update(CfgFileParseFileDTO.UpdateDTO dto);


    /**
    * 分页列表查询
    * @author jack
    * @date: 2026-06-29
    * @param pagingParamDTO
    * @return PagingVO<CfgFileParseFileDTO.ListDTO>>
    */
    PagingVO<CfgFileParseFileDTO.ListDTO> paging(PagingDTO<CfgFileParseFileDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author jack
    * @date: 2026-06-29
    * @param dto
    * @return List<CfgFileParseFileDTO.TabListDTO>>
    */
    List<CfgFileParseFileDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author jack
    * @date: 2026-06-29
    * @param id
    * @return
    */
    CfgFileParseFileDTO.ViewDTO view(String id);


    /**
    * 导出Excel
    * @author jack
    * @date: 2026-06-29
    * @param dto
    * @param response
    * @return
    */
    void exportList(CfgFileParseFileDTO.ExportDTO dto, HttpServletResponse response);
}
