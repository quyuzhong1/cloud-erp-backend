package com.erp.server.tms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.common.business.dto.ApproveDTO;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.vo.LoginUser;

import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.tms.entity.CfgDeclareRuleConditionEntity;
import com.erp.server.tms.mapper.CfgDeclareRuleConditionMapper;
import com.erp.server.tms.service.CfgDeclareRuleConditionService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.tms.service.OperateLogService;
import com.common.core.exception.ServiceException;
import cn.hutool.core.util.ObjectUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.CfgDeclareRuleConditionDTO;
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
 * 报关规则条件表 服务实现类
 * </p>
 *
 * @author jack
 * @since 2026-04-20
 */
@Slf4j
@Service
public class CfgDeclareRuleConditionServiceImpl extends SuperServiceImpl<CfgDeclareRuleConditionMapper, CfgDeclareRuleConditionEntity> implements CfgDeclareRuleConditionService {
    @Resource
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgDeclareRuleConditionDTO.AddDTO addDTO) {
        CfgDeclareRuleConditionEntity cfgDeclareRuleConditionEntity = new CfgDeclareRuleConditionEntity();
        BeanMapperUtils.copy(addDTO, cfgDeclareRuleConditionEntity);

        // 数据处理
        handleData(cfgDeclareRuleConditionEntity);

        log.info("开始新增报关规则条件单");
        boolean save = super.save(cfgDeclareRuleConditionEntity);
        if(!save) {
            throw new ServiceException("报关规则条件单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "报关规则条件单" , cfgDeclareRuleConditionEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, cfgDeclareRuleConditionEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(cfgDeclareRuleConditionEntity.getId(), cfgDeclareRuleConditionEntity.getId());
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgDeclareRuleConditionDTO.UpdateDTO addOrUpdateDTO) {
        CfgDeclareRuleConditionEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "报关规则条件单"));
        CfgDeclareRuleConditionEntity cfgDeclareRuleConditionEntity =  BeanMapperUtils.map(CfgDeclareRuleConditionEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(cfgDeclareRuleConditionEntity);
        log.info("编辑 开始修改报关规则条件单数据，id：【{}】", old.getId());
        boolean save = super.updateById(cfgDeclareRuleConditionEntity);
        if(!save) {
            throw new ServiceException("报关规则条件单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录报关规则条件单日志数据，id：【{}】", cfgDeclareRuleConditionEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), cfgDeclareRuleConditionEntity.getId(), "报关规则条件单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, cfgDeclareRuleConditionEntity, null, cfgDeclareRuleConditionEntity.getId(), msg);
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<CfgDeclareRuleConditionDTO.ListDTO> paging(PagingDTO<CfgDeclareRuleConditionDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<CfgDeclareRuleConditionDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<CfgDeclareRuleConditionDTO.TabListDTO> tabList(PermissionsDTO param) {
        CfgDeclareRuleConditionDTO.PagingParamDTO searchParam = new CfgDeclareRuleConditionDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<CfgDeclareRuleConditionDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        // 获取状态列表
        // TODO 替换当前表Tab状态字段
        List<String> statusList = null;
        // 不存在的状态赋值为0
        List<String> existStatusList = list.stream().map(CfgDeclareRuleConditionDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        statusList.parallelStream().forEach(status -> {
            if(!existStatusList.contains(status)) {
            list.add(new CfgDeclareRuleConditionDTO.TabListDTO(status, 0));
        }
        });
        list.add(new CfgDeclareRuleConditionDTO.TabListDTO("all", list.stream().mapToInt(CfgDeclareRuleConditionDTO.TabListDTO::getCount).sum()));
        // 计算合计数量
        return list;
    }

    @Override
    public void exportList(CfgDeclareRuleConditionDTO.ExportDTO param, HttpServletResponse response) {
        List<CfgDeclareRuleConditionDTO.ListDTO> list = this.baseMapper.listExport(param);
        if(CollUtil.isEmpty(list)) {
           return;
        }
        // 数据处理
        fillList(list);

        // 导出数据
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/cfgDeclareRuleCondition.xlsx";
        String name = "报关规则条件单导出";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date).append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (Exception e) {
            throw new ServiceException(ApiError.FILE_EXPORT_FAILED);
        }
    }
    /**
    * 新增修改处理数据
    */
    private void handleData(CfgDeclareRuleConditionEntity cfgDeclareRuleConditionEntity) {
    // TODO 验证数据 & 数据赋值
    }

    @Override
    public CfgDeclareRuleConditionDTO.ViewDTO view(String id) {
    CfgDeclareRuleConditionEntity cfgDeclareRuleConditionEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到报关规则条件单数据"));
    CfgDeclareRuleConditionDTO.ViewDTO data = BeanMapperUtils.map(CfgDeclareRuleConditionDTO.ViewDTO.class, cfgDeclareRuleConditionEntity);
    // 数据填充处理
    fillOne(data);
    // TODO 查询明细数据（如果有的话）
    return data;
    }

    private void fillOne(CfgDeclareRuleConditionDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
          return;
        }
    }

   /**
    * 分页查询、导出 数据处理
   */
   private void fillList(List<CfgDeclareRuleConditionDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
            return;
        }
        // 属性赋值
        for(CfgDeclareRuleConditionDTO.ListDTO data : list) {
        // TODO 其他如需要显示名称的字段赋值
        }
   }
}
