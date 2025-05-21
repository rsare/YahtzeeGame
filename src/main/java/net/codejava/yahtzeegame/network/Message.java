package net.codejava.yahtzeegame.network;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Message implements Serializable {
    public String type; // örn: "GAME_START", "ROLL", "CATEGORY_CHOICE"
    public Map<String, Object> data;

    public Message(String type, Object value) {
        this.type = type;
        this.data = new HashMap<>();
        this.data.put("value", value);
    }

    public Message(String type) {
        this(type, null);
    }

    public void put(String key, Object value) {
        data.put(key, value);
    }

    public Object get(String key) {
        return data.get(key);
    }

    public Object getOrDefault(String key, Object defaultValue) {
        return data.containsKey(key) ? data.get(key) : defaultValue;
    }


}
