package com.erp.server.wms.service;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.validator.ValidList;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.TransferInDTO;
import com.erp.model.wms.entity.TransferInEntity;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 分布式调入单 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
public interface TransferInService extends SuperService<TransferInEntity> {

    List<TransferInDTO.TabListDTO> tabList();

    
    /**
     * 下推单据保存
     * @author yl
     * @date 2023-05-26 11:33
     * @param list
     * @return java.lang.Boolean
     */
    Boolean generateTransferIn(ValidList<TransferInDTO.ViewGenerateTransferInDTO> list);

    /**
     * 分页查询
     * @author yl
     * @date 2023-05-26 16:04
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.wms.dto.TransferInDTO.PagingViewDTO>
     */
    PagingVO<TransferInDTO.PagingViewDTO> paging(PagingDTO<TransferInDTO.PagingParamDTO> dto);

    /**
     * 提交审核
     * @author yl
     * @date 2023-05-26 16:52
     * @param ids
     * @return java.lang.Boolean
     */
    Boolean submit(List<String> ids);

    /**
     * 审核
     * @author yl
     * @date 2023-05-26 16:58
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean approve(BaseApproveParamDTO dto);

    /**
     * 撤销流程
     * @author yl
     * @date 2023-05-26 19:00
     * @param ids
     * @return java.lang.Boolean
     */
    Boolean cancelProcess(List<String> ids);

    /**
     * 删除分布是调入单
     * @author yl
     * @date 2023-05-26 19:05
     * @param ids
     * @return java.lang.Boolean
     */
    Boolean deleteByIds(List<String> ids);

    /**
     * 反审核
     * @author yl
     * @date 2023-05-29 9:47
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean disApprove(BaseIdsDTO.IdsDTO dto);

    /**
     * 作废
     * @author yl
     * @date 2023-05-29 9:51
     * @param ids
     * @param remark
     * @return java.lang.Boolean
     */
    Boolean invalid(List<String> ids, String remark);

    
    /**
     * 导出数据
     * @author yl
     * @date 2023-05-29 10:04
     * @param dto
     * @param response
     * @return java.lang.Boolean
     */
    Boolean exportExcel(TransferInDTO.ExportDTO dto, HttpServletResponse response);

    /**
     * 分布是调入详情
     * @author yl
     * @date 2023-05-29 10:52
     * @param id
     * @return com.erp.model.wms.dto.TransferInDTO.ViewDTO
     */
    TransferInDTO.ViewDTO view(String id);
}
