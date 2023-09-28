package com.erp.server.sys.service;
import com.erp.model.sys.dto.SysLogRecordFieldDTO;
import com.erp.model.sys.dto.SysLogRecordFieldListDTO;
import com.erp.model.sys.entity.SysLogRecordFieldEntity;
import com.common.business.service.SuperService;

import java.util.List;

/**
 * <p>
 * SYS系统日志字段保存配置表 服务类
 * </p>
 *
 * @author Jim
 * @since 2023-08-29
 */
public interface SysLogRecordFieldService extends SuperService<SysLogRecordFieldEntity> {

    /**
    * 列表
    * @author Jim
    * @date: 2023-08-29
    * @return {@link SysLogRecordFieldEntity}
    */
    List<SysLogRecordFieldEntity> listByClassPaths(List<String> classPaths);


    /**
     * DTO 列表
     * @author Jim
     * @date: 2023-08-29
     */
    List<SysLogRecordFieldListDTO> listByDto(SysLogRecordFieldDTO.ListDTO dto);
}
