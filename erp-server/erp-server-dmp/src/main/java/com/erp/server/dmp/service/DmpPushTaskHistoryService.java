package com.erp.server.dmp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.DmpPushTaskDTO;
import com.erp.model.dmp.entity.DmpPushTaskHistoryEntity;

import java.util.List;

/**
 * <p>
 * 中台同步任务表 服务类
 * </p>
 *
 * @author Cloud
 * @since 2023-09-06
 */
public interface DmpPushTaskHistoryService extends IService<DmpPushTaskHistoryEntity> {

    /**
     * @param dto
     * @return PagingVO<ListDTO>
     * @description: 分页查询
     * @author Will
     * @date: 2023/10/13 11:50
     */
    PagingVO<DmpPushTaskDTO.ListDTO> paging(PagingDTO<DmpPushTaskDTO.ParamDTO> dto);

    /**
     * @param dto
     * @return Boolean
     * @description: 导出
     * @author Will
     * @date: 2023/10/13 15:34
     */
    Boolean exportExcel(DmpPushTaskDTO.ParamDTO dto);

    /**
     * @param ids
     * @return Boolean
     * @description: 批量同步
     * @author Will
     * @date: 2023/10/13 15:48
     */
    Boolean batchSync(List<String> ids);

    /**
     * @param ids
     * @return Boolean
     * @description: 批量查询数据后同步
     * @author Will
     * @date: 2023/10/30 9:42
     */
    Boolean batchFindDataSync(List<String> ids);

    /**
     * 同步3个月前同步数据到归档表
     */
    void syncPushTaskHistory();

    PagingVO<DmpPushTaskDTO.ListDTO> exportPushTaskHistory(PagingDTO<DmpPushTaskDTO.ParamDTO> dto);
}
