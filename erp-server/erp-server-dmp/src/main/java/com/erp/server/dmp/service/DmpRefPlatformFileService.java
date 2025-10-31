package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpRefPlatformFileEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpRefPlatformFileDTO;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 平台文件转存FastDFS关系记录表 服务类
 * </p>
 *
 * @author Jim
 * @since 2025-10-30
 */
public interface DmpRefPlatformFileService extends SuperService<DmpRefPlatformFileEntity> {

    /**
    * 新增
    * @author Jim
    * @date: 2025-10-30
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpRefPlatformFileDTO.AddDTO dto);

    /**
    * 修改
    * @author Jim
    * @date: 2025-10-30
    * @param dto
    * @return
    */
    Boolean update(DmpRefPlatformFileDTO.UpdateDTO dto);


    /**
     * 根据sourceSystem和fileKey列表获取映射关系
     * @param sourceSystem 平台系统
     * @param fileKeyList 平台文件唯一IDS
     * @return 已存在的信息
     */
    Map<String, DmpRefPlatformFileEntity> mapByFileKey(String sourceSystem, List<String> fileKeyList);
}
