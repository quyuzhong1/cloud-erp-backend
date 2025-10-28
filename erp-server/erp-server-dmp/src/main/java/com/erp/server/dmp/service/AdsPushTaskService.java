package com.erp.server.dmp.service;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;

import java.util.List;

import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.AdsPushTaskDTO;
import com.erp.model.dmp.dto.DmpOutputTaskRecordDTO;
import com.erp.model.dmp.entity.doris.AdsPushTaskEntity;

/**
 * <p>
 * ads推送任务 服务类
 * </p>
 *
 * @author shukai
 * @since 2025-10-28
 */
public interface AdsPushTaskService extends SuperService<AdsPushTaskEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2025-10-28
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(AdsPushTaskDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2025-10-28
    * @param dto
    * @return
    */
    Boolean update(AdsPushTaskDTO.UpdateDTO dto);

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
    
    /**
     * 导出
     * @Author Luo_WG
     * @Date 2024/9/5 17:30
     * @param dto
     * @return java.lang.Boolean
     **/
    Boolean exportExcel(DmpOutputTaskRecordDTO.ExpotParamDTO dto);
    
    /**
     * 无需同步
     * @Author Luo_WG
     * @Date 2024/9/5 16:30
     * @param ids
     * @return java.lang.Boolean
     **/
    Boolean batchNoNeedSync(List<String> ids , String remark);

    /**
     * 加入黑名单
     * @Author Luo_WG
     * @Date 2024/9/5 11:36
     * @param dto
     * @return java.lang.Object
     **/
    Boolean addOutputBlack(DmpOutputTaskRecordDTO.AddOutputBlackDTO dto);

    /**
     * 取消黑名单
     * @Author Luo_WG
     * @Date 2024/9/11 19:21
     * @param id
     * @return java.lang.Boolean
     **/
    BatchResultDTO cancelOutputBlack(String id);
}
