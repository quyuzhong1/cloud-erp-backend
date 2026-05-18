package com.erp.server.wms.service;

import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.AfterSalePackDTO;
import com.erp.model.wms.entity.AfterSalePackEntity;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 售后装箱表 服务类
 * </p>
 *
 * @author lei.nie
 * @since 2026-05-12
 */
public interface AfterSalePackService extends SuperService<AfterSalePackEntity> {

    /**
     * 申请箱唛
     *
     * @param dto AfterSalePackDTO.BoxCodeApplicationDTO
     * @return List<String>
     * @author lei.nie
     * @date: 2026-05-12
     */
    List<String> boxCodeApplication(AfterSalePackDTO.BoxCodeApplicationDTO dto);

    /**
     * 新增
     *
     * @param dto
     * @return
     * @author lei.nie
     * @date: 2026-05-12
     */
    BaseResultDTO.AddDTO add(AfterSalePackDTO.AddDTO dto);

    /**
     * 修改
     *
     * @param dto AfterSalePackDTO.UpdateDTO
     * @return Boolean
     * @author lei.nie
     * @date: 2026-05-12
     */
    Boolean update(AfterSalePackDTO.UpdateDTO dto);

    /**
     * 确定提审
     *
     * @param dto AfterSalePackDTO.UpdateDTO
     * @return Boolean
     * @author lei.nie
     * @date: 2026-05-12
     */
    Boolean submit(AfterSalePackDTO.UpdateDTO dto);

    /**
     * 复核驳回
     *
     * @param dto AfterSalePackDTO.UpdateDTO
     * @return Boolean
     * @author lei.nie
     * @date: 2026-05-12
     */
    Boolean reject(AfterSalePackDTO.UpdateDTO dto);

    /**
     * 确认并封箱
     *
     * @param dto AfterSalePackDTO.UpdateDTO
     * @return ApiResult
     * @author lei.nie
     * @date: 2026-05-12
     */
    Boolean confirm(AfterSalePackDTO.UpdateDTO dto);

    /**
     * 分页列表查询
     *
     * @param pagingParamDTO
     * @return PagingVO<AfterSalePackDTO.ListDTO>>
     * @author lei.nie
     * @date: 2026-05-12
     */
    PagingVO<AfterSalePackDTO.ListDTO> paging(PagingDTO<AfterSalePackDTO.PagingParamDTO> pagingParamDTO);

    /**
     * 详情
     *
     * @param id
     * @return
     * @author lei.nie
     * @date: 2026-05-12
     */
    AfterSalePackDTO.ViewDTO view(String id);

    /**
     * 导出Excel
     *
     * @param dto
     * @param response
     * @return
     * @author lei.nie
     * @date: 2026-05-12
     */
    void exportList(AfterSalePackDTO.ExportDTO dto, HttpServletResponse response);

    /**
     * 删除
     *
     * @param dto BaseIdsDTO.IdsDTO
     * @return List<BatchResultDTO>
     * @author lei.nie
     * @date: 2026-05-12
     */
    List<BatchResultDTO> delete(BaseIdsDTO.IdsDTO dto);

    /**
     * 根据code查询详情
     *
     * @param code String
     * @return AfterSalePackDTO.ViewDTO
     * @author lei.nie
     * @date: 2026-05-12
     */
    AfterSalePackDTO.ViewDTO viewByCode(String code);

    /**
     * 根据code批量查询详情
     *
     * @param codes 箱唛号列表
     * @return List<AfterSalePackDTO.ViewDTO>
     */
    List<AfterSalePackDTO.ViewDTO> viewByCodes(List<String> codes);

    /**
     * 将指定装箱单批量标记为已移仓（is_move_warehouse = true）。
     * <p>
     * 方法内部会<b>重新从库查询</b>最新的 is_move_warehouse 状态，若发现已有箱唛被标记为已移仓
     * （说明存在并发/重复提交），直接抛出异常终止整个事务，防止重复生成移仓单。
     * 确认均未移仓后，使用条件更新（WHERE is_move_warehouse = false）保证原子性。
     *
     * @param ids after_sale_pack 主键列表
     */
    void markBoxesAsMoved(List<String> ids);

}
