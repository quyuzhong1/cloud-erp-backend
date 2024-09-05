package com.erp.server.dmp.service;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.DmpOutputTaskDTO;
import com.erp.model.dmp.entity.DmpOutputTaskRecordEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpOutputTaskRecordDTO;

import java.util.List;

/**
 * <p>
 * 推送任务记录 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
 */
public interface DmpOutputTaskRecordService extends SuperService<DmpOutputTaskRecordEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-06-11
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpOutputTaskRecordDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-06-11
    * @param dto
    * @return
    */
    Boolean update(DmpOutputTaskRecordDTO.UpdateDTO dto);



    /**
     * 获取 tab列表
     * @Author Luo_WG
     * @Date 2024/9/3 14:53
     * @param dto
     * @return java.util.List<com.erp.model.dmp.dto.DmpOutputTaskDTO.TabListDTO>
     **/
    List<DmpOutputTaskRecordDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
     * 推送任务列表分页查询
     * @Author Luo_WG
     * @Date 2024/9/3 14:35
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.dmp.dto.DmpOutputTaskDTO.PagingDTO>
     **/
    PagingVO<DmpOutputTaskRecordDTO.PagingDTO> paging(PagingDTO<DmpOutputTaskRecordDTO.PagingParamDTO> dto);

}
