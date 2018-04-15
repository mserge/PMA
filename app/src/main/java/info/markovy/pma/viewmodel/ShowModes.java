package info.markovy.pma.viewmodel;

import java.util.HashMap;
import java.util.Map;

public enum ShowModes {
    POPULAR(0),
    TOP(1),
    STARRED(2);

    private final int Value;

    private ShowModes(int value) {
        this.Value = value;
    }
    private static final Map<Integer, ShowModes> _map = new HashMap<Integer, ShowModes>();

    static
    {
        for (ShowModes difficulty : ShowModes.values())
            _map.put(difficulty.Value, difficulty);
    }

    /**
     * Get difficulty from value
     * @param value Value
     * @return ShowModes
     */
    public static ShowModes from(int value)
    {
        return _map.get(value);
    }

    public int getValue() {
        return Value;
    }
}
