package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.BaseDicDTO;
import com.erp.model.plm.dto.BasicDictDTO;
import com.erp.model.plm.entity.BasicDictEntity;

import java.util.List;

/**
 * <p>
 * plm 字典表 服务类
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
public interface BasicDictService extends IService<BasicDictEntity> {

    Boolean saveOrUpdateDict(List<BasicDictDTO> dtos);

    List<BasicDictEntity>  listByType(String type);
}
