package com.erp.server.bi.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.dto.MyDashboardDTO;
import com.erp.model.bi.dto.SubjectDTO;
import com.erp.model.bi.entity.BiSubjectEntity;

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
    PagingVO<BiSubjectEntity> queryByPage();



    /**
     * 修改数据
     *
     * @param biSubject 实例对象
     * @return 实例对象
     */
    Boolean update(BiSubjectEntity biSubject);

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
    Boolean addSubject(SubjectDTO dto);
}
