package marshalling;

public class SerializableMarshallingTest extends MarshallingTest {
    public SerializableMarshallingTest() {
        super(new SerializableMessageMarshaller());
    }
}
