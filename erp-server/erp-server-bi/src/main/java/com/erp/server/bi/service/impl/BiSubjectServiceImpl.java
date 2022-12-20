package com.erp.server.bi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.common.dto.base.BaseSearchDTO;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.dto.base.UpdateStateDTO;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.dto.*;
import com.erp.model.bi.entity.BiDictEntity;
import com.erp.model.bi.entity.BiSubjectDefaultEntity;
import com.erp.model.bi.entity.BiSubjectEntity;
import com.erp.server.bi.constant.IsDeleted;
import com.erp.server.bi.enums.DashboardEnum;
import com.erp.server.bi.enums.DictEnum;
import com.erp.server.bi.mapper.BiSubjectMapper;
import com.erp.server.bi.service.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 专题表(BiSubject)表服务实现类
 *
 * @author yl
 * @since 2022-12-08 14:31:58
 */
@Service
public class BiSubjectServiceImpl extends ServiceImpl<BiSubjectMapper, BiSubjectEntity> implements BiSubjectService {


    @Resource
    private BiSubjectShareService subjectShareService;

    @Resource
    private BiSubjectDefaultService subjectDefaultService;


    @Resource
    private BiDictService dictService;


    @Resource
    private BiLayoutService layoutService;

    @Resource
    private CommonService commonService;


    /**
     * 分页展示对应的数据
     *
     * @param dto
     * @return com.erp.common.vo.PagingVO<com.erp.model.bi.dto.SubjectPagingDTO>
     * @author yl
     * @date 2022-12-13 14:11
     */
    @Override
    public PagingVO<SubjectPagingDTO> queryByPage(PagingDTO<BaseSearchDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        BaseSearchDTO params = dto.getParams();
        IPage pageData = baseMapper.paging(query, params);
        return new PagingVO(pageData);
    }


