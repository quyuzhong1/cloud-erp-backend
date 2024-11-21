package com.erp.server.dmp.service;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.dmp.dto.DmpInoutDTO;
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
     * 根据系统代号和业务代号查询最新任务记录
     * @param systemCodeList 系统代号列表
     * @param billTypeList 业务代号列表
     * @param nextLevelIdList 下一级ID列表
     * @return 最新任务信息
     */
    List<DmpInoutDTO.LastOneDTO> lastBySystemCodeAndBillType(List<String> systemCodeList, List<String> billTypeList, List<String> nextLevelIdList);
}
