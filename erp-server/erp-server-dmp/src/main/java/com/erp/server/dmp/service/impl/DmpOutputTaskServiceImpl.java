package com.erp.server.dmp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.vo.LoginUser;

import com.erp.model.dmp.dto.DmpOutputTaskDTO;
import com.erp.model.dmp.enums.DmpTaskStatuEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.dmp.service.OperateLogService;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.dmp.entity.DmpOutputTaskEntity;
import com.erp.server.dmp.mapper.DmpOutputTaskMapper;
import com.erp.server.dmp.service.DmpOutputTaskService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.dto.DmpOutputTaskDTO;
import com.erp.model.dmp.entity.DmpOutputTaskEntity;
import com.erp.model.dmp.enums.DmpOutputTaskStatusEnum;
import com.erp.server.dmp.mapper.DmpOutputTaskMapper;
import com.erp.server.dmp.service.DmpOutputTaskService;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;

import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import com.erp.model.sys.dto.SysCodeDTO;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.date.DateUtil;

import javax.servlet.http.HttpServletResponse;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 推送任务 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-07-01
 */
@Slf4j
@Service
public class DmpOutputTaskServiceImpl extends SuperServiceImpl<DmpOutputTaskMapper, DmpOutputTaskEntity> implements DmpOutputTaskService {

    @Resource
    private OperateLogService operateLogService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpOutputTaskDTO.AddDTO addDTO) {
        DmpOutputTaskEntity dmpOutputTaskEntity = new DmpOutputTaskEntity();
        BeanMapperUtils.copy(addDTO, dmpOutputTaskEntity);

        // 数据处理
        handleData(dmpOutputTaskEntity);

        log.info("开始新增推送任务");
        boolean save = super.save(dmpOutputTaskEntity);
        if(!save) {
            throw new ServiceException("推送任务保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "推送任务" , dmpOutputTaskEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dmpOutputTaskEntity.getId(), dmpOutputTaskEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpOutputTaskDTO.UpdateDTO updateDTO) {
        DmpOutputTaskEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "推送任务"));
        DmpOutputTaskEntity dmpOutputTaskEntity =  BeanMapperUtils.map(DmpOutputTaskEntity.class, updateDTO);

        // 数据处理
        handleData(dmpOutputTaskEntity);
        log.info("编辑 开始修改推送任务数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpOutputTaskEntity);
        if(!save) {
            throw new ServiceException("推送任务保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录推送任务日志数据，id：【{}】", dmpOutputTaskEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpOutputTaskEntity.getId(), "推送任务");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DmpOutputTaskEntity dmpOutputTaskEntity) {
    // TODO 验证数据 & 数据赋值
    }

    @Transactional(rollbackFor = Exception.class , propagation = Propagation.REQUIRES_NEW)
    public boolean updateErrorStatus(String id , boolean errorFlag , Integer errorCount , Exception e) {
    	String errorBeforeStatus = "";
    	if(errorFlag) {
    		errorBeforeStatus = getById(id).getStatus() + "@@";
    	}
    	return lambdaUpdate().eq(DmpOutputTaskEntity::getId, id)
				.set(DmpOutputTaskEntity::getErrorCount, errorCount)
				.set(errorFlag , DmpOutputTaskEntity::getStatus, DmpOutputTaskStatusEnum.ERROR.getCode())
				.set(DmpOutputTaskEntity::getUpdateTime, LocalDateTime.now())
				.set(DmpOutputTaskEntity::getErrorMessage,  errorBeforeStatus + "traceId=【" + MDC.get("traceId") + "】" + ExceptionUtil.stacktraceToString(e))
				.update();
	}

    @Override
    public PagingVO<DmpOutputTaskDTO.ListDTO> paging(PagingDTO<DmpOutputTaskDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<DmpOutputTaskDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<DmpOutputTaskDTO.TabListDTO> tabList(PermissionsDTO param) {
        DmpOutputTaskDTO.PagingParamDTO searchParam = new DmpOutputTaskDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<DmpOutputTaskDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        int total = list.stream().mapToInt(DmpOutputTaskDTO.TabListDTO::getCount).sum();
        List<DmpOutputTaskDTO.TabListDTO> resultList = new ArrayList<>();
        // 计算合计数量
//        resultList.add(new DmpOutputTaskDTO.TabListDTO("all", "全部", total));

        List<DmpTaskStatuEnum> statusList = Arrays.stream(DmpTaskStatuEnum.values()).collect(Collectors.toList());
        // 不存在的状态赋值为0
        Map<String, DmpOutputTaskDTO.TabListDTO> listMap = list.stream().collect(Collectors.toMap(DmpOutputTaskDTO.TabListDTO::getTabFlag, e -> e));
        statusList.forEach(status -> {
            DmpOutputTaskDTO.TabListDTO tabListDTO = listMap.get(status.getCode());
            if (tabListDTO != null) {
                resultList.add(tabListDTO);
            } else {
                resultList.add(new DmpOutputTaskDTO.TabListDTO(status.getCode(), status.getName(), 0));
            }
        });
        return resultList;
    }

    @Override
    public void exportList(DmpOutputTaskDTO.ExportDTO dto, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("推送任务Excel导出", FileTaskEventEnum.EXPORT_DMP_OUTPUT_TASK.getCode(), dto);
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        DmpOutputTaskEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到推送任务数据"));
        // 只有待提交数据允许删除
        // TODO 删除明细数据（如果有明细数据的话）

        // 删除主单数据
        log.info("删除 开始删除推送任务主单数据，id：【{}】", id);
        super.removeById(id);
        // 删除日志数据
        log.info("删除 开始删除推送任务日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getId(), "推送任务");
        operateLogService.addModuleOperateLog(msg, null, entity.getId(), "删除推送任务数据");
        return BatchResultDTO.success(entity.getId(), entity.getId(), OperationTypeEnum.DELETE);
    }


    @Override
    public DmpOutputTaskDTO.ViewDTO view(String id) {
        DmpOutputTaskEntity dmpOutputTaskEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到推送任务数据"));
        DmpOutputTaskDTO.ViewDTO data = BeanMapperUtils.map(DmpOutputTaskDTO.ViewDTO.class, dmpOutputTaskEntity);
        // 数据填充处理
        fillOne(data);
        // TODO 查询明细数据（如果有的话）
        return data;
    }

    private void fillOne(DmpOutputTaskDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
            return;
        }
    }

    /**
     * 分页查询、导出 数据处理
     */
    private void fillList(List<DmpOutputTaskDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
            return;
        }

        // 属性赋值
        for(DmpOutputTaskDTO.ListDTO data : list) {
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            // TODO 其他如需要显示名称的字段赋值
        }
    }
}
