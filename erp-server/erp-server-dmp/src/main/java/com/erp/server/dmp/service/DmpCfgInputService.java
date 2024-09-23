package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpCfgInputEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpCfgInputDTO;

import java.util.List;

/**
 * <p>
 * 输入信息 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
 */
public interface DmpCfgInputService extends SuperService<DmpCfgInputEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-06-11
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpCfgInputDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-06-11
    * @param dto
    * @return
    */
    Boolean update(DmpCfgInputDTO.UpdateDTO dto);

    /**
     * 查询系统单据
     * @Author Luo_WG
     * @Date 2024/9/5 19:33
     * @param id
     * @return java.util.List<com.erp.model.dmp.dto.DmpCfgInputDTO.ListDmpCfgInputDTO>
     **/
    List<DmpCfgInputDTO.ListDmpCfgInputDTO> listDmpCfgInput(String id);

    /**
     * 查询所有输入配置
     * @Author Luo_WG
     * @Date 2024/9/23 9:42
     * @return java.util.List<com.erp.model.dmp.dto.DmpCfgInputDTO.ListDmpCfgInputDTO>
     **/
    List<DmpCfgInputDTO.ListDmpCfgInputDTO> allDmpCfgInput();
}
