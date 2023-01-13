package com.erp.server.dmp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.dto.base.BaseSearchDTO;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.dmp.dto.CfgApiFieldMapDTO;
import com.erp.model.dmp.dto.CfgApiFieldMapValueDTO;
import com.erp.model.dmp.entity.CfgApiFieldMapEntity;
import com.erp.model.dmp.vo.CfgApiFieldMapVO;

import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/1/11 11:38
 */
public interface CfgApiFieldMapService extends IService<CfgApiFieldMapEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2023/1/11 12:14
     * @param dto
     * @return PagingVO<CfgApiFieldMapVO>
     */
    PagingVO<CfgApiFieldMapVO> paging(PagingDTO<BaseSearchDTO> dto);
    /**
     * @description: 新增
     * @author Will
     * @date: 2023/1/11 12:15
     * @param dto
     * @return Boolean
     */
    Boolean insert(CfgApiFieldMapDTO dto);
    /**
     * @description:批量新增
     * @author Will
     * @date: 2023/1/12 19:10
     * @param list
     * @return Boolean
     */
    Boolean batchAdd(List<CfgApiFieldMapDTO> list);
    /**
     * @description: 编辑
     * @author Will
     * @date: 2023/1/11 12:15
     * @param dto
     * @return Boolean
     */
    void update(CfgApiFieldMapDTO dto);
    /**
     * @description: 批量删除
     * @author Will
     * @date: 2023/1/11 12:20
     * @param ids

     */
    void batchDelete(List<String> ids);
    /**
     * @description: 查询明细数据
     * @author Will
     * @date: 2023/1/11 12:23
     * @param fieldMapId 
     * @return List<CfgApiFieldMapValueDTO> 
     */
    List<CfgApiFieldMapValueDTO> listDetails(String fieldMapId);
    /**
     * @description: 查询单条数据
     * @author Will
     * @date: 2023/1/11 16:44
     * @param id
     * @return CfgApiFieldMapDTO
     */
    CfgApiFieldMapDTO getCfgApiFieldMapById(String id);
    /**
     * @description: 根据平台id、模块类型查询
     * @author Will
     * @date: 2023/1/12 9:22
     * @param dto
     * @return List<CfgApiFieldMapDTO>
     */
    List<CfgApiFieldMapDTO> getByParams(CfgApiFieldMapDTO dto);

}
