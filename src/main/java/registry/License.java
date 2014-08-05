package registry;

/**
 * registry
 * User: jennifer
 * Date: 04/08/14
 * Time: 15:28
 */
public class License {

    private String type;
    private String url;

    public License() {
    }
    public License(String type, String url){
        this.type=type;
        this.url=url;
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
}
