package org.treblereel.yaml.schema.model;

/**
 * Thrown when schema parsing or resolution encounters an error.
 * Carries an optional {@code context} string for programmatic inspection
 * of the failing element (ref path, type name, source URI, etc.).
 *
 * @author Dmitrii Tikhomirov
 */
public class SchemaParseException extends RuntimeException {

  private final String context;

  public SchemaParseException(String message) {
    super(message);
    this.context = null;
  }

  public SchemaParseException(String message, String context) {
    super(message);
    this.context = context;
  }

  public SchemaParseException(String message, String context, Throwable cause) {
    super(message, cause);
    this.context = context;
  }

  /**
   * Returns the context element that caused the error, such as a {@code $ref} path,
   * unsupported type name, or source URI. May be {@code null}.
   */
  public String getContext() {
    return context;
  }
}
