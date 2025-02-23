package me.astroreen.liblanka.domain.product.util;

import java.util.regex.Pattern;

public class ColorValidator {

    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("^#[0-9A-Fa-f]{6}$");

    public static boolean isValidHexColor(String hex) {
        return HEX_COLOR_PATTERN.matcher(hex).matches();
    }
}
