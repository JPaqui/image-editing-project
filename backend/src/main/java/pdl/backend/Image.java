package pdl.backend;

import org.springframework.http.MediaType;

public class Image {
  private static Long count = 0L;
  private final Long id;
  private String name;
  private final byte[] data;
  public final MediaType mediaType;

  public Image(final String name, final byte[] data, final MediaType mediaType) {
    id = count++;
    this.name = name;
    this.data = data;
    this.mediaType = mediaType;
  }

  public long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public byte[] getData() {
    return data;
  }
}
