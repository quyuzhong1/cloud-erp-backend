package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpBasicSystemEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpBasicSystemDTO;
import com.erp.model.plm.dto.DictControllerDTO;

import java.util.List;

/**
 * <p>
 * 外部系统 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
 */
public interface DmpBasicSystemService extends SuperService<DmpBasicSystemEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-06-11
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpBasicSystemDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-06-11
    * @param dto
    * @return
    */
    Boolean update(DmpBasicSystemDTO.UpdateDTO dto);

    /**
     * 查询所有系统（下拉接口）
     * @Author Luo_WG
     * @Date 2024/9/5 18:42
     * @return java.util.List<com.erp.model.plm.dto.DictControllerDTO.DictDropDownDTO>
     **/
    List<DictControllerDTO.DictDropDownDTO> listDmpBasicSystem();

    /**
     * 根据系统编号查询数据
     * @Author Luo_WG
     * @Date 2024/9/19 10:04
     * @param code
     * @return java.util.List<com.erp.model.dmp.entity.DmpBasicSystemEntity>
     **/
    DmpBasicSystemEntity listByCode(String code);
}
