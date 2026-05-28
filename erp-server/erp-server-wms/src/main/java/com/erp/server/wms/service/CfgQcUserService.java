package com.erp.server.wms.service;

import com.erp.model.wms.entity.CfgQcUserEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.CfgQcUserDTO;
import com.common.business.vo.PagingVO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 质检员配置服务类
 * </p>
 *
 * @author wtr
 * @since 2026-05-27
 */
public interface CfgQcUserService extends SuperService<CfgQcUserEntity> {

    /**
    * 新增
    * @author wtr
    * @date: 2026-05-27
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(CfgQcUserDTO.AddDTO dto);

    /**
    * 修改
    * @author wtr
    * @date: 2026-05-27
    * @param dto
    * @return
    */
    Boolean update(CfgQcUserDTO.UpdateDTO dto);


    /**
    * 分页列表查询
    * @author wtr
    * @date: 2026-05-27
    * @param pagingParamDTO
    * @return PagingVO<CfgQcUserDTO.ListDTO>>
    */
    PagingVO<CfgQcUserDTO.ListDTO> paging(PagingDTO<CfgQcUserDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 详情
    * @author wtr
    * @date: 2026-05-27
    * @param id
    * @return
    */
    CfgQcUserDTO.ViewDTO view(String id);


    /**
    * 导出Excel
    * @author wtr
    * @date: 2026-05-27
    * @param dto
    * @param response
    * @return
    */
    void exportList(CfgQcUserDTO.ExportDTO dto, HttpServletResponse response);

    /**
    * 导入Excel（异步）
    * @author wtr
    * @date: 2026-05-27
    * @param dto
    * @return
    */
    Boolean importFile(BaseDTO.ImportDTO dto);

    /**
    * 导入Excel（实际执行）
    * @author wtr
    * @date: 2026-05-27
    * @param dto
    */
    void importCfgQcUser(BaseDTO.ImportDTO dto);

    /**
    * 获取质检员下拉列表（根据组织查询）
    * @author wtr
    * @date: 2026-05-27
    * @param warehouseId
    * @return
    */
    List<CfgQcUserDTO.QcUserSelectDTO> qcUserList(String warehouseId);

    /**
    * 删除
    * @author wtr
    * @date: 2026-05-27
    * @param id
    * @return BatchResultDTO
    */
    BatchResultDTO delete(String id);

    /**
    * 根据供应商ID查询质检员配置
    * @author wtr
    * @date: 2026-05-28
    * @param supplierId
    * @return
    */
    CfgQcUserEntity getBySupplierId(String supplierId);

}