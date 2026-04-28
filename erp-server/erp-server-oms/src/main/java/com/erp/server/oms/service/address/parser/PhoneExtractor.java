package com.erp.server.oms.service.address.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts phone and zip code.
 */
public class PhoneExtractor {

    private static final Pattern MOBILE_PATTERN = Pattern.compile(
            "(?<!\\d)(?:\\+?86[-\\s]?)?(1[3-9]\\d[-\\s]?\\d{4}[-\\s]?\\d{4})(?!\\d)");
    private static final Pattern ZIP_PATTERN = Pattern.compile("(?<!\\d)(\\d{6})(?!\\d)");

    private static final List<String> KEYWORDS = Arrays.asList(
            "电话", "手机", "收件", "联系人", "收货人", "收件人", "姓名");

    public ExtractedPhone extractBest(String text) {
        if (text == null || text.trim().isEmpty()) {
            return ExtractedPhone.empty();
        }
        List<PhoneCandidate> candidates = new ArrayList<PhoneCandidate>();
        Matcher matcher = MOBILE_PATTERN.matcher(text);
        while (matcher.find()) {
            String mobilePart = matcher.group(1);
            String normalized = normalize(mobilePart);
            if (!isValidMobile(normalized)) {
                continue;
            }
            PhoneCandidate candidate = new PhoneCandidate(normalized, matcher.start(), matcher.end());
            candidate.setScore(score(text, candidate));
            candidates.add(candidate);
        }
        if (candidates.isEmpty()) {
            return ExtractedPhone.empty();
        }
        candidates.sort(Comparator.comparingDouble(PhoneCandidate::getScore).reversed()
                .thenComparingInt(PhoneCandidate::getStart));
        PhoneCandidate best = candidates.get(0);
        return new ExtractedPhone(best.getPhone(), best.getStart(), best.getEnd());
    }

    public String extractZipCode(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        Matcher matcher = ZIP_PATTERN.matcher(text);
        while (matcher.find()) {
            String zip = matcher.group(1);
            int start = matcher.start();
            if (start > 0 && text.charAt(start - 1) == '1') {
                continue;
            }
            return zip;
        }
        return null;
    }

    public boolean isValidMobile(String mobile) {
        return mobile != null && mobile.matches("^1[3-9]\\d{9}$");
    }

    private String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        String digits = raw.replaceAll("[^0-9]", "");
        if (digits.length() == 13 && digits.startsWith("86")) {
            digits = digits.substring(2);
        }
        return digits;
    }

    private double score(String text, PhoneCandidate candidate) {
        double score = 0.5d;
        int bestDistance = Integer.MAX_VALUE;
        for (String keyword : KEYWORDS) {
            int idx = text.indexOf(keyword);
            while (idx >= 0) {
                int distance = Math.abs(candidate.getStart() - idx);
                if (distance < bestDistance) {
                    bestDistance = distance;
                }
                idx = text.indexOf(keyword, idx + 1);
            }
        }
        if (bestDistance <= 8) {
            score += 0.35d;
        } else if (bestDistance <= 20) {
            score += 0.2d;
        } else if (bestDistance <= 40) {
            score += 0.1d;
        }
        return score;
    }

    public static class ExtractedPhone {
        private final String phone;
        private final int start;
        private final int end;

        public ExtractedPhone(String phone, int start, int end) {
            this.phone = phone;
            this.start = start;
            this.end = end;
        }

        public static ExtractedPhone empty() {
            return new ExtractedPhone("", -1, -1);
        }

        public String getPhone() {
            return phone;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }

        public boolean present() {
            return phone != null && !phone.isEmpty();
        }

        public String removeFrom(String text) {
            if (!present() || text == null || start < 0 || end > text.length()) {
                return text == null ? "" : text;
            }
            String left = text.substring(0, start);
            String right = text.substring(end);
            return (left + " " + right).trim();
        }
    }

    private static class PhoneCandidate {
        private final String phone;
        private final int start;
        private final int end;
        private double score;

        private PhoneCandidate(String phone, int start, int end) {
            this.phone = phone;
            this.start = start;
            this.end = end;
        }

        public String getPhone() {
            return phone;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }

        public double getScore() {
            return score;
        }

        public void setScore(double score) {
            this.score = score;
        }
    }
}
