package registry;

/**
 * registry
 * User: jennifer
 * Date: 01/08/14
 * Time: 21:00
 */
public class ExtensionObj {
    protected String exName;
    protected  String description;
    protected  String homepageURL;
    protected  String version;
    protected  String docURL;
    protected  String[] categories;

    public ExtensionObj(String exName) {
        this.exName = exName;
    }

    public ExtensionObj(String exName, String description, String homepageURL, String version, String docURL, String[] categories) {
        this.exName = exName;
        this.description = description;
        this.homepageURL = homepageURL;
        this.version = version;
        this.docURL = docURL;
        this.categories = categories;
    }

    public String getExName() {
        return exName;
    }

    public void setExName(String exName) {
        this.exName = exName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHomepageURL() {
        return homepageURL;
    }

    public void setHomepageURL(String homepageURL) {
        this.homepageURL = homepageURL;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDocURL() {
        return docURL;
    }

    public void setDocURL(String docURL) {
        this.docURL = docURL;
    }

    public String[] getCategories() {
        return categories;
    }

    public void setCategories(String[] categories) {
        this.categories = categories;
    }
}
