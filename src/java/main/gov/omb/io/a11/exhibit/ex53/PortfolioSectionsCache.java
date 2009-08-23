package gov.omb.io.a11.exhibit.ex53;

import org.netspective.io.spreadsheet.cache.MultipleRowsForCacheValueException;
import org.netspective.io.spreadsheet.cache.NoRowsForCacheValueException;
import org.netspective.io.spreadsheet.cache.TableRowCache;
import org.netspective.io.spreadsheet.model.TableRow;
import org.netspective.io.spreadsheet.template.Column;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PortfolioSectionsCache implements TableRowCache
{
    protected static final String AGENCY_TOTAL_IT_INVESTMENT_PORTFOLIO = "Agency Total IT Investment Portfolio";
    protected static final String SECTION_IDENTIFIER = "Section";

    private final Pattern totalItInvestmentPattern = Pattern.compile("[0-9]{3}-[0-9]{2}-00-00-00-0000-00");
    private final Pattern sectionIdentifierPattern = Pattern.compile("[0-9]{3}-[0-9]{2}-(0[1-6])-00-00-0000-00");

    private final Column activeUPIColumn;
    private final Map<String, List<TableRow>> rowCache = new HashMap<String, List<TableRow>>();

    public PortfolioSectionsCache(final Column activeUPIColumn)
    {
        this.activeUPIColumn = activeUPIColumn;
    }

    public String getCacheName()
    {
        return PortfolioSectionsCache.class.getName();
    }

    public void cache(final TableRow row)
    {
        final String upiText = (String) row.findCellForColumn(activeUPIColumn).getValue(null);
        if(upiText == null)
            return;

        final String cacheValue;

        final Matcher totalITInvestmentMatcher = totalItInvestmentPattern.matcher(upiText);
        if(totalITInvestmentMatcher.matches())
            cacheValue = AGENCY_TOTAL_IT_INVESTMENT_PORTFOLIO;
        else
        {
            final Matcher sectionIdMatcher = sectionIdentifierPattern.matcher(upiText);
            if(sectionIdMatcher.matches())
                cacheValue = String.format("%s %s", SECTION_IDENTIFIER, sectionIdMatcher.group(1));
            else
                cacheValue = null;
        }

        if(cacheValue != null)
        {
            List<TableRow> rowsForPortfolioSection = rowCache.get(cacheValue);
            if(rowsForPortfolioSection == null)
            {
                rowsForPortfolioSection = new ArrayList<TableRow>();
                rowCache.put(cacheValue, rowsForPortfolioSection);
            }
            rowsForPortfolioSection.add(row);
        }
    }

    public String createValueNotFoundErrorMessage(final String value)
    {
        return String.format("Unable to find portfolio row '%s'.", value);
    }

    public String createMultipleValuesFoundErrorMessage(final String value, final List<TableRow> tableRowsFound)
    {
        return String.format("Found %d rows for portfolio row '%s'. Only one expected.", tableRowsFound.size(), value);
    }

    public TableRow findUniqueTableRow(final String value) throws NoRowsForCacheValueException, MultipleRowsForCacheValueException
    {
        final List<TableRow> foundRows = findAllTableRows(value);
        if(foundRows == null)
            throw new NoRowsForCacheValueException(this, value);
        if(foundRows.size() != 1)
            throw new MultipleRowsForCacheValueException(this, value, foundRows);
        return foundRows.get(0);
    }

    public List<TableRow> findAllTableRows(final String value)
    {
        return rowCache.get(value);
    }

}
