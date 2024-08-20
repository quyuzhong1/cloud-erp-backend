package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ConfirmStatusEnum;
import com.common.business.vo.PagingVO;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.tms.dto.InitFirstMileAllocationDTO;
import com.erp.model.tms.entity.FirstMileCostAllocationEntity;
import com.erp.model.tms.entity.InitFirstMileAllocationEntity;
import com.erp.server.tms.mapper.FirstMileCostAllocationMapper;
import com.erp.server.tms.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.FirstMileCostAllocationDTO;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 头程费用分摊 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2024-08-20
 */
@Slf4j
@Service
public class FirstMileCostAllocationServiceImpl extends SuperServiceImpl<FirstMileCostAllocationMapper, FirstMileCostAllocationEntity> implements FirstMileCostAllocationService {
    @Autowired
    private OperateLogService operateLogService;
    @Resource
    private FirstMileSkuCostAllocationDetailService firstMileSkuCostAllocationDetailService;
    @Resource
    private FirstMileSkuCostAllocationService firstMileSkuCostAllocationService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(FirstMileCostAllocationDTO.AddDTO addDTO) {
        FirstMileCostAllocationEntity firstMileCostAllocationEntity = new FirstMileCostAllocationEntity();
        BeanMapperUtils.copy(addDTO, firstMileCostAllocationEntity);

        // 数据处理
        handleData(firstMileCostAllocationEntity);

        log.info("开始新增头程费用分摊");
        boolean save = super.save(firstMileCostAllocationEntity);
        if(!save) {
            throw new ServiceException("头程费用分摊保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "头程费用分摊" , firstMileCostAllocationEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, firstMileCostAllocationEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(firstMileCostAllocationEntity.getId(), firstMileCostAllocationEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(FirstMileCostAllocationDTO.UpdateDTO updateDTO) {
        FirstMileCostAllocationEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "头程费用分摊"));
        FirstMileCostAllocationEntity firstMileCostAllocationEntity =  BeanMapperUtils.map(FirstMileCostAllocationEntity.class, updateDTO);

        // 数据处理
        handleData(firstMileCostAllocationEntity);
        log.info("编辑 开始修改头程费用分摊数据，id：【{}】", old.getId());
        boolean save = super.updateById(firstMileCostAllocationEntity);
        if(!save) {
            throw new ServiceException("头程费用分摊保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录头程费用分摊日志数据，id：【{}】", firstMileCostAllocationEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), firstMileCostAllocationEntity.getId(), "头程费用分摊");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, firstMileCostAllocationEntity, null, firstMileCostAllocationEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<FirstMileCostAllocationDTO.TabListDTO> tabList(PermissionsDTO dto) {
        List<FirstMileCostAllocationDTO.TabListDTO> list = baseMapper.tabList(dto.getPermissionSql());
        List<FirstMileCostAllocationDTO.TabListDTO> tabListDTOList = new ArrayList<>(2);
        tabListDTOList.add(FirstMileCostAllocationDTO.TabListDTO.builder().tabFlag(ConfirmStatusEnum.WAIT_CONFIRM.getCode()).tabFlagName(ConfirmStatusEnum.WAIT_CONFIRM.getName()).count(getTabCount(ConfirmStatusEnum.WAIT_CONFIRM.getCode(), list)).build());
        tabListDTOList.add(FirstMileCostAllocationDTO.TabListDTO.builder().tabFlag(ConfirmStatusEnum.CONFIRM.getCode()).tabFlagName(ConfirmStatusEnum.CONFIRM.getName()).count(getTabCount(ConfirmStatusEnum.CONFIRM.getCode(), list)).build());
        return tabListDTOList;
    }

    @Override
    public PagingVO<FirstMileCostAllocationDTO.PagingVO> paging(PagingDTO<FirstMileCostAllocationDTO.PagingParamDTO> dto) {
        FirstMileCostAllocationDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page<FirstMileCostAllocationDTO.PagingVO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<FirstMileCostAllocationDTO.PagingVO> pageData = baseMapper.paging(query, params);
        List<FirstMileCostAllocationDTO.PagingVO> list = pageData.getRecords();
        fillPagingDb(list);
        return new PagingVO<>(pageData);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO delete(FirstMileCostAllocationEntity entity) {
        //TODO 已确认不能删除
        if (ConfirmStatusEnum.CONFIRM.getCode().equals(entity.getStatus())){
            return BatchResultDTO.fail(entity.getId(),entity.getSourceCode(),"发货单分摊数据已确认不能删除");
        }
        firstMileSkuCostAllocationService.removeByMainId(entity.getId());
        firstMileSkuCostAllocationDetailService.removeByMainId(entity.getId());
        this.lambdaUpdate().eq(FirstMileCostAllocationEntity::getId, entity.getId()).remove();
        return BatchResultDTO.success(entity.getId(), entity.getSourceCode(), "删除记录操作成功");
    }

    @Override
    public void exportExcel(FirstMileCostAllocationDTO.PagingParamDTO params, HttpServletResponse response) {
        params.setPermissionSql(params.getPermissionSql());
        List<FirstMileCostAllocationDTO.PagingVO> list = baseMapper.exportList(params);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.EXPORT_DATA_EMPTY);
        }
        // 填充字段值
        fillPagingDb(list);
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/initFirstMileAllocationExport.xlsx";
        String name = "期初头程分摊导出";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (IOException e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }
    }
    private void fillPagingDb(List<FirstMileCostAllocationDTO.PagingVO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        list.forEach(e -> {
            e.setStatusName(ApproveStatusEnum.getName(e.getStatus()));
        });
    }
    /**
     * 根据状态获取分页统计数量
     *
     * @param status
     * @param list
     * @return
     */
    private Integer getTabCount(String status, List<FirstMileCostAllocationDTO.TabListDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return MathUtil.ZERO;
        }
        FirstMileCostAllocationDTO.TabListDTO tabListDTO = list.stream().filter(e -> Objects.nonNull(e) && status.equals(e.getTabFlag())).findFirst().orElse(null);
        if (Objects.nonNull(tabListDTO)) {
            return tabListDTO.getCount();
        } else {
            return MathUtil.ZERO;
        }
    }
    /**
    * 新增修改处理数据
    */
    private void handleData(FirstMileCostAllocationEntity firstMileCostAllocationEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
