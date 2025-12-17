package com.erp.server.dmp.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.ThirdShopDTO;
import com.erp.model.dmp.entity.ThirdLogisticsEntity;
import com.erp.server.dmp.mapper.ThirdLogisticsMapper;
import com.erp.server.dmp.service.ThirdLogisticsService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.dmp.service.OperateLogService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.dmp.dto.ThirdLogisticsDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 三方渠道表 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2024-12-11
 */
@Slf4j
@Service
public class ThirdLogisticsServiceImpl extends SuperServiceImpl<ThirdLogisticsMapper, ThirdLogisticsEntity> implements ThirdLogisticsService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(ThirdLogisticsDTO.AddDTO addDTO) {
        ThirdLogisticsEntity thirdLogisticsEntity = new ThirdLogisticsEntity();
        BeanMapperUtils.copy(addDTO, thirdLogisticsEntity);

        // 数据处理
        handleData(thirdLogisticsEntity);

        log.info("开始新增三方渠道单");
        boolean save = super.save(thirdLogisticsEntity);
        if(!save) {
            throw new ServiceException("三方渠道单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "三方渠道单" , thirdLogisticsEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, thirdLogisticsEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(thirdLogisticsEntity.getId(), thirdLogisticsEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(ThirdLogisticsDTO.UpdateDTO updateDTO) {
        ThirdLogisticsEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "三方渠道单"));
        ThirdLogisticsEntity thirdLogisticsEntity =  BeanMapperUtils.map(ThirdLogisticsEntity.class, updateDTO);

        // 数据处理
        handleData(thirdLogisticsEntity);
        log.info("编辑 开始修改三方渠道单数据，id：【{}】", old.getId());
        boolean save = super.updateById(thirdLogisticsEntity);
        if(!save) {
            throw new ServiceException("三方渠道单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录三方渠道单日志数据，id：【{}】", thirdLogisticsEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), thirdLogisticsEntity.getId(), "三方渠道单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, thirdLogisticsEntity, null, thirdLogisticsEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<ThirdLogisticsDTO.PageSelectDTO> pagingSelect(PagingDTO<ThirdLogisticsDTO.SelectDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<ThirdLogisticsDTO.PageSelectDTO> pageData = this.baseMapper.pagingSelect(query, dto.getParams());
        if (CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        return new PagingVO<>(pageData);
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(ThirdLogisticsEntity thirdLogisticsEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
