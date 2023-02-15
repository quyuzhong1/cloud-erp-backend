package com.erp.server.sys.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.sys.dto.CustomizeFieldLayoutDTO;
import com.erp.model.sys.dto.FindCustomizeFieldDTO;
import com.erp.model.sys.entity.CustomizeFieldLayoutEntity;
import com.erp.model.sys.vo.UserFieldVO;

/**
 * (CustomizeFieldDisplay)表服务接口
 *
 * @author yl
 * @since 2023-02-03 18:59:41
 */
public interface CustomizeFieldLayoutService extends IService<CustomizeFieldLayoutEntity> {


    Boolean add(CustomizeFieldLayoutDTO dto);

    UserFieldVO getByUserId(FindCustomizeFieldDTO dto);
}
