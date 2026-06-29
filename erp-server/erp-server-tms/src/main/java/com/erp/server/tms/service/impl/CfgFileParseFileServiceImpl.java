package com.erp.server.tms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.common.business.dto.ApproveDTO;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.vo.LoginUser;

import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.tms.entity.CfgFileParseFileEntity;
import com.erp.server.tms.mapper.CfgFileParseFileMapper;
import com.erp.server.tms.service.CfgFileParseFileService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.tms.service.OperateLogService;
import com.common.core.exception.ServiceException;
import cn.hutool.core.util.ObjectUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.CfgFileParseFileDTO;
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
 * 清洗配置-文件识别规则子表 服务实现类
 * </p>
 *
 * @author jack
 * @since 2026-06-29
 */
@Slf4j
@Service
public class CfgFileParseFileServiceImpl extends SuperServiceImpl<CfgFileParseFileMapper, CfgFileParseFileEntity> implements CfgFileParseFileService {
    @Resource
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgFileParseFileDTO.AddDTO addOrUpdateDTO) {
        CfgFileParseFileEntity cfgFileParseFileEntity = new CfgFileParseFileEntity();
        BeanMapperUtils.copy(addOrUpdateDTO, cfgFileParseFileEntity);

        // 数据处理
        handleData(cfgFileParseFileEntity);

        log.info("开始新增清洗配置-文件识别规则子单");
        boolean save = super.save(cfgFileParseFileEntity);
        if(!save) {
            throw new ServiceException("清洗配置-文件识别规则子单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "清洗配置-文件识别规则子单" , cfgFileParseFileEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, cfgFileParseFileEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(cfgFileParseFileEntity.getId(), cfgFileParseFileEntity.getId());
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgFileParseFileDTO.UpdateDTO addOrUpdateDTO) {
        CfgFileParseFileEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "清洗配置-文件识别规则子单"));
        CfgFileParseFileEntity cfgFileParseFileEntity =  BeanMapperUtils.map(CfgFileParseFileEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(cfgFileParseFileEntity);
        log.info("编辑 开始修改清洗配置-文件识别规则子单数据，id：【{}】", old.getId());
        boolean save = super.updateById(cfgFileParseFileEntity);
        if(!save) {
            throw new ServiceException("清洗配置-文件识别规则子单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录清洗配置-文件识别规则子单日志数据，id：【{}】", cfgFileParseFileEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), cfgFileParseFileEntity.getId(), "清洗配置-文件识别规则子单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, cfgFileParseFileEntity, null, cfgFileParseFileEntity.getId(), msg);
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<CfgFileParseFileDTO.ListDTO> paging(PagingDTO<CfgFileParseFileDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<CfgFileParseFileDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<CfgFileParseFileDTO.TabListDTO> tabList(PermissionsDTO param) {
        CfgFileParseFileDTO.PagingParamDTO searchParam = new CfgFileParseFileDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<CfgFileParseFileDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        // 获取状态列表
        // TODO 替换当前表Tab状态字段
        List<String> statusList = null;
        // 不存在的状态赋值为0
        List<String> existStatusList = list.stream().map(CfgFileParseFileDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        statusList.parallelStream().forEach(status -> {
            if(!existStatusList.contains(status)) {
            list.add(new CfgFileParseFileDTO.TabListDTO(status, 0));
        }
        });
        list.add(new CfgFileParseFileDTO.TabListDTO("all", list.stream().mapToInt(CfgFileParseFileDTO.TabListDTO::getCount).sum()));
        // 计算合计数量
        return list;
    }

    @Override
    public void exportList(CfgFileParseFileDTO.ExportDTO param, HttpServletResponse response) {
        List<CfgFileParseFileDTO.ListDTO> list = this.baseMapper.listExport(param);
        if(CollUtil.isEmpty(list)) {
           return;
        }
        // 数据处理
        fillList(list);

        // 导出数据
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/cfgFileParseFile.xlsx";
        String name = "清洗配置-文件识别规则子单导出";
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
    private void handleData(CfgFileParseFileEntity cfgFileParseFileEntity) {
    // TODO 验证数据 & 数据赋值
    }

    @Override
    public CfgFileParseFileDTO.ViewDTO view(String id) {
    CfgFileParseFileEntity cfgFileParseFileEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到清洗配置-文件识别规则子单数据"));
    CfgFileParseFileDTO.ViewDTO data = BeanMapperUtils.map(CfgFileParseFileDTO.ViewDTO.class, cfgFileParseFileEntity);
    // 数据填充处理
    fillOne(data);
    // TODO 查询明细数据（如果有的话）
    return data;
    }

    private void fillOne(CfgFileParseFileDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
          return;
        }
    }

   /**
    * 分页查询、导出 数据处理
   */
   private void fillList(List<CfgFileParseFileDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
            return;
        }
        // 属性赋值
        for(CfgFileParseFileDTO.ListDTO data : list) {
        // TODO 其他如需要显示名称的字段赋值
        }
   }
}
