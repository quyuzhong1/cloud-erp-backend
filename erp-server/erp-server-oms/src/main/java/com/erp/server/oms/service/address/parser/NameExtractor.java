package com.erp.server.oms.service.address.parser;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts contact name using HanLP first, then regex fallback.
 */
public class NameExtractor {

    private static final List<String> NAME_HINT_KEYWORDS = Arrays.asList("收货人", "收件人", "联系人", "姓名");
    private static final Pattern KEYWORD_NAME_PATTERN = Pattern.compile(
            "(?:收货人|收件人|联系人|姓名)\\s*[:：]?\\s*([\\u4e00-\\u9fa5]{2,6})");
    private static final Pattern ADDRESS_BUILDING_SUFFIX_PATTERN = Pattern.compile(
            "^(?:\\s|,|，|;|；|:|：|-|_|/|\\\\)*([0-9A-Za-z一二三四五六七八九十百]+)?(楼|层|栋|幢|座|号|室|单元|楼栋|门牌).*");
    private static final Pattern ADDRESS_PLACE_SUFFIX_PATTERN = Pattern.compile(
            "^(?:\\s|,|，|;|；|:|：|-|_|/|\\\\)*(大厦|广场|中心|公寓|花园|小区|大楼|商厦|产业园|工业园|园区|城|苑).*");

    public ExtractedName extract(String textWithoutPhone, int phoneStartIndex) {
        if (textWithoutPhone == null || textWithoutPhone.trim().isEmpty()) {
            return ExtractedName.empty();
        }
        List<NameCandidate> candidates = extractFromNer(textWithoutPhone);
        if (!candidates.isEmpty()) {
            NameCandidate best = selectBest(candidates, textWithoutPhone, phoneStartIndex);
            return new ExtractedName(best.getName(), best.getStart(), best.getEnd(), 0.9d);
        }

        Matcher matcher = KEYWORD_NAME_PATTERN.matcher(textWithoutPhone);
        if (matcher.find()) {
            String name = matcher.group(1);
            if (isLikelyName(name)) {
                int start = matcher.start(1);
                return new ExtractedName(name, start, start + name.length(), 0.8d);
            }
        }

        List<NameCandidate> fallback = extractNameLikePieces(textWithoutPhone);
        if (fallback.isEmpty()) {
            return ExtractedName.empty();
        }
        NameCandidate best = selectBest(fallback, textWithoutPhone, phoneStartIndex);
        return new ExtractedName(best.getName(), best.getStart(), best.getEnd(), 0.55d);
    }

    private List<NameCandidate> extractFromNer(String text) {
        List<NameCandidate> list = new ArrayList<NameCandidate>();
        try {
            List<Term> terms = HanLP.segment(text);
            int cursor = 0;
            for (Term term : terms) {
                if (term == null || term.word == null) {
                    continue;
                }
                String nature = term.nature == null ? "" : term.nature.toString();
                if (!nature.startsWith("nr")) {
                    continue;
                }
                String word = term.word.trim();
                if (!isLikelyName(word)) {
                    continue;
                }
                int idx = text.indexOf(word, cursor);
                if (idx < 0) {
                    idx = text.indexOf(word);
                }
                if (idx < 0) {
                    continue;
                }
                cursor = idx + word.length();
                if (isSuppressedByAddressContext(text, idx, idx + word.length())) {
                    continue;
                }
                list.add(new NameCandidate(word, idx, idx + word.length()));
            }
        } catch (Throwable ignored) {
        }
        return list;
    }

    private List<NameCandidate> extractNameLikePieces(String text) {
        List<NameCandidate> candidates = new ArrayList<NameCandidate>();
        Matcher matcher = Pattern.compile("[\\u4e00-\\u9fa5]{2,6}").matcher(text);
        while (matcher.find()) {
            String token = matcher.group();
            if (!isLikelyName(token)) {
                continue;
            }
            if (isSuppressedByAddressContext(text, matcher.start(), matcher.end())) {
                continue;
            }
            candidates.add(new NameCandidate(token, matcher.start(), matcher.end()));
        }
        return candidates;
    }

    private NameCandidate selectBest(List<NameCandidate> candidates, String text, int phoneStartIndex) {
        NameCandidate keywordPreferred = pickKeywordPreferred(candidates, text);
        if (keywordPreferred != null) {
            return keywordPreferred;
        }
        return candidates.stream()
                .min(Comparator.comparingInt((NameCandidate c) -> c.getName().length())
                        .thenComparingInt(c -> distance(c.getStart(), phoneStartIndex))
                        .thenComparingInt(NameCandidate::getStart))
                .orElse(candidates.get(0));
    }

    private NameCandidate pickKeywordPreferred(List<NameCandidate> candidates, String text) {
        NameCandidate best = null;
        int bestScore = Integer.MIN_VALUE;
        for (NameCandidate candidate : candidates) {
            int score = 0;
            for (String key : NAME_HINT_KEYWORDS) {
                int keyIdx = text.lastIndexOf(key, candidate.getStart());
                if (keyIdx >= 0) {
                    int distance = candidate.getStart() - keyIdx;
                    if (distance <= 6) {
                        score = Math.max(score, 100 - distance);
                    } else if (distance <= 20) {
                        score = Math.max(score, 50 - distance);
                    }
                }
            }
            if (score > bestScore) {
                bestScore = score;
                best = candidate;
            }
        }
        return bestScore > 0 ? best : null;
    }

    private boolean isLikelyName(String token) {
        if (token == null || token.length() < 2 || token.length() > 6) {
            return false;
        }
        if (!token.matches("[\\u4e00-\\u9fa5]{2,6}")) {
            return false;
        }
        return !token.matches(".*(省|市|区|县|路|街|道|巷|号|栋|单元|室|园|大厦|广场|大道|小区).*");
    }

    private boolean isSuppressedByAddressContext(String text, int start, int end) {
        if (text == null || start < 0 || end < start || end > text.length()) {
            return false;
        }
        String suffix = text.substring(end);
        return ADDRESS_BUILDING_SUFFIX_PATTERN.matcher(suffix).matches()
                || ADDRESS_PLACE_SUFFIX_PATTERN.matcher(suffix).matches();
    }

    private int distance(int a, int b) {
        if (b < 0) {
            return a;
        }
        return Math.abs(a - b);
    }

    private static class NameCandidate {
        private final String name;
        private final int start;
        private final int end;

        private NameCandidate(String name, int start, int end) {
            this.name = name;
            this.start = start;
            this.end = end;
        }

        public String getName() {
            return name;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }
    }

    public static class ExtractedName {
        private final String name;
        private final int start;
        private final int end;
        private final double confidence;

        public ExtractedName(String name, int start, int end, double confidence) {
            this.name = name;
            this.start = start;
            this.end = end;
            this.confidence = confidence;
        }

        public static ExtractedName empty() {
            return new ExtractedName("", -1, -1, 0d);
        }

        public String getName() {
            return name;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }

        public double getConfidence() {
            return confidence;
        }

        public boolean present() {
            return name != null && !name.isEmpty();
        }
    }
}
