// src/main/java/aurora/engine/parser/aml/NamedModel.java
package aurora.engine.parser.aml;

import java.util.List;
import java.util.Map;

public final class NamedModel extends Model {
        private final String fullId;
        private final Map<String, Attribute> attributes;
        private final Map<String, Object> fields;
        private final List<Model> children;

        public NamedModel(String fullId, Map<String, Attribute> attributes,
                        Map<String, Object> fields, List<Model> children) {
                this.fullId = fullId;
                this.attributes = Map.copyOf(attributes);
                this.fields = Map.copyOf(fields);
                this.children = List.copyOf(children);
        }

        @Override
        public String fullId() {
                return fullId;
        }

        @Override
        public Map<String, Attribute> attributes() {
                return attributes;
        }

        @Override
        public Map<String, Object> fields() {
                return fields;
        }

        @Override
        public List<Model> children() {
                return children;
        }

        @Override
        public String toString() {
                return "NamedModel[fullId=" + fullId + ", fields=" + fields.size() + ", children=" + children.size()
                                + "]";
        }
}