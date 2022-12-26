package com.erp.server.bi.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.dto.base.BaseSearchDTO;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.dto.base.UpdateStateDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.dto.CategorySubjectDTO;
import com.erp.model.bi.dto.MyDashboardDTO;
import com.erp.model.bi.dto.SubjectDTO;
import com.erp.model.bi.dto.SubjectPagingDTO;
import com.erp.model.bi.entity.BiSubjectEntity;

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
    List<CategorySubjectDTO> homePage(String searchKeyword);

    /**
     * 复制专题id
     * @param subjectId
     * @return
     */
    Boolean copy(String subjectId);

    /**
     * 添加仪表盘
     * @author yl
     * @date 2022-12-26 9:31
     * @param dto
     * @return java.lang.String
     */
    String addDashboard(SubjectDTO dto);
}
