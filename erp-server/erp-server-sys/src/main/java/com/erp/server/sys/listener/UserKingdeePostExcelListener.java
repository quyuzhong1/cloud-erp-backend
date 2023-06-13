package com.erp.server.sys.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.business.dto.FindUserDTO;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.sys.dto.excel.UserKingdeePostImportExcelDTO;
import com.erp.model.sys.entity.UserKingdeePostEntity;
import com.erp.server.sys.service.UserKingdeePostService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Lambda
 * @Classname UserKingdeePostExcelListener
 * @Description TODO
 * @Date 2023-06-02 16:05
 * @Created by yl
 */
public class UserKingdeePostExcelListener extends AnalysisEventListener<UserKingdeePostImportExcelDTO> {


    private List<FindUserDTO> userList;

    private UserKingdeePostService userKingdeePostService;


    private List<UserKingdeePostEntity> userKingdeePostList;


    private List<UserKingdeePostEntity> addOrUpdateList = new ArrayList<>();

    private List<UserKingdeePostImportExcelDTO> errorList = new ArrayList<>();


    public UserKingdeePostExcelListener(UserKingdeePostService userKingdeePostService, List<FindUserDTO> userList, List<UserKingdeePostEntity> userKingdeePostList) {
        this.userList = userList;
        this.userKingdeePostList = userKingdeePostList;
        this.userKingdeePostService = userKingdeePostService;
    }

    /**
     * 解析一行 执行一遍
     *
     * @param excelDTO
     * @param analysisContext
     * @return void
     * @author yl
     * @date 2023-06-02 16:06
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invoke(UserKingdeePostImportExcelDTO excelDTO, AnalysisContext analysisContext) {
        List<String> errorMsgList = new ArrayList<>();
        //基础验证
        List<String> msgList = FieldValidUtil.fieldValid(excelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        //用户名
        String userName = excelDTO.getUserName();
        FindUserDTO user = userList.stream().filter(u -> u.getUserName().equals(userName)).findFirst().orElse(null);
        if (Objects.isNull(user)) {
            errorMsgList.add("用户不存在ERP");
        }
        //存在错误数据则直接返回 因为 这里可能给一个错误的 日期格式
        if (errorMsgList.size() > 0) {
            excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(excelDTO);
            return;
        }
        UserKingdeePostEntity post = userKingdeePostList.stream().filter(p -> p.getUserId().equals(user.getUserId())).findFirst().orElse(new UserKingdeePostEntity());
        post.setKingdeePostCode(excelDTO.getKingdeePostCode());
        post.setKingdeeUserCode(excelDTO.getKingdeeUserCode());
        post.setUseOrgName(excelDTO.getUseOrgName());
        post.setPostName(excelDTO.getPostName());
        post.setUserName(userName);
        post.setUserId(user.getUserId());
        addOrUpdateList.add(post);
    }


    /**
     * 全部解析完成后执行
     *
     * @param analysisContext
     * @return void
     * @author yl
     * @date 2023-06-02 16:06
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        userKingdeePostService.saveOrUpdateBatch(addOrUpdateList);
    }

    public List<UserKingdeePostImportExcelDTO> getErrorList() {
        return errorList;
    }
}
