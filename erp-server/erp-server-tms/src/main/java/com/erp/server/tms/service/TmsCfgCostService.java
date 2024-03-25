package com.erp.server.tms.service;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.TmsCfgCostDTO;
import com.erp.model.tms.entity.TmsCfgCostEntity;

import java.util.List;

/**
 * <p>
 * 费用管理配置表 服务类
 * </p>
 *
 * @author will
 * @since 2024-03-15
 */
public interface TmsCfgCostService extends SuperService<TmsCfgCostEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-03-15
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(TmsCfgCostDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2024-03-15
    * @param dto
    * @return
    */
    Boolean update(TmsCfgCostDTO.UpdateDTO dto);

    /**
     * @description:
     * @author Will
     * @date: 2024/3/18 11:48
     * @param dto
     * @return ListDTO
     */
    PagingVO<TmsCfgCostDTO.ListDTO> paging(PagingDTO<TmsCfgCostDTO.PagingParamDTO> dto);
    /**
     * @description: 查看详情
     * @author Will
     * @date: 2024/3/18 11:59
     * @param id
     * @return ViewDTO
     */
    TmsCfgCostDTO.ViewDTO view(String id);
    /**
     * @description: 删除
     * @author Will
     * @date: 2024/3/18 12:01
     * @param id
     * @return BatchResultDTO
     */
    BatchResultDTO delete(String id);
    /**
     * @description: 费用配置下拉
     * @author Will
     * @date: 2024/3/22 15:54
     * @param dto
     * @return List<DropDownDTO>
     */
    List<TmsCfgCostDTO.DropDownDTO> listDropDown(TmsCfgCostDTO.DropDownParamDTO dto);

    /**
     * 根据归属和分类查询
     */
    List<TmsCfgCostEntity> listCostAttributionAndCategory(String dictCostAttribution ,String dictCostCategory);
    /**
     * @description: 根据费用名称集合查询
     * @author Will
     * @date: 2024/3/25 11:20
     * @param costNameList
     * @return List<TmsCfgCostEntity>
     */
    List<TmsCfgCostEntity> listByCostNameList(List<String> costNameList);
}
