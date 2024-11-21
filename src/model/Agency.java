package model;

import java.util.Optional;

public class Agency {
    private Optional<String> agencyId;      // Optional (for multi-agency systems)
    private String agencyName;              // Required
    private String agencyUrl;               // Required
    private String agencyTimezone;          // Required
    private Optional<String> agencyLang;    // Optional
    private Optional<String> agencyPhone;   // Optional

    // Constructor with all fields
    public Agency(Optional<String> agencyId, String agencyName, String agencyUrl, String agencyTimezone,
                  Optional<String> agencyLang, Optional<String> agencyPhone) {
        this.agencyId = agencyId;
        this.agencyName = agencyName;
        this.agencyUrl = agencyUrl;
        this.agencyTimezone = agencyTimezone;
        this.agencyLang = agencyLang;
        this.agencyPhone = agencyPhone;
    }

    // Constructor with only required fields
    public Agency(String agencyName, String agencyUrl, String agencyTimezone) {
        this(Optional.empty(), agencyName, agencyUrl, agencyTimezone, Optional.empty(), Optional.empty());
    }

    // Getters and setters
    public Optional<String> getAgencyId() { return agencyId; }
    public void setAgencyId(Optional<String> agencyId) { this.agencyId = agencyId; }

    public String getAgencyName() { return agencyName; }
    public void setAgencyName(String agencyName) { this.agencyName = agencyName; }

    public String getAgencyUrl() { return agencyUrl; }
    public void setAgencyUrl(String agencyUrl) { this.agencyUrl = agencyUrl; }

    public String getAgencyTimezone() { return agencyTimezone; }
    public void setAgencyTimezone(String agencyTimezone) { this.agencyTimezone = agencyTimezone; }

    public Optional<String> getAgencyLang() { return agencyLang; }
    public void setAgencyLang(Optional<String> agencyLang) { this.agencyLang = agencyLang; }

    public Optional<String> getAgencyPhone() { return agencyPhone; }
    public void setAgencyPhone(Optional<String> agencyPhone) { this.agencyPhone = agencyPhone; }
}