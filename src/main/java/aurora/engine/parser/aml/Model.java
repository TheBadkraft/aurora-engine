// src/main/java/aurora/engine/parser/aml/Model.java
package aurora.engine.parser.aml;

import java.util.List;
import java.util.Map;

public abstract class Model {
    public abstract String fullId();

    public abstract Map<String, Attribute> attributes();

    public abstract Map<String, Object> fields();

    public abstract List<? extends Model> children();

    public record Attribute(String key, String value) {
    }
}