package com.erp.server.wms.service;

import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.*;
import com.common.business.service.SuperService;
import com.common.business.validator.ValidList;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.QcApplicationDTO;
import com.erp.model.wms.dto.excel.QcApplicationImportExcelDTO;
import com.erp.model.wms.entity.QcApplicationEntity;

import java.util.List;

/**
 * <p>
 * 质检申请单主表 服务类
 * </p>
 *
 * @author will
 * @since 2026-03-20
 */
public interface QcApplicationService extends SuperService<QcApplicationEntity> {

    /**
    * 新增
    * @author will
    * @date: 2026-03-20
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(QcApplicationDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2026-03-20
    * @param dto
    * @return
    */
    Boolean update(QcApplicationDTO.UpdateDTO dto);


    /**
    * 分页列表查询
    * @author will
    * @date: 2026-03-20
    * @param pagingParamDTO
    * @return PagingVO<QcApplicationDTO.ListDTO>>
    */
    PagingVO<QcApplicationDTO.ListDTO> paging(PagingDTO<QcApplicationDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author will
    * @date: 2026-03-20
    * @param dto
    * @return List<QcApplicationDTO.TabListDTO>>
    */
    List<QcApplicationDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author will
    * @date: 2026-03-20
    * @param id
    * @return
    */
    QcApplicationDTO.ViewDTO view(String id);

     /**
     * 提交审核
     * @author will
     * @date: 2026-03-20
     * @param id
     * @return
     */
    BatchResultDTO submit(String id);

    /**
    * 审核
    * @author will
    * @date: 2026-03-20
    * @param dto
    * @return
    */
    BatchResultDTO approve(ApproveOneDTO dto);

    /**
    * 反审核
    * @author will
    * @date: 2026-03-20
    * @param id
    * @return
    */
    BatchResultDTO disApprove(String id);

    /**
    * 删除
    * @author will
    * @date: 2026-03-20
    * @param id
    * @return
    */
    BatchResultDTO delete(String id);

    /**
    * 撤销
    * @author will
    * @date: 2026-03-20
    * @param id
    * @return
    */
    BatchResultDTO cancelProcess(ApproveDTO.CancelProcessDTO dto);

    /**
    * 审核通过回调方法
    * @param dto
    * @param entity
    * @return
    */
    Boolean approveEnd(ApproveOneDTO dto, QcApplicationEntity entity);

    /**
    * 导出Excel
    * @author will
    * @date: 2026-03-20
    * @param dto
    * @return
    */
    Boolean exportList(QcApplicationDTO.PagingParamDTO dto);
    /**
     *  下推质检通知数据回显
     * @author will
     * @date 2026/3/23 12:25
     * @param ids
     * @return  List<QcApplicationDTO.ListPushQcNoticeDTO>
     */
    List<QcApplicationDTO.ListPushQcNoticeDTO> listPushQcNotice( List<String> ids);
    /**
     *  下推质检通知保存
     * @author will
     * @date 2026/3/23 12:25
     * @param list
     * @return  Boolean
     */
    Boolean generateQcNotice(ValidList<QcApplicationDTO.GenerateQcNoticeDTO> list);
}