    /**
     * 修改专题
     *
     * @param dto 实例对象
     * @return 实例对象
     */
    @Override
    public String update(SubjectDTO dto) {
        String name = dto.getName();
        String subjectId = dto.getId();
        String categoryId = dto.getCategoryId();
        String userId = commonService.getUserInfo().getUid();
        //检查名字是否重复
        checkName(subjectId, name);
        BiDictEntity dict = dictService.getById(categoryId);
        String categoryName = "";
        if (dict != null) {
            categoryName = dict.getName();
        }
        BiSubjectEntity subject = this.getById(subjectId);
        if (Objects.isNull(subject)) {
            throw new ServiceException(ApiError.ERROR_97000);
        }
        //检查能否操作
        checkCanHandle(subject, userId);
        String shareFlag = dto.getShareFlag();
        //专题id
        subject.setName(name);
        subject.setId(subjectId);
        subject.setShareFlag(shareFlag);
        subject.setCategoryId(categoryId);
        subject.setCategoryName(categoryName);
        Boolean result = this.updateById(subject);
        if (result) {
            //如果是分享
            if (DashboardEnum.SHARE.getFlag().equals(shareFlag)) {
                List<String> userList = dto.getShareUserIdList();
                //添加专题的分享用户
                subjectShareService.addSubjectShare(userList, subjectId);
            }
            return subjectId;
        }
        return "";

    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    public Boolean deleteById(String id) {

        boolean flag = this.removeById(id);
        if (flag) {
            //默认的专题删除
            subjectDefaultService.deleteBySubjectId(id);
            //分享的专题删除
            subjectShareService.deleteBySubjectId(id);
        }
        return flag;
    }

    /**
     * 检查专题的名字是否重复
     *
     * @param subjectId
     * @param name
     * @return void
     * @author yl
     * @date 2022-12-08 17:38
     */
    @Override
    public void checkName(String subjectId, String name) {
        LambdaQueryWrapper<BiSubjectEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BiSubjectEntity::getName, name);
        if (StringUtils.isNotBlank(subjectId)) {
            queryWrapper.ne(BiSubjectEntity::getId, subjectId);
        }
        queryWrapper.last("LIMIT 1");
        int count = this.count(queryWrapper);
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_97001);
        }

    }


    /**
     * 根据用户id 获取我的仪表盘
     *
     * @param userId
     * @return com.erp.model.bi.dto.MyDashboardDTO
     * @author yl
     * @date 2022-12-09 10:09
     */
    @Override
    public MyDashboardDTO myDashboard(String userId, String searchKeyword) {
        MyDashboardDTO result = new MyDashboardDTO();
        String type = DictEnum.DASHBOARD.getType();
        String dashboardFlag = DictEnum.DASHBOARD.getValue();
        /**
         * 根据用户id获取 他默认的主题
         */
        List<BiSubjectDefaultEntity> userDefaultList = subjectDefaultService.getByUserId(userId);
        List<String> userDefaultSubjectIds = userDefaultList.stream().map(BiSubjectDefaultEntity::getSubjectId).collect(Collectors.toList());
        //这个是我创造的
        List<DashboardDTO> myCreateList = baseMapper.getMyCreateDashboardList(type, dashboardFlag, userId, searchKeyword);
        for (DashboardDTO my : myCreateList) {
            if (userDefaultSubjectIds.contains(my.getId())) {
                my.setIsDefault(true);
            }
        }
        List<String> findIdList = new ArrayList<>();
        List<String> myCreateIds = myCreateList.stream().map(DashboardDTO::getId).collect(Collectors.toList());
        findIdList.addAll(myCreateIds);
        /**
         * 根据用户id
         * 查询出分享给我的专题id
         */
        List<String> shareDashboardIds = subjectShareService.getShareToMeDashboardIds(userId);
        findIdList.addAll(shareDashboardIds);

        //这个是常用的
        List<DashboardDTO> frequentlyList = baseMapper.getDashboardFrequentlyList(type, dashboardFlag, findIdList, searchKeyword);
        for (DashboardDTO frequently : frequentlyList) {
            if (userDefaultSubjectIds.contains(frequently.getId())) {
                frequently.setIsDefault(true);
            }
        }
        result.setFrequentlyList(frequentlyList);
        result.setMyCreateList(myCreateList);
        return result;
    }


    /**
     * 添加专题
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-12-09 16:52
     */
    @Override
    @Transactional
    public String addSubject(SubjectDTO dto) {
        String name = dto.getName();
        String categoryId = dto.getCategoryId();
        //检查名字是否重复
        checkName(null, name);
        BiDictEntity dict = dictService.getById(categoryId);
        String categoryName = "";
        if (dict != null) {
            categoryName = dict.getName();
        }

        BiSubjectEntity subject = new BiSubjectEntity();
        //专题id
        String subjectId = IdWorker.getIdStr();
        String shareFlag = dto.getShareFlag();
        subject.setName(name);
        subject.setId(subjectId);
        subject.setShareFlag(shareFlag);
        subject.setCategoryId(categoryId);
        subject.setCategoryName(categoryName);
        Boolean result = this.save(subject);
        if (result) {
            //如果是分享
            if (DashboardEnum.SHARE.getFlag().equals(shareFlag)) {
                List<String> userList = dto.getShareUserIdList();
                //添加专题的分享用户
                subjectShareService.addSubjectShare(userList, subjectId);
            }
            return subjectId;
        }
        return "";
    }

    /**
     * 设置专题状态
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-12-13 14:20
     */
    @Override
    public Boolean updateState(UpdateStateDTO dto) {
        BiSubjectEntity subject = this.getById(dto.getId());
        if (Objects.isNull(subject)) {
            throw new ServiceException(ApiError.ERROR_97000);
        }
        String userId = commonService.getUserInfo().getUid();
        checkCanHandle(subject, userId);
        Boolean stateFlag = dto.getState();
        if (stateFlag) {
            subject.setState(IsDeleted.YES);
        } else {
            subject.setState(IsDeleted.NO);
        }
        return this.updateById(subject);
    }

    /**
     * 检查是否可以操作专题
     * 只有创建人和当前登录人相同 才能操作
     *
     * @param subject
     * @param userId
     * @return void
     * @author yl
     * @date 2022-12-13 17:45
     */
    @Override
    public void checkCanHandle(BiSubjectEntity subject, String userId) {
        boolean handleFlag = false;
        if (subject != null) {
            String createUserId = subject.getCreateUserId();
            handleFlag = userId.equals(createUserId);
        }
        if (!handleFlag) {
            throw new ServiceException(ApiError.ERROR_97005);
        }
    }


    /**
     * 专题首页
     *
     * @param
     * @return java.util.List<com.erp.model.bi.dto.CategorySubjectDTO>
     * @author yl
     * @date 2022-12-14 16:14
     */
    @Override
    public List<CategorySubjectDTO> homePage(String searchKeyword) {
        List<CategorySubjectDTO> resultList = new ArrayList<>(10);
        String userId = commonService.getUserInfo().getUid();
        List<Pair<String, String>> pairList = dictService.getCategory(DictEnum.DASHBOARD.getType());

        //分享给我的
        List<String> shareToMeIds = subjectShareService.getShareToMeDashboardIds(userId);

        //查询到用户可见的专题
        List<String> subjectIdList = baseMapper.getUserVisibleSubjectId(userId);
        List<SubjectDTO> subjectList = baseMapper.getByIds(subjectIdList, searchKeyword);
        for (Pair<String, String> pair : pairList) {
            CategorySubjectDTO result = new CategorySubjectDTO();
            String categoryId = pair.getKey();
            result.setCategoryId(categoryId);
            result.setCategoryName(pair.getValue());
            List<SubjectDTO> subjectResultList = subjectList.stream().filter(m -> categoryId.equals(m.getCategoryId())).collect(Collectors.toList());
            result.setSubjectList(subjectResultList);
            resultList.add(result);
        }
        //我创建的
        CategorySubjectDTO myCreate = new CategorySubjectDTO();
        myCreate.setCategoryName("我创建的专题");
        List<SubjectDTO> myCreateList = subjectList.stream().filter(s -> userId.equals(s.getCreateUserId())).collect(Collectors.toList());
        myCreate.setSubjectList(myCreateList);
        resultList.add(myCreate);

        //分享给我的
        CategorySubjectDTO shareToMeDTO = new CategorySubjectDTO();
        shareToMeDTO.setCategoryName("共享专题");
        List<SubjectDTO> shareToMeList = subjectList.stream().filter(s ->shareToMeIds.contains(s.getId()) ).collect(Collectors.toList());
        shareToMeDTO.setSubjectList(shareToMeList);
        resultList.add(shareToMeDTO);
        return resultList;
    }


    /**
     * 复制专题
     *
     * @param subjectId
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-12-15 9:27
     */
    @Override
    public Boolean copy(String subjectId) {
        String userId = commonService.getUserInfo().getUid();
        String userName = commonService.getUserInfo().getUserName();
        LocalDateTime nowDate = LocalDateTime.now();
        BiSubjectEntity subject = this.getById(subjectId);
        if (Objects.isNull(subject)) {
            throw new ServiceException(ApiError.ERROR_97000);
        }
        BiSubjectEntity copySubject = new BiSubjectEntity();
        BeanMapper.copy(subject, copySubject);
        String newSubjectId = IdWorker.getIdStr();
        copySubject.setId(newSubjectId);
        copySubject.setCreateUserId(userId);
        copySubject.setCreateTime(nowDate);
        copySubject.setCreateUserName(userName);
        copySubject.setUpdateUserId(userId);
        copySubject.setUpdateTime(nowDate);
        copySubject.setUpdateUserName(userName);
        boolean flag = this.save(copySubject);
        //当复制成功的时候
        if (flag) {
            layoutService.copySubjectLayout(newSubjectId, subjectId);
        }
        return null;
    }


}
