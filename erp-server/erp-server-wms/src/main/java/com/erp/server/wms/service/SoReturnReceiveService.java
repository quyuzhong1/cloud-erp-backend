package com.erp.server.wms.service;

import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.SoDetailDTO;
import com.erp.model.oms.dto.listAddDetailViewDTO;
import com.erp.model.wms.dto.QcInfoDTO;
import com.erp.model.wms.dto.SoReturnNoticeDTO;
import com.erp.model.wms.dto.SoReturnReceiveDTO;
import com.erp.model.wms.entity.SoReturnReceiveEntity;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 采购退货签收单
 * @author Luo_WG
 * @since 2023-05-10
 */
public interface SoReturnReceiveService extends SuperService<SoReturnReceiveEntity> {
    /**
     * 分页查询
     * @Author Luo_WG
     * @Date 2023/4/13 15:41
     * @param pagingParamDTO pagingParamDTO
     * @return com.common.business.vo.PagingVO<com.erp.model.wms.dto.SoReturnReceiveDTO.PagingViewDTO>
     **/
    PagingVO<SoReturnReceiveDTO.PagingView> paging(PagingDTO<SoReturnReceiveDTO.PagingParam> pagingParamDTO);

    /**
     * 列表状态数量统计
     * @Author Luo_WG
     * @Date 2023/4/17 13:13
     * @param dto dto
     * @return java.util.List<com.erp.model.wms.dto.SoReturnReceiveDTO.StatusCountDTO>
     **/
    List<SoReturnReceiveDTO.StatusCountDTO> listCount(PermissionsDTO dto);

    /**
     * 新增
     * @Author Luo_WG
     * @Date 2023/4/13 11:03
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    String add(SoReturnReceiveDTO.Add dto);

    /**
     * 修改
     * @Author Luo_WG
     * @Date 2023/4/13 14:51
     * @param dto dto
     * @return java.lang.Boolean
     **/
    Boolean update(SoReturnReceiveDTO.Update dto);

    /**
     * 查询详情
     * @Author Luo_WG
     * @Date 2023/4/13 17:10
     * @param id id
     * @return com.erp.model.wms.dto.SoReturnReceiveDTO.ViewDTO
     **/
    SoReturnReceiveDTO.View view(String id);

    /**
     * 提交
     * @Author Luo_WG
     * @Date 2023/4/14 10:04
     * @param ids ids
     * @return java.lang.Boolean
     **/
    Boolean submit(List<String> ids);

    /**
     * 新增提交
     * @Author Luo_WG
     * @Date 2023/4/6 18:52
     * @param dto dto
     * @return java.lang.Boolean
     **/
    Boolean addAndSubmit(SoReturnReceiveDTO.Add dto);

    /**
     * 修改提交
     * @Author Luo_WG
     * @Date 2023/4/6 18:52
     * @param dto dto
     * @return java.lang.Boolean
     **/
    Boolean updateAndSubmit(SoReturnReceiveDTO.Update dto);

    /**
     * 批量审核
     * @Author Luo_WG
     * @Date 2023/4/6 19:06
     * @param entity
     * @param type
     * @param comment
     * @param isNeedProcess
     * @return java.lang.Boolean
     **/
    BatchResultDTO approve(SoReturnReceiveEntity entity, String type, String comment, Boolean isNeedProcess);

    /**
     * 批量反审核
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     * @param entity
     * @return java.lang.Boolean
     **/
    BatchResultDTO disApprove(SoReturnReceiveEntity entity);

    /**
     * 取消流程
     * @Author Luo_WG
     * @Date 2023/4/13 18:58
     * @param ids ids
     * @return java.lang.Boolean
     **/
    Boolean cancelProcess(List<String> ids);

    /**
     * 批量作废
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     * @param ids ids
     * @param remark remark
     * @return java.lang.Boolean
     **/
    Boolean invalid(List<String> ids, String remark);

    /**
     * 批量删除
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     * @param ids ids
     * @return java.lang.Boolean
     **/
    Boolean delete(List<String> ids);

    /**
     * 导出
     *
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     * @Author Luo_WG
     * @Date 2023/4/13 18:59
     **/
    Boolean exportExcel(@RequestBody SoReturnReceiveDTO.PagingParam dto);

    /**
     * 下推退货签收单-保存
     * @Author Luo_WG
     * @Date 2023/5/11 11:35
     * @param list
     * @return java.lang.Boolean
     **/
    Boolean generateSoReturnReceiveSave(List<SoReturnNoticeDTO.GenerateSoReturnReceiveView> list);

    /**
     * 签收单下推质检单-列表查询
     * @Author Luo_WG
     * @Date 2023/5/11 11:46
     * @param ids
     * @return java.util.List<com.erp.model.wms.dto.SoReturnReceiveDTO.GenerateSoReturnInstockView>
     **/
    List<QcInfoDTO.ReceiveGenerateQcView> receiveGenerateQcView(List<String> ids);

    /**
     * 根据来源id查询签收单
     * @Author Luo_WG
     * @Date 2023/5/16 10:11
     * @param id
     * @return java.util.List<com.erp.model.wms.entity.SoReturnNoticeEntity>
     **/
    List<SoReturnReceiveEntity> listBySourceIds(List<String> id);

    /**
     * 下推退货入库单-列表查询
     * @Author Luo_WG
     * @Date 2023/5/23 15:52
     * @param ids
     * @return java.util.List<com.erp.model.wms.dto.SoReturnInstockDTO.GenerateSoReturnInstockView>
     **/
    List<SoReturnReceiveDTO.ReceiveGenerateSoReturnInstockView> generateSoReturnInstockView(List<String> ids);

    /**
     * 根据id集合获取退货签收单
     * @param ids
     * @return
     */
    List<SoReturnReceiveEntity> listByIds(List<String> ids);

    /**
     * PDA:列表查询
     * @Author Luo_WG
     * @Date 2023/8/15 11:24
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.wms.dto.SoReturnReceiveDTO.PdaPagingView>
     **/
    PagingVO<SoReturnReceiveDTO.PdaPagingView> pdaPaging(PagingDTO<SoReturnReceiveDTO.PdaPagingParamDTO> dto);

    /**
     * 列表状态数量统计
     * @Author Luo_WG
     * @Date 2023/8/15 12:18
     * @param dto
     * @return java.util.List<com.erp.model.wms.dto.SoReturnReceiveDTO.PdaPoReceiveCount>
     **/
    List<SoReturnReceiveDTO.PdaSoReturnReceiveCount> pdaListCount(PermissionsDTO dto);

    /**
     * 条件查询销售退货签收单
     * @Author Luo_WG
     * @Date 2023/8/18 12:01
     * @param dto
     * @return java.util.List<com.erp.model.wms.dto.SoReturnReceiveDTO.PdaSoReceive>
     **/
    List<SoReturnReceiveDTO.PdaSoReceive> pdaList(SoReturnReceiveDTO.PdaSoReceiveParam dto);

    /**
     * 批量反审核
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     * @param ids ids
     * @return java.lang.Boolean
     **/
    Boolean pdaDisApprove(List<String> ids);

    PagingVO<SoReturnReceiveDTO.PagingView> exportSoReturnReceive(PagingDTO<SoReturnReceiveDTO.PagingParam> dto);
}
