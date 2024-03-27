package com.erp.server.tms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.TmsB2cDeclareReconciliationDetailDTO;
import com.erp.model.tms.dto.TmsFirstMileReconciliationDTO;
import com.erp.model.tms.entity.TmsB2cDeclareReconciliationDetailEntity;
import com.erp.model.tms.entity.TmsFirstMileReconciliationDetailEntity;
import com.erp.model.tms.enums.ReconciliationStatusEnum;
import com.erp.model.tms.enums.TmsB2cDeclareReconciliationStatusEnum;
import com.erp.server.tms.mapper.TmsFirstMileReconciliationDetailMapper;
import com.erp.server.tms.service.TmsFirstMileReconciliationDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.TmsFirstMileReconciliationDetailDTO;

import java.util.*;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 头程对账单明细 服务实现类
 * </p>
 *
 * @author Jim
 * @since 2024-03-25
 */
@Slf4j
@Service
public class TmsFirstMileReconciliationDetailServiceImpl extends SuperServiceImpl<TmsFirstMileReconciliationDetailMapper, TmsFirstMileReconciliationDetailEntity> implements TmsFirstMileReconciliationDetailService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private CommonService commonService;



    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(TmsFirstMileReconciliationDetailDTO.AddDTO addDTO) {
        TmsFirstMileReconciliationDetailEntity tmsFirstMileReconciliationDetailEntity = new TmsFirstMileReconciliationDetailEntity();
        BeanMapperUtils.copy(addDTO, tmsFirstMileReconciliationDetailEntity);

        // 数据处理
        handleData(tmsFirstMileReconciliationDetailEntity);

        log.info("开始新增头程对账单明细");
        boolean save = super.save(tmsFirstMileReconciliationDetailEntity);
        if (!save) {
            throw new ServiceException("头程对账单明细保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "头程对账单明细", tmsFirstMileReconciliationDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, tmsFirstMileReconciliationDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(tmsFirstMileReconciliationDetailEntity.getId(), tmsFirstMileReconciliationDetailEntity.getId());
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(TmsFirstMileReconciliationDetailDTO.UpdateDTO updateDTO) {
        TmsFirstMileReconciliationDetailEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "头程对账单明细"));
        TmsFirstMileReconciliationDetailEntity tmsFirstMileReconciliationDetailEntity = BeanMapperUtils.map(TmsFirstMileReconciliationDetailEntity.class, updateDTO);

        // 数据处理
        handleData(tmsFirstMileReconciliationDetailEntity);
        log.info("编辑 开始修改头程对账单明细数据，id：【{}】", old.getId());
        boolean save = super.updateById(tmsFirstMileReconciliationDetailEntity);
        if (!save) {
            throw new ServiceException("头程对账单明细保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
        log.info("编辑 开始记录头程对账单明细日志数据，id：【{}】", tmsFirstMileReconciliationDetailEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), tmsFirstMileReconciliationDetailEntity.getId(), "头程对账单明细");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, tmsFirstMileReconciliationDetailEntity, null, tmsFirstMileReconciliationDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<TmsFirstMileReconciliationDetailDTO.ListDTO> paging(PagingDTO<TmsFirstMileReconciliationDetailDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page<?> query = new Page<>(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<TmsFirstMileReconciliationDetailDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO<>(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    private void fillList(List<TmsFirstMileReconciliationDetailDTO.ListDTO> records) {

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO updateStatus(String id, String status) {
        TmsFirstMileReconciliationDetailEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到头程对账单明细数据"));
        if (!StrUtil.equals(entity.getStatus(), ReconciliationStatusEnum.TO_BE_CONFIRM.getCode())) {
            throw new ServiceException("只有待对账数据支持更新对账");
        }
        lambdaUpdate().eq(TmsFirstMileReconciliationDetailEntity::getId,id)
                .set(TmsFirstMileReconciliationDetailEntity::getStatus,status)
                .update();
        // 记录主单操作日志
        operateLogService.addModuleOperateLog(StrUtil.format("头程对账单【{}】更新状态为【{}】",entity.getSourceCode(), TmsB2cDeclareReconciliationStatusEnum.getName(status)), ModuleTypeEnum.TMS_FIRST_MILE_RECONCILIATION.getCode(), entity.getId(), "更新状态操作");
        return BatchResultDTO.success(entity.getId(), entity.getSourceCode(), OperationTypeEnum.UPDATE_STATUS);
    }

    @Override
    public TmsFirstMileReconciliationDetailDTO.ImportDTO importFile(TmsB2cDeclareReconciliationDetailDTO.ExcelImportDTO excelImportDTO, HttpServletResponse response) {
        return null;
    }

    @Override
    public PagingVO<TmsFirstMileReconciliationDetailDTO.WaitListDTO> waitPaging(PagingDTO<TmsFirstMileReconciliationDetailDTO.PagingParamDTO> dto) {
        return null;
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(TmsFirstMileReconciliationDetailEntity tmsFirstMileReconciliationDetailEntity) {
        // TODO 验证数据 & 数据赋值
    }
}
