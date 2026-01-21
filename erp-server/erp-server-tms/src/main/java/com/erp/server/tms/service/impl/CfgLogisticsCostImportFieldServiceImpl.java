package com.erp.server.tms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.vo.LoginUser;

import cn.hutool.core.util.StrUtil;
import com.erp.model.sys.dto.DepartmentDTO;
import com.erp.model.sys.dto.SysDepartmentTreeDTO;
import com.erp.model.sys.dto.SysDepartmentUserNumberDTO;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.tms.entity.CfgLogisticsCostImportFieldEntity;
import com.erp.server.tms.mapper.CfgLogisticsCostImportFieldMapper;
import com.erp.server.tms.service.CfgLogisticsCostImportFieldService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.tms.service.OperateLogService;
import com.common.core.exception.ServiceException;
import cn.hutool.core.util.ObjectUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.CfgLogisticsCostImportFieldDTO;
import javax.annotation.Resource;
import java.util.stream.Collectors;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Sets;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import com.erp.model.sys.dto.SysCodeDTO;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.date.DateUtil;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 费用项配置字段基础表 服务实现类
 * </p>
 *
 * @author jack
 * @since 2026-01-20
 */
@Slf4j
@Service
public class CfgLogisticsCostImportFieldServiceImpl extends SuperServiceImpl<CfgLogisticsCostImportFieldMapper, CfgLogisticsCostImportFieldEntity> implements CfgLogisticsCostImportFieldService {


    @Override
    public List<CfgLogisticsCostImportFieldDTO.ListDTO> listByBusinessType(String businessType) {
        return this.baseMapper.listByBusinessType(businessType);
    }

    @Override
    public List<CfgLogisticsCostImportFieldDTO.TreeDTO> tree(String businessType) {
        List<CfgLogisticsCostImportFieldDTO.TreeDTO> flagList = this. baseMapper.findTree(businessType);

        List<CfgLogisticsCostImportFieldDTO.TreeDTO> departList = BeanMapperUtils.copyList(CfgLogisticsCostImportFieldDTO.TreeDTO.class, this.baseMapper.listByBusinessType(businessType));

        List<CfgLogisticsCostImportFieldDTO.TreeDTO> treeList = departList.stream().
                filter(item -> "0".equals(item.getParentId()))
                .map(item -> {
                    item.setChildrenList(getChildren(item, departList, flagList));
                    return item;
                }).collect(Collectors.toList());

        return treeList;
    }

    private List<CfgLogisticsCostImportFieldDTO.TreeDTO> getChildren(CfgLogisticsCostImportFieldDTO.TreeDTO item, List<CfgLogisticsCostImportFieldDTO.TreeDTO> departList, List<CfgLogisticsCostImportFieldDTO.TreeDTO> flagList) {
        List<CfgLogisticsCostImportFieldDTO.TreeDTO> collect = departList.stream().filter(dept -> item.getId().equals(dept.getParentId()))
                .map(d -> {
                    d.setChildrenList(getChildren(d, departList, flagList));
                    return d;
                }).collect(Collectors.toList());
        return CollectionUtils.isEmpty(collect) ? null : collect;
    }
}
