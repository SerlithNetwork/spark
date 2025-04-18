/*
 * This file is part of spark.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.lucko.spark.common.util;

import com.google.common.base.Strings;
import me.lucko.spark.api.statistic.misc.DoubleAverageInfo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;

import java.lang.management.MemoryUsage;
import java.util.Locale;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;

public enum StatisticFormatter {
    ;

    private static final String BAR_TRUE_CHARACTER = "┃";
    private static final String BAR_FALSE_CHARACTER = "╻";

    private static final TextColor COLOR_HEALTH_GOOD = TextColor.color(155, 255, 255);
    private static final TextColor COLOR_HEALTH_MEDIUM = TextColor.color(255, 190, 0);
    private static final TextColor COLOR_HEALTH_BAD = TextColor.color(255, 20, 20);

    public static TextComponent formatTps(double tps) {
        TextColor color;
        if (tps > 18.0) {
            color = COLOR_HEALTH_GOOD;
        } else if (tps > 16.0) {
            color = COLOR_HEALTH_MEDIUM;
        } else {
            color = COLOR_HEALTH_BAD;
        }

        return text((tps > 20.0 ? "*" : "") + Math.min(Math.round(tps * 100.0) / 100.0, 20.0), color);
    }

    public static TextComponent formatTickDurations(DoubleAverageInfo average) {
        return text()
                .append(formatTickDuration(average.min()))
                .append(text('/', GRAY))
                .append(formatTickDuration(average.median()))
                .append(text('/', GRAY))
                .append(formatTickDuration(average.percentile95th()))
                .append(text('/', GRAY))
                .append(formatTickDuration(average.max()))
                .build();
    }

    public static TextComponent formatTickDuration(double duration) {
        TextColor color;
        if (duration >= 50d) {
            color = COLOR_HEALTH_BAD;
        } else if (duration >= 40d) {
            color = COLOR_HEALTH_MEDIUM;
        } else {
            color = COLOR_HEALTH_GOOD;
        }

        return text(String.format(Locale.ENGLISH, "%.1f", duration), color);
    }

    public static TextComponent formatCpuUsage(double usage) {
        TextColor color;
        if (usage > 0.9) {
            color = COLOR_HEALTH_BAD;
        } else if (usage > 0.65) {
            color = COLOR_HEALTH_MEDIUM;
        } else {
            color = COLOR_HEALTH_GOOD;
        }

        return text(FormatUtil.percent(usage, 1d), color);
    }

    public static TextComponent formatPingRtts(double min, double median, double percentile95th, double max) {
        return text()
                .append(formatPingRtt(min))
                .append(text('/', GRAY))
                .append(formatPingRtt(median))
                .append(text('/', GRAY))
                .append(formatPingRtt(percentile95th))
                .append(text('/', GRAY))
                .append(formatPingRtt(max))
                .build();
    }

    public static TextComponent formatPingRtt(double ping) {
        TextColor color;
        if (ping >= 200) {
            color = COLOR_HEALTH_BAD;
        } else if (ping >= 100) {
            color = COLOR_HEALTH_MEDIUM;
        } else {
            color = COLOR_HEALTH_GOOD;
        }

        return text((int) Math.ceil(ping), color);
    }

    public static TextComponent generateMemoryUsageDiagram(MemoryUsage usage, int length) {
        double used = usage.getUsed();
        double committed = usage.getCommitted();
        double max = usage.getMax();

        int usedChars = (int) ((used * length) / max);
        int committedChars = (int) ((committed * length) / max);

        TextComponent.Builder line = text().content(Strings.repeat(BAR_TRUE_CHARACTER, usedChars)).color(COLOR_HEALTH_MEDIUM);
        if (committedChars > usedChars) {
            line.append(text(Strings.repeat(BAR_FALSE_CHARACTER, (committedChars - usedChars) - 1), GRAY));
            line.append(Component.text(BAR_FALSE_CHARACTER, COLOR_HEALTH_BAD));
        }
        if (length > committedChars) {
            line.append(text(Strings.repeat(BAR_FALSE_CHARACTER, (length - committedChars)), GRAY));
        }

        return text()
                .append(text("[", DARK_GRAY))
                .append(line.build())
                .append(text("]", DARK_GRAY))
                .build();
    }

    public static TextComponent generateMemoryPoolDiagram(MemoryUsage usage, MemoryUsage collectionUsage, int length) {
        double used = usage.getUsed();
        double collectionUsed = used;
        if (collectionUsage != null) {
            collectionUsed = collectionUsage.getUsed();
        }
        double committed = usage.getCommitted();
        double max = usage.getMax();

        int usedChars = (int) ((used * length) / max);
        int collectionUsedChars = (int) ((collectionUsed * length) / max);
        int committedChars = (int) ((committed * length) / max);

        TextComponent.Builder line = text().content(Strings.repeat(BAR_TRUE_CHARACTER, collectionUsedChars)).color(COLOR_HEALTH_MEDIUM);

        if (usedChars > collectionUsedChars) {
            line.append(Component.text(BAR_TRUE_CHARACTER, COLOR_HEALTH_BAD));
            line.append(text(Strings.repeat(BAR_TRUE_CHARACTER, (usedChars - collectionUsedChars) - 1), COLOR_HEALTH_MEDIUM));
        }
        if (committedChars > usedChars) {
            line.append(text(Strings.repeat(BAR_FALSE_CHARACTER, (committedChars - usedChars) - 1), GRAY));
            line.append(Component.text(BAR_FALSE_CHARACTER, COLOR_HEALTH_MEDIUM));
        }
        if (length > committedChars) {
            line.append(text(Strings.repeat(BAR_FALSE_CHARACTER, (length - committedChars)), GRAY));
        }

        return text()
                .append(text("[", DARK_GRAY))
                .append(line.build())
                .append(text("]", DARK_GRAY))
                .build();
    }

    public static TextComponent generateDiskUsageDiagram(double used, double max, int length) {
        int usedChars = (int) ((used * length) / max);
        int freeChars = length - usedChars;
        return text()
                .append(text("[", DARK_GRAY))
                .append(text(Strings.repeat(BAR_TRUE_CHARACTER, usedChars), COLOR_HEALTH_MEDIUM))
                .append(text(Strings.repeat(BAR_FALSE_CHARACTER, freeChars), GRAY))
                .append(text("]", DARK_GRAY))
                .build();
    }
}
