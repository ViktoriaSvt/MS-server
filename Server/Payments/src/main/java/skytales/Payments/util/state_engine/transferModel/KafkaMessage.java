package skytales.Payments.util.state_engine.transferModel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class KafkaMessage<T> {
    private String type;
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

}

