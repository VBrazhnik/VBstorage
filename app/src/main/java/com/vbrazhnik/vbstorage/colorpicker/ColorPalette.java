package com.vbrazhnik.vbstorage.colorpicker;

public enum ColorPalette {

    RED_1   (0xFFFA6261),
    RED_2   (0xFFF87B60),
    RED_3   (0xFFF6935E),
    RED_4   (0xFFE9A159),
    ORANGE_1(0xFFF0BE5B),
    ORANGE_2(0xFFEACA58),
    ORANGE_3(0xFFE5D555),
    ORANGE_4(0xFFDEE052),
    YELLOW_1(0xFFD8EB4F),
    YELLOW_2(0xFFAEEE6B),
    YELLOW_3(0xFF85EF81),
    YELLOW_4(0xFF58F096),
    GREEN_1 (0xFF19EFA9),
    GREEN_2 (0xFF33E3B8),
    GREEN_3 (0xFF42D7C7),
    GREEN_4 (0xFF50C9D7),
    BLUE_1  (0xFF5BBBE7),
    BLUE_2  (0xFF64A2E5),
    BLUE_3  (0xFF6E87E3),
    BLUE_4  (0xFF7568E1),
    INDIGO_1(0xFF7C42DF),
    INDIGO_2(0xFF9142D9),
    INDIGO_3(0xFFA842D3),
    INDIGO_4(0xFFBD3DCA),
    VIOLET_1(0xFFD73AC5),
    VIOLET_2(0xFFE04CAD),
    VIOLET_3(0xFFE95595),
    VIOLET_4(0xFFF15E7C);

    private int color;

    ColorPalette(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }
}
