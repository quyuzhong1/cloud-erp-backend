package com.erp.server.oms.service.address.parser.region;

import com.common.core.enums.DictCityTypeEnum;
import com.erp.model.sys.entity.DictCityEntity;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Region lexicon built from dict_city.
 */
public class DictRegionLexicon implements RegionLexicon {

    private final List<RegionNode> nodes;
    private final Map<String, RegionNode> idIndex;
    private final Map<String, List<RegionNode>> aliasIndex;

    public DictRegionLexicon(List<DictCityEntity> dictCityEntities) {
        List<RegionNode> nodeList = buildNodes(dictCityEntities);
        this.nodes = Collections.unmodifiableList(nodeList);
        this.idIndex = Collections.unmodifiableMap(nodeList.stream()
                .collect(Collectors.toMap(RegionNode::getId, v -> v, (a, b) -> a, LinkedHashMap::new)));
        this.aliasIndex = Collections.unmodifiableMap(buildAliasIndex(nodeList));
    }

    private List<RegionNode> buildNodes(List<DictCityEntity> dictCityEntities) {
        if (dictCityEntities == null) {
            return Collections.emptyList();
        }
        List<RegionNode> result = new ArrayList<RegionNode>();
        for (DictCityEntity entity : dictCityEntities) {
            if (entity == null || Boolean.TRUE.equals(entity.getDisabled())) {
                continue;
            }
            RegionLevel level = mapLevel(entity.getType());
            if (level == null) {
                continue;
            }
            String id = entity.getId();
            String name = StringUtils.trimToEmpty(entity.getName());
            if (StringUtils.isBlank(id) || StringUtils.isBlank(name)) {
                continue;
            }
            List<String> aliases = buildAliases(name, level);
            result.add(new RegionNode(id, name, level, entity.getParentId(), aliases));
        }
        return result;
    }

    private RegionLevel mapLevel(String type) {
        if (StringUtils.equals(type, DictCityTypeEnum.PROVINCE.getCode())) {
            return RegionLevel.PROVINCE;
        }
        if (StringUtils.equals(type, DictCityTypeEnum.CITY.getCode())) {
            return RegionLevel.CITY;
        }
        if (StringUtils.equals(type, DictCityTypeEnum.DISTRICT.getCode())) {
            return RegionLevel.DISTRICT;
        }
        return null;
    }

    private List<String> buildAliases(String name, RegionLevel level) {
        Set<String> aliases = new LinkedHashSet<String>();
        aliases.add(name);
        String normalized = name.replace(" ", "");
        aliases.add(normalized);

        addTrimAlias(aliases, normalized, "省");
        addTrimAlias(aliases, normalized, "市");
        addTrimAlias(aliases, normalized, "区");
        addTrimAlias(aliases, normalized, "县");
        addTrimAlias(aliases, normalized, "自治州");
        addTrimAlias(aliases, normalized, "自治区");
        addTrimAlias(aliases, normalized, "特别行政区");
        addTrimAlias(aliases, normalized, "盟");
        addTrimAlias(aliases, normalized, "旗");

        if (level == RegionLevel.PROVINCE) {
            aliases.add(normalized.replace("壮族", "").replace("维吾尔", "").replace("回族", ""));
        }
        aliases.removeIf(StringUtils::isBlank);
        return new ArrayList<String>(aliases);
    }

    private void addTrimAlias(Set<String> aliases, String name, String suffix) {
        if (name.endsWith(suffix) && name.length() > suffix.length()) {
            aliases.add(name.substring(0, name.length() - suffix.length()));
        }
    }

    private Map<String, List<RegionNode>> buildAliasIndex(List<RegionNode> nodeList) {
        Map<String, List<RegionNode>> map = new LinkedHashMap<String, List<RegionNode>>();
        for (RegionNode node : nodeList) {
            for (String alias : node.getAliases()) {
                String key = StringUtils.trimToEmpty(alias);
                if (StringUtils.isBlank(key)) {
                    continue;
                }
                List<RegionNode> entries = map.get(key);
                if (entries == null) {
                    entries = new ArrayList<RegionNode>();
                    map.put(key, entries);
                }
                entries.add(node);
            }
        }
        return map;
    }

    @Override
    public List<RegionNode> allNodes() {
        return nodes;
    }

    @Override
    public Map<String, RegionNode> idIndex() {
        return idIndex;
    }

    @Override
    public Map<String, List<RegionNode>> aliasIndex() {
        return aliasIndex;
    }
}
