package com.erp.server.wms.service;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.CfgRuleWaveDTO;
import com.erp.model.wms.entity.CfgRuleWaveEntity;

/**
 * <p>
 * 波次规则 服务类
 * </p>
 *
 * @author will
 * @since 2024-06-20
 */
public interface CfgRuleWaveService extends SuperService<CfgRuleWaveEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-06-20
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(CfgRuleWaveDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2024-06-20
    * @param dto
    * @return
    */
    Boolean update(CfgRuleWaveDTO.UpdateDTO dto);

    /**
     * 分页查询
     * @author will
     * @date 2024/6/24 12:34
     * @param dto
     * @return PagingVO<ListDTO>
     */
    PagingVO<CfgRuleWaveDTO.ListDTO> paging(PagingDTO<CfgRuleWaveDTO.PagingParamDTO> dto);
    /**
     * 更新状态
     * @author will
     * @date 2024/6/24 16:54
     * @param id
     * @param disabled
     * @return Boolean
     */
    BatchResultDTO updateStatus(String id, Boolean disabled);
    /**
     * 删除
     * @author will
     * @date 2024/6/24 18:20
     * @param id
     * @return BatchResultDTO
     */
    BatchResultDTO delete(String id);
    /**
     * 查看详情
     * @author will
     * @date 2024/6/24 18:27
     * @param id
     * @return ViewDTO
     */
    CfgRuleWaveDTO.ViewDTO view(String id);

    /**
     * 执行规则
     * @author will
     * @date 2024/6/25 15:43
     * @param id
     * @return BatchResultDTO
     */
    BatchResultDTO executeRule(String id);
}
