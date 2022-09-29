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

    /**
     * @Description 根据名称查询字段是否存在
     * @Author Luo_WG
     * @Date 2022/9/29 11:02
     * @param type:字典类型
     * @param value:字典值
     * @return com.erp.model.plm.entity.BasicDictEntity
     **/
    BasicDictEntity checkBasicDict(String type, String value);
}
