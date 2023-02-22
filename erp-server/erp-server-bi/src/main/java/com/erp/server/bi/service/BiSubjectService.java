package com.erp.server.bi.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BaseSearchDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.bi.dto.*;
import com.erp.model.bi.entity.BiSubjectEntity;
import com.erp.model.bi.vo.CategorySubjectVO;

import java.util.List;

/**
 * 专题表(BiSubject)表服务接口
 *
 * @author yl
 * @since 2022-12-08 14:31:58
 */
public interface BiSubjectService  extends IService<BiSubjectEntity> {



    /**
     * 分页查询
     *
     * @return 查询结果
     */
    PagingVO<SubjectPagingDTO> queryByPage(PagingDTO<BaseSearchDTO> dto);



    /**
     * 修改数据
     *
     * @param dto 实例对象
     * @return 实例对象
     */
    String update(SubjectDTO  dto);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(String id);

    /**
     * 检查专题名是否重复
     * @author yl
     * @date 2022-12-08 17:38
     * @param subjectId
     * @param name
     * @return void
     */
    void checkName(String subjectId, String name);

    /**
     * 我的仪表盘
     * @param userId
     * @return
     */
    MyDashboardDTO myDashboard(String userId,String searchKeyword);


    /**
     * 添加主题
     * @param dto
     * @return
     */
    String addSubject(SubjectDTO dto);

    /**
     * 设置专题状态
     * @author yl
     * @date 2022-12-13 14:20
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean updateState(UpdateStateDTO dto);
    
    
    /**
     * 检查是否是自己新建的 专题
     * @author yl
     * @date 2022-12-14 11:59
     * @param entity
     * @return 
     */
    void checkCanHandle(BiSubjectEntity entity,String userId);

    
    /**
     * 专题首页
     * @author yl
     * @date 2022-12-14 16:13
     * @param
     * @return java.util.List<com.erp.model.bi.dto.CategorySubjectDTO>
     */
    List<CategorySubjectVO> homePage(String searchKeyword);

    /**
     * 复制专题id
     * @param dto
     * @return
     */
    String copy(CopySubjectDTO dto);

    /**
     * 添加仪表盘
     * @author yl
     * @date 2022-12-26 9:31
     * @param dto
     * @return java.lang.String
     */
    String addDashboard(SubjectDTO dto);

    /**
     * 专题的列表
     * @author yl
     * @date 2022-12-29 9:45
     * @return java.util.List<com.erp.model.bi.dto.CategorySubjectListDTO>
     */
    List<CategorySubjectDTO> categoryList(String searchKeyword);

    
    /**
     * 获取到默认的仪表盘
     * @author yl
     * @date 2022-12-29 10:52
     * @param
     * @return com.erp.model.bi.dto.SubjectLayoutDetailsDTO
     */
    SubjectLayoutDetailsDTO dashboardInfo();

    /**
     * 复制仪表盘
     * @param dto
     * @return
     */
    String copyDashboard(CopySubjectDTO dto);

    /**
     * 检查能否编辑
     * @author yl
     * @date 2023-01-05 17:47
     * @param id
     * @return void
     */
    void checkEditSubject(BaseIdDTO id);
}
