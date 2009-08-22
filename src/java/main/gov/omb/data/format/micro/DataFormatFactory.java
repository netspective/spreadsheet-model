package gov.omb.data.format.micro;

import gov.omb.data.format.micro.impl.DefaultUPI;

import java.util.List;

public class DataFormatFactory
{
    public final static DataFormatFactory INSTANCE = new DataFormatFactory();

    public static DataFormatFactory getInstance()
    {
        return INSTANCE;
    }

    public UPI createUPI(final int budgetYear, final String rawUPI)
    {
        return new DefaultUPI(budgetYear, rawUPI, null, null);
    }

    public UPI createUPI(final int budgetYear, final String rawUPI, final String validateAgencyCode)
    {
        return new DefaultUPI(budgetYear, rawUPI, validateAgencyCode, null);
    }

    public UPI createUPI(final int budgetYear, final String rawUPI, final String validateAgencyCode, final List<String> validateBureauCodes)
    {
        return new DefaultUPI(budgetYear, rawUPI, validateAgencyCode, validateBureauCodes);
    }
}
