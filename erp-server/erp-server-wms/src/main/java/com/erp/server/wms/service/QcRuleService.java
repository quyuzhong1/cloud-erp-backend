package com.erp.server.wms.service;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.QcRuleDTO;
import com.erp.model.wms.entity.QcRuleEntity;

import java.util.List;

/**
 * <p>
 * 质检规则 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-04-13
 */
public interface QcRuleService extends SuperService<QcRuleEntity> {

    
    /**
     * 添加质检规则
     * @author yl
     * @date 2023-04-13 10:18
     * @param dto
     * @return java.lang.String
     */
    String add(QcRuleDTO.AddDTO dto);

    /**
     * 质检规则详情
     * @author yl
     * @date 2023-04-13 14:11
     * @param id
     * @return com.erp.model.wms.dto.QcRuleDTO.ViewDTO
     */
    QcRuleDTO.ViewDTO view(String id);

    /**
     * 添加并提交
     * @author yl
     * @date 2023-04-13 14:34
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean addAndSubmit(QcRuleDTO.AddDTO dto);

    /**
     * 提交审核
     * @author yl
     * @date 2023-04-13 14:36
     * @param ids
     * @return java.lang.Boolean
     */
    Boolean submit(List<String> ids);

    /**
     * 修改质检规则
     * @author yl
     * @date 2023-04-13 14:43
     * @param dto
     * @return java.lang.String
     */
    String updateQcRule(QcRuleDTO.UpdateDTO dto);

    /**
     * 修改并提交
     * @author yl
     * @date 2023-04-13 15:17
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean updateAndSubmit(QcRuleDTO.UpdateDTO dto);

    /**
     * 审核
     * @author yl
     * @date 2023-04-13 15:19
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean approve(BaseApproveParamDTO dto);

    /**
     * 反审核
     * @author yl
     * @date 2023-04-13 15:23
     * @param ids
     * @return java.lang.Boolean
     */
    Boolean disApprove(List<String> ids);

    
    /**
     * 撤销流程
     * @author yl
     * @date 2023-04-13 15:33
     * @param ids
     * @return java.lang.Boolean
     */
    Boolean cancelProcess(List<String> ids);

    /**
     * 删除质检规则
     * @author yl
     * @date 2023-04-13 15:44
     * @param ids
     * @return java.lang.Boolean
     */
    List<BatchResultDTO>  deleteByIds(List<String> ids);
    
    /**
     * 分页信息
     * @author yl
     * @date 2023-04-13 16:02
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.wms.dto.QcRuleDTO.PagingViewDTO>
     */
    PagingVO<QcRuleDTO.PagingViewDTO> paging(PagingDTO<QcRuleDTO.PagingParamDTO> dto);

    /**
     * 更改启用禁用状态
     * @author yl
     * @date 2023-04-13 17:10
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean updateDisabledState(UpdateStateDTO dto);

    /**
     * 获取审核通过且启用的
     * @return
     */
    List<QcRuleEntity> listByApprove();
}
