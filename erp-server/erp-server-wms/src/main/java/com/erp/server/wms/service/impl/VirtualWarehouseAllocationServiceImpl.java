package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.entity.VirtualWarehouseAllocationEntity;
import com.erp.model.wms.enums.VirtualWarehouseAllocationStatusEnum;
import com.erp.model.wms.enums.VirtualWarehouseAllocationTypeEnum;
import com.erp.server.wms.mapper.VirtualWarehouseAllocationMapper;
import com.erp.server.wms.service.VirtualWarehouseAllocationDetailService;
import com.erp.server.wms.service.VirtualWarehouseAllocationService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.wms.service.OperateLogService;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.VirtualWarehouseAllocationDTO;

import java.util.*;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;

/**
 * <p>
 * 虚拟仓分货单 服务实现类
 * </p>
 *
 * @author hyj
 * @since 2024-06-05
 */
@Slf4j
@Service
public class VirtualWarehouseAllocationServiceImpl extends SuperServiceImpl<VirtualWarehouseAllocationMapper, VirtualWarehouseAllocationEntity> implements VirtualWarehouseAllocationService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private VirtualWarehouseAllocationDetailService virtualWarehouseAllocationDetailService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(VirtualWarehouseAllocationDTO.AddDTO addDTO) {
        VirtualWarehouseAllocationEntity virtualWarehouseAllocationEntity = new VirtualWarehouseAllocationEntity();
        BeanMapperUtils.copy(addDTO, virtualWarehouseAllocationEntity);

