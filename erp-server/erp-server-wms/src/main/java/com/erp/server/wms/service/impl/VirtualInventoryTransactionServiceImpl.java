package com.erp.server.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.vo.LoginUser;

import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.wms.entity.VirtualInventoryTransactionEntity;
import com.erp.server.wms.mapper.VirtualInventoryTransactionMapper;
import com.erp.server.wms.service.VirtualInventoryTransactionService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.wms.service.OperateLogService;
import com.common.core.exception.ServiceException;
import cn.hutool.core.util.ObjectUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.VirtualInventoryTransactionDTO;
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
 * 虚拟仓库存事务表 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2025-12-18
 */
@Slf4j
@Service
public class VirtualInventoryTransactionServiceImpl extends SuperServiceImpl<VirtualInventoryTransactionMapper, VirtualInventoryTransactionEntity> implements VirtualInventoryTransactionService {
    @Resource
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(VirtualInventoryTransactionDTO.AddDTO addDTO) {
        VirtualInventoryTransactionEntity virtualInventoryTransactionEntity = new VirtualInventoryTransactionEntity();
        BeanMapperUtils.copy(addDTO, virtualInventoryTransactionEntity);

        // 数据处理
        handleData(virtualInventoryTransactionEntity);

        log.info("开始新增虚拟仓库存事务单");
        boolean save = super.save(virtualInventoryTransactionEntity);
        if(!save) {
            throw new ServiceException("虚拟仓库存事务单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "虚拟仓库存事务单" , virtualInventoryTransactionEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, virtualInventoryTransactionEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(virtualInventoryTransactionEntity.getId(), virtualInventoryTransactionEntity.getId());
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(VirtualInventoryTransactionDTO.UpdateDTO addOrUpdateDTO) {
        VirtualInventoryTransactionEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "虚拟仓库存事务单"));
        VirtualInventoryTransactionEntity virtualInventoryTransactionEntity =  BeanMapperUtils.map(VirtualInventoryTransactionEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(virtualInventoryTransactionEntity);
        log.info("编辑 开始修改虚拟仓库存事务单数据，id：【{}】", old.getId());
        boolean save = super.updateById(virtualInventoryTransactionEntity);
        if(!save) {
            throw new ServiceException("虚拟仓库存事务单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录虚拟仓库存事务单日志数据，id：【{}】", virtualInventoryTransactionEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), virtualInventoryTransactionEntity.getId(), "虚拟仓库存事务单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, virtualInventoryTransactionEntity, null, virtualInventoryTransactionEntity.getId(), msg);
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<VirtualInventoryTransactionDTO.ListDTO> paging(PagingDTO<VirtualInventoryTransactionDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<VirtualInventoryTransactionDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<VirtualInventoryTransactionDTO.TabListDTO> tabList(PermissionsDTO param) {
        VirtualInventoryTransactionDTO.PagingParamDTO searchParam = new VirtualInventoryTransactionDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<VirtualInventoryTransactionDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        // 获取状态列表
        // TODO 替换当前表Tab状态字段
        List<String> statusList = null;
        // 不存在的状态赋值为0
        List<String> existStatusList = list.stream().map(VirtualInventoryTransactionDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        statusList.parallelStream().forEach(status -> {
            if(!existStatusList.contains(status)) {
            list.add(new VirtualInventoryTransactionDTO.TabListDTO(status, 0));
        }
        });
        list.add(new VirtualInventoryTransactionDTO.TabListDTO("all", list.stream().mapToInt(VirtualInventoryTransactionDTO.TabListDTO::getCount).sum()));
        // 计算合计数量
        return list;
    }

    @Override
    public void exportList(VirtualInventoryTransactionDTO.ExportDTO param, HttpServletResponse response) {
        List<VirtualInventoryTransactionDTO.ListDTO> list = this.baseMapper.listExport(param);
        if(CollUtil.isEmpty(list)) {
           return;
        }
        // 数据处理
        fillList(list);

        // 导出数据
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/virtualInventoryTransaction.xlsx";
        String name = "虚拟仓库存事务单导出";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date).append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }
    }
    /**
    * 新增修改处理数据
    */
    private void handleData(VirtualInventoryTransactionEntity virtualInventoryTransactionEntity) {
    // TODO 验证数据 & 数据赋值
    }

    @Override
    public VirtualInventoryTransactionDTO.ViewDTO view(String id) {
    VirtualInventoryTransactionEntity virtualInventoryTransactionEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到虚拟仓库存事务单数据"));
    VirtualInventoryTransactionDTO.ViewDTO data = BeanMapperUtils.map(VirtualInventoryTransactionDTO.ViewDTO.class, virtualInventoryTransactionEntity);
    // 数据填充处理
    fillOne(data);
    // TODO 查询明细数据（如果有的话）
    return data;
    }

    private void fillOne(VirtualInventoryTransactionDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
          return;
        }
    }

   /**
    * 分页查询、导出 数据处理
   */
   private void fillList(List<VirtualInventoryTransactionDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
            return;
        }
        // 属性赋值
        for(VirtualInventoryTransactionDTO.ListDTO data : list) {
        // TODO 其他如需要显示名称的字段赋值
        }
   }
}
