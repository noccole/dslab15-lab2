package entities;

/**
 * Created by phili on 11/26/15.
 */
public class Domain {
    private String domain;

    public Domain(String domain) {
        this.domain = domain;
    }

    public Domain subdomain() {
        return new Domain(domain.substring(0, domain.lastIndexOf(".")));
    }

    public String root() {
        return domain.substring(domain.lastIndexOf(".") + 1);
    }

    public boolean hasSubdomain() {
        return domain.contains(".");
    }

    public boolean isValid() {
        return domain.matches("^[a-zA-Z.]+$");
    }

    @Override
    public String toString() {
        return domain;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Domain domain1 = (Domain) o;

        return !(domain != null ? !domain.equals(domain1.domain) : domain1.domain != null);

    }

    @Override
    public int hashCode() {
        return domain != null ? domain.hashCode() : 0;
    }
}
