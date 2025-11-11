package aurora.engine.parser;

public class AuroraDocument {
    // can be set internally by the parser
    boolean isParsed = false;
    Dialect dialect = null;

    public AuroraDocument() {

    }

    /*
        Returns true if the document contains no content.
     */
    public boolean isEmpty() {
        return hasNoDialect();
    }
    /*
        Returns true if the document was fully parsed.
     */
    public boolean isParsed() {
        return isParsed;
    }
    /*
        Returns true if the document has a dialect.
     */
    public boolean hasNoDialect() {
        return dialect == null;
    }
    /*
        Returns the document dialect, or null if not set.
     */
    public Dialect getDialect() {
        return dialect;
    }
}
