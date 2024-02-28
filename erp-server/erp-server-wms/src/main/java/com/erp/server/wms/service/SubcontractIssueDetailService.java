package com.erp.server.wms.service;
import com.common.business.service.SuperService;
import com.erp.model.wms.dto.SubcontractIssueDetailDTO;
import com.erp.model.wms.entity.SubcontractIssueDetailEntity;

import java.util.List;

/**
 * <p>
 * 委外发料明细单 服务类
 * </p>
 *
 * @author will
 * @since 2024-01-08
 */
public interface SubcontractIssueDetailService extends SuperService<SubcontractIssueDetailEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-01-08
    * @param details
    * @param mainId
    * @return
    */
    void add(List<SubcontractIssueDetailDTO.AddDTO> details, String mainId);;

    /**
    * 修改
    * @author will
    * @date: 2024-01-08
    * @param details
    * @param mainId
    * @return
    */
    Boolean update(List<SubcontractIssueDetailDTO.UpdateDTO> details, String mainId);

    /**
     * @description: 根据主表ids查询
     * @author Will
     * @date: 2024/1/9 14:43
     * @param mainIdList
     * @return List<SubcontractIssueDetailEntity>
     */
    List<SubcontractIssueDetailEntity> listByMainIds(List<String> mainIdList);

    /**
     * @description: 根据mainId删除
     * @author Will
     * @date: 2024/1/9 15:46
     * @param mainId
     */
    void deleteByMainId(String mainId);

    /**
     * @description: 根据来源明细id集合查询
     * @author Will
     * @date: 2024/1/9 16:56
     * @param sourceDetailIdList
     * @return List<SubcontractIssueDetailEntity>
     */
    List<SubcontractIssueDetailEntity> listBySourceDetailIdList(List<String> sourceDetailIdList);

    /**
     * @description: 根据委外订单明细id集合查询
     * @author Will
     * @date: 2024/2/23 11:48
     * @param subcontractOrderDetailIdList
     * @return List<SubcontractIssueDetailEntity>
     */
    List<SubcontractIssueDetailEntity> listBySubcontractOrderDetailIdList(List<String> subcontractOrderDetailIdList);
}
