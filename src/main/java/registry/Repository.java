package registry;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Javabean for our repository structure.
 */
@Entity
public class Repository {
    @Id
    private String id;

    private String type;

    private String url;

    public Repository() {
    }

    public Repository(String type, String url) {
        this.type = type;
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getId() {
        return id;
    }
}
