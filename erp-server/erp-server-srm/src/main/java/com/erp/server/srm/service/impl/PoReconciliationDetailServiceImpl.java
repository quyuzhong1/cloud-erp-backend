package com.erp.server.srm.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.vo.PagingVO;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.srm.entity.PoReconciliationDetailEntity;
import com.erp.model.wms.dto.SubcontractIssueDTO;
import com.erp.server.srm.mapper.PoReconciliationDetailMapper;
import com.erp.server.srm.service.PoReconciliationDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.srm.service.OperateLogService;
import com.erp.server.srm.service.CommonService;
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
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(PoReconciliationDetailDTO.AddDTO addDTO) {
        PoReconciliationDetailEntity poReconciliationDetailEntity = new PoReconciliationDetailEntity();
        BeanMapperUtils.copy(addDTO, poReconciliationDetailEntity);


        log.info("开始新增采购对账单明细");
        boolean save = super.save(poReconciliationDetailEntity);
        if(!save) {
            throw new ServiceException("采购对账单明细保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "采购对账单明细" , poReconciliationDetailEntity.getId());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.PO_RECONCILIATION.getCode(), poReconciliationDetailEntity.getId(), "新增操作");
        return new BaseResultDTO.AddDTO(poReconciliationDetailEntity.getId(), poReconciliationDetailEntity.getId());
    }

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
        List<String> detailIdList = list.stream().map(PoReconciliationDetailEntity::getId).collect(Collectors.toList());
        List<PoReconciliationDetailEntity> poReconciliationDetailList = this.listByIds(detailIdList);

        for (PoReconciliationDetailEntity entity : list) {
            //添加日志
            PoReconciliationDetailEntity old = poReconciliationDetailList.stream().filter(obj -> StrUtil.equals(obj.getId(), entity.getId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(old)) {
                throw new ServiceException(ApiError.ERROR_PO_RECONCILIATION_DETAIL_NOT_EXIST);
            }
            if (StrUtil.isBlank(old.getMainId())) {
                String content = StrUtil.format("新增了一条SKU【{}】", old.getSkuNo());
                operateLogService.addModuleOperateLog(content, ModuleTypeEnum.PO_RECONCILIATION.getCode(),mainId, "编辑操作");
            } else {
                operateLogService.addModuleOperateLogByObj(old,entity, ModuleTypeEnum.PO_RECONCILIATION.getCode(),mainId,"",String.format("【%s】",old.getSkuNo()));
            }
            entity.setMainId(mainId);
        }


        //新增不需要添加新增SKU的日志
        List<PoReconciliationDetailEntity> addList = list.stream().filter(obj -> StrUtil.isBlank(obj.getMainId())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(addList)) {
            List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(mainId, obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("新增了一条SKU【%s】", ModuleTypeEnum.PO_RECONCILIATION.getCode(), addPairList, "编辑操作");
        }

    }

    @Override
    public PagingVO<PoReconciliationDetailDTO.ListDTO> paging(PagingDTO<PoReconciliationDetailDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<PoReconciliationDetailDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public void exportList(PoReconciliationDetailDTO.PagingParamDTO dto, HttpServletResponse response) {
        List<PoReconciliationDetailDTO.ListDTO> list = this.baseMapper.listExport(dto);
        if(CollUtil.isEmpty(list)) {
            return;
        }
        // 数据处理
        fillList(list);
        // 导出数据
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/poReconciliationDetail.xlsx";
        String name = "对账明细导出";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date).append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }
    }



    /**
     * 分页查询、导出 数据处理
     */
    private void fillList(List<PoReconciliationDetailDTO.ListDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
    }
}
