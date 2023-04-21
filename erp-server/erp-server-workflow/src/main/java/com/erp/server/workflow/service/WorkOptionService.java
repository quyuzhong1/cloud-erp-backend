package com.erp.server.workflow.service;

import com.common.business.service.SuperService;
import com.erp.model.workflow.dto.WorkOptionDTO;
import com.erp.model.workflow.entity.WorkOptionEntity;
import java.util.List;

/**
 * <p>
 *  工作台选项表服务类
 * </p>
 *
 * @author LUO_WG
 * @since 2023-04-11
 */
public interface WorkOptionService extends SuperService<WorkOptionEntity> {
    /**
     * 待办模块-模块分类下拉
     * @return
     */
    List<WorkOptionDTO.WaitDoMenu> listWaitDoMenu(String sysClassify);

    /**
     * 待办模块-模块分类下拉
     * @return
     */
    List<WorkOptionDTO.WaitDoMenu> listOftenMenu(String sysClassify);

    /**
     * 新增代办模块
     * @Author Luo_WG
     * @Date 2023/4/20 19:45
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    Boolean addWaitDo(WorkOptionDTO.addDTO dto);

    /**
     * 编辑修改代办模块
     * @Author Luo_WG
     * @Date 2023/4/20 19:45
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    Boolean updateWaitDo(WorkOptionDTO.updateDTO dto);
}
