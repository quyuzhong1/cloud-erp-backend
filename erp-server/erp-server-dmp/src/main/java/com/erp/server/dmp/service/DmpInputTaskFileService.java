package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpInputTaskFileEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpInputTaskFileDTO;

/**
 * <p>
 * 拉取任务文件存储 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
 */
public interface DmpInputTaskFileService extends SuperService<DmpInputTaskFileEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-06-11
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpInputTaskFileDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-06-11
    * @param dto
    * @return
    */
    Boolean update(DmpInputTaskFileDTO.UpdateDTO dto);


}
