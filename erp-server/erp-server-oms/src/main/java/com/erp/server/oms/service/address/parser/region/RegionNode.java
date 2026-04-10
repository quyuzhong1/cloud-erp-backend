package com.erp.server.oms.service.address.parser.region;

import java.util.Collections;
import java.util.List;

/**
 * Region lexicon node.
 */
public class RegionNode {
    private final String id;
    private final String name;
    private final RegionLevel level;
    private final String parentId;
    private final List<String> aliases;

    public RegionNode(String id, String name, RegionLevel level, String parentId, List<String> aliases) {
        this.id = id;
        this.name = name;
        this.level = level;
        this.parentId = parentId;
        this.aliases = aliases == null ? Collections.<String>emptyList() : aliases;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public RegionLevel getLevel() {
        return level;
    }

    public String getParentId() {
        return parentId;
    }

    public List<String> getAliases() {
        return aliases;
    }
}
