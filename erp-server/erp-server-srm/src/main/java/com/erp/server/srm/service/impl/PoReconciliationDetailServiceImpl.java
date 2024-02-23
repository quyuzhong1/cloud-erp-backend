package com.erp.server.srm.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.TabFlagEnum;
import com.common.business.vo.PagingVO;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.srm.dto.PoReconciliationDTO;
import com.erp.model.srm.entity.PoReconciliationDetailEntity;
import com.erp.model.srm.entity.PoReconciliationEntity;
import com.erp.model.srm.enums.ConfirmStatusEnum;
import com.erp.model.srm.enums.PoReconciliationEnum;
import com.erp.model.wms.dto.SubcontractIssueDTO;
import com.erp.server.srm.mapper.PoReconciliationDetailMapper;
import com.erp.server.srm.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.srm.dto.PoReconciliationDetailDTO;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import org.stringtemplate.v4.ST;

import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 采购对账单明细 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-01-19
 */
@Slf4j
@Service
public class PoReconciliationDetailServiceImpl extends SuperServiceImpl<PoReconciliationDetailMapper, PoReconciliationDetailEntity> implements PoReconciliationDetailService {
    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private PoReconciliationScmService poReconciliationScmService;

    @Autowired
    private PoReconciliationDetailScmService poReconciliationDetailScmService;

    @Autowired
    private CommonService commonService;

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(List<PoReconciliationDetailDTO.UpdateDTO> detailList,String mainId) {
        if (CollectionUtils.isEmpty(detailList)) {
            return Boolean.TRUE;
        }
        List<PoReconciliationDetailEntity> list =  BeanMapperUtils.copyList(PoReconciliationDetailEntity.class, detailList);

        handleUpdateData (list,mainId);

        log.info("编辑 开始修改采购对账单数据，id：【{}】", mainId);
        boolean save = super.saveOrUpdateBatch(list);
        if(!save) {
            throw new ServiceException("采购对账单明细保存失败");
        }
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<PoReconciliationDetailDTO.ListDTO> paging(PagingDTO<PoReconciliationDetailDTO.PagingParamDTO> pagingParamDTO) {
        //默认查询当前登录人的绑定的供应商数据
        SupplierEntity supplierEntity = commonService.getSupplierEntity();
        pagingParamDTO.getParams().setSupplierId(supplierEntity.getId());
        return poReconciliationDetailScmService.paging(pagingParamDTO);
    }

    @Override
    public void exportList(PoReconciliationDetailDTO.PagingParamDTO dto, HttpServletResponse response) {
        //默认查询当前登录人的绑定的供应商数据
        SupplierEntity supplierEntity = commonService.getSupplierEntity();
        dto.setSupplierId(supplierEntity.getId());
        poReconciliationDetailScmService.exportList(dto,response);
    }


    /**
     * @description: 修改处理
     * @author Will
     * @date: 2024/1/20 16:55
     * @param list
     * @param mainId
     */
    private void handleUpdateData (List<PoReconciliationDetailEntity> list,String mainId) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        //已存在对账明细
        List<String> detailIdList = list.stream().map(PoReconciliationDetailEntity::getId).collect(Collectors.toList());
        List<PoReconciliationDetailEntity> poReconciliationDetailList = this.listByIds(detailIdList);

        //对账单数据
        for (PoReconciliationDetailEntity entity : list) {
            //添加日志
            PoReconciliationDetailEntity old = poReconciliationDetailList.stream().filter(obj -> StrUtil.equals(obj.getId(), entity.getId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(old)) {
                throw new ServiceException(ApiError.ERROR_PO_RECONCILIATION_DETAIL_NOT_EXIST);
            }
            operateLogService.addModuleOperateLogByObj(old,entity, ModuleTypeEnum.PO_RECONCILIATION.getCode(),mainId,"",String.format("【%s】",old.getSkuNo()));
        }
    }
}
