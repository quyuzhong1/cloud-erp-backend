package com.erp.server.oms.service.address.parser.region;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Longest-match based region parser with parent backtracking.
 */
public class RegionMatcher {

    private final RegionLexicon lexicon;

    public RegionMatcher(RegionLexicon lexicon) {
        this.lexicon = lexicon;
    }

    public RegionMatchResult match(List<String> hints, String fullText) {
        RegionMatchResult fromHints = resolve(bestMatches(hints));
        if (isCompleteEnough(fromHints)) {
            return fromHints;
        }
        RegionMatchResult fromFullText = resolve(bestMatches(singleton(fullText)));
        merge(fromHints, fromFullText);
        return fromHints;
    }

    private List<String> singleton(String text) {
        List<String> list = new ArrayList<String>();
        if (StringUtils.isNotBlank(text)) {
            list.add(text);
        }
        return list;
    }

    private boolean isCompleteEnough(RegionMatchResult result) {
        return StringUtils.isNotBlank(result.getProvince()) &&
                StringUtils.isNotBlank(result.getCity()) &&
                StringUtils.isNotBlank(result.getDistrict());
    }

    private void merge(RegionMatchResult main, RegionMatchResult backup) {
        if (main == null || backup == null) {
            return;
        }
        if (StringUtils.isBlank(main.getProvince())) {
            main.setProvinceId(backup.getProvinceId());
            main.setProvince(backup.getProvince());
            main.setMatchedProvinceToken(backup.getMatchedProvinceToken());
        }
        if (StringUtils.isBlank(main.getCity())) {
            main.setCityId(backup.getCityId());
            main.setCity(backup.getCity());
            main.setMatchedCityToken(backup.getMatchedCityToken());
        }
        if (StringUtils.isBlank(main.getDistrict())) {
            main.setDistrictId(backup.getDistrictId());
            main.setDistrict(backup.getDistrict());
            main.setMatchedDistrictToken(backup.getMatchedDistrictToken());
        }
    }

    private List<MatchedNode> bestMatches(List<String> sources) {
        List<MatchedNode> all = new ArrayList<MatchedNode>();
        if (sources == null) {
            return all;
        }
        for (String source : sources) {
            if (StringUtils.isBlank(source)) {
                continue;
            }
            all.addAll(matchInText(source));
        }
        all.sort(Comparator.comparingInt(MatchedNode::getLength).reversed()
                .thenComparingInt(MatchedNode::getStart));
        return all;
    }

    private List<MatchedNode> matchInText(String text) {
        List<MatchedNode> matches = new ArrayList<MatchedNode>();
        Map<String, List<RegionNode>> aliasIndex = lexicon.aliasIndex();
        for (Map.Entry<String, List<RegionNode>> entry : aliasIndex.entrySet()) {
            String alias = entry.getKey();
            if (StringUtils.isBlank(alias)) {
                continue;
            }
            int idx = text.indexOf(alias);
            while (idx >= 0) {
                for (RegionNode node : entry.getValue()) {
                    matches.add(new MatchedNode(node, alias, idx, idx + alias.length()));
                }
                idx = text.indexOf(alias, idx + 1);
            }
        }
        return matches;
    }

    private RegionMatchResult resolve(List<MatchedNode> matches) {
        RegionMatchResult result = new RegionMatchResult();
        if (matches == null || matches.isEmpty()) {
            return result;
        }
        RegionNode district = selectBest(matches, RegionLevel.DISTRICT);
        MatchedNode districtNode = selectBestNode(matches, RegionLevel.DISTRICT);

        RegionNode city = null;
        MatchedNode cityNode = null;
        RegionNode province = null;
        MatchedNode provinceNode = null;

        if (district != null) {
            city = parentOfLevel(district, RegionLevel.CITY);
            province = parentOfLevel(district, RegionLevel.PROVINCE);
            if (districtNode != null) {
                result.setMatchedDistrictToken(districtNode.getAlias());
            }
        } else {
            city = selectBest(matches, RegionLevel.CITY);
            cityNode = selectBestNode(matches, RegionLevel.CITY);
            if (city != null) {
                province = parentOfLevel(city, RegionLevel.PROVINCE);
                if (cityNode != null) {
                    result.setMatchedCityToken(cityNode.getAlias());
                }
            }
            if (province == null) {
                province = selectBest(matches, RegionLevel.PROVINCE);
                provinceNode = selectBestNode(matches, RegionLevel.PROVINCE);
                if (provinceNode != null) {
                    result.setMatchedProvinceToken(provinceNode.getAlias());
                }
            }
        }

        if (province == null) {
            province = selectBest(matches, RegionLevel.PROVINCE);
            provinceNode = selectBestNode(matches, RegionLevel.PROVINCE);
        }
        if (city == null) {
            city = selectBest(matches, RegionLevel.CITY);
            cityNode = selectBestNode(matches, RegionLevel.CITY);
        }

        if (province != null) {
            result.setProvinceId(province.getId());
            result.setProvince(province.getName());
            if (result.getMatchedProvinceToken() == null && provinceNode != null) {
                result.setMatchedProvinceToken(provinceNode.getAlias());
            }
        }
        if (city != null) {
            result.setCityId(city.getId());
            result.setCity(city.getName());
            if (result.getMatchedCityToken() == null && cityNode != null) {
                result.setMatchedCityToken(cityNode.getAlias());
            }
        }
        if (district != null) {
            result.setDistrictId(district.getId());
            result.setDistrict(district.getName());
        }
        return result;
    }

    private RegionNode selectBest(List<MatchedNode> matches, RegionLevel level) {
        MatchedNode node = selectBestNode(matches, level);
        return node == null ? null : node.getNode();
    }

    private MatchedNode selectBestNode(List<MatchedNode> matches, RegionLevel level) {
        return matches.stream()
                .filter(m -> m.getNode().getLevel() == level)
                .max(Comparator.comparingInt(MatchedNode::getLength)
                        .thenComparingInt(m -> -m.getStart()))
                .orElse(null);
    }

    private RegionNode parentOfLevel(RegionNode node, RegionLevel target) {
        RegionNode cursor = node;
        while (cursor != null) {
            if (cursor.getLevel() == target) {
                return cursor;
            }
            String parentId = cursor.getParentId();
            cursor = parentId == null ? null : lexicon.idIndex().get(parentId);
        }
        return null;
    }

    private static class MatchedNode {
        private final RegionNode node;
        private final String alias;
        private final int start;
        private final int end;

        private MatchedNode(RegionNode node, String alias, int start, int end) {
            this.node = node;
            this.alias = alias;
            this.start = start;
            this.end = end;
        }

        public RegionNode getNode() {
            return node;
        }

        public String getAlias() {
            return alias;
        }

        public int getStart() {
            return start;
        }

        public int getLength() {
            return end - start;
        }
    }
}
