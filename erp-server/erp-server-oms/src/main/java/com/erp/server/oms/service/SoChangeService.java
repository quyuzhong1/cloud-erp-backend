package com.erp.server.oms.service;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.SoChangeDTO;
import com.erp.model.oms.dto.SoChangeDetailDTO;
import com.erp.model.oms.entity.SoChangeEntity;

import java.util.List;

/**
 * <p>
 * 销售订单变更 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
public interface SoChangeService extends SuperService<SoChangeEntity> {

    
    /**
     * 添加销售订单变更
     * @author yl
     * @date 2023-05-18 11:54
     * @param dto
     * @return java.lang.String
     */
    String add(SoChangeDTO.AddDTO dto);

    
    /**
     * 提交审核
     * @author yl
     * @date 2023-05-24 14:45
     * @param ids
     * @return java.lang.Boolean
     */
    Boolean submit(List<String> ids);

    
    /**
     * 新增并提交
     * @author yl
     * @date 2023-05-24 14:53
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean addAndSubmit(SoChangeDTO.AddDTO dto);

    /**
     * 获取tab 列表
     * @author yl
     * @date 2023-05-24 14:56
     * @param
     * @return java.util.List<com.erp.model.oms.dto.SoChangeDTO.TabListDTO>
     */
    List<SoChangeDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
     * 分页列表
     * @author yl
     * @date 2023-05-24 16:44
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.oms.dto.SoChangeDTO.PagingViewDTO>
     */
    PagingVO<SoChangeDTO.PagingViewDTO> paging(PagingDTO<SoChangeDTO.PagingParamDTO> dto);

    
    /**
     * 导出数据
     * @author yl
     * @date 2023-05-24 17:47
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean exportExcel(SoChangeDTO.PagingParamDTO dto);

    
    /**
     * 详情
     * @author yl
     * @date 2023-05-24 18:04
     * @param id
     * @return com.erp.model.oms.dto.SoChangeDTO.ViewDTO
     */
    SoChangeDTO.ViewDTO view(String id);

    /**
     * 审核
     * @author yl
     * @date 2023-05-25 10:40
     * @param dto
     * @return java.lang.Boolean
     */
    BatchResultDTO approve(BaseApproveParamDTO dto, SoChangeEntity entity);

    /**
     * @description: 结束审核
     * @author Will
     * @date: 2023/7/4 10:13
     * @param dto
     * @param list
     * @return Boolean
     */
    Boolean approveEnd (BaseApproveParamDTO dto,List<SoChangeEntity> list);

    /**
     * 撤销流程
     * @author yl
     * @date 2023-05-25 11:01
     * @param ids
     * @return java.lang.Boolean
     */
    Boolean cancelProcess(List<String> ids);

    /**
     * 删除销售变更单
     * @author yl
     * @date 2023-05-25 11:05
     * @param ids
     * @return java.lang.Boolean
     */
    Boolean deleteByIds(List<String> ids);

    
    /**
     * 作废单据
     * @author yl
     * @date 2023-05-25 11:14
     * @param ids
     * @param remark
     * @return java.lang.Boolean
     */
    Boolean invalid(List<String> ids, String remark);

    /**
     * 更改销售变更单
     * @author yl
     * @date 2023-05-25 11:39
     * @param dto
     * @return java.lang.String
     */
    String updateSoChange(SoChangeDTO.UpdateDTO dto);

    /**
     * 修改并提交
     * @author yl
     * @date 2023-05-25 12:23
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean updateAndSubmit(SoChangeDTO.UpdateDTO dto);

    /**
     * 根据销售订单id 获取到对应详情
     * @author yl
     * @date 2023-05-25 14:04
     * @param soDetailIds
     * @return com.erp.model.oms.dto.SoChangeDTO.ViewDTO
     */
    SoChangeDTO.ViewDTO getViewBySoDetailIds(List<String>  soDetailIds);

    /**
     * 销售订单 关联的销售变更单
     * @author yl
     * @date 2023-05-25 16:06
     * @param soId
     * @return java.util.List<com.erp.model.oms.dto.SoChangeDTO.SoRefDTO>
     */
    List<SoChangeDTO.SoRefDTO> listSoRefSoChangeBySoId(String soId);

    
    /**
     * 根据选择销售订单id 获取到对应的sku 信息
     * @author yl
     * @date 2023-05-26 9:26
     * @param soId
     * @return java.util.List<com.erp.model.oms.dto.SoChangeDetailDTO.ViewDTO>
     */
    List<SoChangeDetailDTO.SoDetailViewDTO> listSoSkuBySoId(String soId);

    
    /**
     * 检测能否变更根据so 详情id
     * @author yl
     * @date 2023-05-26 14:42
     * @param soDetailIds
     * @return java.util.List<java.lang.String>
     */
    List<String> checkBySoDetailIds(List<String> soDetailIds);

    /**
     * 根据销售订单详情 获取到变更的
     * @author yl
     * @date 2023-05-30 17:24
     * @param soIds
     * @return void
     */
    List<SoChangeEntity> listBySoIds(List<String> soIds);

    /**
     * 更改销售订单金蝶推送的状态
     *
     * @param id
     * @param syncKingdeeId
     * @return
     * @author yl
     * @date 2023-05-31 14:20
     */
    Boolean updateSyncKingdeeId(String id, String syncKingdeeId);

    /**
     * 销售变更单导出
     * @param dto 参数
     */
    PagingVO<SoChangeDTO.PagingViewDTO> exportSoChange(PagingDTO<SoChangeDTO.PagingParamDTO> dto);
}
