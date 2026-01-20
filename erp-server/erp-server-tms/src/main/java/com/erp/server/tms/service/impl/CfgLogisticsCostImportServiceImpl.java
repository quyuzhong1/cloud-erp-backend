package com.erp.server.tms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.vo.LoginUser;

import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.tms.entity.CfgLogisticsCostImportEntity;
import com.erp.server.tms.mapper.CfgLogisticsCostImportMapper;
import com.erp.server.tms.service.CfgLogisticsCostImportService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.tms.service.OperateLogService;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.CfgLogisticsCostImportDTO;
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
 * 费用项配置 服务实现类
 * </p>
 *
 * @author jack
 * @since 2026-01-20
 */
@Slf4j
@Service
public class CfgLogisticsCostImportServiceImpl extends SuperServiceImpl<CfgLogisticsCostImportMapper, CfgLogisticsCostImportEntity> implements CfgLogisticsCostImportService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private DocNoGenHelper docNoGenHelper;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgLogisticsCostImportDTO.AddDTO addDTO) {
        CfgLogisticsCostImportEntity cfgLogisticsCostImportEntity = new CfgLogisticsCostImportEntity();
        BeanMapperUtils.copy(addDTO, cfgLogisticsCostImportEntity);

        // 数据处理
        handleData(cfgLogisticsCostImportEntity);

        log.info("开始新增费用项配置");
        // 生成单号
        // TODO 此处的null需填写生成单号类型，type查看BusinessNoTypeEnum枚举类 注意需要填写prefix 为单号前缀
        String code = docNoGenHelper.generateCode(null);
        cfgLogisticsCostImportEntity.setCode(code);
        boolean save = super.save(cfgLogisticsCostImportEntity);
        if(!save) {
            throw new ServiceException("费用项配置保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "费用项配置" , cfgLogisticsCostImportEntity.getCode());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, cfgLogisticsCostImportEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(cfgLogisticsCostImportEntity.getId(), code);
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgLogisticsCostImportDTO.UpdateDTO addOrUpdateDTO) {
        CfgLogisticsCostImportEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "费用项配置"));
        CfgLogisticsCostImportEntity cfgLogisticsCostImportEntity =  BeanMapperUtils.map(CfgLogisticsCostImportEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(cfgLogisticsCostImportEntity);
        log.info("编辑 开始修改费用项配置数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(cfgLogisticsCostImportEntity);
        if(!save) {
            throw new ServiceException("费用项配置保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录费用项配置日志数据，单号：【{}】", cfgLogisticsCostImportEntity.getCode());
            String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), cfgLogisticsCostImportEntity.getCode(), "费用项配置");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, cfgLogisticsCostImportEntity, null, cfgLogisticsCostImportEntity.getId(), msg);
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<CfgLogisticsCostImportDTO.ListDTO> paging(PagingDTO<CfgLogisticsCostImportDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<CfgLogisticsCostImportDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<CfgLogisticsCostImportDTO.TabListDTO> tabList(PermissionsDTO param) {
        CfgLogisticsCostImportDTO.PagingParamDTO searchParam = new CfgLogisticsCostImportDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<CfgLogisticsCostImportDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        // 获取状态列表
        // TODO 替换当前表Tab状态字段
        List<String> statusList = null;
        // 不存在的状态赋值为0
        List<String> existStatusList = list.stream().map(CfgLogisticsCostImportDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        statusList.parallelStream().forEach(status -> {
            if(!existStatusList.contains(status)) {
            list.add(new CfgLogisticsCostImportDTO.TabListDTO(status, 0));
        }
        });
        list.add(new CfgLogisticsCostImportDTO.TabListDTO("all", list.stream().mapToInt(CfgLogisticsCostImportDTO.TabListDTO::getCount).sum()));
        // 计算合计数量
        return list;
    }

    @Override
    public void exportList(CfgLogisticsCostImportDTO.ExportDTO param, HttpServletResponse response) {
        List<CfgLogisticsCostImportDTO.ListDTO> list = this.baseMapper.listExport(param);
        if(CollUtil.isEmpty(list)) {
           return;
        }
        // 数据处理
        fillList(list);

        // 导出数据
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/cfgLogisticsCostImport.xlsx";
        String name = "费用项配置导出";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date).append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_FILE_EXPORT_FAILED);
        }
    }
    /**
    * 新增修改处理数据
    */
    private void handleData(CfgLogisticsCostImportEntity cfgLogisticsCostImportEntity) {
    // TODO 验证数据 & 数据赋值
    }

    @Override
    public CfgLogisticsCostImportDTO.ViewDTO view(String id) {
    CfgLogisticsCostImportEntity cfgLogisticsCostImportEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到费用项配置数据"));
    CfgLogisticsCostImportDTO.ViewDTO data = BeanMapperUtils.map(CfgLogisticsCostImportDTO.ViewDTO.class, cfgLogisticsCostImportEntity);
    // 数据填充处理
    fillOne(data);
    // TODO 查询明细数据（如果有的话）
    return data;
    }

    private void fillOne(CfgLogisticsCostImportDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
          return;
        }
    }

   /**
    * 分页查询、导出 数据处理
   */
   private void fillList(List<CfgLogisticsCostImportDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
            return;
        }
        // 属性赋值
        for(CfgLogisticsCostImportDTO.ListDTO data : list) {
        // TODO 其他如需要显示名称的字段赋值
        }
   }
}
