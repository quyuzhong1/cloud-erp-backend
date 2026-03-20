package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.wms.dto.QcApplicationDetailDTO;
import com.erp.model.wms.entity.QcApplicationDetailEntity;
import com.erp.server.wms.mapper.QcApplicationDetailMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.QcApplicationDetailService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 * 质检申请单明细表 服务实现类
 * </p>
 *
 * @author will
 * @since 2026-03-20
 */
@Slf4j
@Service
public class QcApplicationDetailServiceImpl extends SuperServiceImpl<QcApplicationDetailMapper, QcApplicationDetailEntity> implements QcApplicationDetailService {
    @Resource
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(QcApplicationDetailDTO.AddDTO addDTO) {
        QcApplicationDetailEntity qcApplicationDetailEntity = new QcApplicationDetailEntity();
        BeanMapperUtils.copy(addDTO, qcApplicationDetailEntity);

        // 数据处理
        handleData(qcApplicationDetailEntity);

        log.info("开始新增质检申请单明细单");
        boolean save = super.save(qcApplicationDetailEntity);
        if(!save) {
            throw new ServiceException("质检申请单明细单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "质检申请单明细单" , qcApplicationDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, qcApplicationDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(qcApplicationDetailEntity.getId(), qcApplicationDetailEntity.getId());
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(QcApplicationDetailDTO.UpdateDTO addOrUpdateDTO) {
        QcApplicationDetailEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "质检申请单明细单"));
        QcApplicationDetailEntity qcApplicationDetailEntity =  BeanMapperUtils.map(QcApplicationDetailEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(qcApplicationDetailEntity);
        log.info("编辑 开始修改质检申请单明细单数据，id：【{}】", old.getId());
        boolean save = super.updateById(qcApplicationDetailEntity);
        if(!save) {
            throw new ServiceException("质检申请单明细单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录质检申请单明细单日志数据，id：【{}】", qcApplicationDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), qcApplicationDetailEntity.getId(), "质检申请单明细单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, qcApplicationDetailEntity, null, qcApplicationDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<QcApplicationDetailDTO.ListDTO> paging(PagingDTO<QcApplicationDetailDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<QcApplicationDetailDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<QcApplicationDetailDTO.TabListDTO> tabList(PermissionsDTO param) {
        QcApplicationDetailDTO.PagingParamDTO searchParam = new QcApplicationDetailDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<QcApplicationDetailDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        // 获取状态列表
        // TODO 替换当前表Tab状态字段
        List<String> statusList = null;
        // 不存在的状态赋值为0
        List<String> existStatusList = list.stream().map(QcApplicationDetailDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        statusList.parallelStream().forEach(status -> {
            if(!existStatusList.contains(status)) {
            list.add(new QcApplicationDetailDTO.TabListDTO(status, 0));
        }
        });
        list.add(new QcApplicationDetailDTO.TabListDTO("all", list.stream().mapToInt(QcApplicationDetailDTO.TabListDTO::getCount).sum()));
        // 计算合计数量
        return list;
    }

    @Override
    public void exportList(QcApplicationDetailDTO.ExportDTO param, HttpServletResponse response) {
        List<QcApplicationDetailDTO.ListDTO> list = this.baseMapper.listExport(param);
        if(CollUtil.isEmpty(list)) {
           return;
        }
        // 数据处理
        fillList(list);

        // 导出数据
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/qcApplicationDetail.xlsx";
        String name = "质检申请单明细单导出";
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
    private void handleData(QcApplicationDetailEntity qcApplicationDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }

    @Override
    public QcApplicationDetailDTO.ViewDTO view(String id) {
    QcApplicationDetailEntity qcApplicationDetailEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到质检申请单明细单数据"));
    QcApplicationDetailDTO.ViewDTO data = BeanMapperUtils.map(QcApplicationDetailDTO.ViewDTO.class, qcApplicationDetailEntity);
    // 数据填充处理
    fillOne(data);
    // TODO 查询明细数据（如果有的话）
    return data;
    }

    private void fillOne(QcApplicationDetailDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
          return;
        }
    }

   /**
    * 分页查询、导出 数据处理
   */
   private void fillList(List<QcApplicationDetailDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
            return;
        }
        // 属性赋值
        for(QcApplicationDetailDTO.ListDTO data : list) {
        // TODO 其他如需要显示名称的字段赋值
        }
   }
}
