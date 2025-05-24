package net.codejava.yahtzeegame.network;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * <p><b>Message</b> – istemci <-> sunucu arasında taşınan temel serileştirilebilir  zarf

 */
public class Message implements Serializable {

    // Fİelds
    public String type;                 // Mesaj başlığı
    public Map<String, Object> data;    // Taşınan içerik

    // Constructur'lar

    public Message(String type, Object value) {
        this.type = type;
        this.data = new HashMap<>();
        this.data.put("value", value);
    }

    // Sadece tip belirterek boş payload’lı mesaj üretir
    public Message(String type) {
        this(type, null);
    }

    //Yardımcı metotlar

    // data.put(key, value) kısayolu
    public void put(String key, Object value) {
        data.put(key, value);
    }

    // Güvenli alma – anahtar yoksa null döner
    public Object get(String key) {
        return data.get(key);
    }

    // Anahtar yoksa varsayılan değer döndüren get
    public Object getOrDefault(String key, Object defaultValue) {
        return data.containsKey(key) ? data.get(key) : defaultValue;
    }
}
