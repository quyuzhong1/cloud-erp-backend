package com.erp.server.bi.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.model.bi.dto.LayoutDTO;
import com.erp.model.bi.dto.SubjectLayoutDTO;
import com.erp.model.bi.dto.SubjectLayoutDetailsDTO;
import com.erp.model.bi.entity.BiLayoutEntity;
import com.erp.model.bi.entity.BiSubjectEntity;
import com.erp.server.bi.enums.LayoutBlockEnum;
import com.erp.server.bi.mapper.BiLayoutMapper;
import com.erp.server.bi.service.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 布局表(BiLayout)表服务实现类
 *
 * @author yl
 * @since 2022-12-08 14:28:27
 */
@Service
public class BiLayoutServiceImpl extends ServiceImpl<BiLayoutMapper, BiLayoutEntity> implements BiLayoutService {


    @Resource
    private BiLayoutRefModuleService layoutRefModuleService;

    @Resource
    private BiSubjectRefLayoutService subjectRefLayoutService;

    @Resource
    private CommonService commonService;


    @Resource
    private BiSubjectService subjectService;

    @Resource
    private BiSubjectShareService subjectShareService;


    /**
     * 添加布局与专题
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-12-13 16:14
     */
    @Override
    public Boolean addSubjectLayout(SubjectLayoutDTO dto) {
        String subjectId = dto.getSubjectId();
        List<LayoutDTO> layoutList = dto.getLayoutList();
        List<String> LayoutIds = new ArrayList<>();
        for (LayoutDTO layout : layoutList) {
            BiLayoutEntity entity = new BiLayoutEntity();
            String layoutId = IdWorker.getIdStr();
            String blockNo = layout.getBlockNo();
            entity.setBlockNo(blockNo);
            entity.setHeight(layout.getHeight());
            Integer columnCount = LayoutBlockEnum.getCount(blockNo);
            entity.setColumnCount(columnCount);
            Boolean flag = this.save(entity);
            if (flag) {
                LayoutIds.add(layoutId);
                //保存布局与 模块关系
                layoutRefModuleService.addLayoutRefModule(layoutId, blockNo, layout.getModuleIdList());
            }
        }

        //保存专题与布局关系表
        subjectRefLayoutService.addSubjectRefLayout(subjectId, LayoutIds);
        return null;
    }

    /**
     * 根据专题id 获取专题详情信息
     *
     * @param subjectId
     * @return com.erp.model.bi.dto.SubjectLayoutDetailsDTO
     * @author yl
     * @date 2022-12-13 17:30
     */
    @Override
    public SubjectLayoutDetailsDTO subjectInfo(String subjectId) {
        String userId = commonService.getUserInfo().getUid();
        BiSubjectEntity subject = subjectService.getById(subjectId);
        if (Objects.isNull(subject)) {
            throw new ServiceException(ApiError.ERROR_97000);
        }

        subjectShareService.checkPermission(userId, subject);

        return null;
    }
}
