package com.erp.server.sys.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.sys.entity.CfgThirdNoticeEntity;
import com.erp.model.workflow.dto.CfgApproveSyncDTO;
import com.erp.model.workflow.entity.WorkMenuEntity;
import com.erp.server.sys.mapper.CfgThirdNoticeMapper;
import com.erp.server.sys.service.CfgThirdNoticeService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import com.erp.server.sys.service.OperateLogService;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.sys.dto.CfgThirdNoticeDTO;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 三方通知配置 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-05-23
 */
@Slf4j
@Service
public class CfgThirdNoticeServiceImpl extends SuperServiceImpl<CfgThirdNoticeMapper, CfgThirdNoticeEntity> implements CfgThirdNoticeService {
    @Resource
    private OperateLogService operateLogService;



    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgThirdNoticeDTO.AddDTO addDTO) {
        CfgThirdNoticeEntity cfgThirdNoticeEntity = new CfgThirdNoticeEntity();
        BeanMapperUtils.copy(addDTO, cfgThirdNoticeEntity);

        // 数据处理
        handleData(cfgThirdNoticeEntity);

        log.info("开始新增三方通知配置");
        boolean save = super.save(cfgThirdNoticeEntity);
        if(!save) {
            throw new ServiceException("三方通知配置保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "三方通知配置" , cfgThirdNoticeEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, cfgThirdNoticeEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(cfgThirdNoticeEntity.getId(), cfgThirdNoticeEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgThirdNoticeDTO.UpdateDTO addOrUpdateDTO) {
        CfgThirdNoticeEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "三方通知配置"));
        CfgThirdNoticeEntity cfgThirdNoticeEntity =  BeanMapperUtils.map(CfgThirdNoticeEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(cfgThirdNoticeEntity);
        log.info("编辑 开始修改三方通知配置数据，id：【{}】", old.getId());
        boolean save = super.updateById(cfgThirdNoticeEntity);
        if(!save) {
            throw new ServiceException("三方通知配置保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录三方通知配置日志数据，id：【{}】", cfgThirdNoticeEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), cfgThirdNoticeEntity.getId(), "三方通知配置");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, cfgThirdNoticeEntity, null, cfgThirdNoticeEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<CfgThirdNoticeDTO.TabListDTO> tabList(PermissionsDTO param) {
        CfgThirdNoticeDTO.PagingParamDTO searchParam = new CfgThirdNoticeDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<CfgThirdNoticeDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        List<CfgThirdNoticeDTO.TabListDTO> result = new ArrayList<>();
        CfgThirdNoticeDTO.TabListDTO enable = list.stream().filter(e -> e.getTabFlag().equals("t")).findFirst().orElse(null);
        CfgThirdNoticeDTO.TabListDTO disable = list.stream().filter(e -> e.getTabFlag().equals("f")).findFirst().orElse(null);
        result.add(new CfgThirdNoticeDTO.TabListDTO("all", "全部" , 0));
        result.add(new CfgThirdNoticeDTO.TabListDTO("true", "启用" , null == enable ? 0 : enable.getCount()));
        result.add(new CfgThirdNoticeDTO.TabListDTO("false", "停用" ,null == disable ? 0 : disable.getCount()));
        return result;
    }

    @Override
    public PagingVO<CfgThirdNoticeDTO.ListDTO> paging(PagingDTO<CfgThirdNoticeDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<CfgThirdNoticeDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    private void fillList(List<CfgThirdNoticeDTO.ListDTO> records) {
        //飞书
        for (CfgThirdNoticeDTO.ListDTO record : records) {


        }
    }

    @Override
    public CfgThirdNoticeDTO.ViewDTO view(String id) {
        return null;
    }

    @Override
    public BatchResultDTO delete(String id) {
        return null;
    }

    @Override
    public BatchResultDTO enable(String id, Boolean noticeStatus) {
        return null;
    }

    @Override
    public void exportList(CfgThirdNoticeDTO.PagingParamDTO dto, HttpServletResponse response) {

    }


    /**
    * 新增修改处理数据
    */
    private void handleData(CfgThirdNoticeEntity cfgThirdNoticeEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
