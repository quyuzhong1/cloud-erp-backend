package com.erp.server.dmp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.DmpPullTaskDTO;
import com.erp.model.dmp.entity.DmpPullTaskHistoryEntity;

import java.util.List;

/**
 * <p>
 * 中台同步任务表 服务类
 * </p>
 *
 * @author zhangchunlin
 * @since 2023-06-29
 */
public interface DmpPullTaskHistoryService extends IService<DmpPullTaskHistoryEntity> {

    /**
     * @description: 分页查询
     * @author Will
     * @date: 2023/10/17 14:38
     * @param dto
     * @return PagingVO<ListDTO>
     */
    PagingVO<DmpPullTaskDTO.ListDTO> paging(PagingDTO<DmpPullTaskDTO.ParamDTO> dto);
    /**
     * @param dto
     * @return Boolean
     * @description: 导出
     * @author Will
     * @date: 2023/10/17 14:38
     */
    Boolean exportExcel(DmpPullTaskDTO.ParamDTO dto);
    /**
     * @description: 批量同步
     * @author Will
     * @date: 2023/10/17 14:38
     * @param ids
     * @return Boolean
     */
    Boolean batchSync(List<String> ids);


    /**
     * 同步3个月前拉取数据到归档表
     */
    void syncPullTaskHistory(Integer month);

    PagingVO<DmpPullTaskDTO.ListDTO> exportPullTaskHistory(PagingDTO<DmpPullTaskDTO.ParamDTO> dto);

}
