package com.erp.server.plm.service;

import com.common.business.service.SuperService;
import com.erp.model.plm.dto.BasicLabelDTO;
import com.erp.model.plm.entity.BasicLabelEntity;
import com.erp.model.plm.vo.LabelBasicVO;
import com.erp.model.plm.vo.LabelLevelTreeVO;

import java.util.List;

/**
 * <p>
 * 基础标签表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-09-13
 */
public interface BasicLabelService extends SuperService<BasicLabelEntity> {

    List<LabelBasicVO> listByCondition(BasicLabelDTO.SearchDTO dto);

    /**
     *
     * @return
     */
    List<LabelLevelTreeVO> listByTree();

    /**
    * 新增
    * @author Lambda
    * @date: 2023-09-13
    * @param dto
    * @return
    */
    String add(BasicLabelDTO.AddDTO dto);

    /**
     * 新增
     * @author Lambda
     * @date: 2023-09-13
     * @param list
     * @return
     */
    Boolean batchAdd(List<BasicLabelDTO.AddDTO> list);

    /**
    * 修改
    * @author Lambda
    * @date: 2023-09-13
    * @param dto
    * @return
    */
    Boolean update(BasicLabelDTO.UpdateDTO dto);

    /**
     * 修改
     * @author zdy
     * @date: 2023-09-16
     * @param id
     * @return
     */
    void removeBasicLabelById(String id);

    /**
     * 获取详情
     */
    BasicLabelEntity view(String id);
}
