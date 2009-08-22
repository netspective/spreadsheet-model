package org.netspective.io.spreadsheet.util;

import org.apache.poi.ss.usermodel.Cell;

public class Util
{
    /**
     * Return a string without leading or trailing spaces, new lines as spaces, and multiples spaces replaced as one
     * @param original The string to filter
     * @return The filtered string
     */
    public static String getTrimmedStringWithoutNewlinesOrMultipleSpaces(final String original)
    {
        if(original == null) return original;
        return original.trim().replace("\n", " ").replaceAll("\\s+", " ");
    }

    /**
     * Return a string that represents a column number in the spreadsheet ('A' .. 'Z', "AA' .. 'ZZ', 'AAA...ZZZ', etc.)
     * @param cellIndex The cell index where column number 1 is the first column
     * @return The letters that represent the cellNum column in the sheet
     */
    public static String getColumnIndexLetters(final int cellIndex)
    {
        final int cellNum = cellIndex - 1;
        final StringBuffer ret = new StringBuffer();

        int tempCellNum = cellNum;
        do
        {
            ret.insert(0, (char) ('A' + (tempCellNum % 26)));
            tempCellNum = (tempCellNum / 26) - 1;
        } while (tempCellNum >= 0);
        return ret.toString();
    }

    /**
     * Return a string that represents a column number in the spreadsheet ('A' .. 'Z', "AA' .. 'ZZ', 'AAA...ZZZ', etc.)
     * along with the row in Excel-like format like 'A1', 'AB3', 'C7', etc.
     * @param cellIndex The cell index where column number 1 is the first column
     * @param rowNumber The row index where row number 1 is the first row
     * @return The letters and number that represent the cellNum column in the sheet
     */
    public static String getCellLocator(final int cellIndex, final int rowNumber)
    {
        return String.format("%s%d", getColumnIndexLetters(cellIndex), rowNumber);
    }

    /**
     * Return a string that represents a column number in the spreadsheet ('A' .. 'Z', "AA' .. 'ZZ', 'AAA...ZZZ', etc.)
     * along with the row in Excel-like format like 'A1', 'AB3', 'C7', etc.
     * @param cell The cell we're interested in locating
     * @return The letters and number that represent the cellNum column in the sheet
     */
    public static String getCellLocator(final Cell cell)
    {
        return getCellLocator(cell.getColumnIndex()+1, cell.getRowIndex()+1);
    }
}
