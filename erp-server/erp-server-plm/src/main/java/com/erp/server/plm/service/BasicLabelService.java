package com.erp.server.plm.service;
import com.erp.model.plm.entity.BasicLabelEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.plm.dto.BasicLabelDTO;

/**
 * <p>
 * 基础标签表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-09-13
 */
public interface BasicLabelService extends SuperService<BasicLabelEntity> {

    /**
    * 新增
    * @author Lambda
    * @date: 2023-09-13
    * @param dto
    * @return
    */
    String add(BasicLabelDTO.AddDTO dto);

    /**
    * 修改
    * @author Lambda
    * @date: 2023-09-13
    * @param dto
    * @return
    */
    Boolean update(BasicLabelDTO.UpdateDTO dto);


}
