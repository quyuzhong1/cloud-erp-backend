package com.erp.server.sys.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.sys.entity.DictNoticeRoleOptionEntity;
import com.erp.server.sys.mapper.DictNoticeRoleOptionMapper;
import com.erp.server.sys.service.DictNoticeRoleOptionService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.sys.service.OperateLogService;
import com.erp.server.sys.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.sys.dto.DictNoticeRoleOptionDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
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
