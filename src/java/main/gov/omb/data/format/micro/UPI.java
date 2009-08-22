package gov.omb.data.format.micro;

public interface UPI
{
    public int getBudgetYear();
    public String getUPIText();

    public String getAgencyCode();
    public String getBureauCode();
    public String getExhibit53PartIdentifier();
    public String getMissionAreaIdentifier();
    public String getInvestmentTypeIdentifier();
    public String getInvestmentIdentificationNumber();

    public String getLineType();
    public boolean isFundingSourceLine();
    public boolean isFundingSourceSummaryLine();

    public boolean isValid();
    public String[] getIssues();
}