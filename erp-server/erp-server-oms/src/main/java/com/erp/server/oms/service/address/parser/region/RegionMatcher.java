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
        MatchedNode provinceNode = selectBestNode(matches, RegionLevel.PROVINCE);
        RegionNode province = provinceNode == null ? null : provinceNode.getNode();

        MatchedNode cityNode = selectBestCityNode(matches, province);
        RegionNode city = cityNode == null ? null : cityNode.getNode();

        MatchedNode districtNode = selectBestDistrictNode(matches, province, city);
        RegionNode district = districtNode == null ? null : districtNode.getNode();

        if (district != null) {
            city = parentOfLevel(district, RegionLevel.CITY);
            province = parentOfLevel(district, RegionLevel.PROVINCE);
            result.setMatchedDistrictToken(districtNode.getAlias());
        }
        if (city == null && cityNode != null) {
            city = cityNode.getNode();
        }
        if (province == null && provinceNode != null) {
            province = provinceNode.getNode();
        }
        if (city == null && province != null) {
            cityNode = selectBestCityNode(matches, province);
            city = cityNode == null ? null : cityNode.getNode();
        }
        if (district == null) {
            districtNode = selectBestDistrictNode(matches, province, city);
            district = districtNode == null ? null : districtNode.getNode();
            if (district != null) {
                result.setMatchedDistrictToken(districtNode.getAlias());
                if (city == null) {
                    city = parentOfLevel(district, RegionLevel.CITY);
                }
                if (province == null) {
                    province = parentOfLevel(district, RegionLevel.PROVINCE);
                }
            }
        }
        if (province == null && city != null) {
            province = parentOfLevel(city, RegionLevel.PROVINCE);
        }

        if (province != null) {
            result.setProvinceId(province.getId());
            result.setProvince(province.getName());
            if (provinceNode != null) {
                result.setMatchedProvinceToken(provinceNode.getAlias());
            }
        }
        if (city != null) {
            result.setCityId(city.getId());
            result.setCity(city.getName());
            if (cityNode != null) {
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

    private MatchedNode selectBestCityNode(List<MatchedNode> matches, RegionNode province) {
        List<MatchedNode> candidates = filterByLevel(matches, RegionLevel.CITY);
        if (province != null) {
            candidates.removeIf(candidate -> !isSameAncestor(candidate.getNode(), province, RegionLevel.PROVINCE));
        }
        List<MatchedNode> supported = new ArrayList<MatchedNode>();
        for (MatchedNode candidate : candidates) {
            if (hasMatchedAncestor(candidate.getNode(), RegionLevel.PROVINCE, matches)) {
                supported.add(candidate);
            }
        }
        return pickBest(supported.isEmpty() ? candidates : supported);
    }

    private MatchedNode selectBestDistrictNode(List<MatchedNode> matches, RegionNode province, RegionNode city) {
        List<MatchedNode> candidates = filterByLevel(matches, RegionLevel.DISTRICT);
        if (city != null) {
            candidates.removeIf(candidate -> !isSameAncestor(candidate.getNode(), city, RegionLevel.CITY));
        }
        if (province != null) {
            candidates.removeIf(candidate -> !isSameAncestor(candidate.getNode(), province, RegionLevel.PROVINCE));
        }
        List<MatchedNode> citySupported = new ArrayList<MatchedNode>();
        for (MatchedNode candidate : candidates) {
            if (hasMatchedAncestor(candidate.getNode(), RegionLevel.CITY, matches)) {
                citySupported.add(candidate);
            }
        }
        if (!citySupported.isEmpty()) {
            return pickBest(citySupported);
        }
        List<MatchedNode> provinceSupported = new ArrayList<MatchedNode>();
        for (MatchedNode candidate : candidates) {
            if (hasMatchedAncestor(candidate.getNode(), RegionLevel.PROVINCE, matches)) {
                provinceSupported.add(candidate);
            }
        }
        return pickBest(provinceSupported.isEmpty() ? candidates : provinceSupported);
    }

    private MatchedNode selectBestNode(List<MatchedNode> matches, RegionLevel level) {
        return pickBest(filterByLevel(matches, level));
    }

    private List<MatchedNode> filterByLevel(List<MatchedNode> matches, RegionLevel level) {
        List<MatchedNode> candidates = new ArrayList<MatchedNode>();
        for (MatchedNode match : matches) {
            if (match.getNode().getLevel() == level) {
                candidates.add(match);
            }
        }
        return candidates;
    }

    private MatchedNode pickBest(List<MatchedNode> candidates) {
        return candidates.stream()
                .max(Comparator.comparingInt(MatchedNode::getLength)
                        .thenComparingInt(m -> -m.getStart()))
                .orElse(null);
    }

    private boolean hasMatchedAncestor(RegionNode node, RegionLevel ancestorLevel, List<MatchedNode> matches) {
        RegionNode ancestor = parentOfLevel(node, ancestorLevel);
        if (ancestor == null || ancestor == node) {
            return false;
        }
        for (MatchedNode match : matches) {
            if (match.getNode().getLevel() == ancestorLevel && StringUtils.equals(match.getNode().getId(), ancestor.getId())) {
                return true;
            }
        }
        return false;
    }

    private boolean isSameAncestor(RegionNode node, RegionNode expectedAncestor, RegionLevel ancestorLevel) {
        if (expectedAncestor == null) {
            return true;
        }
        RegionNode ancestor = parentOfLevel(node, ancestorLevel);
        return ancestor != null && StringUtils.equals(ancestor.getId(), expectedAncestor.getId());
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
