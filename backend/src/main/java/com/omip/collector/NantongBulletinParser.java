package com.omip.collector;

import com.omip.domain.*;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Pattern;

@Component
public class NantongBulletinParser {
    private static final Pattern DATE = Pattern.compile("(\\d{4})年(\\d{1,2})月(\\d{1,2})日");
    private static final Set<String> ORANGES = Set.of("橙子", "脐橙", "赣南脐橙");

    public List<RawPrice> parse(String html, String url) {
        var document = Jsoup.parse(html, url);
        var matcher = DATE.matcher(document.title() + " " + document.select("h1,h2,h3").text());
        if (!matcher.find()) throw new IllegalArgumentException("Nantong bulletin has no reference date");
        var date = LocalDate.of(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)), Integer.parseInt(matcher.group(3)));
        List<RawPrice> result = new ArrayList<>();
        for (var row : document.select("tr")) {
            var cells = row.select("th,td").stream().map(org.jsoup.nodes.Element::text).toList();
            if (cells.size() < 4 || !ORANGES.contains(cells.get(0).trim())) continue;
            var unit = cells.get(2).replace(" ", "");
            if (!unit.contains("元/500克")) continue;
            BigDecimal price;
            try { price = new BigDecimal(cells.get(3).trim()); } catch (NumberFormatException ignored) { continue; }
            var origin = cells.get(1).isBlank() ? "Unspecified origin" : cells.get(1).trim();
            result.add(new RawPrice("CN", "Nantong", "Nantong Agricultural Products Logistics — " + origin,
                    Product.FRESH_ORANGE, Category.WHOLESALE, price, "CNY", "500g", new BigDecimal("0.5"),
                    "Nantong Municipal Development and Reform Commission", url, SourceType.OFFICIAL, date,
                    cells.get(0).trim(), "Origin: " + origin));
        }
        if (result.isEmpty()) throw new IllegalArgumentException("Nantong bulletin contains no supported orange rows");
        return result;
    }
}
