package gov.omb.data.format.micro.impl;

import gov.omb.data.format.micro.UPI;

import java.util.ArrayList;
import java.util.List;

public class DefaultUPI implements UPI
{
    public static final String UPI_REG_EX = "^[0-9]{3}-[0-9]{2}-[0-9]{2}-[0-9]{2}-[0-9]{2}-[0-9]{4}-[0-9]{2}$";
    public static final int	UPI_FIELD_INDEX_AGENCY				  = 0;
    public static final int	UPI_FIELD_INDEX_BUREAU				  = 1;
    public static final int	UPI_FIELD_INDEX_PART_NUMBER			  = 2;
    public static final int	UPI_FIELD_INDEX_MISSION_AREA		  = 3;
    public static final int	UPI_FIELD_INDEX_INVESTMENT_TYPE		  = 4;
    public static final int	UPI_FIELD_INDEX_IDENTIFICATION_NUMBER = 5;
    public static final int	UPI_FIELD_INDEX_LINE_TYPE			  = 6;
    public static final String EGOV_LINETYPE                      = "24";

    private final int budgetYear;
    private final String upiText;
    private final List<String> issues = new ArrayList<String>();

    private final String agencyCode;
    private final String bureauCode;
    private final String partNumber;
    private final String missionArea;
    private final String investmentType;
    private final String identificationNumber;
    private final String lineType;
    private final boolean eGovInvestment;

    public DefaultUPI(final int getBudgetYear, final String upiText, final String validateAgencyCode, final List<String> validateBureauCodes)
    {
        this.budgetYear = getBudgetYear;
        this.upiText = upiText;

        if (!upiText.matches(UPI_REG_EX))
        {
            issues.add(String.format("UPI '%s' is not valid. It must be 17 digits and look something like '%s-%s-00-00-00-0000-00'.",
                       upiText, validateAgencyCode == null ? "123" : validateAgencyCode,
                       validateBureauCodes == null ? "45" : validateBureauCodes));
            agencyCode = null;
            bureauCode = null;
            partNumber = null;
            missionArea = null;
            investmentType = null;
            identificationNumber = null;
            lineType = null;
            eGovInvestment = false;
            return;
        }

        final String[] components = upiText.split("-");
        agencyCode = components[UPI_FIELD_INDEX_AGENCY];
        if(validateAgencyCode != null && ! agencyCode.equals(validateAgencyCode))
            issues.add(String.format("Agency component '%s' of UPI '%s' is not valid. It should be %s.", agencyCode, upiText, validateAgencyCode));

        bureauCode = components[UPI_FIELD_INDEX_BUREAU];
        if(validateBureauCodes != null && ! validateBureauCodes.contains(bureauCode))
            issues.add(String.format("Bureau component '%s' of UPI '%s' is not valid. It should be %s.", bureauCode, upiText, validateBureauCodes));

        partNumber = components[UPI_FIELD_INDEX_PART_NUMBER];
        missionArea = components[UPI_FIELD_INDEX_MISSION_AREA];
        investmentType = components[UPI_FIELD_INDEX_INVESTMENT_TYPE];
        identificationNumber = components[UPI_FIELD_INDEX_IDENTIFICATION_NUMBER];
        lineType = components[UPI_FIELD_INDEX_LINE_TYPE];

        if (!partNumber.equals("00") && !partNumber.equals("01") && !partNumber.equals("02")
                && !partNumber.equals("03") && !partNumber.equals("04") && !partNumber.equals("05") && !partNumber.equals("06"))
            issues.add(String.format("Part number component '%s' of UPI '%s' is not valid. It should be 00, 01, 02, 03, 04, 05, or 06.", partNumber, upiText));

        if (!investmentType.equals("00") && !investmentType.equals("01") && !investmentType.equals("02")
                && !investmentType.equals("03") && !investmentType.equals("04"))
            issues.add(String.format("Investment type component '%s' of UPI '%s' is not valid. It should be 00, 01, 02, 03, or 04.", investmentType, upiText));

        eGovInvestment = lineType.equals(EGOV_LINETYPE);

        if (!lineType.equals("00") && !lineType.equals("04") && !lineType.equals("09") && !eGovInvestment)
            issues.add(String.format("Investment line type component '%s' of UPI '%s' is not valid. It should be 00, 04, or 09, or 24.", lineType, upiText));
    }

    public int getBudgetYear()
    {
        return budgetYear;
    }

    public String getUPIText()
    {
        return upiText;
    }

    public String getAgencyCode()
    {
        return agencyCode;
    }

    public String getBureauCode()
    {
        return bureauCode;
    }

    public String getExhibit53PartIdentifier()
    {
        return partNumber;
    }

    public String getMissionAreaIdentifier()
    {
        return missionArea;
    }

    public String getInvestmentTypeIdentifier()
    {
        return investmentType;
    }

    public String getInvestmentIdentificationNumber()
    {
        return identificationNumber;
    }

    public String getLineType()
    {
        return lineType;
    }

    public boolean isFundingSourceLine()
    {
        return lineType != null && lineType.equals("04");
    }

    public boolean isSubtotalLine()
    {
        return lineType != null && lineType.equals("09");
    }

    public boolean isEGovInvesment()
    {
        return eGovInvestment;
    }

    public boolean isValid()
    {
        return issues.size() == 0;
    }

    public String[] getIssues()
    {
        return issues.toArray(new String[issues.size()]);
    }
}
