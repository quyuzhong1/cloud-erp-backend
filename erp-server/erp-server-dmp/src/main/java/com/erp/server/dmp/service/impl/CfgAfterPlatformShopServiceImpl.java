package com.erp.server.dmp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.common.business.dto.ApproveDTO;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.vo.LoginUser;

import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.dmp.entity.CfgAfterPlatformShopEntity;
import com.erp.server.dmp.mapper.CfgAfterPlatformShopMapper;
import com.erp.server.dmp.service.CfgAfterPlatformShopService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.dmp.service.OperateLogService;
import com.common.core.exception.ServiceException;
import cn.hutool.core.util.ObjectUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.dmp.dto.CfgAfterPlatformShopDTO;
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
 *  服务实现类
 * </p>
 *
 * @author wtr
 * @since 2026-03-03
 */
@Slf4j
@Service
public class CfgAfterPlatformShopServiceImpl extends SuperServiceImpl<CfgAfterPlatformShopMapper, CfgAfterPlatformShopEntity> implements CfgAfterPlatformShopService {
    @Resource
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgAfterPlatformShopDTO.AddDTO addDTO) {
        CfgAfterPlatformShopEntity cfgAfterPlatformShopEntity = new CfgAfterPlatformShopEntity();
        BeanMapperUtils.copy(addDTO, cfgAfterPlatformShopEntity);

        // 数据处理
        handleData(cfgAfterPlatformShopEntity);

        log.info("开始新增");
        boolean save = super.save(cfgAfterPlatformShopEntity);
        if(!save) {
            throw new ServiceException("保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "" , cfgAfterPlatformShopEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, cfgAfterPlatformShopEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(cfgAfterPlatformShopEntity.getId(), cfgAfterPlatformShopEntity.getId());
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgAfterPlatformShopDTO.UpdateDTO addOrUpdateDTO) {
        CfgAfterPlatformShopEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, ""));
        CfgAfterPlatformShopEntity cfgAfterPlatformShopEntity =  BeanMapperUtils.map(CfgAfterPlatformShopEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(cfgAfterPlatformShopEntity);
        log.info("编辑 开始修改数据，id：【{}】", old.getId());
        boolean save = super.updateById(cfgAfterPlatformShopEntity);
        if(!save) {
            throw new ServiceException("保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录日志数据，id：【{}】", cfgAfterPlatformShopEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), cfgAfterPlatformShopEntity.getId(), "");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, cfgAfterPlatformShopEntity, null, cfgAfterPlatformShopEntity.getId(), msg);
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<CfgAfterPlatformShopDTO.ListDTO> paging(PagingDTO<CfgAfterPlatformShopDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<CfgAfterPlatformShopDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<CfgAfterPlatformShopDTO.TabListDTO> tabList(PermissionsDTO param) {
        CfgAfterPlatformShopDTO.PagingParamDTO searchParam = new CfgAfterPlatformShopDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<CfgAfterPlatformShopDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        // 获取状态列表
        // TODO 替换当前表Tab状态字段
        List<String> statusList = null;
        // 不存在的状态赋值为0
        List<String> existStatusList = list.stream().map(CfgAfterPlatformShopDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        statusList.parallelStream().forEach(status -> {
            if(!existStatusList.contains(status)) {
            list.add(new CfgAfterPlatformShopDTO.TabListDTO(status, 0));
        }
        });
        list.add(new CfgAfterPlatformShopDTO.TabListDTO("all", list.stream().mapToInt(CfgAfterPlatformShopDTO.TabListDTO::getCount).sum()));
        // 计算合计数量
        return list;
    }

    @Override
    public void exportList(CfgAfterPlatformShopDTO.ExportDTO param, HttpServletResponse response) {
        List<CfgAfterPlatformShopDTO.ListDTO> list = this.baseMapper.listExport(param);
        if(CollUtil.isEmpty(list)) {
           return;
        }
        // 数据处理
        fillList(list);

        // 导出数据
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/cfgAfterPlatformShop.xlsx";
        String name = "导出";
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
    private void handleData(CfgAfterPlatformShopEntity cfgAfterPlatformShopEntity) {
    // TODO 验证数据 & 数据赋值
    }

    @Override
    public CfgAfterPlatformShopDTO.ViewDTO view(String id) {
    CfgAfterPlatformShopEntity cfgAfterPlatformShopEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到数据"));
    CfgAfterPlatformShopDTO.ViewDTO data = BeanMapperUtils.map(CfgAfterPlatformShopDTO.ViewDTO.class, cfgAfterPlatformShopEntity);
    // 数据填充处理
    fillOne(data);
    // TODO 查询明细数据（如果有的话）
    return data;
    }

    private void fillOne(CfgAfterPlatformShopDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
          return;
        }
    }

   /**
    * 分页查询、导出 数据处理
   */
   private void fillList(List<CfgAfterPlatformShopDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
            return;
        }
        // 属性赋值
        for(CfgAfterPlatformShopDTO.ListDTO data : list) {
        // TODO 其他如需要显示名称的字段赋值
        }
   }
}
