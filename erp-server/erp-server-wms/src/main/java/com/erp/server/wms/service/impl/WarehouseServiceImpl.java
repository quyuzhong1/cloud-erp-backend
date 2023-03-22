package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.serveice.SuperServiceImpl;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.server.wms.constant.WmsConstant;
import com.erp.server.wms.mapper.WarehouseMapper;
import com.erp.server.wms.service.DictBasicService;
import com.erp.server.wms.service.WarehouseService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 仓库表 服务实现类
 * </p>
 *
 * @author will
 * @since 2023-03-15
 */
@Service
public class WarehouseServiceImpl extends SuperServiceImpl<WarehouseMapper, WarehouseEntity> implements WarehouseService {

    @Resource
    private DictBasicService dictBasicService;


    @Override
    public List<WarehouseDTO.UpdateDTO> listWarehouseByIds(List<String> ids) {
        List<WarehouseEntity> list = this.listByIds(ids);
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        return BeanMapperUtils.copyList(WarehouseDTO.UpdateDTO.class, list);
    }

    @Override
    public List<WarehouseDTO.UpdateDTO> listApproveWarehouse() {
        String approveStatus = ApproveStatusEnum.APPROVE.getStatus();
        List<WarehouseEntity> list = lambdaQuery().
                eq(WarehouseEntity::getApproveStatus, approveStatus).
                eq(WarehouseEntity::getDisabled, false).
                list();
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        return BeanMapperUtils.copyList(WarehouseDTO.UpdateDTO.class, list);
    }


    /**
     * 添加仓库
     *
     * @param dto
     * @return com.erp.model.wms.entity.WarehouseEntity
     * @author yl
     * @date 2023-03-22 10:17
     */
    @Override
    public WarehouseEntity add(WarehouseDTO.AddDTO dto) {
        //检查名称
        checkName(null, dto.getName());
        //检查金蝶编号
        checkKingdeeWarehouseCode("", dto.getKingdeeWarehouseCode());
        WarehouseEntity warehouse = new WarehouseEntity();
        BeanMapper.copy(dto, warehouse);
        warehouse.setDisabled(!dto.getDisabled());
        Boolean result = this.save(warehouse);
        if (result) {
            return warehouse;
        }
        return null;
    }


    /**
     * 修改仓库
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-22 11:08
     */
    @Override
    public Boolean updateWarehouse(WarehouseDTO.UpdateDTO dto) {
        //仓库id
        String warehouseId = dto.getId();
        WarehouseEntity warehouse = this.getById(warehouseId);
        if (Objects.isNull(warehouse)) {
            throw new ServiceException(ApiError.ERROR_99001);
        }
        String code = dto.getKingdeeWarehouseCode();
        String name = dto.getName();
        checkName(warehouseId, name);
        checkKingdeeWarehouseCode(code, name);
        BeanMapper.copy(warehouse, dto);
        Boolean result = this.updateById(warehouse);
        return result;
    }


    /**
     * 提交并审核
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-22 11:16
     */
    @Override
    public Boolean addAndSubmit(WarehouseDTO.AddDTO dto) {
        WarehouseEntity warehouse = this.add(dto);
        if (warehouse != null) {
            return updateSubmitApproveStatus(warehouse, ApproveStatusEnum.APPROVE_ING.getStatus());
        }
        return false;
    }


    /**
     * 仓库提交审核
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-22 11:31
     */
    @Override
    public Boolean submit(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return false;
        }
        List<WarehouseEntity> list = this.listByIds(ids);
        //待审核
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();

        //审核不通过
        String rejectStatus = ApproveStatusEnum.REJECT.getStatus();

        //审核中
        String ingStatus = ApproveStatusEnum.APPROVE_ING.getStatus();

