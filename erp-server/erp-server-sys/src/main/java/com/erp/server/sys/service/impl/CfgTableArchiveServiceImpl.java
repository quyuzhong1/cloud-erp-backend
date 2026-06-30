package com.erp.server.sys.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.aspect.DictCore;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.utils.DmpFeishuUtils;
import com.common.business.enums.ServiceCodeNameEnum;
import com.common.business.feign.BaseDataFeign;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.constant.EnumMessage;
import com.common.core.exception.ServiceException;
import com.erp.model.sys.dto.CfgTableArchiveDTO;
import com.erp.model.sys.entity.CfgTableArchiveEntity;
import com.erp.server.sys.mapper.CfgTableArchiveMapper;
import com.erp.server.sys.service.CfgTableArchiveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.regex.Pattern;

/**
 * <p>
 * 归档配置表 服务实现类
 * </p>
 */
@Slf4j
@Service
public class CfgTableArchiveServiceImpl extends SuperServiceImpl<CfgTableArchiveMapper, CfgTableArchiveEntity> implements CfgTableArchiveService {

    @Resource
    private DictCore dictCore;

    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_.]*$");
    private static final Pattern DDL_PATTERN = Pattern.compile("(?i)(DROP|TRUNCATE|ALTER|CREATE|GRANT|REVOKE)");

    @Override
    public PagingVO<CfgTableArchiveDTO.ListDTO> paging(PagingDTO<CfgTableArchiveDTO.SearchParamDTO> pagingDTO) {
        Page<?> query = new Page<>(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<CfgTableArchiveDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingDTO.getParams());
        return new PagingVO<>(pageData);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean add(CfgTableArchiveDTO.AddDTO dto) {
        validateConfig(dto.getTableName(), dto.getTimeField(), dto.getExtSql());
        CfgTableArchiveEntity entity = new CfgTableArchiveEntity();
        BeanUtil.copyProperties(dto, entity);
        return this.save(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(CfgTableArchiveDTO.UpdateDTO dto) {
        CfgTableArchiveEntity entity = this.getById(dto.getId());
        if (entity == null) {
            throw new ServiceException("归档配置不存在");
        }
        if (dto.getTableName() != null || dto.getTimeField() != null || dto.getExtSql() != null) {
            validateConfig(
                    dto.getTableName() != null ? dto.getTableName() : entity.getTableName(),
                    dto.getTimeField() != null ? dto.getTimeField() : entity.getTimeField(),
                    dto.getExtSql() != null ? dto.getExtSql() : entity.getExtSql()
            );
        }
        BeanUtil.copyProperties(dto, entity, "id");
        return this.updateById(entity);
    }

    @Override
    public Boolean delete(BaseIdsDTO.IdsDTO idsDTO) {
        return this.removeByIds(idsDTO.getIds());
    }

    @Override
    public int executeArchive(String id) {
        CfgTableArchiveEntity config = this.getById(id);
        if (config == null) {
            throw new ServiceException("归档配置不存在");
        }
        return doArchive(config);
    }

    @Override
    public int executeAllArchive() {
        List<CfgTableArchiveEntity> configList = this.lambdaQuery()
                .eq(CfgTableArchiveEntity::getIsDeleted, false)
                .list();
        int totalDeleted = 0;
        for (CfgTableArchiveEntity config : configList) {
            try {
                int deleted = doArchive(config);
                totalDeleted += deleted;
            } catch (Exception e) {
                log.error("归档任务执行失败, bizName={}, tableName={}", config.getBizName(), config.getTableName(), e);
                DmpFeishuUtils.sendFeiShuMsg(String.format("归档任务执行失败, bizName=%s, tableName=%s, error=%s",
                        config.getBizName(), config.getTableName(), e.getMessage()));
            }
        }
        return totalDeleted;
    }

    private int doArchive(CfgTableArchiveEntity config) {
        validateConfig(config.getTableName(), config.getTimeField(), config.getExtSql());

        String tableName = config.getTableName();
        String timeField = config.getTimeField();
        int retentionDay = config.getRetentionDay();
        int limitCount = config.getLimitCount();
        String extSql = config.getExtSql();

        ServiceCodeNameEnum serviceCodeNameEnum = EnumMessage.getByCode(ServiceCodeNameEnum.class, config.getSystemCode());
        if (serviceCodeNameEnum == null) {
            throw new ServiceException("归档配置的系统代码不合法: " + config.getSystemCode());
        }
        BaseDataFeign baseDataFeign = dictCore.getBaseDataFeign(serviceCodeNameEnum);

        log.warn("开始归档清理: bizName={}, systemCode={}, tableName={}, timeField={}, retentionDay={}, limitCount={}",
                config.getBizName(), config.getSystemCode(), tableName, timeField, retentionDay, limitCount);

        long startTime = System.currentTimeMillis();
        int deleted = baseDataFeign.deleteArchiveData(tableName, timeField, retentionDay, limitCount, extSql);
        long elapsed = System.currentTimeMillis() - startTime;
        log.warn("归档清理完成: bizName={}, tableName={}, 删除={}, 耗时={}ms",
                config.getBizName(), tableName, deleted, elapsed);
        return deleted;
    }

    private void validateConfig(String tableName, String timeField, String extSql) {
        if (tableName != null && !IDENTIFIER_PATTERN.matcher(tableName).matches()) {
            throw new ServiceException("表名格式不合法，只允许字母、数字、下划线和点号");
        }
        if (timeField != null && !IDENTIFIER_PATTERN.matcher(timeField).matches()) {
            throw new ServiceException("时间字段格式不合法，只允许字母、数字、下划线和点号");
        }
        if (extSql != null && !extSql.isEmpty() && DDL_PATTERN.matcher(extSql).find()) {
            throw new ServiceException("扩展SQL不允许包含DDL关键词");
        }
    }
}
