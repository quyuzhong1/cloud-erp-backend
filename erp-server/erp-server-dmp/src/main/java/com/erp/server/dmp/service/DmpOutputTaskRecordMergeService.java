package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpOutputTaskRecordEntity;
import com.erp.model.dmp.entity.DmpOutputTaskRecordMergeEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpOutputTaskRecordMergeDTO;
import com.erp.model.dmp.enums.OutputTaskRecordMergeStatusEnum;

import java.util.List;

/**
 * <p>
 * 推送任务记录合并表 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2025-02-18
 */
public interface DmpOutputTaskRecordMergeService extends SuperService<DmpOutputTaskRecordMergeEntity> {

    /**
    * 新增
    * @author Luo_WG
    * @date: 2025-02-18
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpOutputTaskRecordMergeDTO.AddDTO dto);

    /**
    * 修改
    * @author Luo_WG
    * @date: 2025-02-18
    * @param dto
    * @return
    */
    Boolean update(DmpOutputTaskRecordMergeDTO.UpdateDTO dto);


    void sdyMergePush();
}
