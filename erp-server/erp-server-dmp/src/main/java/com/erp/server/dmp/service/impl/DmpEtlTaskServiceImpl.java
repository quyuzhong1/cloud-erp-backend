package com.erp.server.dmp.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.dmp.dto.DmpEtlTaskDTO;
import com.erp.model.dmp.dto.DmpEtlTaskDTO;
import com.erp.model.dmp.entity.DmpEtlTaskEntity;
import com.erp.model.dmp.enums.DmpTaskStatuEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.dmp.mapper.DmpEtlTaskMapper;
import com.erp.server.dmp.service.DmpEtlTaskService;
import com.erp.server.dmp.service.OperateLogService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
/**
 * <p>
 * etl任务 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2025-07-21
 */
@Slf4j
@Service
public class DmpEtlTaskServiceImpl extends SuperServiceImpl<DmpEtlTaskMapper, DmpEtlTaskEntity> implements DmpEtlTaskService {
    @Autowired
    private OperateLogService operateLogService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpEtlTaskDTO.AddDTO addDTO) {
        DmpEtlTaskEntity dmpEtlTaskEntity = new DmpEtlTaskEntity();
        BeanMapperUtils.copy(addDTO, dmpEtlTaskEntity);

        // 数据处理
        handleData(dmpEtlTaskEntity);

        log.info("开始新增etl任务");
        boolean save = super.save(dmpEtlTaskEntity);
        if(!save) {
            throw new ServiceException("etl任务保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "etl任务" , dmpEtlTaskEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, dmpEtlTaskEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dmpEtlTaskEntity.getId(), dmpEtlTaskEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpEtlTaskDTO.UpdateDTO addOrUpdateDTO) {
        DmpEtlTaskEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "etl任务"));
        DmpEtlTaskEntity dmpEtlTaskEntity =  BeanMapperUtils.map(DmpEtlTaskEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(dmpEtlTaskEntity);
        log.info("编辑 开始修改etl任务数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpEtlTaskEntity);
        if(!save) {
            throw new ServiceException("etl任务保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录etl任务日志数据，id：【{}】", dmpEtlTaskEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpEtlTaskEntity.getId(), "etl任务");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, dmpEtlTaskEntity, null, dmpEtlTaskEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DmpEtlTaskEntity dmpEtlTaskEntity) {
    // TODO 验证数据 & 数据赋值
    }

    @Override
    public PagingVO<DmpEtlTaskDTO.ListDTO> paging(PagingDTO<DmpEtlTaskDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<DmpEtlTaskDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }


    @Override
    public List<DmpEtlTaskDTO.TabListDTO> tabList(PermissionsDTO param) {
        DmpEtlTaskDTO.PagingParamDTO searchParam = new DmpEtlTaskDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<DmpEtlTaskDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        int total = list.stream().mapToInt(DmpEtlTaskDTO.TabListDTO::getCount).sum();
        List<DmpEtlTaskDTO.TabListDTO> resultList = new ArrayList<>();
        // 计算合计数量
        resultList.add(new DmpEtlTaskDTO.TabListDTO("all", "全部", total));

        List<DmpTaskStatuEnum> statusList = Arrays.stream(DmpTaskStatuEnum.values()).collect(Collectors.toList());
        // 不存在的状态赋值为0
        Map<String, DmpEtlTaskDTO.TabListDTO> listMap = list.stream().collect(Collectors.toMap(DmpEtlTaskDTO.TabListDTO::getTabFlag, e -> e));
        statusList.forEach(status -> {
            DmpEtlTaskDTO.TabListDTO tabListDTO = listMap.get(status.getCode());
            if (tabListDTO != null) {
                resultList.add(tabListDTO);
            } else {
                resultList.add(new DmpEtlTaskDTO.TabListDTO(status.getCode(), status.getName(), 0));
            }
        });
        return resultList;
    }

    @Override
    public void exportList(DmpEtlTaskDTO.ExportDTO dto, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("清洗任务Excel导出", FileTaskEventEnum.EXPORT_DMP_ETL_TASK.getCode(), dto);
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        DmpEtlTaskEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到etl任务数据"));
        // 只有待提交数据允许删除
        // TODO 删除明细数据（如果有明细数据的话）

        // 删除主单数据
        log.info("删除 开始删除etl任务主单数据，id：【{}】", id);
        super.removeById(id);
        // 删除日志数据
        log.info("删除 开始删除etl任务日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getId(), "etl任务");
        operateLogService.addModuleOperateLog(msg, null, entity.getId(), "删除etl任务数据");
        return BatchResultDTO.success(entity.getId(), entity.getId(), OperationTypeEnum.DELETE);
    }


    @Override
    public DmpEtlTaskDTO.ViewDTO view(String id) {
        DmpEtlTaskEntity dmpEtlTaskEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到etl任务数据"));
        DmpEtlTaskDTO.ViewDTO data = BeanMapperUtils.map(DmpEtlTaskDTO.ViewDTO.class, dmpEtlTaskEntity);
        // 数据填充处理
        fillOne(data);
        // TODO 查询明细数据（如果有的话）
        return data;
    }


    private void fillOne(DmpEtlTaskDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
            return;
        }
    }

    /**
     * 分页查询、导出 数据处理
     */
    private void fillList(List<DmpEtlTaskDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
            return;
        }

        // 属性赋值
        for(DmpEtlTaskDTO.ListDTO data : list) {
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            // TODO 其他如需要显示名称的字段赋值
        }
    }
}
