package com.erp.server.bi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.business.dto.base.BaseSearchDTO;
import com.erp.common.business.dto.base.PagingDTO;
import com.erp.common.business.dto.base.UpdateStateDTO;
import com.erp.common.business.vo.PagingVO;
import com.erp.model.bi.dto.CategoryModuleDTO;
import com.erp.model.bi.dto.ModuleDTO;
import com.erp.model.bi.dto.ModulePagingDTO;
import com.erp.model.bi.entity.BiModuleEntity;

import java.util.List;

/**
 * 模块表(BiModule)表服务接口
 *
 * @author yl
 * @since 2022-12-08 14:31:14
 */
public interface BiModuleService  extends IService<BiModuleEntity> {
    
    

    /**
     * 新增加模块
     *
     * @param biModule 实例对象
     * @return 实例对象
     */
    Boolean insert(ModuleDTO biModule);

    /**
     * 修改数据
     *
     * @param dto 实例对象
     * @return 实例对象
     */
    Boolean update(ModuleDTO dto);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(String id);

    
    /**
     * 分页查询模块分页信息
     * @author yl
     * @date 2022-12-12 11:36
     * @param dto
     * @return
     */
    PagingVO<ModulePagingDTO> paging(PagingDTO<BaseSearchDTO> dto);

    /**
     * 修改模板状态
     * @author yl
     * @date 2022-12-12 11:59
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean updateState(UpdateStateDTO dto);

    List<CategoryModuleDTO> categoryList(String searchKeyword);


    /**
     * 模块详情
     * @param id
     * @return
     */
    ModuleDTO details(String id);

    List<BiModuleEntity> getByIds(List<String> moduleIdList);
}
