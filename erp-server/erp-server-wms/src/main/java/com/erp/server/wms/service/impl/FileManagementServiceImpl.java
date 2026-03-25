package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.annotation.DistributeLocker;
import com.common.business.config.DocNoGenHelper;
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
import com.erp.model.wms.dto.FileManagementDTO;
import com.erp.model.wms.entity.FileManagementEntity;
import com.erp.server.wms.mapper.FileManagementMapper;
import com.erp.server.wms.service.FileManagementService;
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
import java.util.stream.Collectors;

/**
 * <p>
 * 文件管理 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2026-03-20
 */
@Slf4j
@Service
public class FileManagementServiceImpl extends SuperServiceImpl<FileManagementMapper, FileManagementEntity> implements FileManagementService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private DocNoGenHelper docNoGenHelper;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(FileManagementDTO.AddDTO addDTO) {
        FileManagementEntity fileManagementEntity = new FileManagementEntity();
        BeanMapperUtils.copy(addDTO, fileManagementEntity);

        // 数据处理
        handleData(fileManagementEntity);

        log.info("开始新增文件管理");
        // 生成单号
        // TODO 此处的null需填写生成单号类型，type查看BusinessNoTypeEnum枚举类 注意需要填写prefix 为单号前缀
        String code = docNoGenHelper.generateCode(null);
        fileManagementEntity.setCode(code);
        boolean save = super.save(fileManagementEntity);
        if (!save) {
            throw new ServiceException("文件管理保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "文件管理", fileManagementEntity.getCode());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, fileManagementEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(fileManagementEntity.getId(), code);
    }

    /**
     * 修改
     */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(FileManagementDTO.UpdateDTO addOrUpdateDTO) {
        FileManagementEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "文件管理"));
        FileManagementEntity fileManagementEntity = BeanMapperUtils.map(FileManagementEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(fileManagementEntity);
        log.info("编辑 开始修改文件管理数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(fileManagementEntity);
        if (!save) {
            throw new ServiceException("文件管理保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
        log.info("编辑 开始记录文件管理日志数据，单号：【{}】", fileManagementEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), fileManagementEntity.getCode(), "文件管理");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, fileManagementEntity, null, fileManagementEntity.getId(), msg);
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<FileManagementDTO.ListDTO> paging(PagingDTO<FileManagementDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<FileManagementDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if (CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<FileManagementDTO.TabListDTO> tabList(PermissionsDTO param) {
        FileManagementDTO.PagingParamDTO searchParam = new FileManagementDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<FileManagementDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        // 获取状态列表
        // TODO 替换当前表Tab状态字段
        List<String> statusList = null;
        // 不存在的状态赋值为0
        List<String> existStatusList = list.stream().map(FileManagementDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        statusList.parallelStream().forEach(status -> {
            if (!existStatusList.contains(status)) {
                list.add(new FileManagementDTO.TabListDTO(status, 0));
            }
        });
        list.add(new FileManagementDTO.TabListDTO("all", list.stream().mapToInt(FileManagementDTO.TabListDTO::getCount).sum()));
        // 计算合计数量
        return list;
    }

    @Override
    public void exportList(FileManagementDTO.ExportDTO param, HttpServletResponse response) {
        List<FileManagementDTO.ListDTO> list = this.baseMapper.listExport(param);
        if (CollUtil.isEmpty(list)) {
            return;
        }
        // 数据处理
        fillList(list);

        // 导出数据
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/fileManagement.xlsx";
        String name = "文件管理导出";
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
    private void handleData(FileManagementEntity fileManagementEntity) {
        // TODO 验证数据 & 数据赋值
    }

    @Override
    public FileManagementDTO.ViewDTO view(String id) {
        FileManagementEntity fileManagementEntity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到文件管理数据"));
        FileManagementDTO.ViewDTO data = BeanMapperUtils.map(FileManagementDTO.ViewDTO.class, fileManagementEntity);
        // 数据填充处理
        fillOne(data);
        // TODO 查询明细数据（如果有的话）
        return data;
    }

    private void fillOne(FileManagementDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
            return;
        }
    }

    /**
     * 分页查询、导出 数据处理
     */
    private void fillList(List<FileManagementDTO.ListDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        // 属性赋值
        for (FileManagementDTO.ListDTO data : list) {
            // TODO 其他如需要显示名称的字段赋值
        }
    }
}
