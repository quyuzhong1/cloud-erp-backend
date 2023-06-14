package com.erp.model.plm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * @Classname ProjectInfoDTO
 * @Description TODO
 * @Date 2022-09-19 11:52
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ProductOverviewDTO implements Serializable {


    /**
     * 基础信息
     */
    @Data
    @NoArgsConstructor
    public class BaseInfoDTO {

        /**
         * 产品id
         */
        private String id;


        /**
         * 项目id
         */
        private String projectId;

        /**
         * 名称
         */
        private String name;

        /**
         * 示意图
         */
        private String imageUrl;

        /**
         * 分类id
         */
        private String categoryId;

        /**
         * 英文名
         */
        private String spuEn;


        /**
         * 项目经理id
         */
        private String projectChargeId;

        /**
         * 项目经理
         */
        private String projectChargeName;

        /**
         * 产品经理id
         */
        private String productChargeId;

        /**
         * 产品经理名
         */
        private String productChargeName;

        /**
         * 产品状态
         */
        private Integer productStatus;


        /**
         * 产品状态名
         */
        private String productStatusName;

        /**
         * 预计开始时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
        private LocalDate planStartTime;


        /**
         * 预计结束时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
        private LocalDate planEndTime;

        /**
         * 是否延期 true 是
         */
        private Boolean isDelay;


        /**
         * 归属主体
         */
        private String productOwnerOrgId;


        /**
         * 归属主体名
         */
        private String productOwnerOrgName;

        /**
         * 产品等级
         */
        private String productGrade;

    }


    /**
     * 概览
     */
    @Data
    @NoArgsConstructor
    public class InfoDTO extends BaseInfoDTO {

        /**
         * 总任务信息
         */
        private TotalTaskDTO totalTask;

        /**
         * 周任务信息
         */
        private WeekTaskDTO weekTask;

        /**
         * 到期任务信息
         */
        private ExpireTaskDTO expireTask;

        /**
         * 延期任务信息
         */
        private DelayTaskDTO delayTask;


        /**
         * 团队成员
         */
        private TeamMemberDTO teamMember;


        /**
         * 产品经理，项目经理信息
         */
        private ProductMemberDTO chargeMember;

        /**
         * 其它成员信息
         */
        private ProductMemberDTO productMember;
    }

    /**
     * 总任务信息
     */
    @Data
    @NoArgsConstructor
    public class TotalTaskDTO {

        /**
         * 总任务数
         */
        private Integer totalCount;


        /**
         * 完成任务数
         */
        private Integer finishCount;


        /**
         * 进行中任务数
         */
        private Integer doingCount;


        /**
         * 未开始任务数
         */
        private Integer notStartCount;


        /**
         * 取消的任务数
         */
        private Integer cancelCount;


        /**
         * 变更的任务数
         */
        private Integer changeCount;


        /**
         * 完成率
         */
        private BigDecimal finishRate;

    }


    /**
     * 周任务信息
     */
    @Data
    @NoArgsConstructor
    public class WeekTaskDTO{

        /**
         * 总任务数
         */
        private Integer weekCount;


        /**
         * 完成任务数
         */
        private Integer finishCount;


        /**
         * 进行中任务数
         */
        private Integer doingCount;


        /**
         * 未开始任务数
         */
        private Integer notStartCount;


        /**
         * 延期的任务数
         */
        private Integer delayCount;



        /**
         * 完成率
         */
        private BigDecimal finishRate;

    }



    /**
     * 到期任务信息
     */
    @Data
    @NoArgsConstructor
    public class ExpireTaskDTO  {

        /**
         * 到期的任务总数
         */
        private Integer expireCount;


        /**
         * 完成任务数
         */
        private Integer finishCount;


        /**
         * 进行中任务数
         */
        private Integer doingCount;


        /**
         * 未开始任务数
         */
        private Integer notStartCount;

        /**
         * 完成率
         */
        private BigDecimal finishRate;

    }



    /**
     * 延期的任务信息
     */
    @Data
    @NoArgsConstructor
    public class DelayTaskDTO  {

        /**
         * 到期的任务总数
         */
        private Integer delayCount;


        /**
         * 完成任务数
         */
        private Integer finishCount;

        /**
         * 未成任务数
         */
        private Integer unfinishedCount;


        /**
         * 未完成(前置任务已完成)
         */
        private Integer preTaskFinishCount;

        /**
         * 未完成(前置任务已完成) 的任务id
         */
        private List<String> preTaskIdList;



        /**
         * 完成率
         */
        private BigDecimal finishRate;

    }



    /**
     * 团队成员
     */
    @Data
    @NoArgsConstructor
    public class TeamMemberDTO  {

        /**
         * 成员总数
         */
        private Integer memberCount;


        /**
         * 图像链接
         */
        private List<String> imgUrlList;



    }


    /**
     * 产品，项目负责人信息
     */
    @Data
    @NoArgsConstructor
    public class ProductMemberDTO  {




        /**
         * 用户名
         */
        private String userName;


        /**
         * 部门id
         */
        private String deptId;


        /**
         * 部门名
         */
        private String deptName;




        /**
         * 图像
         */
        private String imgUrl;


        /**
         * 总任务数
         */
        private Integer totalTaskCount;


        /**
         * 完成的任务数
         */
        private Integer finishTaskCount;



    }




}
