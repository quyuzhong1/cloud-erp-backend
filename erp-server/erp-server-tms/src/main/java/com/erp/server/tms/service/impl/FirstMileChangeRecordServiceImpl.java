package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.FirstMileChangeRecordDTO;
import com.erp.model.tms.entity.FirstMileChangeRecordEntity;
import com.erp.model.tms.enums.*;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.tms.mapper.FirstMileChangeRecordMapper;
import com.erp.server.tms.service.FirstMileChangeRecordService;
import com.erp.server.tms.service.OperateLogService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_TMS_FIRST_MILE_COST_ALLOCATION;

/**
 * <p>
 * 头程调整记录 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2025-05-12
 */
@Slf4j
@Service
public class FirstMileChangeRecordServiceImpl extends SuperServiceImpl<FirstMileChangeRecordMapper, FirstMileChangeRecordEntity> implements FirstMileChangeRecordService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(FirstMileChangeRecordDTO.AddDTO addDTO) {
        FirstMileChangeRecordEntity firstMileChangeRecordEntity = new FirstMileChangeRecordEntity();
        BeanMapperUtils.copy(addDTO, firstMileChangeRecordEntity);
        // 数据处理
        handleData(firstMileChangeRecordEntity);
        log.info("开始新增头程调整记录");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_TCTZ);
        firstMileChangeRecordEntity.setCode(code);
        boolean save = super.save(firstMileChangeRecordEntity);
        if(!save) {
            throw new ServiceException("头程调整记录保存失败");
        }
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "头程调整记录" , firstMileChangeRecordEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.FIRST_MILE_CHANGE_RECORD.getCode(), firstMileChangeRecordEntity.getId(), "新增操作");
        return new BaseResultDTO.AddDTO(firstMileChangeRecordEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(FirstMileChangeRecordDTO.UpdateDTO addOrUpdateDTO) {
        FirstMileChangeRecordEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "头程调整记录"));
        FirstMileChangeRecordEntity firstMileChangeRecordEntity =  BeanMapperUtils.map(FirstMileChangeRecordEntity.class, addOrUpdateDTO);
        // 数据处理
        handleData(firstMileChangeRecordEntity);
        log.info("编辑 开始修改头程调整记录数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(firstMileChangeRecordEntity);
        if(!save) {
            throw new ServiceException("头程调整记录保存失败");
        }
        // 记录主单操作日志
        log.info("编辑 开始记录头程调整记录日志数据，单号：【{}】", firstMileChangeRecordEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), firstMileChangeRecordEntity.getCode(), "头程调整记录");
        operateLogService.addModuleOperateLogByObj(old, firstMileChangeRecordEntity, ModuleTypeEnum.FIRST_MILE_CHANGE_RECORD.getCode(), firstMileChangeRecordEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<FirstMileChangeRecordDTO.PagingVO> paging(PagingDTO<FirstMileChangeRecordDTO.PagingParamDTO> dto) {
        FirstMileChangeRecordDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page<FirstMileChangeRecordDTO.PagingVO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<FirstMileChangeRecordDTO.PagingVO> pageData = baseMapper.paging(query, params);
        List<FirstMileChangeRecordDTO.PagingVO> list = pageData.getRecords();
        fillPagingDb(list);
        return new PagingVO<>(pageData);
    }

    @Override
    public void exportList(FirstMileChangeRecordDTO.PagingParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("头程调整记录导出", EXPORT_TMS_FIRST_MILE_COST_ALLOCATION.getCode(), dto);
    }

    private void fillPagingDb(List<FirstMileChangeRecordDTO.PagingVO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        //添加类型名称
        list.forEach(e -> {
            e.setCategoryName(FirstMileChangeRecordCategoryEnum.getName(e.getCategory()));
            e.setCategoryFieldName(FirstMileChangeRecordCategoryFieldEnum.getName(e.getCategoryField()));
            e.setChangeRangeName(FirstMileChangeRecordChangeRangeEnum.getName(e.getChangeRange()));
            e.setSourceTypeName(FirstMileChangeRecordSourceTypeEnum.getName(e.getSourceType()));
            e.setTypeName(FirstMileChangeRecordTypeEnum.getName(e.getType()));
        });
    }
    /**
    * 新增修改处理数据
    */
    private void handleData(FirstMileChangeRecordEntity firstMileChangeRecordEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
