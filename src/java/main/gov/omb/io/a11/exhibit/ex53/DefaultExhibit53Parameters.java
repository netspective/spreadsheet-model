package gov.omb.io.a11.exhibit.ex53;

import java.util.List;

public class DefaultExhibit53Parameters implements Exhibit53Parameters
{
    private int budgetYear;
    private String agencyCode;
    private List<String> bureauCodes;

    private boolean validateAnySubtotals = true;
    private boolean validateFundingSourceSubtotals = true;
    private boolean validateInvestmentLineSubtotalWithFundingSourceSubtotals = true;
    private boolean validateInvestmentsGroupSubtotals = true;
    private boolean validateMissionAreaSubtotals = true;
    private boolean validatePartSubtotals = true;
    private boolean validatePortfolioTotals = true;

    private boolean validateBudgetAccountsCodesInFundingSources = true;

    public DefaultExhibit53Parameters(final int budgetYear, final String agencyCode, final List<String> bureauCodes)
    {
        setBudgetYear(budgetYear);
        setAgencyCode(agencyCode);
        setBureauCodes(bureauCodes);
    }

    public int getBudgetYear()
    {
        return budgetYear;
    }

    public void setBudgetYear(final int budgetYear)
    {
        this.budgetYear = budgetYear;
    }

    public String getAgencyCode()
    {
        return agencyCode;
    }

    public void setAgencyCode(final String agencyCode)
    {
        this.agencyCode = agencyCode;
    }

    public List<String> getBureauCodes()
    {
        return bureauCodes;
    }

    public void setBureauCodes(final List<String> bureauCodes)
    {
        this.bureauCodes = bureauCodes;
    }

    public boolean isValidateAnySubtotals()
    {
        return validateAnySubtotals;
    }

    public void setValidateAnySubtotals(final boolean validateAnySubtotals)
    {
        this.validateAnySubtotals = validateAnySubtotals;
    }

    public boolean isValidateFundingSourceSubtotals()
    {
        return validateFundingSourceSubtotals;
    }

    public void setValidateFundingSourceSubtotals(final boolean validateFundingSourceSubtotals)
    {
        this.validateFundingSourceSubtotals = validateFundingSourceSubtotals;
    }

    public boolean isValidateInvestmentLineSubtotalWithFundingSourceSubtotals()
    {
        return validateInvestmentLineSubtotalWithFundingSourceSubtotals;
    }

    public void setValidateInvestmentLineSubtotalWithFundingSourceSubtotals(final boolean validateInvestmentLineSubtotalWithFundingSourceSubtotals)
    {
        this.validateInvestmentLineSubtotalWithFundingSourceSubtotals = validateInvestmentLineSubtotalWithFundingSourceSubtotals;
    }

    public boolean isValidateInvestmentsGroupSubtotals()
    {
        return validateInvestmentsGroupSubtotals;
    }

    public void setValidateInvestmentsGroupSubtotals(final boolean validateInvestmentsGroupSubtotals)
    {
        this.validateInvestmentsGroupSubtotals = validateInvestmentsGroupSubtotals;
    }

    public boolean isValidateMissionAreaSubtotals()
    {
        return validateMissionAreaSubtotals;
    }

    public void setValidateMissionAreaSubtotals(final boolean validateMissionAreaSubtotals)
    {
        this.validateMissionAreaSubtotals = validateMissionAreaSubtotals;
    }

    public boolean isValidatePartSubtotals()
    {
        return validatePartSubtotals;
    }

    public void setValidatePartSubtotals(final boolean validatePartSubtotals)
    {
        this.validatePartSubtotals = validatePartSubtotals;
    }

    public boolean isValidatePortfolioTotals()
    {
        return validatePortfolioTotals;
    }

    public void setValidatePortfolioTotals(final boolean validatePortfolioTotals)
    {
        this.validatePortfolioTotals = validatePortfolioTotals;
    }

    public boolean isValidateBudgetAccountsCodesInFundingSources()
    {
        return validateBudgetAccountsCodesInFundingSources;
    }

    public void setValidateBudgetAccountsCodesInFundingSources(final boolean validateBudgetAccountsCodesInFundingSources)
    {
        this.validateBudgetAccountsCodesInFundingSources = validateBudgetAccountsCodesInFundingSources;
    }
}
