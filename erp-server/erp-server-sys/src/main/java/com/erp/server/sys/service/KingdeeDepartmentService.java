package com.erp.server.sys.service;
import com.erp.model.sys.entity.KingdeeDepartmentEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.sys.dto.KingdeeDepartmentDTO;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Lambda
 * @since 2024-03-11
 */
public interface KingdeeDepartmentService extends SuperService<KingdeeDepartmentEntity> {

    /**
    * 新增
    * @author Lambda
    * @date: 2024-03-11
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(KingdeeDepartmentDTO.AddDTO dto);

    /**
    * 修改
    * @author Lambda
    * @date: 2024-03-11
    * @param dto
    * @return
    */
    Boolean update(KingdeeDepartmentDTO.UpdateDTO dto);


    /**
     * 初始化金蝶部门
     * @description
     * @param
     * @return
     * @date 2024-03-11 11:48
     * @author Lambda
     */
    Boolean init();

    /**
     * 详情
     * @param id
     * @return
     */
    KingdeeDepartmentDTO.ViewDTO view(String id);
}
