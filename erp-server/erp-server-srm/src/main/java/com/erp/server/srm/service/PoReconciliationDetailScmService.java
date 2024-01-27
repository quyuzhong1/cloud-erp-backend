package com.erp.server.srm.service;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.srm.dto.PoReconciliationDetailDTO;
import com.erp.model.srm.entity.PoReconciliationDetailEntity;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 采购对账单明细 服务类
 * </p>
 *
 * @author will
 * @since 2024-01-19
 */
public interface PoReconciliationDetailScmService extends SuperService<PoReconciliationDetailEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-01-19
    * @param addList
    * @return
    */
    BaseResultDTO.AddDTO add(List<PoReconciliationDetailDTO.AddDTO> addList);

    /**
    * 修改
    * @author will
    * @date: 2024-01-19
    * @param detailList
    * @param mainId
    * @return
    */
    Boolean update(List<PoReconciliationDetailDTO.ScmUpdateDTO> detailList,String mainId);

    /**
     * @description: 分页查询
     * @author Will
     * @date: 2024/1/20 11:31
     * @param dto
     * @return PagingVO<ListDTO>
     */
    PagingVO<PoReconciliationDetailDTO.ListDTO> paging(PagingDTO<PoReconciliationDetailDTO.PagingParamDTO> dto);
    /**
     * @description: 导出
     * @author Will
     * @date: 2024/1/20 12:03
     * @param dto
     * @param response
     */
    void exportList(PoReconciliationDetailDTO.PagingParamDTO dto, HttpServletResponse response);
    /**
     * @description: 查看详情
     * @author Will
     * @date: 2024/1/23 15:50
     * @param dto
     * @return List<ViewDTO>
     */
    List<PoReconciliationDetailDTO.ViewDTO> viewDetail(PoReconciliationDetailDTO.PagingParamDTO dto);
    /**
     * @description: 生成对账单
     * @author Will
     * @date: 2024/1/23 18:04
     * @param dto
     * @return Boolean
     */
    Boolean generatePoReconciliation(PoReconciliationDetailDTO.GeneratePoReconciliationDTO dto);
    /**
     * @description: 根据组表id集合查询
     * @author Will
     * @date: 2024/1/25 11:41
     * @param mainIdList 
     * @return List<PoReconciliationDetailEntity> 
     */
    List<PoReconciliationDetailEntity> listMainIdList(List<String> mainIdList);
    /**
     * @description: 分页格式化
     * @author Will
     * @date: 2024/1/25 11:44
     * @param list

     */
    void fillList(List<PoReconciliationDetailDTO.ListDTO> list);
    /**
     * @description: 根据来源明细id集合查询
     * @author Will
     * @date: 2024/1/25 16:58
     * @param sourceDetailIdList
     * @return List<PoReconciliationDetailEntity>
     */
    List<PoReconciliationDetailEntity> listDetailBySourceDetailIdList(List<String> sourceDetailIdList);
    /**
     * @description: 根据来源明细id集合删除
     * @author Will
     * @date: 2024/1/25 17:02
     * @param sourceDetailIdList
     */
    void deleteDetailBySourceDetailIdList(List<String> sourceDetailIdList,boolean isFromDisApprove);
    /**
     * @description: 清除明细主表id
     * @author Will
     * @date: 2024/1/27 11:42
     * @param id
     */
    void cleanDetailMainId(String id);
}
