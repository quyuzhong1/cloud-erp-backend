package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.wms.dto.AfterSalePackDetailDTO;
import com.erp.model.wms.entity.AfterSalePackDetailEntity;
import com.erp.server.wms.mapper.AfterSalePackDetailMapper;
import com.erp.server.wms.service.AfterSalePackDetailService;
import com.erp.server.wms.service.OperateLogService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Optional;

/**
 * <p>
 * 售后装箱明细表 服务实现类
 * </p>
 *
 * @author lei.nie
 * @since 2026-05-12
 */
@Slf4j
@Service
public class AfterSalePackDetailServiceImpl extends SuperServiceImpl<AfterSalePackDetailMapper, AfterSalePackDetailEntity> implements AfterSalePackDetailService {
    @Resource
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(AfterSalePackDetailDTO.AddDTO addOrUpdateDTO) {
        AfterSalePackDetailEntity afterSalePackDetailEntity = new AfterSalePackDetailEntity();
        BeanMapperUtils.copy(addOrUpdateDTO, afterSalePackDetailEntity);
        log.info("开始新增售后装箱明细单");
        boolean save = super.save(afterSalePackDetailEntity);
        if (!save) {
            throw new ServiceException("售后装箱明细单保存失败");
        }
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "售后装箱明细单", afterSalePackDetailEntity.getId());
        operateLogService.addModuleOperateLog(msg, null, afterSalePackDetailEntity.getId(), "新增操作");
        return new BaseResultDTO.AddDTO(afterSalePackDetailEntity.getId(), afterSalePackDetailEntity.getId());
    }

    /**
     * 修改
     */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(AfterSalePackDetailDTO.UpdateDTO addOrUpdateDTO) {
        AfterSalePackDetailEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "售后装箱明细单"));
        AfterSalePackDetailEntity afterSalePackDetailEntity = BeanMapperUtils.map(AfterSalePackDetailEntity.class, addOrUpdateDTO);
        log.info("编辑 开始修改售后装箱明细单数据，id：【{}】", old.getId());
        boolean save = super.updateById(afterSalePackDetailEntity);
        if (!save) {
            throw new ServiceException("售后装箱明细单保存失败");
        }
        // 记录主单操作日志
        log.info("编辑 开始记录售后装箱明细单日志数据，id：【{}】", afterSalePackDetailEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), afterSalePackDetailEntity.getId(), "售后装箱明细单");
        operateLogService.addModuleOperateLogByObj(old, afterSalePackDetailEntity, null, afterSalePackDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<AfterSalePackDetailDTO.ListDTO> paging(PagingDTO<AfterSalePackDetailDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<AfterSalePackDetailDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if (CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        return new PagingVO(pageData);
    }

    @Override
    public AfterSalePackDetailDTO.ViewDTO view(String id) {
        AfterSalePackDetailEntity afterSalePackDetailEntity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到售后装箱明细单数据"));
        AfterSalePackDetailDTO.ViewDTO data = BeanMapperUtils.map(AfterSalePackDetailDTO.ViewDTO.class, afterSalePackDetailEntity);
        return data;
    }

}
