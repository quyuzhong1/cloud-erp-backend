package com.erp.server.sys.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.enums.ThirdpartyPlatformEnum;
import com.erp.model.sys.entity.SysDepartmentThirdEntity;
import com.erp.model.sys.entity.SysUserThirdEntity;
import com.erp.model.sys.vo.ThirdUnionDTO;
import com.erp.model.workflow.enums.CfgApproveSyncSyncPlatformEnum;
import com.erp.sdk.fs.service.FsService;
import com.erp.server.sys.mapper.SysDepartmentThirdMapper;
import com.erp.server.sys.service.SysDepartmentThirdService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import com.lark.oapi.service.contact.v3.model.ChildrenDepartmentReq;
import com.lark.oapi.service.contact.v3.model.ChildrenDepartmentResp;
import com.lark.oapi.service.contact.v3.model.ChildrenDepartmentRespBody;
import com.lark.oapi.service.contact.v3.model.Department;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.sys.dto.SysDepartmentThirdDTO;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;

/**
 * <p>
 * 第三方 部门信息 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-05-15
 */
@Slf4j
@Service
public class SysDepartmentThirdServiceImpl extends SuperServiceImpl<SysDepartmentThirdMapper, SysDepartmentThirdEntity> implements SysDepartmentThirdService {

    @Resource
    private FsService fsService;

    @Override
    public List<SysDepartmentThirdDTO.ThirdDeptDropDownDTO> listThirdDeptDropDown(SysDepartmentThirdDTO.ThirdDeptParamDTO dto) {
        LambdaQueryWrapper<SysDepartmentThirdEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SysDepartmentThirdEntity::getPlatform, dto.getPlatformList());
        if(StringUtils.isNotBlank(dto.getDepartmentName())){
            queryWrapper.like(SysDepartmentThirdEntity::getThirdDepartmentName, dto.getDepartmentName());
        }
        List<SysDepartmentThirdEntity> list = this.list(queryWrapper);
        if(CollUtil.isEmpty(list)){
            return Collections.emptyList();
        }
        return BeanMapper.copyList(list, SysDepartmentThirdDTO.ThirdDeptDropDownDTO.class);
    }

    @Override
    public void syncFsDept() {
        try {
            ChildrenDepartmentResp childrenDepartment = fsService.getChildrenDepartments(null);
            processAndSaveDepartmentData(childrenDepartment);
            // 分页处理
            while (childrenDepartment != null && StringUtils.isNotBlank(childrenDepartment.getData().getPageToken())) {
                String pageToken = childrenDepartment.getData().getPageToken();
                ChildrenDepartmentReq req = buildPageRequest(pageToken);
                childrenDepartment = fsService.getChildrenDepartments(req);
                processAndSaveDepartmentData(childrenDepartment);
            }
        } catch (Exception e) {
            // 记录日志或通知
            e.printStackTrace(); // 替换为实际日志记录方式
        }
    }

    @Override
    public SysDepartmentThirdEntity findByDepartmentId(String platform, String departmentId) {
        SysDepartmentThirdEntity one = lambdaQuery().eq(SysDepartmentThirdEntity::getPlatform, platform)
                .in(SysDepartmentThirdEntity::getDeptId, departmentId)
                .ne(SysDepartmentThirdEntity::getThirdOpenDeptId, "")
                .ne(SysDepartmentThirdEntity::getThirdDeptId, "")
                .one();
        return one;
    }

    private void processAndSaveDepartmentData(ChildrenDepartmentResp response) {
        if (response == null || response.getData() == null) {
            return;
        }

        Department[] items = response.getData().getItems();
        if (items == null || items.length == 0) {
            return;
        }

        List<SysDepartmentThirdEntity> result = new ArrayList<>();
        for (Department department : items) {
            if (department.getStatus() != null && department.getStatus().getIsDeleted()) {
                continue;
            }
            SysDepartmentThirdEntity entity = new SysDepartmentThirdEntity();
            entity.setDeptId("");
            entity.setPlatform(CfgApproveSyncSyncPlatformEnum.FEISHU.getCode());
            entity.setThirdDeptId(department.getDepartmentId());
            entity.setThirdOpenDeptId(department.getOpenDepartmentId());
            entity.setThirdParentOpenDeptId(department.getParentDepartmentId());
            entity.setThirdDepartmentName(department.getName());
            result.add(entity);
        }

        if (!result.isEmpty()) {
            saveBatch(result);
        }
    }

    // 构建分页请求对象
    private ChildrenDepartmentReq buildPageRequest(String pageToken) {
        return ChildrenDepartmentReq.newBuilder()
                .departmentId("0")
                .userIdType("open_id")
                .departmentIdType("open_department_id")
                .fetchChild(true)
                .pageToken(pageToken)
                .pageSize(50)
                .build();
    }
}
