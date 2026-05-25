package com.erp.server.dmp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.vo.LoginUser;

import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.dmp.entity.DmpFeishuUserInfoEntity;
import com.erp.server.dmp.mapper.DmpFeishuUserInfoMapper;
import com.erp.server.dmp.service.DmpFeishuUserInfoService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.dmp.service.OperateLogService;
import com.common.core.exception.ServiceException;
import cn.hutool.core.util.ObjectUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.dmp.dto.DmpFeishuUserInfoDTO;
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
 * DMP飞书用户信息 服务实现类
 * </p>
 *
 * @author jack
 * @since 2026-01-13
 */
@Slf4j
@Service
public class DmpFeishuUserInfoServiceImpl extends SuperServiceImpl<DmpFeishuUserInfoMapper, DmpFeishuUserInfoEntity> implements DmpFeishuUserInfoService {
    @Resource
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpFeishuUserInfoDTO.AddDTO addDTO) {
        DmpFeishuUserInfoEntity dmpFeishuUserInfoEntity = new DmpFeishuUserInfoEntity();
        BeanMapperUtils.copy(addDTO, dmpFeishuUserInfoEntity);

        // 数据处理
        handleData(dmpFeishuUserInfoEntity);

        log.info("开始新增DMP飞书用户信息");
        boolean save = super.save(dmpFeishuUserInfoEntity);
        if(!save) {
            throw new ServiceException("DMP飞书用户信息保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "DMP飞书用户信息" , dmpFeishuUserInfoEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, dmpFeishuUserInfoEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dmpFeishuUserInfoEntity.getId(), dmpFeishuUserInfoEntity.getId());
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpFeishuUserInfoDTO.UpdateDTO addOrUpdateDTO) {
        DmpFeishuUserInfoEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "DMP飞书用户信息"));
        DmpFeishuUserInfoEntity dmpFeishuUserInfoEntity =  BeanMapperUtils.map(DmpFeishuUserInfoEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(dmpFeishuUserInfoEntity);
        log.info("编辑 开始修改DMP飞书用户信息数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpFeishuUserInfoEntity);
        if(!save) {
            throw new ServiceException("DMP飞书用户信息保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录DMP飞书用户信息日志数据，id：【{}】", dmpFeishuUserInfoEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpFeishuUserInfoEntity.getId(), "DMP飞书用户信息");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, dmpFeishuUserInfoEntity, null, dmpFeishuUserInfoEntity.getId(), msg);
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<DmpFeishuUserInfoDTO.ListDTO> paging(PagingDTO<DmpFeishuUserInfoDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<DmpFeishuUserInfoDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<DmpFeishuUserInfoDTO.TabListDTO> tabList(PermissionsDTO param) {
        DmpFeishuUserInfoDTO.PagingParamDTO searchParam = new DmpFeishuUserInfoDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<DmpFeishuUserInfoDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        // 获取状态列表
        // TODO 替换当前表Tab状态字段
        List<String> statusList = null;
        // 不存在的状态赋值为0
        Set<String> existStatusSet = list.stream().map(DmpFeishuUserInfoDTO.TabListDTO::getTabFlag).collect(Collectors.toSet());
        for (String status : statusList) {
            if (!existStatusSet.contains(status)) {
                list.add(new DmpFeishuUserInfoDTO.TabListDTO(status, 0));
            }
        }
        list.add(new DmpFeishuUserInfoDTO.TabListDTO("all", list.stream().mapToInt(DmpFeishuUserInfoDTO.TabListDTO::getCount).sum()));
        // 计算合计数量
        return list;
    }

    @Override
    public void exportList(DmpFeishuUserInfoDTO.ExportDTO param, HttpServletResponse response) {

    }
    /**
    * 新增修改处理数据
    */
    private void handleData(DmpFeishuUserInfoEntity dmpFeishuUserInfoEntity) {
    // TODO 验证数据 & 数据赋值
    }

    @Override
    public DmpFeishuUserInfoDTO.ViewDTO view(String id) {
    DmpFeishuUserInfoEntity dmpFeishuUserInfoEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到DMP飞书用户信息数据"));
    DmpFeishuUserInfoDTO.ViewDTO data = BeanMapperUtils.map(DmpFeishuUserInfoDTO.ViewDTO.class, dmpFeishuUserInfoEntity);
    // 数据填充处理
    fillOne(data);
    // TODO 查询明细数据（如果有的话）
    return data;
    }

    private void fillOne(DmpFeishuUserInfoDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
          return;
        }
    }

   /**
    * 分页查询、导出 数据处理
   */
   private void fillList(List<DmpFeishuUserInfoDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
            return;
        }
        // 属性赋值
        for(DmpFeishuUserInfoDTO.ListDTO data : list) {
        // TODO 其他如需要显示名称的字段赋值
        }
   }
}
