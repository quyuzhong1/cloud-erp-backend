package com.erp.server.plm.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.vo.LoginUser;

import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.plm.entity.ProductChangeDetailEntity;
import com.erp.server.plm.mapper.ProductChangeDetailMapper;
import com.erp.server.plm.service.ProductChangeDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.plm.service.OperateLogService;
import com.common.core.exception.ServiceException;
import cn.hutool.core.util.ObjectUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.plm.dto.ProductChangeDetailDTO;
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
 * 产品变更信息表 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2026-02-03
 */
@Slf4j
@Service
public class ProductChangeDetailServiceImpl extends SuperServiceImpl<ProductChangeDetailMapper, ProductChangeDetailEntity> implements ProductChangeDetailService {
    @Resource
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(ProductChangeDetailDTO.AddDTO addDTO) {
        ProductChangeDetailEntity productChangeDetailEntity = new ProductChangeDetailEntity();
        BeanMapperUtils.copy(addDTO, productChangeDetailEntity);

        // 数据处理
        handleData(productChangeDetailEntity);

        log.info("开始新增产品变更信息单");
        boolean save = super.save(productChangeDetailEntity);
        if(!save) {
            throw new ServiceException("产品变更信息单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "产品变更信息单" , productChangeDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, productChangeDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(productChangeDetailEntity.getId(), productChangeDetailEntity.getId());
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(ProductChangeDetailDTO.UpdateDTO addOrUpdateDTO) {
        ProductChangeDetailEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "产品变更信息单"));
        ProductChangeDetailEntity productChangeDetailEntity =  BeanMapperUtils.map(ProductChangeDetailEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(productChangeDetailEntity);
        log.info("编辑 开始修改产品变更信息单数据，id：【{}】", old.getId());
        boolean save = super.updateById(productChangeDetailEntity);
        if(!save) {
            throw new ServiceException("产品变更信息单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录产品变更信息单日志数据，id：【{}】", productChangeDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), productChangeDetailEntity.getId(), "产品变更信息单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addSysLogByUpdate(old, productChangeDetailEntity, null, productChangeDetailEntity.getId(),"", msg);
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<ProductChangeDetailDTO.ListDTO> paging(PagingDTO<ProductChangeDetailDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<ProductChangeDetailDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<ProductChangeDetailDTO.TabListDTO> tabList(PermissionsDTO param) {
        ProductChangeDetailDTO.PagingParamDTO searchParam = new ProductChangeDetailDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<ProductChangeDetailDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        // 获取状态列表
        // TODO 替换当前表Tab状态字段
        List<String> statusList = null;
        // 不存在的状态赋值为0
        List<String> existStatusList = list.stream().map(ProductChangeDetailDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        statusList.parallelStream().forEach(status -> {
            if(!existStatusList.contains(status)) {
            list.add(new ProductChangeDetailDTO.TabListDTO(status, 0));
        }
        });
        list.add(new ProductChangeDetailDTO.TabListDTO("all", list.stream().mapToInt(ProductChangeDetailDTO.TabListDTO::getCount).sum()));
        // 计算合计数量
        return list;
    }

    @Override
    public void exportList(ProductChangeDetailDTO.ExportDTO param, HttpServletResponse response) {

    }
    /**
    * 新增修改处理数据
    */
    private void handleData(ProductChangeDetailEntity productChangeDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }

    @Override
    public ProductChangeDetailDTO.ViewDTO view(String id) {
    ProductChangeDetailEntity productChangeDetailEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到产品变更信息单数据"));
    ProductChangeDetailDTO.ViewDTO data = BeanMapperUtils.map(ProductChangeDetailDTO.ViewDTO.class, productChangeDetailEntity);
    // 数据填充处理
    fillOne(data);
    // TODO 查询明细数据（如果有的话）
    return data;
    }

    private void fillOne(ProductChangeDetailDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
          return;
        }
    }

   /**
    * 分页查询、导出 数据处理
   */
   private void fillList(List<ProductChangeDetailDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
            return;
        }
        // 属性赋值
        for(ProductChangeDetailDTO.ListDTO data : list) {
        // TODO 其他如需要显示名称的字段赋值
        }
   }
}
