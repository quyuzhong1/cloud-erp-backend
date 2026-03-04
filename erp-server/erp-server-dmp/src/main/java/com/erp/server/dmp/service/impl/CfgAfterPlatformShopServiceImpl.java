package com.erp.server.dmp.service.impl;

import cn.hutool.core.util.StrUtil;
import com.common.business.annotation.DistributeLocker;
import com.erp.model.dmp.entity.CfgAfterPlatformShopEntity;
import com.erp.server.dmp.mapper.CfgAfterPlatformShopMapper;
import com.erp.server.dmp.service.CfgAfterPlatformShopService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.dmp.service.OperateLogService;
import com.common.core.exception.ServiceException;
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
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;

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

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<CfgAfterPlatformShopDTO.SaveDTO> save(CfgAfterPlatformShopDTO.SaveDTO addOrUpdateDTO)  {
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
        return null;
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

    /**
    * 新增修改处理数据
    */
    private void handleData(CfgAfterPlatformShopEntity cfgAfterPlatformShopEntity) {
    // TODO 验证数据 & 数据赋值
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
