package com.erp.server.sys.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.sys.dto.CustomizeFieldHiddenDTO;
import com.erp.model.sys.dto.FindCustomizeFieldDTO;
import com.erp.model.sys.entity.CustomizeFieldHiddenEntity;

import java.util.List;

/**
 * (CustomizeFieldDisplay)表服务接口
 *
 * @author yl
 * @since 2023-02-03 18:59:41
 */
public interface CustomizeFieldHiddenService extends IService<CustomizeFieldHiddenEntity> {


    Boolean add(List<CustomizeFieldHiddenDTO> dto);

    List<CustomizeFieldHiddenDTO> getByUserId(FindCustomizeFieldDTO dto);
}
