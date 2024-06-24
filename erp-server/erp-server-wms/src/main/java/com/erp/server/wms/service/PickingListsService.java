package com.erp.server.wms.service;

import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.pickingstrategy.PickingListsDTO;
import com.erp.model.wms.entity.PickingListsEntity;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 拣货单 服务类
 * </p>
 *
 * @author liaohui
 * @since 2024-06-07
 */
public interface PickingListsService extends SuperService<PickingListsEntity> {
    /**
     * 分页查询
     *
     * @param dto 分页查询条件
     */
    PagingVO<PickingListsDTO.PagingView> paging(PagingDTO<PickingListsDTO.PagingParam> dto);

    /**
     * 修改
     *
     * @param dto 编辑参数
     **/
    void add(PickingListsDTO.AddDTO dto);

    /**
     * 批量删除
     *
     * @param id id
     **/
    void delete(String id);
    /**
     * 查询详情
     *
     * @param id id
     **/
    PickingListsDTO.View view(String id);
    /**
     * 批量打印
     *
     * @param dto dto
     **/
    void export(PickingListsDTO.ExportDTO dto, HttpServletResponse response);
    /**
     * 批量打印
     *
     * @param ids ids
     */
    List<PickingListsDTO.PrintView> print(List<String> ids);
    /**
     * 修改
     *
     * @param dto 编辑参数
     **/
    void update(PickingListsDTO.UpdateDTO dto);

    /**
     * 删除拣货单
     * @param ids id
     */
    void deleteBySourceId(List<String> ids);

    /**
     * 根据来源id查询拣货单及明细
     */
    List<PickingListsDTO.SourceView> listBySourceIds(List<String> sourceIds);

    void exist(String id);
    void exist(List<String> id);
}
