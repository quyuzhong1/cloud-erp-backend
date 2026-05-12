package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
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
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.date.DateUtil;
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
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
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
public class AfterSalePackingDetailServiceImpl extends SuperServiceImpl<AfterSalePackDetailMapper, AfterSalePackDetailEntity> implements AfterSalePackDetailService {
    @Resource
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(AfterSalePackDetailDTO.AddDTO addOrUpdateDTO) {
        AfterSalePackDetailEntity afterSalePackingDetailEntity = new AfterSalePackDetailEntity();
        BeanMapperUtils.copy(addOrUpdateDTO, afterSalePackingDetailEntity);

        // 数据处理
        handleData(afterSalePackingDetailEntity);

        log.info("开始新增售后装箱明细单");
        boolean save = super.save(afterSalePackingDetailEntity);
        if (!save) {
            throw new ServiceException("售后装箱明细单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "售后装箱明细单", afterSalePackingDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, afterSalePackingDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(afterSalePackingDetailEntity.getId(), afterSalePackingDetailEntity.getId());
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
        AfterSalePackDetailEntity afterSalePackingDetailEntity = BeanMapperUtils.map(AfterSalePackDetailEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(afterSalePackingDetailEntity);
        log.info("编辑 开始修改售后装箱明细单数据，id：【{}】", old.getId());
        boolean save = super.updateById(afterSalePackingDetailEntity);
        if (!save) {
            throw new ServiceException("售后装箱明细单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
        log.info("编辑 开始记录售后装箱明细单日志数据，id：【{}】", afterSalePackingDetailEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), afterSalePackingDetailEntity.getId(), "售后装箱明细单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, afterSalePackingDetailEntity, null, afterSalePackingDetailEntity.getId(), msg);
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
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public void exportList(AfterSalePackDetailDTO.ExportDTO param, HttpServletResponse response) {
        List<AfterSalePackDetailDTO.ListDTO> list = this.baseMapper.listExport(param);
        if (CollUtil.isEmpty(list)) {
            return;
        }
        // 数据处理
        fillList(list);

        // 导出数据
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/afterSalePackingDetail.xlsx";
        String name = "售后装箱明细单导出";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date).append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (Exception e) {
            throw new ServiceException(ApiError.FILE_EXPORT_FAILED);
        }
    }

    /**
     * 新增修改处理数据
     */
    private void handleData(AfterSalePackDetailEntity afterSalePackingDetailEntity) {
        // TODO 验证数据 & 数据赋值
    }

    @Override
    public AfterSalePackDetailDTO.ViewDTO view(String id) {
        AfterSalePackDetailEntity afterSalePackingDetailEntity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到售后装箱明细单数据"));
        AfterSalePackDetailDTO.ViewDTO data = BeanMapperUtils.map(AfterSalePackDetailDTO.ViewDTO.class, afterSalePackingDetailEntity);
        // 数据填充处理
        fillOne(data);
        // TODO 查询明细数据（如果有的话）
        return data;
    }

    private void fillOne(AfterSalePackDetailDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
            return;
        }
    }

    /**
     * 分页查询、导出 数据处理
     */
    private void fillList(List<AfterSalePackDetailDTO.ListDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        // 属性赋值
        for (AfterSalePackDetailDTO.ListDTO data : list) {
            // TODO 其他如需要显示名称的字段赋值
        }
    }
}
