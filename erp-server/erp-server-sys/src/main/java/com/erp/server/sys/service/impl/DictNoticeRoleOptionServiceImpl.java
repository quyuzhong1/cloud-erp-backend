package com.erp.server.sys.service.impl;


import cn.hutool.core.util.StrUtil;
import com.erp.model.sys.entity.DictNoticeRoleOptionEntity;
import com.erp.server.sys.mapper.DictNoticeRoleOptionMapper;
import com.erp.server.sys.service.DictNoticeRoleOptionService;
import com.common.business.service.impl.SuperServiceImpl;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.sys.dto.DictNoticeRoleOptionDTO;
import java.util.*;
import com.common.core.utils.*;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-06-04
 */
@Slf4j
@Service
public class DictNoticeRoleOptionServiceImpl extends SuperServiceImpl<DictNoticeRoleOptionMapper, DictNoticeRoleOptionEntity> implements DictNoticeRoleOptionService {


    @Override
    public List<DictNoticeRoleOptionDTO.DropDownDTO> dropDownList(String businessType) {
        //1.判断businessType 未空着返回空集合
        if (StrUtil.isBlank(businessType)) {
            return Collections.emptyList();
        }
        //2.使用lambdaQuery查询数据 查询出businessType，并且disabled为false的数据，根据index进行排序
        List<DictNoticeRoleOptionEntity> list = lambdaQuery().eq(DictNoticeRoleOptionEntity::getBusinessType, businessType)
                .eq(DictNoticeRoleOptionEntity::getDisabled, Boolean.FALSE)
                .orderByAsc(DictNoticeRoleOptionEntity::getIndex)
                .list();
        return BeanMapper.copyList(list,DictNoticeRoleOptionDTO.DropDownDTO.class);
    }

}