        // 数据处理
        handleData(virtualWarehouseAllocationEntity);
        log.info("开始新增虚拟仓分货单");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_FH);
        virtualWarehouseAllocationEntity.setCode(code);
        boolean save = super.save(virtualWarehouseAllocationEntity);
        if (!save) {
            throw new ServiceException("虚拟仓分货单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "虚拟仓分货单", virtualWarehouseAllocationEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.VIRTUAL_WAREHOUSE_ALLOCATION.getCode(), virtualWarehouseAllocationEntity.getId(), "新增操作");
        // 新增明细
        virtualWarehouseAllocationDetailService.batchAdd(addDTO, virtualWarehouseAllocationEntity.getId());
        return new BaseResultDTO.AddDTO(virtualWarehouseAllocationEntity.getId(), code);
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(VirtualWarehouseAllocationDTO.UpdateDTO updateDTO) {
        VirtualWarehouseAllocationEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "虚拟仓分货单"));
        VirtualWarehouseAllocationEntity virtualWarehouseAllocationEntity = BeanMapperUtils.map(VirtualWarehouseAllocationEntity.class, updateDTO);

        // 数据处理
        handleData(virtualWarehouseAllocationEntity);
        log.info("编辑 开始修改虚拟仓分货单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(virtualWarehouseAllocationEntity);
        if (!save) {
            throw new ServiceException("虚拟仓分货单保存失败");
        }
        // 记录主单操作日志
        log.info("编辑 开始记录虚拟仓分货单日志数据，单号：【{}】", virtualWarehouseAllocationEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), virtualWarehouseAllocationEntity.getCode(), "虚拟仓分货单");
        operateLogService.addModuleOperateLogByObj(old, virtualWarehouseAllocationEntity, ModuleTypeEnum.VIRTUAL_WAREHOUSE_ALLOCATION.getCode(), virtualWarehouseAllocationEntity.getId(), msg);
        // 新增明细
        virtualWarehouseAllocationDetailService.batchUpdate(updateDTO, virtualWarehouseAllocationEntity.getId());
        return Boolean.TRUE;
    }

    /**
     * 列表查询
     *
     * @param dto
     * @return PagingVO
     * @author hyj
     * @date: 2024-06-05
     */
    @Override
    public PagingVO<VirtualWarehouseAllocationDTO.ListDTO> paging(PagingDTO<VirtualWarehouseAllocationDTO.PagingParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<VirtualWarehouseAllocationDTO.ListDTO> pageData = this.baseMapper.paging(query, dto.getParams());
        if (CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        return new PagingVO(pageData);
    }

    @Override
    public List<BatchResultDTO> submit(List<String> ids) {
        //获取原始数据
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        //待提交
        String waitSubmitStatus = VirtualWarehouseAllocationStatusEnum.WAIT_SUBMIT.getCode();
        for (String id : ids) {
            BatchResultDTO submit;
            String flagCode = id;
            try {
                VirtualWarehouseAllocationEntity allocationEntity = this.getById(id);
                if (Objects.isNull(allocationEntity)) {
                    submit = BatchResultDTO.fail(id, id, "分货单不存在");
                } else {
                    //只有待提交状态可以修改
                    if (!Objects.equals(waitSubmitStatus, allocationEntity.getStatus())) {
                        submit = BatchResultDTO.fail(id, allocationEntity.getCode(), ApiError.IS_SUBMIT_IN_SUBMIT.msg);
                    } else {
                        flagCode = allocationEntity.getCode();
                        submit = updateStatus(allocationEntity, VirtualWarehouseAllocationStatusEnum.HANDLE.getCode(), "");
                    }
                }
            } catch (Exception e) {
                log.error("提交分货单失败>>>>{}", e);
                submit = BatchResultDTO.fail(id, flagCode, e.getMessage());
            }
            resultDTOS.add(submit);
        }

        //todo 创建中台任务数据进行同步
        //todo
        //todo
        if (CollectionUtils.isNotEmpty(resultDTOS)) {
            resultDTOS.forEach(resultDTO -> {
                if (resultDTO.getSuccess()) {

                }
            });
        }

        return resultDTOS;
    }

    @Override
    public List<BatchResultDTO> invalid(BaseIdsDTO.RemarkDTO dto) {
        List<String> ids = dto.getIds();
        //获取原始数据
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        //待提交
        String waitSubmitStatus = VirtualWarehouseAllocationStatusEnum.INVALID.getCode();
        for (String id : ids) {
            BatchResultDTO submit;
            String flagCode = id;
            try {
                VirtualWarehouseAllocationEntity allocationEntity = this.getById(id);
                if (Objects.isNull(allocationEntity)) {
                    submit = BatchResultDTO.fail(id, id, "分货单不存在");
                } else {
                    //只有待提交状态可以修改
                    if (!Objects.equals(waitSubmitStatus, allocationEntity.getStatus())) {
                        submit = BatchResultDTO.fail(id, allocationEntity.getCode(), ApiError.ERROR_98009.msg);
                    } else {
                        flagCode = allocationEntity.getCode();
                        submit = updateStatus(allocationEntity, VirtualWarehouseAllocationStatusEnum.INVALID.getCode(), dto.getRemark());
                    }
                }
            } catch (Exception e) {
                log.error("作废分货单失败>>>>{}", e);
                submit = BatchResultDTO.fail(id, flagCode, e.getMessage());
            }
            resultDTOS.add(submit);
        }
        return resultDTOS;
    }

    /**
     * 手动完结
     *
     * @param dto
     * @return
     */
    @Override
    public BatchResultDTO manualFinish(VirtualWarehouseAllocationDTO.ManualFinishDto dto) {
        String id = dto.getId();
        //待提交
        String waitSubmitStatus = VirtualWarehouseAllocationStatusEnum.INVALID.getCode();
        BatchResultDTO submit;
        String flagCode = id;
        try {
            VirtualWarehouseAllocationEntity allocationEntity = this.getById(id);
            if (Objects.isNull(allocationEntity)) {
                submit = BatchResultDTO.fail(id, id, "分货单不存在");
            } else {
                //只有待提交状态可以修改
                if (!Objects.equals(waitSubmitStatus, allocationEntity.getStatus())) {
                    submit = BatchResultDTO.fail(id, allocationEntity.getCode(), ApiError.ERROR_98009.msg);
                } else {
                    flagCode = allocationEntity.getCode();
                    submit = updateStatus(allocationEntity, VirtualWarehouseAllocationStatusEnum.INVALID.getCode(), dto.getFinishDescription());
                }
            }
        } catch (Exception e) {
            log.error("作废分货单失败>>>>{}", e);
            submit = BatchResultDTO.fail(id, flagCode, e.getMessage());
        }
        return submit;
    }

    /**
     * 变更状态
     *
     * @param allocationEntity
     * @param status
     * @param invalidDescription
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO updateStatus(VirtualWarehouseAllocationEntity allocationEntity, String status, String invalidDescription) {
        if (Objects.nonNull(allocationEntity)) {
            //数据库的禁用状态
            String existStatus = allocationEntity.getStatus();
            if (existStatus.equals(status)) {
                throw new ServiceException("存在相同的状态");
            }
            allocationEntity.setStatus(status);
            allocationEntity.setInvalidDescription(invalidDescription);
            this.updateById(allocationEntity);
            return BatchResultDTO.success(allocationEntity.getId(), allocationEntity.getCode(), OperationTypeEnum.DISABLED);
        }
        return BatchResultDTO.fail(allocationEntity.getId(), allocationEntity.getCode(), "分货单不存在");
    }

    /**
     * 新增修改处理数据
     */
    private void handleData(VirtualWarehouseAllocationEntity virtualWarehouseAllocationEntity) {
        // TODO 校验库存数量
        //获取调转方向：调拨方向-1
        if (VirtualWarehouseAllocationTypeEnum.TRANSFER.getCode().equals(virtualWarehouseAllocationEntity.getType())) {
            virtualWarehouseAllocationEntity.setDirection(-1);
        }
    }
}
