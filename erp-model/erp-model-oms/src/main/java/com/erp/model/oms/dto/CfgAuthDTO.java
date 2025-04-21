package com.erp.model.oms.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.erp.model.oms.entity.CfgAuthCountryEntity;
import com.erp.model.oms.entity.CfgAuthRegionEntity;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.constraints.NotBlank;

/**
 * <p>
 * 授权国家配置请求响应实体
 * </p>
 *
 * @author Jim
 * @since 2025-04-21
 */
@Data
@NoArgsConstructor
public class CfgAuthDTO implements Serializable {


    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {
        /**
         * 主键
         */
        private String id;
        /**
         * 备注
         */
        private String remark;

        /**
         * 区域代码（如 us-east-1）
         */
        private String region;
        /**
         * 区域名称（如 北美/欧洲）
         */
        private String name;
        /**
         * OMS平台代号
         */
        private String dictPlatform;
        /**
         * 排序
         */
        private Integer index;
        /**
         * 授权国家
         */
        private List<CountryViewDTO> countryList;

        public ViewDTO(CfgAuthRegionEntity regionEntity, List<CfgAuthCountryEntity> countryEntityList) {
            this.id = regionEntity.getId();
            this.remark = regionEntity.getRemark();
            this.region = regionEntity.getRegion();
            this.name = regionEntity.getName();
            this.dictPlatform = regionEntity.getDictPlatform();
            this.index = regionEntity.getIndex();
            this.countryList = Collections.emptyList();
            if (CollectionUtils.isNotEmpty(countryEntityList)) {
                this.countryList = countryEntityList.stream()
                        .map(CountryViewDTO::new)
                        .sorted(Comparator.comparing(CountryViewDTO::getIndex))
                        .collect(Collectors.toList());
            }

        }

    }

    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class CountryViewDTO {

        /**
         * 明细主键id
         */
        private String countryId;

        /**
         * 商城编号 MarketplaceId
         */
        private String marketplaceId;

        /**
         * 商城中文名称
         */
        private String name;

        /**
         * 国家/地区代码（ISO标准）
         */
        private String countryCode;

        /**
         * 单站点授权卖家平台URL
         */
        private String sellerCentralUrl;

        /**
         * 备注
         */
        private String remark;

        /**
         * cfg_auth_region区域编码ID
         */
        private String mainId;
        /**
         * 排序
         */
        private Integer index;


        public CountryViewDTO(CfgAuthCountryEntity entity) {
            this.countryId = entity.getId();
            this.marketplaceId = entity.getMarketplaceId();
            this.name = entity.getName();
            this.countryCode = entity.getCountryCode();
            this.sellerCentralUrl = entity.getSellerCentralUrl();
            this.remark = entity.getRemark();
            this.mainId = entity.getMainId();
            this.index = entity.getIndex();
        }
    }

    /**
     * 列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
         * 平台代号
         */
        @NotBlank(message = "平台代号不能为空")
        private String dictPlatform;
    }


}