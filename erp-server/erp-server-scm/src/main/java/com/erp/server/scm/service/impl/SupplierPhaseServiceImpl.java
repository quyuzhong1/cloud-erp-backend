package com.erp.server.scm.service.impl;

import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.serveice.SuperServiceImpl;
import com.erp.model.scm.dto.SupplierPhaseDTO;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.scm.entity.SupplierPhaseEntity;
import com.erp.server.scm.mapper.SupplierPhaseMapper;
import com.erp.server.scm.service.SupplierPhaseService;
import com.erp.server.scm.service.SupplierService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * <p>
 * 供应商升降级 服务实现类
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
@Service
public class SupplierPhaseServiceImpl extends SuperServiceImpl<SupplierPhaseMapper, SupplierPhaseEntity> implements SupplierPhaseService {


    @Resource
    private SupplierService supplierService;

    /**
     * 添加供应商阶段
     *
     * @param dto
     * @return com.erp.model.scm.entity.SupplierPhaseEntity
     * @author yl
     * @date 2023-03-23 12:20
     */
    @Override
    public SupplierPhaseEntity add(SupplierPhaseDTO.AddDTO dto) {
        SupplierPhaseEntity entity = new SupplierPhaseEntity();
        String supplierId = dto.getSupplierId();
        SupplierEntity supplier = supplierService.getById(supplierId);
        if (Objects.isNull(supplier)) {
            throw new ServiceException(ApiError.ERROR_SUPPLIER_ABSENCE);
        }
        //供应商的现阶段
        String phase = supplier.getPhase().getPhase();
        if (!phase.equals(dto.getCurrentPhase())) {
            throw new ServiceException(ApiError.ERROR_98016);
        }
        //检查阶段能否变更
        checkPhase(phase,dto.getTargetPhase(),dto.getType());
        return null;
    }

    
    /**
     * 检查阶段能否变更
     * @author yl
     * @date 2023-03-23 14:04
     * @param currentPhase 当前阶段
     * @param targetPhase  目标阶段
     * @return void
     */
    private void checkPhase(String currentPhase, String targetPhase,String type) {


    }
}
