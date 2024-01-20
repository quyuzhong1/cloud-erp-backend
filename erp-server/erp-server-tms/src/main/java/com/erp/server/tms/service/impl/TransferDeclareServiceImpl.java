package com.erp.server.tms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.entity.TransferDeclareEntity;
import com.erp.model.wms.dto.FbaInventoryDTO;
import com.erp.server.tms.mapper.TransferDeclareMapper;
import com.erp.server.tms.service.TransferDeclareService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.CommonService;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.TransferDeclareDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 中转报关表 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2024-01-19
 */
@Slf4j
@Service
public class TransferDeclareServiceImpl extends SuperServiceImpl<TransferDeclareMapper, TransferDeclareEntity> implements TransferDeclareService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;

    @Override
    public PagingVO<TransferDeclareDTO.ListDTO> paging(PagingDTO<TransferDeclareDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        //列表Tab查询状态处理
        handleTableParam(pagingParamDTO.getParams());

        IPage<TransferDeclareDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if (CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    private void handleTableParam(TransferDeclareDTO.PagingParamDTO params) {

/*        if () {

        }*/
    }

    private void fillList(List<TransferDeclareDTO.ListDTO> records) {

    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(TransferDeclareDTO.AddDTO addDTO) {
        TransferDeclareEntity transferDeclareEntity = new TransferDeclareEntity();
        BeanMapperUtils.copy(addDTO, transferDeclareEntity);

        // 数据处理
        handleData(transferDeclareEntity);

        log.info("开始新增中转报关单");
        // 生成单号
        // TODO 此处的null需填写生成单号类型，type查看BusinessNoTypeEnum枚举类 注意需要填写prefix 为单号前缀
        String code = docNoGenHelper.generateCode(null);
        transferDeclareEntity.setCode(code);
        boolean save = super.save(transferDeclareEntity);
        if(!save) {
            throw new ServiceException("中转报关单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", commonService.getUserInfo().getUserName(), "中转报关单" , transferDeclareEntity.getCode());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, transferDeclareEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(transferDeclareEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(TransferDeclareDTO.UpdateDTO updateDTO) {
        TransferDeclareEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "中转报关单"));
        TransferDeclareEntity transferDeclareEntity =  BeanMapperUtils.map(TransferDeclareEntity.class, updateDTO);

        // 数据处理
        handleData(transferDeclareEntity);
        log.info("编辑 开始修改中转报关单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(transferDeclareEntity);
        if(!save) {
            throw new ServiceException("中转报关单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录中转报关单日志数据，单号：【{}】", transferDeclareEntity.getCode());
            String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), transferDeclareEntity.getCode(), "中转报关单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, transferDeclareEntity, null, transferDeclareEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public TransferDeclareEntity checkExistByChannelIds(List<String> ids) {
        return lambdaQuery().in(TransferDeclareEntity::getTransferChannelId, ids).last("LIMIT 1").one();
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(TransferDeclareEntity transferDeclareEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
