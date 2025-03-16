package skytales.Library.util.state_engine.utils;

import com.fasterxml.jackson.annotation.JsonProperty;

public class KafkaMessage<T> {

    @JsonProperty
    private String type;

    @JsonProperty
    private Object data;

    public KafkaMessage(String type, T data) {
        this.type = type;
        this.data = data;
    }

    public KafkaMessage(T data) {
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public Object getData() {
        return data;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public KafkaMessage() {
    }
}

