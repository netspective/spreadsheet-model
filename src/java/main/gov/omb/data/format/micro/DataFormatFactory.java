package gov.omb.data.format.micro;

import gov.omb.data.format.micro.impl.DefaultUPI;

public class DataFormatFactory
{
    public final static DataFormatFactory INSTANCE = new DataFormatFactory();

    public static DataFormatFactory getInstance()
    {
        return INSTANCE;
    }

    public UPI createUPI(final int budgetYear, final String rawUPI)
    {
        return new DefaultUPI(budgetYear, rawUPI, null);
    }

    public UPI createUPI(final int budgetYear, final String rawUPI, final UPI.ValidationRules validationRules)
    {
        return new DefaultUPI(budgetYear, rawUPI, validationRules);
    }
}
