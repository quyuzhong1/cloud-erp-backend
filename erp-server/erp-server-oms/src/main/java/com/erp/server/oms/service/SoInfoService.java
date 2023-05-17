package com.erp.server.oms.service;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.SoInfoDTO;
import com.erp.model.oms.entity.SoInfoEntity;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 销售订单信息 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
public interface SoInfoService extends SuperService<SoInfoEntity> {

    /**
     * 添加销售订单
     * @author yl
     * @date 2023-05-15 16:28
     * @param dto
     * @return java.lang.String
     */
    String add(SoInfoDTO.AddDTO dto);

    
    /**
     * 提交
     * @author yl
     * @date 2023-05-16 14:41
     * @param ids
     * @return java.lang.Boolean
     */
    Boolean submit(List<String> ids);

    
    /**
     * 新增并提交
     * @author yl
     * @date 2023-05-16 14:49
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean addAndSubmit(SoInfoDTO.AddDTO dto);

    
    /**
     * 销售订单详情
     * @author yl
     * @date 2023-05-16 15:01
     * @param id
     * @return com.erp.model.oms.dto.SoInfoDTO.ViewDTO
     */
    SoInfoDTO.ViewDTO view(String id);

    PagingVO<SoInfoDTO.PagingViewDTO> paging(PagingDTO<SoInfoDTO.PagingParamDTO> dto);


    /**
     * 暂存数据
     * @author yl
     * @date 2023-05-17 15:00
     * @param dto
     * @return java.lang.String
     */
    String draft(SoInfoDTO.AddDTO dto);

    
    /**
     * 修改 销售订单
     * @author yl
     * @date 2023-05-17 15:42
     * @param dto
     * @return java.lang.String
     */
    String updateSo(SoInfoDTO.UpdateDTO dto);

    
    /**
     * 修改并提交
     * @author yl
     * @date 2023-05-17 16:43
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean updateAndSubmit(SoInfoDTO.UpdateDTO dto);

    /**
     * 审核
     * @author yl
     * @date 2023-05-17 16:46
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean approve(BaseApproveParamDTO dto);

    /**
     * 反审核
     * @author yl
     * @date 2023-05-17 16:48
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean disApprove(BaseIdsDTO.IdsDTO dto);

    /**
     * 撤销流程
     * @author yl
     * @date 2023-05-17 16:51
     * @param ids
     * @return java.lang.Boolean
     */
    Boolean cancelProcess(List<String> ids);

    /**
     * 批量删除
     * @author yl
     * @date 2023-05-17 16:53
     * @param ids
     * @return java.lang.Boolean
     */
    Boolean deleteByIds(List<String> ids);

    /**
     * 作废
     * @author yl
     * @date 2023-05-17 17:13
     * @param ids
     * @param remark
     * @return java.lang.Boolean
     */
    Boolean invalid(List<String> ids, String remark);

    /**
     * 导出数据
     * @author yl
     * @date 2023-05-17 18:02
     * @param dto
     * @param response
     * @return java.lang.Boolean
     */
    Boolean exportExcel(SoInfoDTO.ExportDTO dto, HttpServletResponse response);
}