        List<String> statusList = new ArrayList<>(2);
        statusList.add(rejectStatus);
        statusList.add(waitSubmitStatus);
        long count = list.stream().filter(s -> !statusList.contains(s.getApproveStatus().getStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_WAIT_SUBMIT_TO_APPROVE_ING);
        }
        Boolean result = this.updateApproveStatus(list, ApproveStatusEnum.getByStatus(ingStatus));
        return result;
    }


    /**
     * 更改仓库状态
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-22 11:43
     */
    @Override
    public Boolean updateStatus(UpdateStateDTO dto) {
        //仓库id
        String warehouseId = dto.getId();
        WarehouseEntity warehouse = this.getById(warehouseId);
        if (Objects.isNull(warehouse)) {
            throw new ServiceException(ApiError.ERROR_99001);
        }
        warehouse.setDisabled(!dto.getState());
        return this.updateById(warehouse);
    }

    /**
     * 审核仓库
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-22 11:45
     */
    @Override
    public Boolean approve(BaseApproveParamDTO dto) {
        List<String> warehouseIds = dto.getIds();
        List<WarehouseEntity> list = this.listByIds(warehouseIds);
        String ingStatus = ApproveStatusEnum.APPROVE_ING.getStatus();
        long count = list.stream().filter(s -> !ingStatus.equals(s.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        if (dto.getType().equals(WmsConstant.PASS)) {
            //审核通过
            String approveStatus = ApproveStatusEnum.APPROVE.getStatus();
            Boolean result = this.updateApproveStatus(list, ApproveStatusEnum.getByStatus(approveStatus));
            return result;
        } else {
            //审核不通过
            String rejectStatus = ApproveStatusEnum.REJECT.getStatus();
            Boolean result = this.updateApproveStatus(list, ApproveStatusEnum.getByStatus(rejectStatus));
            return result;
        }


    }


    /**
     * 反审核
     * @author yl
     * @date 2023-03-22 11:59
     * @param warehouseIds
     * @return java.lang.Boolean
     */
    @Override
    public Boolean disApprove(List<String> warehouseIds) {
        List<WarehouseEntity> list = this.listByIds(warehouseIds);
        //审核中
        String approveIngStatus = ApproveStatusEnum.APPROVE_ING.getStatus();

        //审核通过
        String approveStatus = ApproveStatusEnum.APPROVE.getStatus();

        //待提交
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();

        List<String> statusList = new ArrayList<>(2);
        statusList.add(approveIngStatus);
        statusList.add(approveStatus);
        long count = list.stream().filter(s -> !statusList.contains(s.getApproveStatus().getStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98014);
        }
        Boolean result = this.updateApproveStatus(list, ApproveStatusEnum.getByStatus(waitSubmitStatus));
        return result;
    }


    /**
     * 更改状态
     *
     * @param list
     * @param statusEnum
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-22 11:41
     */
    private Boolean updateApproveStatus(List<WarehouseEntity> list, ApproveStatusEnum statusEnum) {
        if (CollectionUtils.isNotEmpty(list)) {
            list.forEach(s -> s.setApproveStatus(statusEnum));
            return this.updateBatchById(list);
        }
        return true;
    }


    /**
     * 更改仓库的状态
     *
     * @param warehouse
     * @param status
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-22 11:18
     */
    private Boolean updateSubmitApproveStatus(WarehouseEntity warehouse, String status) {
        if (warehouse != null) {
            warehouse.setApproveStatus(ApproveStatusEnum.getByStatus(status));
            return this.updateById(warehouse);
        }
        return true;
    }


    /**
     * 检查金蝶code 是否重复
     *
     * @param kingdeeWarehouseCode
     * @return void
     * @author yl
     * @date 2023-03-22 10:27
     */
    private void checkKingdeeWarehouseCode(String id, String kingdeeWarehouseCode) {
        LambdaQueryWrapper<WarehouseEntity> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(id)) {
            queryWrapper.ne(WarehouseEntity::getId, id);
        }
        queryWrapper.eq(WarehouseEntity::getKingdeeWarehouseCode, kingdeeWarehouseCode);
        queryWrapper.last("LIMIT 1");
        int count = this.count(queryWrapper);
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_99000);
        }

    }

    /**
     * 检查仓库名是否存在
     *
     * @param id
     * @param name
     * @return void
     * @author yl
     * @date 2023-03-22 10:25
     */
    private void checkName(String id, String name) {
        LambdaQueryWrapper<WarehouseEntity> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(id)) {
            queryWrapper.ne(WarehouseEntity::getId, id);
        }
        queryWrapper.eq(WarehouseEntity::getName, name);
        queryWrapper.last("LIMIT 1");
        int count = this.count(queryWrapper);
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_DUPLICATION_NAME);
        }
    }
}
