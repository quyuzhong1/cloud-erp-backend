package com.erp.server.workflow.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.common.core.constant.SqlConstants;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.workflow.dto.DictBasicDTO;
import com.erp.model.workflow.entity.WorkMenuEntity;
import com.erp.server.workflow.mapper.WorkMenuMapper;
import com.erp.server.workflow.service.WorkMenuService;
import com.common.business.service.impl.SuperServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 工作台菜单基础表 服务实现类
 * </p>
 *
 * @author Cloud
 * @since 2023-04-21
 */
@Service
public class WorkMenuServiceImpl extends SuperServiceImpl<WorkMenuMapper, WorkMenuEntity> implements WorkMenuService {

    @Override
    public List<DictBasicDTO.DropDownDTO> listByCode(String code) {
        List<WorkMenuEntity> list = lambdaQuery()
                .eq(CharSequenceUtil.isNotBlank(code), WorkMenuEntity::getModuleCode, code)
                .list();
        return list.stream().map(DictBasicDTO.DropDownDTO::new).collect(java.util.stream.Collectors.toList());
    }

    @Override
    public String getSysClassifyByCode(String code) {
        List<WorkMenuEntity> list = lambdaQuery().eq(WorkMenuEntity::getModuleCode, code).list();
        return list.stream().map(WorkMenuEntity::getSysClassify).distinct().findFirst().orElse("");
    }

    @Override
    public WorkMenuEntity getByModuleCode(String code) {
        return lambdaQuery().eq(WorkMenuEntity::getModuleCode, code)
                .last(SqlConstants.LIMIT_1).oneOpt().orElseThrow(() -> new ServiceException(ApiError.ERROR_WORK_MENU_NOT_EXIST));
    }
}
