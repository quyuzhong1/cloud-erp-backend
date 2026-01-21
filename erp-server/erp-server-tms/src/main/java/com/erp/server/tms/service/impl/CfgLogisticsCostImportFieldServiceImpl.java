package com.erp.server.tms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.vo.LoginUser;

import cn.hutool.core.util.StrUtil;
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
    @Resource
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgLogisticsCostImportFieldDTO.AddDTO addDTO) {
        CfgLogisticsCostImportFieldEntity cfgLogisticsCostImportFieldEntity = new CfgLogisticsCostImportFieldEntity();
        BeanMapperUtils.copy(addDTO, cfgLogisticsCostImportFieldEntity);

        // 数据处理
        handleData(cfgLogisticsCostImportFieldEntity);

        log.info("开始新增费用项配置字段基础单");
        boolean save = super.save(cfgLogisticsCostImportFieldEntity);
        if(!save) {
            throw new ServiceException("费用项配置字段基础单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "费用项配置字段基础单" , cfgLogisticsCostImportFieldEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, cfgLogisticsCostImportFieldEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(cfgLogisticsCostImportFieldEntity.getId(), cfgLogisticsCostImportFieldEntity.getId());
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgLogisticsCostImportFieldDTO.UpdateDTO addOrUpdateDTO) {
        CfgLogisticsCostImportFieldEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "费用项配置字段基础单"));
        CfgLogisticsCostImportFieldEntity cfgLogisticsCostImportFieldEntity =  BeanMapperUtils.map(CfgLogisticsCostImportFieldEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(cfgLogisticsCostImportFieldEntity);
        log.info("编辑 开始修改费用项配置字段基础单数据，id：【{}】", old.getId());
        boolean save = super.updateById(cfgLogisticsCostImportFieldEntity);
        if(!save) {
            throw new ServiceException("费用项配置字段基础单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录费用项配置字段基础单日志数据，id：【{}】", cfgLogisticsCostImportFieldEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), cfgLogisticsCostImportFieldEntity.getId(), "费用项配置字段基础单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, cfgLogisticsCostImportFieldEntity, null, cfgLogisticsCostImportFieldEntity.getId(), msg);
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<CfgLogisticsCostImportFieldDTO.ListDTO> paging(PagingDTO<CfgLogisticsCostImportFieldDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<CfgLogisticsCostImportFieldDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<CfgLogisticsCostImportFieldDTO.TabListDTO> tabList(PermissionsDTO param) {
        CfgLogisticsCostImportFieldDTO.PagingParamDTO searchParam = new CfgLogisticsCostImportFieldDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<CfgLogisticsCostImportFieldDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        // 获取状态列表
        // TODO 替换当前表Tab状态字段
        List<String> statusList = null;
        // 不存在的状态赋值为0
        List<String> existStatusList = list.stream().map(CfgLogisticsCostImportFieldDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        statusList.parallelStream().forEach(status -> {
            if(!existStatusList.contains(status)) {
            list.add(new CfgLogisticsCostImportFieldDTO.TabListDTO(status, 0));
        }
        });
        list.add(new CfgLogisticsCostImportFieldDTO.TabListDTO("all", list.stream().mapToInt(CfgLogisticsCostImportFieldDTO.TabListDTO::getCount).sum()));
        // 计算合计数量
        return list;
    }

    @Override
    public void exportList(CfgLogisticsCostImportFieldDTO.ExportDTO param, HttpServletResponse response) {

    }
    /**
    * 新增修改处理数据
    */
    private void handleData(CfgLogisticsCostImportFieldEntity cfgLogisticsCostImportFieldEntity) {
    // TODO 验证数据 & 数据赋值
    }

    @Override
    public CfgLogisticsCostImportFieldDTO.ViewDTO view(String id) {
    CfgLogisticsCostImportFieldEntity cfgLogisticsCostImportFieldEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到费用项配置字段基础单数据"));
    CfgLogisticsCostImportFieldDTO.ViewDTO data = BeanMapperUtils.map(CfgLogisticsCostImportFieldDTO.ViewDTO.class, cfgLogisticsCostImportFieldEntity);
    // 数据填充处理
    fillOne(data);
    // TODO 查询明细数据（如果有的话）
    return data;
    }

    private void fillOne(CfgLogisticsCostImportFieldDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
          return;
        }
    }

   /**
    * 分页查询、导出 数据处理
   */
   private void fillList(List<CfgLogisticsCostImportFieldDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
            return;
        }
        // 属性赋值
        for(CfgLogisticsCostImportFieldDTO.ListDTO data : list) {
        // TODO 其他如需要显示名称的字段赋值
        }
   }
}
