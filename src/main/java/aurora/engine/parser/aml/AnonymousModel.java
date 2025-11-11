// src/main/java/aurora/engine/parser/aml/AnonymousModel.java
package aurora.engine.parser.aml;

import java.util.List;
import java.util.Map;

public final class AnonymousModel extends Model {
    private final Map<String, Object> fields;
    private final List<AnonymousModel> children;

    public AnonymousModel(Map<String, Object> fields, List<AnonymousModel> children) {
        this.fields = Map.copyOf(fields);
        this.children = List.copyOf(children);
    }

    @Override
    public String fullId() {
        return null;
    }

    @Override
    public Map<String, Attribute> attributes() {
        // Anonymous models have no attributes
        return Map.of();
    }

    @Override
    public Map<String, Object> fields() {
        return fields;
    }

    @Override
    public List<AnonymousModel> children() {
        return children;
    }

    @Override
    public String toString() {
        return "AnonymousModel[fields=" + fields.size() + ", children=" + children.size() + "]";
    }
}