package com.erp.server.dmp.service;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpInputTaskDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * <p>
 * 拉取任务 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
 */
public interface DmpInputTaskService extends SuperService<DmpInputTaskEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-06-11
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpInputTaskDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-06-11
    * @param dto
    * @return
    */
    Boolean update(DmpInputTaskDTO.UpdateDTO dto);

    boolean updateErrorStatus(String id , boolean errorFlag , Integer errorCount , Exception e);

    /**
     * 获取 tab列表
     * @Author Luo_WG
     * @Date 2024/9/3 15:48
     * @param dto
     * @return java.util.List<com.erp.model.dmp.dto.DmpInputTaskDTO.TabListDTO>
     **/
    List<DmpInputTaskDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
     * 推送任务列表分页查询
     * @Author Luo_WG
     * @Date 2024/9/3 15:48
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.dmp.dto.DmpInputTaskDTO.PagingDTO>
     **/
    PagingVO<DmpInputTaskDTO.PagingDTO> paging(PagingDTO<DmpInputTaskDTO.PagingParamDTO> dto);
}
