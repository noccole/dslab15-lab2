package entities;

/**
 * encapsulates operations on the domain string
 * domains consist of multiple zones which are seperated by dots '.'.
 * zones consist of alphabetic characters.
 * e.g.: berlin.de
 */
public class Domain {
    private final String domain;

    /**
     * creates a new domain object on the specified domain
     * @param domain domain
     */
    public Domain(String domain) {
        this.domain = domain;
    }

    /**
     * @return returns the subdomain (e.g.: berlin.de -> berlin)
     */
    public Domain subdomain() {
        return new Domain(domain.substring(0, domain.lastIndexOf(".")));
    }

    /**
     * @return returns the root zone (e.g.: berlin.de -> de)
     */
    public String root() {
        return domain.substring(domain.lastIndexOf(".") + 1);
    }

    /**
     * @return returns true if the domain has one or more subdomains
     */
    public boolean hasSubdomain() {
        return domain.contains(".");
    }

    /**
     * @return returns true if the domain consists only of alphabetic characters and dots
     */
    public boolean isValid() {
        return domain.matches("^[a-zA-Z.]+$");
    }

    /**
     * @return returns the domain as string
     */
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
